package assignment3;

import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Point3f;

import assignment2.HashOctree;
import assignment2.HashOctreeCell;
import assignment2.HashOctreeVertex;
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
	private int vertCount;
	private HashMap<Point2i, Integer> edgeMap;
	private ArrayList<Point3f> createdVerts;
	private ArrayList<Integer> usedVerts;
	
	/**
	 * Implementation of the marching cube algorithm. pass the tree
	 * and either the primary values associated to the trees edges
	 * @param tree
	 * @param byLeaf
	 */
	public MarchingCubes(HashOctree tree){
		this.tree = tree;
		vertCount = 0;
		edgeMap = new HashMap<Point2i, Integer>();
		createdVerts = new ArrayList<Point3f>();
		usedVerts = new ArrayList<Integer>();
	}

	/**
	 * Perform primary Marching cubes on the tree.
	 */
	public void primaryMC(ArrayList<Float> byVertex) {
		this.val = byVertex;
		this.result = new WireframeMesh();
		
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
	
	private void addFunctionValue2Vals(HashOctreeCell cell, int Obxyz, ArrayList<Float> byVertex){
		MarchableCube cornerVert = cell.getCornerElement(Obxyz, tree);
		val.add(byVertex.get(cornerVert.getIndex()));
	}
	
	private void addFunctionValue2Vals(HashOctreeVertex cube, int Obxyz, ArrayList<Float> byVertex){
		MarchableCube cornerCell = cube.getCornerElement(Obxyz, tree);
		// Must compute the interpolated function value at the center (average of corner values)
		float avg = byVertex.get(cornerCell.getCornerElement(0b000, tree).getIndex());
		avg += byVertex.get(cornerCell.getCornerElement(0b001, tree).getIndex());
		avg += byVertex.get(cornerCell.getCornerElement(0b010, tree).getIndex());
		avg += byVertex.get(cornerCell.getCornerElement(0b011, tree).getIndex());
		avg += byVertex.get(cornerCell.getCornerElement(0b100, tree).getIndex());
		avg += byVertex.get(cornerCell.getCornerElement(0b101, tree).getIndex());
		avg += byVertex.get(cornerCell.getCornerElement(0b110, tree).getIndex());
		avg += byVertex.get(cornerCell.getCornerElement(0b111, tree).getIndex());
		val.add(avg/8.f);
	}
	
	/**
	 * Perform dual marchingCubes on the tree
	 */
	public void dualMC(ArrayList<Float> byVertex) {		
		this.val = byVertex;
		this.result = new WireframeMesh();
		
		ArrayList<HashOctreeVertex> verts = tree.getVertices();
		for(  HashOctreeVertex vert : verts){
			if(tree.isOnBoundary(vert)){
				continue;
			}
			val = new ArrayList<Float>();
			// get the function values of all the corner vertices
			addFunctionValue2Vals(vert, 0b000, byVertex);
			addFunctionValue2Vals(vert, 0b001, byVertex);
			addFunctionValue2Vals(vert, 0b010, byVertex);
			addFunctionValue2Vals(vert, 0b011, byVertex);
			addFunctionValue2Vals(vert, 0b100, byVertex);
			addFunctionValue2Vals(vert, 0b101, byVertex);
			addFunctionValue2Vals(vert, 0b110, byVertex);
			addFunctionValue2Vals(vert, 0b111, byVertex);
			pushCube(vert);
		}
		
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
		
		ArrayList<Integer> faceIndexes = new ArrayList<Integer>();
		
		// Iterate over all the triangles to create
		for(int i=0; i<edgeInfo.length; i++){
			if(edgeInfo[i].x==-1 || edgeInfo[i].y==-1){
				continue;
			}
			MarchableCube vert1 = n.getCornerElement(edgeInfo[i].x, tree);
			MarchableCube vert2 = n.getCornerElement(edgeInfo[i].y, tree);
			int vertIndex = vertCount;
			if(edgeMap.containsKey(compute_key(n, edgeInfo[i]))){
				// If edge already stored reuse vertex
				vertIndex = edgeMap.get(compute_key(n, edgeInfo[i]));
			}
			else {
				float val1 = cornerVals[edgeInfo[i].x];
				float val2 = cornerVals[edgeInfo[i].y];
				Point3f pos = interpolatePosition(vert1.getPosition(), vert2.getPosition(), val1, val2);
				createdVerts.add(pos);
				// Add newly created vertex to edgeMap
				edgeMap.put(compute_key(n, edgeInfo[i]), vertCount);
				vertCount++;
			}
			faceIndexes.add(vertIndex);
		}
		extractValidFaces(faceIndexes);
	}

	private void extractValidFaces(ArrayList<Integer> faceIndexes) {
		assert(faceIndexes.size()%3==0);
		for(int i=0; i<faceIndexes.size()/3; i++){
			int idx1 = faceIndexes.get(i*3);
			int idx2 = faceIndexes.get(i*3+1);
			int idx3 = faceIndexes.get(i*3+2);
			if(idx1==idx2||idx1==idx3||idx2==idx3){
				// Skip degenerate triangles
				continue;
			}
			result.faces.add(new int[]{idx1, idx2, idx3});
			addMissingVert2Result(idx1);
			addMissingVert2Result(idx2);
			addMissingVert2Result(idx3);
		}
	}

	private void addMissingVert2Result(int idx) {
		if(!usedVerts.contains(idx)){
			usedVerts.add(idx);
			result.vertices.add(this.createdVerts.get(idx));
		}
		assert(vertCount==createdVerts.size());
		assert(vertCount>=usedVerts.size());
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
		assert(values.size()==15);
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
