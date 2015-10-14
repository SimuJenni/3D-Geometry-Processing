package assignment3;

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import assignment2.HashOctree;
import assignment2.HashOctreeCell;
import assignment2.HashOctreeVertex;
import assignment2.MortonCodes;
import assignment3.marchingCubes.MarchableCube;
import cern.jet.random.Normal;
import meshes.PointCloud;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.LinearSystem;


public class SSDMatrices {
	
	
	/**
	 * Example Matrix creation:
	 * Create an identity matrix, clamped to the requested format.
	 * @param nRows
	 * @param nCols
	 * @return
	 */
	public static CSRMatrix eye(int nRows, int nCols){
		CSRMatrix eye = new CSRMatrix(0, nCols);
		
		//initialize the identity matrix part
		for(int i = 0; i< Math.min(nRows, nCols); i++){
			eye.appendRow();
			eye.lastRow().add(
						//column i, vlue 1
					new col_val(i,1));
		}
		//fill up the matrix with empty rows.
		for(int i = Math.min(nRows, nCols); i < nRows; i++){
			eye.appendRow();
		}
		
		return eye;
	}
	
	
	/**
	 * Example matrix creation. Identity matrix restricted to boundary per vertex values.
	 * @param tree
	 * @return
	 */
	public static CSRMatrix Eye_octree_boundary(HashOctree tree){
		
		CSRMatrix result = new CSRMatrix(0, tree.numberofVertices());
				
		for(HashOctreeVertex v : tree.getVertices()){
			if(MortonCodes.isVertexOnBoundary(v.code, tree.getDepth())){
				result.appendRow();
				result.lastRow().add(new col_val(v.index,1));
			}
		}
		
		return result;
	}
	
	/**
	 * One line per point, One column per vertex,
	 * enforcing that the interpolation of the Octree vertex values
	 * is zero at the point position.
	 * @author Alf
	 *
	 */
	public static CSRMatrix D0Term(HashOctree tree, PointCloud cloud){
		
		ArrayList<Point3f> points = cloud.points;
		ArrayList<HashOctreeVertex> verts = tree.getVertices();
		
		CSRMatrix D0 = new CSRMatrix(points.size(), verts.size()); 
		
		for(int i=0; i<points.size(); i++){
			Point3f point = points.get(i);
			HashOctreeCell cell = tree.getCell(point);
			MarchableCube v1 = cell.getCornerElement(0b000, tree);
			MarchableCube v2 = cell.getCornerElement(0b001, tree);
			MarchableCube v3 = cell.getCornerElement(0b010, tree);
			MarchableCube v4 = cell.getCornerElement(0b011, tree);
			MarchableCube v5 = cell.getCornerElement(0b100, tree);
			MarchableCube v6 = cell.getCornerElement(0b101, tree);
			MarchableCube v7 = cell.getCornerElement(0b110, tree);
			MarchableCube v8 = cell.getCornerElement(0b111, tree);
			float xd = (point.x-v1.getPosition().x) / (v5.getPosition().x-v1.getPosition().x);
			float yd = (point.y-v1.getPosition().y) / (v3.getPosition().y-v1.getPosition().y);
			float zd = (point.z-v1.getPosition().z) / (v2.getPosition().z-v1.getPosition().z);
			float w000 = (1-xd)*(1-yd)*(1-zd);
			float w001 = (1-xd)*(1-yd)*zd;
			float w010 = (1-xd)*yd*(1-zd);
			float w011 = (1-xd)*yd*zd;
			float w100 = xd*(1-yd)*(1-zd);
			float w101 = xd*(1-yd)*zd;
			float w110 = xd*yd*(1-zd);
			float w111 = xd*yd*zd;
			for(int j=0; j<verts.size(); j++){
				if(j==v1.getIndex())
					D0.set(i, j, w000);
				else if(j==v2.getIndex())
					D0.set(i, j, w001);
				else if(j==v3.getIndex())
					D0.set(i, j, w010);
				else if(j==v4.getIndex())
					D0.set(i, j, w011);
				else if(j==v5.getIndex())
					D0.set(i, j, w100);
				else if(j==v6.getIndex())
					D0.set(i, j, w101);
				else if(j==v7.getIndex())
					D0.set(i, j, w110);
				else if(j==v8.getIndex())
					D0.set(i, j, w111);
			}
		}	
		return D0;
	}

	/**
	 * matrix with three rows per point and 1 column per octree vertex.
	 * rows with i%3 = 0 cover x gradients, =1 y-gradients, =2 z gradients;
	 * The row i, i+1, i+2 correxponds to the point/normal i/3.
	 * Three consecutant rows belong to the same gradient, the gradient in the cell
	 * of pointcloud.point[row/3]; 
	 * @param tree
	 * @param cloud
	 * @return
	 */
	public static CSRMatrix D1Term(HashOctree tree, PointCloud cloud) {
		
		ArrayList<Point3f> points = cloud.points;
		ArrayList<HashOctreeVertex> verts = tree.getVertices();
		
		CSRMatrix D1 = new CSRMatrix(3*points.size(), verts.size()); 
		
		for(int i=0; i<points.size(); i++){
			Point3f point = points.get(i);
			HashOctreeCell cell = tree.getCell(point);
			MarchableCube v1 = cell.getCornerElement(0b000, tree);
			MarchableCube v2 = cell.getCornerElement(0b001, tree);
			MarchableCube v3 = cell.getCornerElement(0b010, tree);
			MarchableCube v4 = cell.getCornerElement(0b011, tree);
			MarchableCube v5 = cell.getCornerElement(0b100, tree);
			MarchableCube v6 = cell.getCornerElement(0b101, tree);
			MarchableCube v7 = cell.getCornerElement(0b110, tree);
			MarchableCube v8 = cell.getCornerElement(0b111, tree);
			float scale = 1.f/(4*cell.side);
			for(int j=0; j<verts.size(); j++){
				if(j==v1.getIndex()){
					D1.set(i*3, j, -scale);
					D1.set(i*3+1, j, -scale);
					D1.set(i*3+2, j, -scale);
				}
				else if(j==v2.getIndex()){
					D1.set(i*3, j, -scale);
					D1.set(i*3+1, j, -scale);
					D1.set(i*3+2, j, scale);
				}
				else if(j==v3.getIndex()){
					D1.set(i*3, j, -scale);
					D1.set(i*3+1, j, scale);
					D1.set(i*3+2, j, -scale);
				}
				else if(j==v4.getIndex()){
					D1.set(i*3, j, -scale);
					D1.set(i*3+1, j, scale);
					D1.set(i*3+2, j, scale);
				}
				else if(j==v5.getIndex()){
					D1.set(i*3, j, scale);
					D1.set(i*3+1, j, -scale);
					D1.set(i*3+2, j, -scale);
				}
				else if(j==v6.getIndex()){
					D1.set(i*3, j, scale);
					D1.set(i*3+1, j, -scale);
					D1.set(i*3+2, j, scale);
				}
				else if(j==v7.getIndex()){
					D1.set(i*3, j, scale);
					D1.set(i*3+1, j, scale);
					D1.set(i*3+2, j, -scale);
				}
				else if(j==v8.getIndex()){
					D1.set(i*3, j, scale);
					D1.set(i*3+1, j, scale);
					D1.set(i*3+2, j, scale);
				} 
			}
		}
		return D1;
	}
	
	
			
/**
 * Regularization Term as described on the Exercise Slides.
 * @param tree
 * @return
 */
	public static CSRMatrix RegularizationTerm(HashOctree tree){
		
		ArrayList<HashOctreeVertex> verts = tree.getVertices();
		CSRMatrix R = new CSRMatrix(0, verts.size()); 
		float sumOfDists = 0f;
		
		for(int j=0; j<verts.size(); j++){
			HashOctreeVertex v_j = verts.get(j);
			sumOfDists += checkNeighborhood(tree, R, v_j, 0b100);
			sumOfDists += checkNeighborhood(tree, R, v_j, 0b010);
			sumOfDists += checkNeighborhood(tree, R, v_j, 0b001);
		}
		R.scale(1/sumOfDists);
		return R;
	}


	private static float checkNeighborhood(HashOctree tree, CSRMatrix R, HashOctreeVertex v_j, int Obxyz) {
		HashOctreeVertex v_k = tree.getNbr_v2v(v_j, Obxyz);
		HashOctreeVertex v_i = tree.getNbr_v2vMinus(v_j, Obxyz);
		float distTerm = 0f;
		if(v_k!=null && v_i!=null){
			ArrayList<col_val> row = new ArrayList<col_val>();
			float dist_ij = v_i.getPosition().distance(v_j.getPosition());
			float dist_jk = v_j.getPosition().distance(v_k.getPosition());
			row.add(new col_val(v_j.getIndex(), 1f));
			row.add(new col_val(v_i.getIndex(), -dist_jk/(dist_jk+dist_ij)));
			row.add(new col_val(v_k.getIndex(), -dist_ij/(dist_jk+dist_ij)));
			R.appendRow(row, 1);
			distTerm = dist_ij*dist_jk;
		}
		return distTerm;
	}


	/**
	 * Set up the linear system for ssd: append the three matrices, 
	 * appropriately scaled. And set up the appropriate right hand side, i.e. the
	 * b in Ax = b
	 * @param tree
	 * @param pc
	 * @param lambda0
	 * @param lambda1
	 * @param lambda2
	 * @return
	 */
	public static LinearSystem ssdSystem(HashOctree tree, PointCloud pc, 
			float lambda0,
			float lambda1,
			float lambda2){
		
				
		LinearSystem system = new LinearSystem();
		int N = pc.points.size();
		CSRMatrix mat = D0Term(tree, pc);
		mat.scale((float) Math.sqrt(lambda0/N));
		mat.append(D1Term(tree, pc), (float) Math.sqrt(lambda1/N));
		mat.append(RegularizationTerm(tree), (float) Math.sqrt(lambda2));
		system.mat = mat;
		
		ArrayList<Float> b = new ArrayList<Float>();
		for(int i=0; i<pc.points.size(); i++){
			b.add(0f);
		}
		for(Vector3f n : pc.normals){
			b.add(n.x);
			b.add(n.y);
			b.add(n.z);
		}
		int currentLength = b.size();
		for(int i=0;i<mat.nRows()-currentLength; i++){
			b.add(0f);
		}
		system.b = b;
		return system;
	}

}
