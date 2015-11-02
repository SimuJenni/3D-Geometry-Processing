package assignment4;

import static org.junit.Assert.fail;

import java.util.ArrayList;

import javax.vecmath.Vector3f;

import assignment3.SSDMatrices;
import glWrapper.GLHalfedgeStructure;
import glWrapper.GLWireframeMesh;
import meshes.Face;
import meshes.HEData1d;
import meshes.HEData3d;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import sparse.CSRMatrix;
import sparse.solver.JMTSolver;
import sparse.solver.LSQRSolver;

/**
 * Smoothing
 * @author Alf
 *
 */
public class Assignment4_2_smoothing {

	//implement the implicit smoothing scheme
	public static void main(String[] args) {
		HalfEdgeStructure hs = null;
		WireframeMesh m = null;

		try {
			m = ObjReader.read("objs/head.obj", false);
			hs = new HalfEdgeStructure();
			hs.init(m);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
		float volBefore = hs.volume();
		
		CSRMatrix laplacian = LMatrices.mixedCotanLaplacian(hs);
//		CSRMatrix laplacian = LMatrices.uniformLaplacian(hs);

		float lambda = 0.1f;
		CSRMatrix A = implicitSmoothingMatrix(laplacian,lambda);
		ArrayList<Float> x = new ArrayList<Float>();
		for(Vertex v:hs.getVertices()){
			x.add(0f);
		}
		ArrayList<Float> b_x = new ArrayList<Float>();
		ArrayList<Float> b_y = new ArrayList<Float>();
		ArrayList<Float> b_z = new ArrayList<Float>();
		HSCopyTools.copy(hs, b_x, 0);
		HSCopyTools.copy(hs, b_y, 1);
		HSCopyTools.copy(hs, b_z, 2);

		JMTSolver solver = new JMTSolver();
		solver.solve(A, b_x, x);
		HSCopyTools.copy(x, hs, 0);
		solver.solve(A, b_y, x);
		HSCopyTools.copy(x, hs, 1);
		solver.solve(A, b_z, x);
		HSCopyTools.copy(x, hs, 2);
		
		float volAfter = hs.volume();
		hs.scale((float) Math.pow(volBefore/volAfter, 1f/3));
		float newVol = hs.volume();
		
		MyDisplay disp = new MyDisplay();		
		GLHalfedgeStructure mesh = new GLHalfedgeStructure(hs);
		mesh.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(mesh);
		
	}
	
	public static CSRMatrix implicitSmoothingMatrix(CSRMatrix L, float lambda){
		CSRMatrix S = new CSRMatrix(L.nRows(), L.nCols());
		L.scale(-lambda);
		CSRMatrix I = SSDMatrices.eye(L.nRows(),L.nCols());
		S.add(I,L);
		return S;
	}
	
	
	
}
