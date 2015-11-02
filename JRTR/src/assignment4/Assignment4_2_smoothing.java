package assignment4;

import static org.junit.Assert.fail;

import java.util.ArrayList;

import javax.vecmath.Point3f;
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
			m = ObjReader.read("objs/dragon_10000.obj", false);
			hs = new HalfEdgeStructure();
			hs.init(m);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
		CSRMatrix laplacian = LMatrices.mixedCotanLaplacian(hs);
//		CSRMatrix laplacian = LMatrices.uniformLaplacian(hs);

		float lambda = 0.01f;
		implicitSmoothing(hs, laplacian, lambda);
		
		MyDisplay disp = new MyDisplay();		
		GLHalfedgeStructure mesh = new GLHalfedgeStructure(hs);
		mesh.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(mesh);
		
	}

	/**
	 * Performs implicit smoothing on the HalfEdgeStructure hs with the supplied laplacian.
	 * @param hs
	 * @param laplacian
	 * @param lambda
	 */
	public static void implicitSmoothing(HalfEdgeStructure hs, CSRMatrix laplacian, float lambda) {
		float volBefore = hs.volume();
		CSRMatrix A = implicitSmoothingMatrix(laplacian,lambda);
		solveWithMatrix(hs, A);
		
		float volAfter = hs.volume();
		hs.scale((float) Math.pow(volBefore/volAfter, 1f/3));
	}

	/**
	 * Solves Ab=x for x, where b are the vertex-positions of hs.
	 * @param hs
	 * @param A
	 */
	public static void solveWithMatrix(HalfEdgeStructure hs, CSRMatrix A) {
		ArrayList<Point3f> x = new ArrayList<Point3f>();
		for(Vertex v:hs.getVertices()){
			x.add(new Point3f());
		}
		ArrayList<Point3f> b = new ArrayList<Point3f>();
		HSCopyTools.copy(hs, b);

		JMTSolver solver = new JMTSolver();
		solver.solveP3f(A, b, x);
		HSCopyTools.copy(x, hs);
	}
	
	/**
	 * Computes the matrix (I-lambda*L) used for implicit smoothing
	 * @param L Laplacian
	 * @param lambda
	 * @return
	 */
	public static CSRMatrix implicitSmoothingMatrix(CSRMatrix L, float lambda){
		CSRMatrix S = new CSRMatrix(L.nRows(), L.nCols());
		L.scale(-lambda);
		CSRMatrix I = SSDMatrices.eye(L.nRows(),L.nCols());
		S.add(I,L);
		return S;
	}
	
	
	
}
