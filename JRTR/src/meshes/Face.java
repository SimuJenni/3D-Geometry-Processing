package meshes;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.vecmath.Point3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

public class Face extends HEElement {

	private class ObtuseInfo {
		boolean isObtuse;
		Vertex obtuseVertex;
	}


	private HalfEdge anEdge;
	private ObtuseInfo optuseInfo;
	
	
	public Face(){
		anEdge = null;
	}

	public void setHalfEdge(HalfEdge he) {
		this.anEdge = he;
	}

	public HalfEdge getHalfEdge() {
		return anEdge;
	}
	
	public Iterator<Vertex> iteratorFV(){
		return new IteratorFV(anEdge);
	}
	
	public Iterator<HalfEdge> iteratorFE(){
		return new IteratorFE(anEdge);
	}
	
	
	public Vector3f normal() {
		Vector3f normal = new Vector3f();
		Vector3f v1 = new Vector3f(anEdge.end().pos);
		v1.sub(anEdge.start().pos);
		Vector3f v2 = new Vector3f(anEdge.next.end().pos);
		v2.sub(anEdge.next.start().pos);
		normal.cross(v1, v2);
		normal.normalize();
		return normal;
	}
	
	public String toString(){
		if(anEdge == null){
			return "f: not initialized";
		}
		String s = "f: [";
		Iterator<Vertex> it = this.iteratorFV();
		while(it.hasNext()){
			s += it.next().toString() + " , ";
		}
		s+= "]";
		return s;
		
	}
	
	public final class IteratorFE implements Iterator<HalfEdge> {
		
		private HalfEdge first, actual;

		public IteratorFE(HalfEdge anEdge) {
			first = anEdge;
			actual = null;
		}

		@Override
		public boolean hasNext() {
			return actual == null || actual.next != first;
		}

		@Override
		public HalfEdge next() {
			if(!hasNext()){
				throw new NoSuchElementException();
			}
			actual = (actual == null?
						first:
						actual.next);
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
	

	public final class IteratorFV implements Iterator<Vertex> {
		
		private HalfEdge first, actual;

		public IteratorFV(HalfEdge anEdge) {
			first = anEdge;
			actual = null;
		}

		@Override
		public boolean hasNext() {
			return actual == null || actual.next != first;
		}

		@Override
		public Vertex next() {
			if(!hasNext()){
				throw new NoSuchElementException();
			}
			actual = (actual == null?
						first:
						actual.next);
			return actual.incident_v;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		public Face face() {
			return first.incident_f;
		}
	}


	public float voroniAreaForVertex(Vertex v) {
		if(this.optuseInfo==null){
			optuseInfo = checkObtuseness();
		}
		if(optuseInfo.isObtuse){
			return optuseInfo.obtuseVertex==v ? area()/2 : area()/4;
		} 
		else {
			HalfEdge e1 = edgeStartingWith(v);
			HalfEdge e2 = edgeEndingWith(v);
			float alpha = e2.angleOppositeVertex();
			float beta = e1.angleOppositeVertex();
			return (float) (e1.getVector().lengthSquared()/Math.tan(beta) +
					e2.getVector().lengthSquared()/Math.tan(alpha))/8;
		}
		
	}

	private HalfEdge edgeEndingWith(Vertex v) {
		Iterator<HalfEdge> eIter = iteratorFE();
		while(eIter.hasNext()){
			HalfEdge e = eIter.next();
			if(e.end()==v){
				return e;
			}
		}
		return null;
	}

	private HalfEdge edgeStartingWith(Vertex v) {
		Iterator<HalfEdge> eIter = iteratorFE();
		while(eIter.hasNext()){
			HalfEdge e = eIter.next();
			if(e.start()==v){
				return e;
			}
		}
		return null;
	}

	private float area() {
		Vector3f crossVec = new Vector3f();
		crossVec.cross(anEdge.getVector(), anEdge.next.getVector());
		return crossVec.length()/2;
	}

	private ObtuseInfo checkObtuseness() {
		optuseInfo = new ObtuseInfo();
		Iterator<HalfEdge> edgeIt = iteratorFE();
		while(edgeIt.hasNext()){
			HalfEdge e = edgeIt.next();
			if(e.angleOppositeVertex()>Math.PI/2){
				optuseInfo.isObtuse = true;
				optuseInfo.obtuseVertex = e.oppositeVertx();
			}
		}
		return optuseInfo;
	}

}
