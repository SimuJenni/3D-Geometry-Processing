package assignment5;

import java.util.HashMap;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import glWrapper.GLWireframeMesh;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import openGL.MyDisplay;
import openGL.objects.Transformation;

public class assignment5_2b {
	
	public static void main(String[] args) throws Exception{
		WireframeMesh wf = ObjReader.read("objs/buddha.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(wf);
		
		QSlim qs = new QSlim(hs, false);
		HashMap<Vertex, Transformation> errMats = qs.getErrorMatrices();
		for(Vertex v:hs.getVertices()){
			Transformation Q = errMats.get(v);
			assert(checkNaNInf(Q.m00));
			assert(checkNaNInf(Q.m01));
			assert(checkNaNInf(Q.m02));
			assert(checkNaNInf(Q.m03));
			assert(checkNaNInf(Q.m10));
			assert(checkNaNInf(Q.m11));
			assert(checkNaNInf(Q.m12));
			assert(checkNaNInf(Q.m13));			
			assert(checkNaNInf(Q.m20));
			assert(checkNaNInf(Q.m21));
			assert(checkNaNInf(Q.m22));
			assert(checkNaNInf(Q.m23));			
			assert(checkNaNInf(Q.m30));
			assert(checkNaNInf(Q.m31));
			assert(checkNaNInf(Q.m32));
			assert(checkNaNInf(Q.m33));			
		}
		System.out.println("Matrices are all okay :)");
	}
	
	private static boolean checkNaNInf(float f){
		return !Float.isNaN(f) && !Float.isInfinite(f);
	}

}
