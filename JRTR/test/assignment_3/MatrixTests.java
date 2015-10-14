package assignment_3;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Point3f;

import org.junit.Before;
import org.junit.Test;

import assignment2.HashOctree;
import assignment2.HashOctreeVertex;
import assignment3.SSDMatrices;
import meshes.PointCloud;
import meshes.reader.ObjReader;
import sparse.CSRMatrix;
import utils.Utils;

public class MatrixTests {
	
	private PointCloud pc;
	private HashOctree tree;
	private float epsilon = 0.001f;

	@Before
    public void setUp() {
		try {
			pc = ObjReader.readAsPointCloud("./objs/teapot.obj", true);
			tree = new HashOctree(pc,4,1,1f);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void d0TestXCoord() {
		CSRMatrix d0 = SSDMatrices.D0Term(tree, pc);
		ArrayList<HashOctreeVertex> verts = tree.getVertices();
		
		ArrayList<Float> result = new ArrayList<Float>();
		d0.mult(Utils.cell2Xcoord(verts), result );
		ArrayList<Float> pointCoordinates = Utils.point3f2Xcoord(pc.points);
		for(int i=0; i<result.size(); i++){
			assertEquals(pointCoordinates.get(i), result.get(i), epsilon);
		}
	}
	
	@Test
	public void d0TestYCoord() {
		CSRMatrix d0 = SSDMatrices.D0Term(tree, pc);
		ArrayList<HashOctreeVertex> verts = tree.getVertices();
		
		ArrayList<Float> result = new ArrayList<Float>();
		d0.mult(Utils.cell2Ycoord(verts), result );
		ArrayList<Float> pointCoordinates = Utils.point3f2Ycoord(pc.points);
		for(int i=0; i<result.size(); i++){
			assertEquals(pointCoordinates.get(i), result.get(i), epsilon);
		}
	}
	
	@Test
	public void d1Test() {
		CSRMatrix d1 = SSDMatrices.D1Term(tree, pc);
		ArrayList<Float> result = new ArrayList<Float>();
		float[] coeffs = new float[]{5,7,9};
		d1.mult(linearFunction(tree, coeffs), result );
		for(int i=0; i<result.size(); i++){
			assertEquals(coeffs[i%3], result.get(i), epsilon);
		}
	}
	
	@Test
	public void RTest() {
		CSRMatrix R = SSDMatrices.RegularizationTerm(tree);
		ArrayList<Float> result = new ArrayList<Float>();
		float[] coeffs = new float[]{5,7,9};
		R.mult(linearFunction(tree, coeffs), result );
		for(int i=0; i<result.size(); i++){
			assertEquals(0, result.get(i), epsilon);
		}
	}
	
	/**
	 * Samples the a linear function f = 2x+3y+4z
	 * @param tree
	 * @return
	 */
	private static ArrayList<Float> linearFunction(HashOctree tree, float[] coeffs){
		
		//initialize the array
		ArrayList<Float> primaryValues = new ArrayList<>(tree.numberofVertices());
		for(int i = 0; i <tree.numberofVertices(); i++){
			primaryValues.add(new Float(0));
		}
		
		//compute the implicit function
		for(HashOctreeVertex v : tree.getVertices()){
			Point3f pos = v.position;
			primaryValues.set(v.index, pos.x*coeffs[0]+pos.y*coeffs[1]+pos.z*coeffs[2]); 
		}
		return primaryValues;
	}

}
