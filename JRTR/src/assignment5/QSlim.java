package assignment5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import meshes.Face;
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
	
	/********************************************
	 * Use or discard the skeletton, as you like.
	 ********************************************/
	
	public QSlim(HalfEdgeStructure hs) {
		this.hs = hs;
		this.collapser = new HalfEdgeCollapse(hs);
		this.errorMatrices = new HashMap<Vertex, Transformation>();
		init();
	}
	
	
	/**
	 * Compute per vertex matrices
	 * Compute edge collapse costs,
	 * Fill up the Priority queue/heap or similar
	 */
	private void init(){
		ArrayList<Vertex> verts = hs.getVertices();
		for(Vertex v:verts){
			Transformation Q_tot = new Transformation(new Matrix4f());
			// Iterate over all adjacent faces
			Iterator<Face> fItr = v.iteratorVF();
			while(fItr.hasNext()){
				Face f = fItr.next();
				Vector3f p0 = new Vector3f(v.getPos());
				Vector4f p = new Vector4f(f.normal());
				p.w = -p0.dot(f.normal());
				Transformation Q = new Transformation(new Matrix4f());
				compute_ppT(p,Q);
				Q_tot.add(Q);
			}
			this.errorMatrices.put(v, Q_tot);
		}
		
	}
	
	
	/**
	 * The actual QSlim algorithm, collapse edges until
	 * the target number of vertices is reached.
	 * @param target
	 */
	public void simplify(int target){
		
	}
	
	
	/**
	 * Collapse the next cheapest eligible edge. ; this method can be called
	 * until some target number of vertices is reached.
	 */
	public void collapsEdge(){
		
	}
	
	/**
	 * helper method that might be useful..
	 * @param p
	 * @param ppT
	 */
	private void compute_ppT(Vector4f p, Transformation ppT) {
		assert(p.x*0==0);
		assert(p.y*0==0);
		assert(p.z*0==0);
		assert(p.w*0==0);
		ppT.m00 = p.x*p.x; ppT.m01 = p.x*p.y; ppT.m02 = p.x*p.z; ppT.m03 = p.x*p.w;
		ppT.m10 = p.y*p.x; ppT.m11 = p.y*p.y; ppT.m12 = p.y*p.z; ppT.m13 = p.y*p.w;
		ppT.m20 = p.z*p.x; ppT.m21 = p.z*p.y; ppT.m22 = p.z*p.z; ppT.m23 = p.z*p.w;
		ppT.m30 = p.w*p.x; ppT.m31 = p.w*p.y; ppT.m32 = p.w*p.z; ppT.m33 = p.w*p.w;
			
		
	}
	
	
	
	
	
	/**
	 * Represent a potential collapse
	 * @author Alf
	 *
	 */
	protected class PotentialCollapse implements Comparable<PotentialCollapse>{

		@Override
		public int compareTo(PotentialCollapse arg1) {
			return -1;
		}
	}





	public HashMap<Vertex, Transformation> getErrorMatrices() {
		return errorMatrices;
	}

}
