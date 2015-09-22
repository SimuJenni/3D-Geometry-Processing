package assignment1;

import glWrapper.GLHalfedgeStructure;
import glWrapper.GLWireframeMesh;

import java.io.IOException;

import openGL.MyDisplay;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;

public class Assignment_1_2 {
	
	
	public static void main(String[] args) throws IOException{
		//load a mesh
		WireframeMesh bunny = ObjReader.read("./objs/bunny5k.obj", true);
		HalfEdgeStructure halfBunny = new HalfEdgeStructure();
		
		//create a half-edge structure out of the wireframe description.
		//As not every mesh can be represented as a half-edge structure
		//exceptions could occur.
		try {
			halfBunny.init(bunny);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
		
		//..and display it.
		MyDisplay disp = new MyDisplay();
		//create wrapper which lays out the bunny data in a opengl conform
		//manner 
		GLHalfedgeStructure glbunny = new GLHalfedgeStructure(halfBunny);
		
		//choose the shader for the data
		glbunny.configurePreferredShader("shaders/default.vert", 
				"shaders/default.frag", 
				null);
		//add the data to the display
		disp.addToDisplay(glbunny);
		
	}

}
