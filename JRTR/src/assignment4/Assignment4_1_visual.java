package assignment4;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Vector3f;

import glWrapper.GLHalfedgeStructure;
import glWrapper.GLWireframeMesh;
import meshes.HEData1d;
import meshes.HEData3d;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import sparse.CSRMatrix;

public class Assignment4_1_visual {
	


	public static void main(String[] args) {
		HalfEdgeStructure hs = null;
		WireframeMesh m = null;

		try {
			m = ObjReader.read("objs/sphere.obj", false);
//			m = ObjReader.read("objs/dragon.obj", false);
			hs = new HalfEdgeStructure();
			hs.init(m);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
//		CSRMatrix laplacian = LMatrices.mixedCotanLaplacian(hs);
		CSRMatrix laplacian = LMatrices.uniformLaplacian(hs);

		ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
		LMatrices.mult(laplacian, hs, normals);
		
		HEData3d data3d = new HEData3d(hs);
		HEData1d data1d = new HEData1d(hs);

		ArrayList<Vertex> verts = hs.getVertices();
		for(int i=0; i<verts.size(); i++) {
			Vector3f normal = normals.get(i);
			data1d.put(verts.get(i), normal.length()*0.5f);
			normal.normalize();
			normal.scale(-0.5f);
			data3d.put(verts.get(i), normal);
		}
		
		MyDisplay disp = new MyDisplay();
		
		GLHalfedgeStructure glShape1d = new GLHalfedgeStructure(hs, data1d);
		
		//choose the shader for the data
		glShape1d.configurePreferredShader("shaders/data1dCurvature.vert", 
				"shaders/data1dCurvature.frag", 
				null);
		//add the data to the display
		disp.addToDisplay(glShape1d);

		GLHalfedgeStructure glShape = new GLHalfedgeStructure(hs, data3d);
		
		//choose the shader for the data
		glShape.configurePreferredShader("shaders/normalVisualizer.vert", 
				"shaders/normalVisualizer.frag", 
				"shaders/normalVisualizer.geom");
		//add the data to the display
		disp.addToDisplay(glShape);
		
		//do the same but choose a different shader
		GLWireframeMesh mesh = new GLWireframeMesh(m);

		mesh.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(mesh);
		
	}

}
