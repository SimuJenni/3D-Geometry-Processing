package assignment1;

import glWrapper.GLHalfedgeStructure;
import java.io.IOException;
import java.util.ArrayList;
import openGL.MyDisplay;
import meshes.HEData1d;
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
public class Assignment_1_4_1 {
	

	public static void main(String[] args) throws IOException{
		//Load a wireframe mesh
//		WireframeMesh m = ObjReader.read("./objs/triangle.obj", true);
//		WireframeMesh m = ObjReader.read("./objs/bunny.obj", true);
//		WireframeMesh m = ObjReader.read("./objs/oneNeighborhood.obj", true);
//		WireframeMesh m = ObjReader.read("./objs/uglySphere.obj", true);
		WireframeMesh m = ObjReader.read("./objs/teapot.obj", true);


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
		ArrayList<Vertex> verts = hs.getVertices();

		for(Vertex v:verts){
			float meanCurv = v.getMeanCurvature();
			data.put(v, meanCurv);
		}
		
		MyDisplay disp = new MyDisplay();

		GLHalfedgeStructure glbunny = new GLHalfedgeStructure(hs, data);
		
		//choose the shader for the data
		glbunny.configurePreferredShader("shaders/data1dCurvature.vert", 
				"shaders/data1dCurvature.frag", 
				null);
		//add the data to the display
		disp.addToDisplay(glbunny);
	}

}
