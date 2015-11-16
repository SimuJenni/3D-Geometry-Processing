package assignment4;


import static org.junit.Assert.*;

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.junit.Before;
import org.junit.Test;

import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;

public class Assignment4_1_Tests {
	
	// A sphere of radius 2.
	private HalfEdgeStructure hs; 
	// An ugly sphere of radius 1, don't expect the Laplacians 
	//to perform accurately on this mesh.
	private HalfEdgeStructure hs2; 
	@Before
	public void setUp(){
		try {
			WireframeMesh m = ObjReader.read("objs/sphere.obj", false);
			hs = new HalfEdgeStructure();
			hs.init(m);
			
			m = ObjReader.read("objs/uglySphere.obj", false);
			hs2 = new HalfEdgeStructure();
			hs2.init(m);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		
	}
	
	//Test everything and anything...
	@Test
	public void unifLaplaceCheckMatrix() {
		CSRMatrix unifL = LMatrices.uniformLaplacian(hs);
		assertTrue(unifL.checkMatrix(false));
	}
	
	@Test
	public void unifLaplaceRowSum() {
		CSRMatrix unifL = LMatrices.uniformLaplacian(hs);
		ArrayList<ArrayList<col_val>> rows = unifL.getRows();
		for(ArrayList<col_val> row:rows){
			float rowSum = 0;
			for(col_val col:row){
				rowSum += col.val;
			}
			assertEquals(0, rowSum, 1e-3);
		}		
	}
	
	@Test
	public void cotanLaplaceCheckMatrix() {
		CSRMatrix cotanL = LMatrices.mixedCotanLaplacian(hs);
		assertTrue(cotanL.checkMatrix(false));
	}
	
	@Test
	public void cotanLaplaceRowSum() {
		CSRMatrix cotanL = LMatrices.mixedCotanLaplacian(hs);
		ArrayList<ArrayList<col_val>> rows = cotanL.getRows();
		for(ArrayList<col_val> row:rows){
			float rowSum = 0;
			for(col_val col:row){
				rowSum += col.val;
			}
			assertEquals(0, rowSum, 1e-3);
		}		
	}
	
	@Test
	public void cotanLaplaceMeanCurvProperty() {
		CSRMatrix cotanL = LMatrices.mixedCotanLaplacian(hs);
		ArrayList<Vertex> verts = hs.getVertices();
		float radius = verts.get(0).getPos().distance(new Point3f()); // radius is 2
		ArrayList<Vector3f> res = new ArrayList<Vector3f>();
		LMatrices.mult(cotanL, hs, res);
		for(int i=0; i<verts.size(); i++){
			Vector3f v = res.get(i);
			float h = 1f/2*v.length();
			assertEquals(1f/radius, h, 1e-3);
			Vector3f vertPos = new Vector3f(verts.get(i).getPos());
			// Check for that it is normal to surface
			assertEquals(Math.PI,vertPos.angle(v), 1e-2);
		}
	}
	
	@Test
	public void symCotanRowSum() {
		CSRMatrix symCotan = LMatrices.symmetricCotanLaplacian(hs);
		ArrayList<ArrayList<col_val>> rows = symCotan.getRows();
		for(ArrayList<col_val> row:rows){
			float rowSum = 0;
			for(col_val col:row){
				rowSum += col.val;
			}
			assertEquals(0, rowSum, 1e-3);
		}	
	}
	
	@Test
	public void symCotanSymetry() {
		CSRMatrix symCotan = LMatrices.symmetricCotanLaplacian(hs);
		for(int r=0;r<symCotan.nRows();r++){
			for(int c=r+1;c<symCotan.nCols();c++){
				assertEquals(symCotan.get(r, c),symCotan.get(c, r),1e-3);
			}
		}	
	}
	
}
