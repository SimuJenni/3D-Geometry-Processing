package assignment1;

import glWrapper.GLHalfedgeStructure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.vecmath.Point3f;

import openGL.MyDisplay;
import meshes.HEData3d;
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
public class Assignment_1_3_3 {
	
	// Added a keyboard-listener for this assignment:
	// press "f" to schedule a smoothing iteration
	// press "d" to show the initial mesh

	public static void main(String[] args) throws IOException{
		//Load a wireframe mesh
		WireframeMesh m = ObjReader.read("./objs/cat.obj", true);

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
		
		// Store original positions in HEData3
		HEData3d data = new HEData3d(hs);
		ArrayList<Vertex> verts = hs.getVertices();
		for(Vertex v:verts){
			data.put(v, v.getPos());
		}
		
		MyDisplay disp = new MyDisplay();

		GLHalfedgeStructure glbunny = new GLHalfedgeStructure(hs, data);
		
		//choose the shader for the data
		glbunny.configurePreferredShader("shaders/data3d.vert", 
				"shaders/data3d.frag", 
				null);
		//add the data to the display
		disp.addToDisplay(glbunny);
		
	}

}
