package assignment3;

import java.util.ArrayList;

import assignment2.HashOctree;
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

	
	/**
	 * Implementation of the marching cube algorithm. pass the tree
	 * and either the primary values associated to the trees edges
	 * @param tree
	 * @param byLeaf
	 */
	public MarchingCubes(HashOctree tree){
		this.tree = tree;

	}

	/**
	 * Perform primary Marching cubes on the tree.
	 */
	public void primaryMC(ArrayList<Float> byVertex) {
		this.val = byVertex;
		this.result = new WireframeMesh();
		
		//do your stuff...
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

		//do your stuff
		
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
