package assignment4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.vecmath.Vector3f;

import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;

/**
 * Methods to create different flavours of the cotangent and uniform laplacian.
 * @author Alf
 *
 */
public class LMatrices {
	
	/**
	 * The uniform Laplacian
	 * @param hs
	 * @return
	 */
	public static CSRMatrix uniformLaplacian(HalfEdgeStructure hs){
		ArrayList<Vertex> verts = hs.getVertices();
		int n = verts.size();
		CSRMatrix unifLaplace = new CSRMatrix(n, n);
		for(Vertex v:verts){
			int row = v.index;
			if (v.isOnBoundary()) {
				unifLaplace.set(row, row, 1);
				continue;
			}
			unifLaplace.set(row, row, -1);
			Iterator<Vertex> neighborIter = v.iteratorVV();
			int valence = v.valence();
			while(neighborIter.hasNext()){
				Vertex neighbor = neighborIter.next();
				int col = neighbor.index;
				unifLaplace.set(row, col, 1f/valence);	
			}
		}
		return unifLaplace;
	}
	
	/**
	 * The cotangent Laplacian
	 * @param hs
	 * @return
	 */
	public static CSRMatrix mixedCotanLaplacian(HalfEdgeStructure hs){
		ArrayList<Vertex> verts = hs.getVertices();
		int n = verts.size();
		CSRMatrix cotanLaplace = new CSRMatrix(n, n);
		for(Vertex v:verts){
			int row = v.index;
			if (v.isOnBoundary()) {
				cotanLaplace.set(row, row, 1);
				continue;
			}
			float sumOfCotans = 0;
			float area = v.computeMixedArea();
			Iterator<HalfEdge> edgeIt = v.iteratorVE();
			while(edgeIt.hasNext()){
				HalfEdge edge = edgeIt.next();
				float beta = edge.angleOppositeVertex();
				float alpha = edge.getOpposite().angleOppositeVertex();
				float cotanWeights =  (float) Math.min(1/Math.tan(alpha)+1/Math.tan(beta),1e2);
				sumOfCotans += cotanWeights;
				Vertex endVert = edge.end();
				int col = endVert.index;
				cotanLaplace.set(row, col, cotanWeights/(2*area));	
			}
			cotanLaplace.set(row, row, -sumOfCotans/(2*area));
		}
		return cotanLaplace;	
	}
	
	/**
	 * A symmetric cotangent Laplacian, cf Assignment 4, exercise 4.
	 * @param hs
	 * @return
	 */
	public static CSRMatrix symmetricCotanLaplacian(HalfEdgeStructure hs){
		ArrayList<Vertex> verts = hs.getVertices();
		int n = verts.size();
		CSRMatrix symCotan = new CSRMatrix(n, n);
		for(Vertex v:verts){
			int row = v.index;
			float sumOfEntries = 0;
			float area1 = v.computeMixedArea();
			Iterator<HalfEdge> edgeIt = v.iteratorVE();
			while(edgeIt.hasNext()){
				HalfEdge edge = edgeIt.next();
				float area2 = edge.end().computeMixedArea();
				float scale = (float) Math.sqrt(1/(area1*area2));
				float beta = edge.angleOppositeVertex();
				float alpha = edge.getOpposite().angleOppositeVertex();
				float cotanWeights =  (float) Math.min(1/Math.tan(alpha)+1/Math.tan(beta),1e2);
				sumOfEntries += cotanWeights/(2*scale);
				Vertex endVert = edge.end();
				int col = endVert.index;
				symCotan.set(row, col, cotanWeights/(2*scale));	
			}
			symCotan.set(row, row, -sumOfEntries);
		}
		return symCotan;		
	}
	
	
	
	public static void mult(CSRMatrix m, HalfEdgeStructure s, ArrayList<Vector3f> res){
		ArrayList<Float> x = new ArrayList<>(), b = new ArrayList<>(s.getVertices().size());
		x.ensureCapacity(s.getVertices().size());
		
		res.clear();
		res.ensureCapacity(s.getVertices().size());
		for(Vertex v : s.getVertices()){
			x.add(0.f);
			res.add(new Vector3f());
		}
		
		for(int i = 0; i < 3; i++){
			
			//setup x
			for(Vertex v : s.getVertices()){
				switch (i) {
				case 0:
					x.set(v.index, v.getPos().x);	
					break;
				case 1:
					x.set(v.index, v.getPos().y);	
					break;
				case 2:
					x.set(v.index, v.getPos().z);	
					break;
				}
				
			}
			
			m.mult(x, b);
			
			for(Vertex v : s.getVertices()){
				switch (i) {
				case 0:
					res.get(v.index).x = b.get(v.index);	
					break;
				case 1:
					res.get(v.index).y = b.get(v.index);	
					break;
				case 2:
					res.get(v.index).z = b.get(v.index);	
					break;
				}
				
			}
		}
	}
}
