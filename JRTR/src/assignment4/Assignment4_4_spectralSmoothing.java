package assignment4;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import glWrapper.GLHalfedgeStructure;
import glWrapper.GLWireframeMesh;
import meshes.HEData1d;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import sparse.CSRMatrix;
import sparse.CSRMatrix.col_val;
import sparse.SCIPYEVD;
import utils.Utils;

/**
 * You can implement the spectral smoothing application here....
 * @author Alf
 *
 */
public class Assignment4_4_spectralSmoothing {
	
	private static ArrayList<Float> eigenValues;

	public static void main(String[] args) throws IOException{
		
//		showSpectralHarmonicsOfSphere();
		
		WireframeMesh m = ObjReader.read("./objs/bunny5k.obj", true);

		HalfEdgeStructure hs = new HalfEdgeStructure();
		try {
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
		
//		demoSmoothing(hs);
		demoFiltering(hs);
		
		MyDisplay disp = new MyDisplay();
		GLHalfedgeStructure mesh = new GLHalfedgeStructure(hs);
		//choose the shader for the data
		mesh.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		//add the data to the display
		disp.addToDisplay(mesh);
	}
	
	private static void demoFiltering(HalfEdgeStructure hs) throws IOException {
		int numEVs = hs.getVertices().size()-1;	
		CSRMatrix eigenMat = computeEigenMatrix(hs, numEVs);
		
		CSRMatrix transpEigenMat = eigenMat.transposed();
		ArrayList<Point3f> origPos = new ArrayList<Point3f>();
		HSCopyTools.copy(hs, origPos);
		ArrayList<Float> temp1 = new ArrayList<Float>();
		ArrayList<Float> temp2 = new ArrayList<Float>();
		ArrayList<Float> result = new ArrayList<Float>();
		ArrayList<Float> scales = scale(eigenValues);

		
		// Compute new x-ccord
		eigenMat.mult(Utils.point3f2Xcoord(origPos), temp1);
		temp2 = Utils.multArrays(temp1, scales);
		transpEigenMat.mult(temp2, result);
		Utils.addXCoordFromArray(origPos, result);
		
		// Compute new y-ccord
		eigenMat.mult(Utils.point3f2Ycoord(origPos), temp1);
		temp2 = Utils.multArrays(temp1, scales);
		transpEigenMat.mult(temp2, result);
		Utils.addYCoordFromArray(origPos, result);
		
		// Compute new z-ccord
		eigenMat.mult(Utils.point3f2Zcoord(origPos), temp1);
		temp2 = Utils.multArrays(temp1, scales);
		transpEigenMat.mult(temp2, result);
		Utils.addZCoordFromArray(origPos, result);
		
		HSCopyTools.copy(origPos, hs);
	}

	private static ArrayList<Float> scale(ArrayList<Float> eigenVals) {
		ArrayList<Float> scales = new ArrayList<Float>();
		float min = Float.MAX_VALUE, max = 0f;
		for(Float val:eigenVals){
			if(val.isNaN())
				continue;
			min = (float) Math.min(Math.abs(val), min);
			max = (float) Math.max(Math.abs(val), max);
		}
		for(Float val:eigenVals){
			scales.add(map((float) Math.sqrt(Math.abs(val)), min, max));
		}
		return scales;
	}

	private static float map(float val, float min, float max) {
		float d = max-min;
		if(val<min+d/2)
			return 0.6f;
		else
			return 1.7f;
	}


	private static void demoSmoothing(HalfEdgeStructure hs) throws IOException {
		int numEVs = 500;	
		CSRMatrix eigenMat = computeEigenMatrix(hs, numEVs);
		
		CSRMatrix transpEigenMat = eigenMat.transposed();
		ArrayList<Point3f> origPos = new ArrayList<Point3f>();
		HSCopyTools.copy(hs, origPos);
		ArrayList<Float> temp = new ArrayList<Float>();
		ArrayList<Float> result = new ArrayList<Float>();
		
		// Compute new x-ccord
		eigenMat.mult(Utils.point3f2Xcoord(origPos), temp);
		transpEigenMat.mult(temp, result);
		Utils.addXCoordFromArray(origPos, result);
		
		// Compute new y-ccord
		eigenMat.mult(Utils.point3f2Ycoord(origPos), temp);
		transpEigenMat.mult(temp, result);
		Utils.addYCoordFromArray(origPos, result);
		
		// Compute new z-ccord
		eigenMat.mult(Utils.point3f2Zcoord(origPos), temp);
		transpEigenMat.mult(temp, result);
		Utils.addZCoordFromArray(origPos, result);
		
		HSCopyTools.copy(origPos, hs);
	}

	private static CSRMatrix computeEigenMatrix(HalfEdgeStructure hs, int numEVs) throws IOException {
		CSRMatrix symCotan = LMatrices.symmetricCotanLaplacian(hs);
		String prefix = "eigenDecomp";
		eigenValues = new ArrayList<Float>();
		ArrayList<ArrayList<Float>> eigenVectors = new ArrayList<ArrayList<Float>>();
		SCIPYEVD.doSVD(symCotan, prefix, numEVs, eigenValues, eigenVectors);
		
		// Construct matrix of eigen-vectors
		CSRMatrix eigenMat = new CSRMatrix(numEVs,eigenVectors.get(0).size());
		for(int i = 0; i<numEVs; i++){
			ArrayList<col_val> row = eigenMat.getRow(i);
			ArrayList<Float> rowVals = eigenVectors.get(i);
			for(int j=0;j<rowVals.size(); j++){
				row.add(new col_val(j,rowVals.get(j)));
			}
		}
		return eigenMat;
	}

	private static void showSpectralHarmonicsOfSphere() throws IOException {
		WireframeMesh m = ObjReader.read("./objs/sphere.obj", true);

		HalfEdgeStructure hs = new HalfEdgeStructure();
		try {
			hs.init(m);
		} catch (MeshNotOrientedException | DanglingTriangleException e) {
			e.printStackTrace();
			return;
		}
			
		CSRMatrix symCotan = LMatrices.symmetricCotanLaplacian(hs);
		String prefix = "eigenDecomp";
		int numEVs = 20;
		ArrayList<Float> eigenValues = new ArrayList<Float>();
		ArrayList<ArrayList<Float>> eigenVectors = new ArrayList<ArrayList<Float>>();
		SCIPYEVD.doSVD(symCotan, prefix, numEVs, eigenValues, eigenVectors);
		
		for(int n=0; n<numEVs;n++){
			ArrayList<Float> evs = eigenVectors.get(n);
			float min = Float.MAX_VALUE;
			float max = Float.MIN_VALUE;
			for(Float ev:evs){
				min = Math.min(min, ev);
				max = Math.max(max, ev);
			}
			if(Math.abs(max-min)<1e-3)
				max = (float) (min+1e-3);
			HEData1d data1d = new HEData1d(hs);
			ArrayList<Vertex> verts = hs.getVertices();
			for(int i=0; i<verts.size(); i++) {
				float val = (evs.get(i)-min)/(max-min);
				data1d.put(verts.get(i), val);
			}
			
			MyDisplay disp = new MyDisplay();
			GLHalfedgeStructure glShape1d = new GLHalfedgeStructure(hs, data1d);
			//choose the shader for the data
			glShape1d.configurePreferredShader("shaders/data1d.vert", 
					"shaders/data1d.frag", 
					null);
			//add the data to the display
			disp.addToDisplay(glShape1d);
		}
	}
}
