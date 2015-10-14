package assignment3;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import assignment2.HashOctree;
import assignment2.HashOctreeVertex;
import glWrapper.GLHashtree;
import glWrapper.GLHashtree_Vertices;
import glWrapper.GLWireframeMesh;
import meshes.PointCloud;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import sparse.CSRMatrix;
import sparse.LinearSystem;
import sparse.solver.LSQRSolver;
import sparse.solver.Solver;

public class Assignment3_2_2 {
	
	public static void main(String[] args) throws IOException{
		
		PointCloud pc = ObjReader.readAsPointCloud("./objs/teapot.obj", true);
		HashOctree tree = new HashOctree(pc,8,1,1f);
		tree.refineTree(1);
		LinearSystem linearSys = SSDMatrices.ssdSystem(tree, pc, 1, 0.0001f, 10);
		LSQRSolver solver = new LSQRSolver();
		ArrayList<Float> x = new ArrayList<Float>();
		solver.solve(linearSys, x);	
		MarchingCubes marchingCubes = new MarchingCubes(tree);
		marchingCubes.dualMC(x);
		
		WireframeMesh mesh = marchingCubes.getResult();
		GLWireframeMesh gl_mesh = new GLWireframeMesh(mesh);
		MyDisplay d = new MyDisplay();
		gl_mesh.configurePreferredShader("shaders/trimesh_flat.vert", "shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom");
		d.addToDisplay(gl_mesh);
	}
	
}
