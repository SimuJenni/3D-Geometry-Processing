package meshes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;


/**
 * An implementation of a half-edge Structure.
 * This particular implementation can only handle orientable 
 * Manifold meshes with or without borders. This structure explicitely 
 * stores a list of {@link HalfEdge}s, {@link Face}s and {@link Vertex}s.
 * 
 * <p>
 * Every half-edge has an opposite, next and prev, but on boundary edges 
 * one edge of the half-edge pair will have a null face.
 * </p>
 * <p>
 * Initialize the HalfEdgeStructure with the {@link #init(WireframeMesh)} method
 * </p>
 * 
 * @author bertholet
 *
 */

public class HalfEdgeStructure {
	
	private ArrayList<HalfEdge> edges;
	ArrayList<Face> faces;
	ArrayList<Vertex> vertices;
	
	public HalfEdgeStructure(){
		faces = new ArrayList<Face>();
		edges = new ArrayList<HalfEdge>();
		vertices = new ArrayList<Vertex>();
	}
	
	
	//deep copy constructor
	public HalfEdgeStructure(HalfEdgeStructure hs) {
		faces = new ArrayList<Face>();
		edges = new ArrayList<HalfEdge>();
		vertices = new ArrayList<Vertex>();
		
		HashMap<Face, Face> f2f = new HashMap<>();
		for(Face f: hs.getFaces()){
			Face newFace = new Face();
			faces.add(newFace);
			f2f.put(f, newFace);
		}
		HashMap<Vertex, Vertex> v2v = new HashMap<>();
		for(Vertex v: hs.getVertices()){
			Vertex newV = new Vertex(new Point3f(v.getPos()));
			vertices.add(newV);
			v2v.put(v, newV);
		}
		HashMap<HalfEdge, HalfEdge> e2e = new HashMap<>();
		for(HalfEdge e: hs.getHalfEdges()){
			HalfEdge newE = new HalfEdge(f2f.get(e.getFace()), v2v.get(e.end()));
			e2e.put(e, newE);
			edges.add(newE);
		}
		
		//interlink everything: faces->edge
		for(Face f : hs.getFaces()){
			Face newFace = f2f.get(f);
			newFace.setHalfEdge(e2e.get(f.getHalfEdge()));
		}
		
		//vertex-> edge
		for(Vertex v : hs.getVertices()){
			Vertex newV = v2v.get(v);
			newV.setHalfEdge(e2e.get(v.getHalfEdge()));
		}
		
		//edge -> edge
		for(HalfEdge e: hs.getHalfEdges()){
			HalfEdge newHE = e2e.get(e);
			newHE.setNext(e2e.get(e.next));
			newHE.setPrev(e2e.get(e.prev));
			newHE.setOpposite(e2e.get(e.opposite));
		}
		
		this.enumerateVerticesAndEdges();
	}


	public ArrayList<Vertex> getVertices() {
		return vertices;
	}
	
	public ArrayList<HalfEdge> getHalfEdges() {
		return edges;
	}
	
	public ArrayList<Face> getFaces() {
		return faces;
	}
	
	/**
	 * Get an Iterator over all Vertices
	 * @return
	 */
	public Iterator<Vertex> iteratorV(){
		return vertices.iterator();
	}
	
	/**
	 * Get an Iterator over all Edges
	 * @return
	 */
	public Iterator<HalfEdge> iteratorE(){
		return edges.iterator();
	}
	
	/**
	 * Get an Iterator over all Faces
	 * @return
	 */
	public Iterator<Face> iteratorF(){
		return faces.iterator();
	}
	
	
	/**
	 * Create a Halfedge Structure from a wireframe mesh described by the vertex Positions verts and fcs; this
	 * method throws a MeshNotOrientedException if the input mesh is not oriented consistently.
	 * 
	 * Runs in O(n*m), where n is the number of faces and m is the average number of vertices per face
	 * @param verts
	 * @param fcs
	 * @param v_per_face
	 * @throws MeshNotOrientedException
	 * @throws DanglingTriangleException when dangling Triangles are detected
	 */
	public void init(WireframeMesh m) throws MeshNotOrientedException, DanglingTriangleException{
		
		//add all vertices
		for(Point3f v: m.vertices){
			vertices.add(new Vertex(v));
			
		}
		
		// local vars... 
		int j;
		Point2i key;
		HalfEdge he = null; 
		HalfEdge first_he = null;
		HalfEdge prev_he = null;
		HalfEdge next_he;
		
		HashMap<Point2i, HalfEdge> edgeTable = new HashMap<Point2i, HalfEdge>(m.faces.size() + m.vertices.size());
		
		// for every face:
		int[] fc;
		for(int i = 0; i < m.faces.size(); i++){
			
			//add the face
			faces.add(new Face());
			
			fc = m.faces.get(i);
			//add all adjascent edges
			for(j = 0; j < fc.length; j++){
				
				key = new Point2i(fc[j], fc[(j+1)%fc.length]);
				
				//check well-orientedness:
				//every edge is found once if the mesh is oriented consistently
				if(edgeTable.containsKey(key)){
					throw new MeshNotOrientedException();
				}
				
				//create a new half edge
				he = new HalfEdge(faces.get(faces.size() -1), //the face that just was added
						vertices.get(fc[(j+1)%fc.length])); //the vertex the edge is pointing to
				
							
				//link between previous halfedge and the new halfedge
				if(j == 0){
					first_he = he;
				}
				else{
					prev_he.setNext(he);
					he.setPrev(prev_he);
				}
				//store the he for opposite he linkage
				edgeTable.put(key, he);
				
				//add the edge to vertex its outgoing, 
				//every vertex has to know an arbitrary outgoing halfedge.
				vertices.get(fc[j]).setHalfEdge(he);
				
				//interlink the halfedge with its opposite halfedge if it exists.
				key = new Point2i(key.y, key.x);
				if(edgeTable.containsKey(key)){
					he.setOpposite(edgeTable.get(key));
					edgeTable.get(key).setOpposite(he);
				}
				//update prev_he
				prev_he = he;
			}
			
			
			//close the circle
			he.setNext(first_he);
			first_he.setPrev(he);
			//add a he to the face
			faces.get(faces.size() -1).setHalfEdge(first_he);
		}
		
		edges.addAll(edgeTable.values());

		//finally: treat boundaries
		LinkedHashMap<Vertex,HalfEdge> boundaryEdges = new LinkedHashMap<Vertex, HalfEdge>();
		boolean dangling = false;
		
		//generate and interlink the opposite boundary halfEdges
		for(HalfEdge e: edges){
			if(e.opposite == null){
				he = new HalfEdge(null, e.getPrev().end());
				
				//throw an error later because the datastructure will be disfunctional on boundaries:
				// multiple boundary edges start at this vertex, so
				if(boundaryEdges.containsKey(e.end())){
					dangling = true;
				}
				//register them under the vertex 'he.getStart()'
				boundaryEdges.put(e.end(), he);
				he.setOpposite(e);
				e.setOpposite(he);
				
			}
		}
		
		// interlink previous and next boundary Edges
		for(HalfEdge e: boundaryEdges.values()){
			next_he = boundaryEdges.get(e.end());
			e.setNext(next_he);
			next_he.setPrev(e);
		}
		
		//add all the boundary Edges
		edges.addAll(boundaryEdges.values());
		
		if(dangling){
			throw new DanglingTriangleException();
		}
		
		
		this.enumerateVerticesAndEdges();
		
	}
	
	public void simpleSmooth(){
		int numVerts = vertices.size();
		ArrayList<Vertex> newVerts = new ArrayList<Vertex>();
		for(int i=0; i<numVerts; i++){
			Vertex v = vertices.get(i);
			Point3f avgPos = new Point3f();
			Iterator<Vertex> vertIt = v.iteratorVV();
			int valence = 0;
			while(vertIt.hasNext()){
				avgPos.add(vertIt.next().getPos());
				valence++;
			}
			avgPos.scale(1.0f/valence);
			v.setPos(avgPos);
			newVerts.add(v);
		}
		vertices = newVerts;
	}


/**
 * Assign consecutive values 0...vertices.size()-1 to the Vertex.index fields.
 */
	public void enumerateVerticesAndEdges() {
		int idx =0;
		for(Vertex v: vertices){
			v.index= idx++;
		}
		idx = 0;
		for(HalfEdge e:edges){
			e.setIndex(idx++);
		}
		
	}


public ArrayList<Vector3f> simpleNormals() {
	ArrayList<Vertex> verts = this.getVertices();
	ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
	for(Vertex v : verts){
		Iterator<Face> fitr = v.iteratorVF();
		Vector3f normal = new Vector3f();
		int faceCount = 0;
		while(fitr.hasNext()){
			Face f = fitr.next();
			normal.add(f.normal());
			faceCount++;
		}
		normal.scale((float) (1.0/faceCount));
		normals.add(normal);
	}
	
	return normals;
}

public float volume(){
	float vol = 0;
	for(Face f:this.faces){
		Iterator<Vertex> vertItr = f.iteratorFV();
		Vector3f v1 = new Vector3f(vertItr.next().getPos());
		Vector3f v2 = new Vector3f(vertItr.next().getPos());
		Vector3f v3 = new Vector3f(vertItr.next().getPos());
		Vector3f vCross = new Vector3f();
		vCross.cross(v2, v3);
		vol += v1.dot(vCross)/6;
	}
	return vol;
}

public float surfaceArea(){
	float area = 0;
	for(Face f:this.faces){
		area += f.area();
	}
	return area;
}

public void scale(float factor){
	for(Vertex v:this.vertices){
		v.pos.scale(factor);
	}
}

}
