package assignment5;

import glWrapper.GLHalfedgeStructure;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import openGL.MyDisplay;

public class assignment5_qSlimDemo {
	public static void main(String[] args) throws Exception{
		WireframeMesh wf = ObjReader.read("objs/dragon.obj", true);
		HalfEdgeStructure hs1 = new HalfEdgeStructure();
		HalfEdgeStructure hs2 = new HalfEdgeStructure();
		HalfEdgeStructure hsOrig = new HalfEdgeStructure();

		hs1.init(wf);
		hs2.init(wf);
		hsOrig.init(wf);

		QSlim qs1 = new QSlim(hs1, false);
		QSlim qs2 = new QSlim(hs2, true);

		qs1.simplify(10000);
		qs2.simplify(10000);

		
		MyDisplay disp = new MyDisplay();
		
		GLHalfedgeStructure mesh1 = new GLHalfedgeStructure(hs1);
		GLHalfedgeStructure mesh2 = new GLHalfedgeStructure(hs2);
		GLHalfedgeStructure meshOrig = new GLHalfedgeStructure(hsOrig);
		
		// Green = optimal, Red = average, Blue = Original
		mesh1.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat_red.geom");
		mesh2.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat_green.geom");
		meshOrig.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(meshOrig);
		disp.addToDisplay(mesh1);
		disp.addToDisplay(mesh2);
	}
}
