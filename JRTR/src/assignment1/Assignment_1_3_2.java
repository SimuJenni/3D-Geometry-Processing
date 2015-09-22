package assignment1;

import glWrapper.GLHalfedgeStructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import openGL.MyDisplay;
import meshes.Face;
import meshes.HEData1d;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.IterableHEData;
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
public class Assignment_1_3_2 {

	public static void main(String[] args) throws IOException{
		//Load a wireframe mesh
//		WireframeMesh m = ObjReader.read("./objs/oneNeighborhood.obj", true);
		WireframeMesh m = ObjReader.read("./objs/bunny.obj", true);

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
		
		HEData1d data = new HEData1d(hs);
		
		// Compute valence of each vertex
		ArrayList<Vertex> verts = hs.getVertices();
		ArrayList<Integer> valences = new ArrayList<Integer>();
		int maxValence = 0;
		int minValence = Integer.MAX_VALUE;

		for(Vertex v:verts){
			Iterator<HalfEdge> edgeIt = v.iteratorVE();
			int valence = 0;
			while(edgeIt.hasNext()){
				edgeIt.next();
				valence++;
			}
			valences.add(valence);
			maxValence = valence>maxValence ? valence : maxValence;
			minValence = valence<minValence ? valence : minValence;
		}
		
		for(int i=0; i<verts.size(); i++){
			data.put(verts.get(i), (float) (valences.get(i)-minValence)/(maxValence-minValence));
		}

		
		MyDisplay disp = new MyDisplay();

		GLHalfedgeStructure glbunny = new GLHalfedgeStructure(hs, data);
		
		//choose the shader for the data
		glbunny.configurePreferredShader("shaders/data1d.vert", 
				"shaders/data1d.frag", 
				null);
		//add the data to the display
		disp.addToDisplay(glbunny);
	}

}
