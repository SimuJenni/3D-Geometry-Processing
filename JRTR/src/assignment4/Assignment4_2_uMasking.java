package assignment4;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Point3f;

import glWrapper.GLHalfedgeStructure;
import glWrapper.GLWireframeMesh;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import sparse.CSRMatrix;
import utils.VecMath;

public class Assignment4_2_uMasking {
	
	public static void main(String[] arg) throws IOException{
		headDemo();
					
	}

	private static void headDemo() throws IOException {
		WireframeMesh m = ObjReader.read("./objs/head.obj", true);//*/
		
		MyDisplay disp = new MyDisplay();
		GLWireframeMesh glwf = new GLWireframeMesh(m);
		glwf.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(glwf);
		
		HalfEdgeStructure hs = new HalfEdgeStructure();
			try {
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
		
		//do your unsharp masking thing...
		float lambda = 0.01f;
		ArrayList<Point3f> vertOrig = new ArrayList<Point3f>();
		HSCopyTools.copy(hs, vertOrig);
		CSRMatrix L = LMatrices.mixedCotanLaplacian(hs);
		// Smooth that thing
		Assignment4_2_smoothing.implicitSmoothing(hs, L, lambda);
		ArrayList<Point3f> vertSmoothed = new ArrayList<Point3f>();
		HSCopyTools.copy(hs, vertSmoothed);
		
		float s = 4f;
		ArrayList<Point3f> tmp = VecMath.scaleAdd(vertOrig, vertSmoothed, -1f);
		ArrayList<Point3f> unSharped = VecMath.scaleAdd(vertSmoothed, tmp, s);
		
		HSCopyTools.copy(unSharped, hs);
		GLHalfedgeStructure mesh = new GLHalfedgeStructure(hs);
		mesh.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(mesh);

		
	}


}
