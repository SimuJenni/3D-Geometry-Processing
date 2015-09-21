package assignment1;

import java.io.IOException;
import java.util.Iterator;

import meshes.Face;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

/**
 * 
 * @author Alf
 *
 */
public class Assignment_1_3 {

	public static void main(String[] args) throws IOException{
		//Load a wireframe mesh
		WireframeMesh m = ObjReader.read("./objs/oneNeighborhood.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		
		//create a half-edge structure out of the wireframe description.
		//As not every mesh can be represented as a half-edge structure
		//exceptions could occur.
		try {
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
		
		
		// Tests iteratorFE
		Iterator<Face> itF = hs.iteratorF();
		while(itF.hasNext()){
			Face f = itF.next();
			System.out.println("Starting Face : "+f);
			Iterator<HalfEdge> edgeIt = f.iteratorFE();
			while(edgeIt.hasNext()){
				System.out.println("Adjacent edges: "+edgeIt.next());
			}
		}
		
		// Tests iterotarVE
		Iterator<Vertex> itV = hs.iteratorV();
		while(itV.hasNext()){
			Vertex v = itV.next();
			System.out.println("Starting Vertex : "+v);
			Iterator<HalfEdge> edgeIt = v.iteratorVE();
			while(edgeIt.hasNext()){
				System.out.println("Outgoing edges: "+edgeIt.next());
			}
		}
		
		// Tests iterotarVF
		itV = hs.iteratorV();
		while(itV.hasNext()){
			Vertex v = itV.next();
			System.out.println("Starting Vertex : "+v);
			Iterator<Face> faceIt = v.iteratorVF();
			while(faceIt.hasNext()){
				System.out.println("Neighboring faces: "+faceIt.next());
			}
		}
		
		// Tests iterotarVV
		itV = hs.iteratorV();
		while(itV.hasNext()){
			Vertex v = itV.next();
			System.out.println("Starting Vertex : "+v);
			Iterator<Vertex> vertIt = v.iteratorVV();
			while(vertIt.hasNext()){
				System.out.println("Neighboring vertices: "+vertIt.next());
			}
		}

	}

}
