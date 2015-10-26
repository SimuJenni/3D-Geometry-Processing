package assignment3;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import assignment2.HashOctree;
import assignment2.HashOctreeVertex;
import glWrapper.GLHalfedgeStructure;
import glWrapper.GLHashtree;
import glWrapper.GLHashtree_Vertices;
import glWrapper.GLWireframeMesh;
import meshes.HalfEdgeStructure;
import meshes.PointCloud;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import openGL.MyDisplay;

public class Assignment3_1_5 {
	
	public static void main(String[] args) throws IOException{
		
		marchingCubesDemo();		
				
	}
	
	
	public static void marchingCubesDemo(){
			
		//Test Data: create an octree
		HashOctree tree = new HashOctree( 
				nonUniformPointCloud(15),
				6,7,1.2f);
		//and sample per vertex function values.
		ArrayList<Float> x = sphericalFunction(tree);
		
		//Do your magic here...
		MarchingCubes marchingCubes = new MarchingCubes(tree);
		marchingCubes.dualMC(x);
		
		//And show off...
		WireframeMesh mesh = marchingCubes.getResult();
		HalfEdgeStructure hs = new HalfEdgeStructure();
		try {
			hs.init(mesh);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
		}
		hs.simpleSmooth();
		GLHalfedgeStructure gl_hs = new GLHalfedgeStructure(hs);
		gl_hs.configurePreferredShader("shaders/trimesh_flat.vert", "shaders/trimesh_flat.frag", "shaders/trimesh_flat.geom");

		
//		GLWireframeMesh gl_hs = new GLWireframeMesh(mesh);
		
		MyDisplay d = new MyDisplay();
		d.addToDisplay(gl_hs);
	}
	
	
	
	
	/**
	 * Samples the implicit function of a sphere at the tree's vertex positions.
	 * @param tree
	 * @return
	 */
	private static ArrayList<Float> sphericalFunction(HashOctree tree){
		
		//initialize the array
		ArrayList<Float> primaryValues = new ArrayList<>(tree.numberofVertices());
		for(int i = 0; i <tree.numberofVertices(); i++){
			primaryValues.add(new Float(0));
		}
		
		//compute the implicit function
		Point3f c = new Point3f(0.f,0.f,0.f);
		for(HashOctreeVertex v : tree.getVertices()){
			primaryValues.set(v.index, (float)
					v.position.distance(c) - 1f); 
		}
		return primaryValues;
	}
	
	/**
	 * generating a poitcloud
	 * @param max
	 * @return
	 */
	private static PointCloud nonUniformPointCloud(int max){
		PointCloud pc = new PointCloud();
		float delta = 1.f/max;
		for(int i = -max; i < max; i++){
			for(int j = -max; j < max; j++){
				for(int k = -max; k < max; k++){
					if(k>0){
						k+=3;
						if(j%3 !=0 || i%3 !=0){
							continue;
						}
					}
					pc.points.add(new Point3f(
							delta*i,
							delta*j,
							delta*k));
					pc.normals.add(new Vector3f(1,0,0));
				}
			}
	
		}
		
		return pc;
	}
	
}
