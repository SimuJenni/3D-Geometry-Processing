package meshes;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;


/**
 * Implementation of a vertex for the {@link HalfEdgeStructure}
 */
public class Vertex extends HEElement{
	
	/**position*/
	Point3f pos;
	/**adjacent edge: this vertex is startVertex of anEdge*/
	HalfEdge anEdge;
	
	/**The index of the vertex, mainly used for toString()*/
	public int index;

	public Vertex(Point3f v) {
		pos = v;
		anEdge = null;
	}
	
	
	public Point3f getPos() {
		return pos;
	}

	public void setHalfEdge(HalfEdge he) {
		anEdge = he;
	}
	
	public HalfEdge getHalfEdge() {
		return anEdge;
	}
	
	/**
	 * Get an iterator which iterates over the 1-neighbouhood
	 * @return
	 */
	public Iterator<Vertex> iteratorVV(){
		return new IteratorVV(anEdge);
	}
	
	/**
	 * Iterate over the outgoing edges
	 * @return
	 */
	public Iterator<HalfEdge> iteratorVE(){
		return new IteratorVE(anEdge);
	}
	
	/**
	 * Iterate over the neighboring faces
	 * @return
	 */
	public Iterator<Face> iteratorVF(){
		return new IteratorVF(anEdge);
	}
	
	
	public String toString(){
		return "" + index;
		//return pos.toString();
	}
	
	/**
	 * Test if vertex w is adjacent to this vertex.
	 * Will work once the iterators over neigbors are implemented
	 * @param w
	 * @return
	 */
	public boolean isAdjascent(Vertex w) {
		boolean isAdj = false;
		Vertex v = null;
		Iterator<Vertex> it = iteratorVV();
		while(it.hasNext()){
			v= it.next();
			if( v==w){
				isAdj=true;
			}
		}
		return isAdj;
	}


	/**
	 * Test if vertex w is adjacent to this vertex.
	 * Will work once the iterators over neigbors are implemented
	 * @param w
	 * @return
	 */
	public boolean isOnBoundary() {
		Iterator<HalfEdge> it = iteratorVE();
		while(it.hasNext()){
			if(it.next().isOnBorder()){
				return true;
			}
		}
		return false;
	}
	
	
	
	public final class IteratorVE implements Iterator<HalfEdge> {
		
		private HalfEdge first, actual;

		public IteratorVE(HalfEdge anEdge) {
			first = anEdge;
			actual = null;
		}

		@Override
		public boolean hasNext() {
			return actual == null || actual.opposite.next != first;
		}

		@Override
		public HalfEdge next() {
			if(!hasNext()){
				throw new NoSuchElementException();
			}
			actual = (actual == null?
					first:
					actual.opposite.next);
				
			return actual;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		public Face face() {
			return first.incident_f;
		}
	}
	
	public final class IteratorVV implements Iterator<Vertex> {
		
		private HalfEdge first, actual;

		public IteratorVV(HalfEdge anEdge) {
			first = anEdge;
			actual = null;
		}

		@Override
		public boolean hasNext() {
			return actual == null || actual.opposite.next != first;
		}

		@Override
		public Vertex next() {
			if(!hasNext()){
				throw new NoSuchElementException();
			}
			actual = (actual == null?
					first:
					actual.opposite.next);
				
			return actual.end();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		public Face face() {
			return first.incident_f;
		}
	}
	
	public final class IteratorVF implements Iterator<Face> {
		
		private HalfEdge first, actual;

		public IteratorVF(HalfEdge anEdge) {
			first = anEdge;
			actual = null;
		}

		@Override
		public boolean hasNext() {
			while(actual!=null && actual.opposite.next.incident_f==null){
				actual = actual.opposite.next;
			}	
			return actual == null || actual.opposite.next != first;
		}

		@Override
		public Face next() {
			if(!hasNext()){
				throw new NoSuchElementException();
			}
			actual = (actual == null?
					first:
					actual.opposite.next);
				
			return actual.incident_f;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		public Face face() {
			return first.incident_f;
		}
	}

	public void setPos(Point3f point3f) {
		this.pos=point3f;
	}


	public float computeMixedArea() {
		Iterator<Face> faceIt = iteratorVF();
		float areaSum = 0;
		while(faceIt.hasNext()){
			Face face = faceIt.next();
			areaSum += face.voroniAreaForVertex(this);
		}
		return areaSum;
	}


	public float getMeanCurvature() {
		Iterator<HalfEdge> edgeIt = iteratorVE();
		Vector3f laplacian = new Vector3f();
		// Iterate over all outgoing edges
		while(edgeIt.hasNext()){
			HalfEdge e = edgeIt.next();
			// Get v_i - v_j
			Vector3f diffVec = new Vector3f(e.getOpposite().getVector());
			// Get angles
			float beta = e.angleOppositeVertex();
			float alpha = e.getOpposite().angleOppositeVertex();
			diffVec.scale((float) (1/Math.tan(alpha)+1/Math.tan(beta)));
			laplacian.add(diffVec);
		}
		laplacian.scale(1/(2*computeMixedArea()));
		return laplacian.length()*0.5f;
	}

	
}
