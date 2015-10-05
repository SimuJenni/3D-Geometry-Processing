package assignment1;

import glWrapper.GLHalfedgeStructure;
import glWrapper.GLWireframeMesh;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Vector3f;
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
public class Assignment_1_3_4 {
	

	public static void main(String[] args) throws IOException{
		//Load a wireframe mesh
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
		
		// Store original positions in HEData3
		HEData3d data = new HEData3d(hs);
		ArrayList<Vector3f> normals = hs.simpleNormals();
		ArrayList<Vertex> verts = hs.getVertices();
		for(int i=0; i<verts.size(); i++) {
			data.put(verts.get(i), normals.get(i));
		}
		
		MyDisplay disp = new MyDisplay();

		GLHalfedgeStructure glbunny = new GLHalfedgeStructure(hs, data);
		
		//choose the shader for the data
		glbunny.configurePreferredShader("shaders/normalVisualizer.vert", 
				"shaders/normalVisualizer.frag", 
				"shaders/normalVisualizer.geom");
		//add the data to the display
		disp.addToDisplay(glbunny);
		
		//do the same but choose a different shader
		GLWireframeMesh glbunny2 = new GLWireframeMesh(m);

		glbunny2.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(glbunny2);
	}

}
