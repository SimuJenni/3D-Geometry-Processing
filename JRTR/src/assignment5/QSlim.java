package assignment5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Point4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import meshes.Face;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import openGL.objects.Transformation;


/** 
 * implement the QSlim algorithm here
 * 
 * @author Alf
 *
 */
public class QSlim {

	private HalfEdgeStructure hs;
	private HalfEdgeCollapse collapser;
	private HashMap<Vertex, Transformation> errorMatrices;
	private PriorityQueue<PotentialCollapse> potCollaps;
	private HashMap<HalfEdge, PotentialCollapse> collapseLookup;
	private boolean optimal;
	
	/********************************************
	 * Use or discard the skeletton, as you like.
	 ********************************************/
	
	public QSlim(HalfEdgeStructure hs, boolean opt) {
		this.hs = hs;
		this.collapser = new HalfEdgeCollapse(hs);
		this.errorMatrices = new HashMap<Vertex, Transformation>();
		this.potCollaps = new PriorityQueue<PotentialCollapse>();
		this.collapseLookup = new HashMap<HalfEdge, PotentialCollapse>();
		this.optimal = opt;
		init();
	}
	
	
	/**
	 * Compute per vertex matrices
	 * Compute edge collapse costs,
	 * Fill up the Priority queue/heap or similar
	 */
	private void init(){
		// Compute all per vertex error matrices
		ArrayList<Vertex> verts = hs.getVertices();
		for(Vertex v:verts){
			Transformation Q_tot = computeVertexMatrix(v);
			this.errorMatrices.put(v, Q_tot);
		}
		
		// Compute all potential collapses with costs
		for(HalfEdge edge:hs.getHalfEdges()){
			createPotentialCollapse(edge);

		}
		
	}


	private void createPotentialCollapse(HalfEdge edge) {
		// Compute associated cost
		Transformation Q = new Transformation(this.errorMatrices.get(edge.start()));
		Q.add(errorMatrices.get(edge.start()), errorMatrices.get(edge.end()));
		
		Point3f target = computeTarget(edge, Q, this.optimal);
		float cost = computeCost(Q, target);
		PotentialCollapse collapse = new PotentialCollapse(edge, cost, target);
		collapse.Q = Q;
		updateCollapse(collapse);
	}


	private void updateCollapse(PotentialCollapse pC) {
		if(collapseLookup.containsKey(pC.edge)){
			collapseLookup.get(pC.edge).isDeleted = true;
		}
		potCollaps.add(pC);
		collapseLookup.put(pC.edge, pC);
	}


	private float computeCost(Transformation Q, Point3f target) {
		Vector4f p1 = new Vector4f(target);
		Vector4f p2 = new Vector4f(target);
		p1.w = 1;
		p2.w = 1;
		Q.transform(p1);
		float cost = p1.dot(p2);
		return Math.abs(cost);
	}


	private Point3f computeTarget(HalfEdge edge, Transformation Q, boolean opt) {
		// Compute target position
		Point3f target = new Point3f();
		Transformation M = new Transformation(Q);
		M.setRow(3,0,0,0,1);
		if(opt && Math.abs(M.determinant())>0.001f){
			// Optimal position
			M.invert();
			M.transform(target);
		} else {
			// Average
			target = new Point3f(edge.start().getPos());
			target.add(edge.end().getPos());
			target.scale(0.5f);	
		}
		return target;
	}

	private Transformation computeVertexMatrix(Vertex v) {
		Transformation Q_tot = new Transformation();
		// Iterate over all adjacent faces
		Iterator<Face> fItr = v.iteratorVF();
		while(fItr.hasNext()){
			Face f = fItr.next();
			Vector3f p0 = new Vector3f(f.normal());
			Point4f p = new Point4f(p0);
			p.w = -p0.dot(new Vector3f(v.getPos()));
			Transformation Q = new Transformation();
			compute_ppT(p,Q);
			Q_tot.add(Q);
		}
		return Q_tot;
	}
	
	
	/**
	 * The actual QSlim algorithm, collapse edges until
	 * the target number of vertices is reached.
	 * @param target
	 */
	public void simplify(int target){
		int num = hs.getVertices().size()-target;
		if(num<0){
			return;
		}
		for(int i=0;i<num;i++){
			if(i%(num/4)==0){
				System.out.println("Simplify vert "+i+"/"+num);
			}
			collapsEdge();
		}	
		this.collapser.finish();
	}
	
	
	/**
	 * Collapse the next cheapest eligible edge. ; this method can be called
	 * until some target number of vertices is reached.
	 */
	public void collapsEdge(){
		
		PotentialCollapse collapse = null;
		boolean collapseFound = false;
		while(!collapseFound ){
			// Get highest priority collapse
			collapse = this.potCollaps.poll();
			HalfEdge e = collapse.edge;
			if(collapse.isDeleted || collapser.isEdgeDead(e)){
				continue;
			} else
			// Check if collapse is okay and reinsert with higher cost if not
			if(collapser.isCollapseMeshInv(e, collapse.target) || !HalfEdgeCollapse.isEdgeCollapsable(e)){
				PotentialCollapse newCollapse = new PotentialCollapse(e, (Math.abs(collapse.cost) + 0.1f)*10, collapse.target);
				updateCollapse(newCollapse);
			} else {
				collapseFound = true;
			}
		}
						
		collapser.collapseEdge(collapse.edge, collapse.target);
						
		// Update error-matrix
		Vertex vEnd = collapse.edge.end();
		errorMatrices.put(vEnd, collapse.Q);
		
		// Update all adjacent edges
		Iterator<HalfEdge> edgeItr = vEnd.iteratorVE();
		while(edgeItr.hasNext()){
			HalfEdge e = edgeItr.next();
			createPotentialCollapse(e);
			createPotentialCollapse(e.getOpposite());	
		}
	}
	
	/**
	 * helper method that might be useful..
	 * @param p
	 * @param ppT
	 */
	private void compute_ppT(Point4f p, Transformation ppT) {
		if(p.x*0!=0 || p.y*0!=0 || p.z*0!=0 || p.w*0!=0){
			ppT = new Transformation();
		} else {
			ppT.m00 = p.x*p.x; ppT.m01 = p.x*p.y; ppT.m02 = p.x*p.z; ppT.m03 = p.x*p.w;
			ppT.m10 = p.y*p.x; ppT.m11 = p.y*p.y; ppT.m12 = p.y*p.z; ppT.m13 = p.y*p.w;
			ppT.m20 = p.z*p.x; ppT.m21 = p.z*p.y; ppT.m22 = p.z*p.z; ppT.m23 = p.z*p.w;
			ppT.m30 = p.w*p.x; ppT.m31 = p.w*p.y; ppT.m32 = p.w*p.z; ppT.m33 = p.w*p.w;
		}
	}
	
	/**
	 * Represent a potential collapse
	 * @author Alf
	 *
	 */
	protected class PotentialCollapse implements Comparable<PotentialCollapse> {
		
		public Transformation Q;
		private HalfEdge edge;
		private float cost;
		private Point3f target;
		public boolean isDeleted;
		
		public PotentialCollapse(HalfEdge edge, float cost, Point3f target) {
			super();
			this.edge = edge;
			this.cost = cost;
			this.target = target;
			this.isDeleted = false;
		}

		@Override
		public int compareTo(PotentialCollapse other) {
			return (int) Math.signum(this.cost - other.cost);
		}
		
		public String toString(){
			return "Cost: "+cost;
		}
		
	}

	public HashMap<Vertex, Transformation> getErrorMatrices() {
		return errorMatrices;
	}

}
