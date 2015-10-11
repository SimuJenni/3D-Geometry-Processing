package assignment3;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import assignment2.HashOctree;
import assignment2.HashOctreeCell;
import assignment3.marchingCubes.MCTable;
import assignment3.marchingCubes.MarchableCube;
import meshes.Point2i;
import meshes.WireframeMesh;

/**
 * You can use this sceletton - or completely ignore it and implement
 * the marching cubes algorithm from scratch.
 * @author bertholet
 *
 */
public class MarchingCubes {
	
	//the reconstructed surface
	public WireframeMesh result;
	

	//the tree to march
	private HashOctree tree;
	//per marchable cube values
	private ArrayList<Float> val;
	private int faceCount;

	
	/**
	 * Implementation of the marching cube algorithm. pass the tree
	 * and either the primary values associated to the trees edges
	 * @param tree
	 * @param byLeaf
	 */
	public MarchingCubes(HashOctree tree){
		this.tree = tree;
		faceCount = 0;
	}

	/**
	 * Perform primary Marching cubes on the tree.
	 */
	public void primaryMC(ArrayList<Float> byVertex) {
		this.val = byVertex;
		this.result = new WireframeMesh();
		
		//do your stuff...
		ArrayList<HashOctreeCell> leafCells = tree.getLeafs();
		for(HashOctreeCell cell : leafCells){
			val = new ArrayList<Float>();
			// get the function values of all the corner vertices
			addFunctionValue2Vals(cell, 0b000, byVertex);
			addFunctionValue2Vals(cell, 0b001, byVertex);
			addFunctionValue2Vals(cell, 0b010, byVertex);
			addFunctionValue2Vals(cell, 0b011, byVertex);
			addFunctionValue2Vals(cell, 0b100, byVertex);
			addFunctionValue2Vals(cell, 0b101, byVertex);
			addFunctionValue2Vals(cell, 0b110, byVertex);
			addFunctionValue2Vals(cell, 0b111, byVertex);
			pushCube(cell);
		}
	}
	
	private void addFunctionValue2Vals(MarchableCube cube, int Obxyz, ArrayList<Float> byVertex){
		MarchableCube corner = cube.getCornerElement(Obxyz, tree);
		val.add(byVertex.get(corner.getIndex()));
	}
	
	/**
	 * Perform dual marchingCubes on the tree
	 */
	public void dualMC(ArrayList<Float> byVertex) {

		//do your stuff
		
	}
	
	/**
	 * March a single cube: compute the triangles and add them to the wireframe model
	 * @param n
	 */
	private void pushCube(MarchableCube n){

		// initialize array of edge-info
		Point2i[] edgeInfo = new Point2i[15];	
		for(int i=0; i<edgeInfo.length; i++){
			edgeInfo[i] = new Point2i(0, 0);
		}
		float[] cornerVals = toArray(this.val);
		MCTable.resolve(toArray(this.val), edgeInfo);
		
		// Iterate over all the triangles to create
		for(int i=0; i<edgeInfo.length; i++){
			if(edgeInfo[i].x==-1 || edgeInfo[i].y==-1){
				continue;
			}
			MarchableCube vert1 = n.getCornerElement(edgeInfo[i].x, tree);
			MarchableCube vert2 = n.getCornerElement(edgeInfo[i].y, tree);
			float val1 = cornerVals[edgeInfo[i].x];
			float val2 = cornerVals[edgeInfo[i].y];
			Point3f pos = interpolatePosition(vert1.getPosition(), vert2.getPosition(), val1, val2);
			result.vertices.add(pos);
			faceCount++;
			if(faceCount%3==0){
				result.faces.add(new int[]{faceCount-3,faceCount-2,faceCount-1});
			}
		}

	}

	private Point3f interpolatePosition(Point3f posA, Point3f posB, float a, float b) {
		Point3f pos = new Point3f(posA);
		pos.scale(1.f-a/(a-b));
		Point3f addTerm = new Point3f(posB);
		addTerm.scale(a/(a-b));
		pos.add(addTerm);
		return pos;
	}

	private float[] toArray(ArrayList<Float> values) {
		float[] array = new float[values.size()];
		for(int i=0; i<values.size(); i++){
			array[i] = values.get(i);
		}
		return array;
	}

	/**
	 * Get a nicely marched wireframe mesh...
	 * @return
	 */
	public WireframeMesh getResult() {
		return this.result;
	}

	/**
	 * compute a key value
	 * @param n
	 * @param e
	 * @return
	 */
	private Point2i compute_key(MarchableCube n, Point2i e) {
		Point2i p = new Point2i(n.getCornerElement(e.x, tree).getIndex(),
				n.getCornerElement(e.y, tree).getIndex());
		if(p.x > p.y) {
			int temp = p.x;
			p.x= p.y; p.y = temp;
		}
		return p;
	}

}
