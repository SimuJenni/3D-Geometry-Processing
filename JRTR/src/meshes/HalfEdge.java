package meshes;

import javax.vecmath.Vector3f;

/**
 * Implementation of a half-edge for the {@link HalfEdgeStructure}
 * @author Alf
 *
 */
public class HalfEdge extends HEElement{
	
	/** The end vertex of this edge*/
	Vertex incident_v;
	
	/**The face this half edge belongs to, which is the face
	* this edge is positively oriented for. This can be null if
	* the half edge lies on a boundary*/
	Face incident_f;
	
	/**the opposite, next and previous edge*/
	HalfEdge opposite, next, prev;
	
	private Vector3f edgeVector;
	
	private int index;
	
	/**
	 * Initialize a half-edge with the Face it belongs to (the face it is
	 * positively oriented for) and the vertex it points to 
	 */
	public HalfEdge(Face f, Vertex v) {
		incident_v = v;
		incident_f = f;
		opposite = null;
	}
	
	/**
	 * Initialize a half-edge with the Face it belongs to (the face it is
	 * positively oriented for), the vertex it points to and its opposite
	 * half-edge.
	 * @param f
	 * @param v
	 * @param oppos
	 */
	public HalfEdge(Face f, Vertex v, HalfEdge oppos) {
		incident_v = v;
		incident_f = f;
		opposite = oppos;
	}

	
	/**
	 * If this is the edge (a->b) this returns the edge (b->a).
	 * @return
	 */
	public HalfEdge getOpposite() {
		return opposite;
	}
	
	public void setOpposite(HalfEdge opp) {
		this.opposite = opp;
	}

	/**
	 * will return the next edge on the face this half-edge belongs to. 
	 * (If this is the edge (a->b) on the triangle (a,b,c) this will be (b->c).
	 * @return
	 */
	public HalfEdge getNext() {
		return next;
	}
	
	public void setNext(HalfEdge he) {
		this.next = he;
	}
	
	/**
	 * Returns the face this half-edge belongs to. If this is the edge (b->a) lying
	 * on the faces (a,b,c) and (b,a,d) this will be the face (b,a,d). If the
	 * half-edge lies on a boundary this can be null.
	 * @return
	 */
	public Face getFace() {
		return incident_f;
	}

	/**
	 * will return the previous edge on the face this half-edge belongs to. 
	 * (If this is the edge (a->b) on the triangle (a,b,c) this will be (c->a).
	 * @return
	 */
	public HalfEdge getPrev() {
		return prev;
	}
	
	public void setPrev(HalfEdge he) {
		this.prev = he;
	}
	
	
	public void setEnd(Vertex v){
		this.incident_v = v;
	}

	
	public Vertex start(){
		return opposite.incident_v;
	}
	
	public Vertex end(){
		return incident_v;
	}
	
	
	public boolean hasFace(){
		return this.incident_f != null;
	}
	
	/**
	 * Returns true if this edge and its opposite have a face only on one side.
	 */
	public boolean isOnBorder(){
		return this.incident_f == null || opposite.incident_f == null;
	}
	
	@Override
	public String toString(){
		return "( " + start().toString() + " --> " + end().toString() + ")";
	}

	public Vector3f getVector() {
		edgeVector = new Vector3f(this.end().getPos());
		edgeVector.sub(this.start().getPos());
		return edgeVector;
	}

	public float angleOppositeVertex() {
		// Compute the angle by getting relevant vectors
		Vector3f b2x_i = new Vector3f(getPrev().getVector());
		Vector3f b2x_j = new Vector3f(getNext().getVector());
		b2x_j.negate();
		return b2x_i.angle(b2x_j);		
	}

	public Vertex oppositeVertx() {
		return getNext().end();
	}
	
	public float getLength(){
		return getVector().length();
	}
	
	public float computeCotanWeights() {
		float beta = this.angleOppositeVertex();
		float alpha = this.getOpposite().angleOppositeVertex();
		float cotanWeights =  (float) Math.min(1/Math.tan(alpha)+1/Math.tan(beta),1e2);
		return cotanWeights;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
