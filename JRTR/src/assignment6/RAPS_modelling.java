package assignment6;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import assignment4.HSCopyTools;
import assignment4.LMatrices;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import sparse.CSRMatrix;
import sparse.solver.MATLABSolver;
import utils.Utils;



/**
 * As rigid as possible deformations.
 * @author Alf
 *
 */
public class RAPS_modelling {

	//ArrayList containing all optimized rotations,
	//keyed by vertex.index
	ArrayList<Matrix3f> rotations;
	
	//keep a copy of the original halfEdge structure.
	//The original halfedge structure is needed to compute the correct
	//rotation matrices.
	private HalfEdgeStructure hs_originl, hs_deformed;
	
	//The unnormalized cotan weight matrix, with zero rows for
	//boundary vertices.
	//It can be computed once at setup time and then be reused
	//to compute the matrix needed for position optimization
	CSRMatrix L_cotan;
	CSRMatrix L_cotan_transposed;
	CSRMatrix LTL;

	//The deformed mesh
	CSRMatrix L_deform;
	CSRMatrix I;

	
	//allocate righthand sides and x only once.
	ArrayList<Float>[] b;
	ArrayList<Float> x;

	//sets of vertex indices that are constrained.
	private HashSet<Integer> keepFixed;
	private HashSet<Integer> deform;
	
	//Hashmap from edges to cotan weights to reuse weights
	private HashMap<HalfEdge, Float> weightMap;

	private float constrainWeight = 100;
	
	private Cholesky cholesky;

	/**
	 * The mesh to be deformed
	 * @param hs
	 */
	public RAPS_modelling(HalfEdgeStructure hs){
		this.hs_originl = new HalfEdgeStructure(hs); //deep copy the original mesh
		this.hs_deformed = hs;
		

		this.keepFixed = new HashSet<>();
		this.deform = new HashSet<>();
		
		rotations = new ArrayList<>();
		for(Vertex v : hs_deformed.getVertices()){
			rotations.add(new Matrix3f());
		}
		
		// Init rotations to identity and compute weights
		resetRotations();
		init_b_x(hs);
		initWeightMap();
		
		// Compute the unnormalised cotanLapl at initialisation and reuse later
		L_cotan = LMatrices.unnormalizedCotanLaplacian(hs_originl);
		L_cotan_transposed = L_cotan.transposed();
		// Compute L^T*L 
		LTL = new CSRMatrix(L_cotan.nRows(), L_cotan.nCols());
		L_cotan_transposed.mult(L_cotan, LTL);
		compute_b();
	}
	
	/**
	 * Set which vertices should be kept fixed. 
	 * @param verts_idx
	 */
	public void keep(Collection<Integer> verts_idx) {
		this.keepFixed.clear();
		this.keepFixed.addAll(verts_idx);

	}
	
	/**
	 * constrain these vertices to the new target position
	 */
	public void target(Collection<Integer> vert_idx){
		this.deform.clear();
		this.deform.addAll(vert_idx);
	}
	
	
	/**
	 * update the linear system used to find optimal positions
	 * for the currently constrained vertices.
	 * Good place to do the cholesky decompositoin
	 */
	public void updateL() {
		ArrayList<Vertex> verts = this.hs_deformed.getVertices();
		I = new CSRMatrix(verts.size(), verts.size());
		for(Vertex v:verts){
			if(this.keepFixed.contains(v.index) || this.deform.contains(v.index) || v.isOnBoundary()){
				I.set(v.index, v.index, constrainWeight*constrainWeight);
			}
		}
		this.L_deform = new CSRMatrix( LTL.nRows(), LTL.nCols());
		this.L_deform.add(LTL, I);
//		this.L_deform.assertSymetric();
		this.cholesky = new Cholesky(L_deform);
	}
	
	/**
	 * The RAPS modelling algorithm.
	 * @param t
	 * @param nRefinements
	 */
	public void deform(Matrix4f t, int nRefinements){
		this.transformTarget(t);
		
		deform(nRefinements);
	}
	
	public void deform(int nRefinements){
		for(int i=0; i<nRefinements;i++){
			this.optimalPositions();
			this.optimalRotations();
		}
	}

	/**
	 * Method to transform the target positions and do nothing else.
	 * @param t
	 */
	public void transformTarget(Matrix4f t) {
		for(Vertex v : hs_deformed.getVertices()){
			if(deform.contains(v.index)){
				t.transform(v.getPos());
			}
		}
	}
	/**
	 * ArrayList keyed with the vertex indices.
	 * @return
	 */
	public ArrayList<Matrix3f> getRotations() {
		return rotations;
	}

	/**
	 * Getter for undeformed version of the mesh
	 * @return
	 */
	public HalfEdgeStructure getOriginalCopy() {
		return hs_originl;
	}
	
		
	/**
	 * initialize b and x
	 * @param hs
	 */
	private void init_b_x(HalfEdgeStructure hs) {
		b = new ArrayList[3];
		for(int i = 0; i < 3; i++){
			b[i] = new ArrayList<>(hs.getVertices().size());
			for(int j = 0; j < hs.getVertices().size(); j++){
				b[i].add(0.f);
			}
		}
		x = new ArrayList<>(hs.getVertices().size());
		for(int j = 0; j < hs.getVertices().size(); j++){
			x.add(0.f);
		}
	}
	
	/**
	 * Iinitialises the weightmap with the cotan weights of the edges
	 */
	private void initWeightMap(){
		this.weightMap = new HashMap<HalfEdge, Float>();
		ArrayList<HalfEdge> edges = this.hs_originl.getHalfEdges();
		for(HalfEdge e:edges){
			weightMap.put(e, e.computeCotanWeights());
		}
	}
	
	/**
	 * Compute optimal positions for the current rotations.
	 */
	public void optimalPositions(){
		compute_b();
		int n = this.hs_originl.getVertices().size();
		ArrayList<Float> tmp1 = new ArrayList<>(n);
		ArrayList<Float> tmp2 = new ArrayList<>(n);
		ArrayList<Float> tmp3 = new ArrayList<>(n);

		// Solving for x,y and z positions separately
		for(int i=0;i<3;i++){
			//Construct right-hand side of equation (2)
			this.L_cotan.transposed().mult(this.b[i], tmp1);
			HSCopyTools.copy(hs_deformed, tmp2, i);
			this.I.mult(tmp2, tmp3);
			// Solve for positions
			this.cholesky.solve(Utils.addArrays(tmp1, tmp3), this.x);
			// Copy result to HalfEdgeStructure
			HSCopyTools.copy(x, hs_deformed, i);
		}
	}
	

	/**
	 * compute the righthand side for the position optimization
	 */
	private void compute_b() {
		reset_b();

		ArrayList<Vertex> verts = this.hs_originl.getVertices();
		for(Vertex v:verts){
			if(v.isOnBoundary()){
				// If on boundary keep b_i=0
				continue;
			}
			// Compute entry v.index() of b
			Vector3f b_i = new Vector3f();
			Matrix3f R1 = this.rotations.get(v.index);
			Matrix3f R = new Matrix3f();
			// Iterating over neighborhood of v
			Iterator<HalfEdge> eItr = v.iteratorVE();
			while(eItr.hasNext()){
				HalfEdge e = eItr.next();
				float w_ij = this.weightMap.get(e);
				Matrix3f R2 = this.rotations.get(e.end().index);
				R.add(R1, R2);
				Vector3f diff = new Vector3f(e.getVector());
				R.transform(diff);
				diff.scale(w_ij*0.5f);
				b_i.add(diff);
			}
			// Set resulting values into b
			float[] bVals = new float[]{b_i.x, b_i.y,b_i.z};
			for(int i=0;i<3;i++){
				this.b[i].set(v.index, bVals[i]);
			}
		}	
	}


	/**
	 * helper method
	 */
	private void reset_b() {
		for(int i = 0 ; i < 3; i++){
			for(int j = 0; j < b[i].size(); j++){
				b[i].set(j,0.f);
			}
		}
	}

	/**
	 * helper method
	 */
	public void resetRotations(){
		for(Matrix3f r: rotations){
			r.setIdentity();
		}
	}

	/**
	 * Compute the optimal rotations for 1-neighborhoods, given
	 * the original and deformed positions.
	 */
	public void optimalRotations() {
		//for the svd.
		Linalg3x3 l = new Linalg3x3(3);// argument controls number of iterations for ed/svd decompositions 
										//3 = very low precision but high speed. 3 seems to be good enough
			
		//Note: slightly better results are achieved when the absolute of cotangent
		//weights w_ij are used instead of plain cotangent weights.		
			
		// Matrices for the SVD computation
		Matrix3f U = new Matrix3f();
		Matrix3f V = new Matrix3f();
		Matrix3f sigma = new Matrix3f();
		Matrix3f ppT = new Matrix3f();
		for(int i=0;i<rotations.size();i++){
			Vertex v_orig = this.hs_originl.getVertices().get(i);
			Vertex v_deform = this.hs_deformed.getVertices().get(i);
			Iterator<HalfEdge> eItr_orig = v_orig.iteratorVE();
			Iterator<HalfEdge> eItr_deform = v_deform.iteratorVE();
			Matrix3f S_i = new Matrix3f();
			while(eItr_orig.hasNext() || eItr_deform.hasNext()){
				HalfEdge e_orig = eItr_orig.next();
				HalfEdge e_deform = eItr_deform.next();
				// Use absolute of cotan weights
				float w_ij = Math.abs(this.weightMap.get(e_orig));
				Vector3f e_ij = e_orig.getVector();
				Vector3f e_ij_prime = e_deform.getVector();
				compute_ppT(e_ij, e_ij_prime, ppT);
				ppT.mul(w_ij);
				S_i.add(ppT);
			}
			l.svd(S_i, U, sigma, V);
			if(U.determinant()<0){
				// Scale last column by -1 if determinant is negative
				U.m02 = -U.m02;
				U.m12 = -U.m12;
				U.m22 = -U.m22;
			}
			// Compute the rotation
			Matrix3f R = new Matrix3f();
			U.transpose();
			R.mul(V, U);
			// Update rotation
			this.rotations.set(i, R);
		}
	}

	

	private void compute_ppT(Vector3f p, Vector3f p2, Matrix3f pp2T) {
		assert(p.x*0==0);
		assert(p.y*0==0);
		assert(p.z*0==0);

		pp2T.m00 = p.x*p2.x; pp2T.m01 = p.x*p2.y; pp2T.m02 = p.x*p2.z; 
		pp2T.m10 = p.y*p2.x; pp2T.m11 = p.y*p2.y; pp2T.m12 = p.y*p2.z; 
		pp2T.m20 = p.z*p2.x; pp2T.m21 = p.z*p2.y; pp2T.m22 = p.z*p2.z; 

	}
	
}
