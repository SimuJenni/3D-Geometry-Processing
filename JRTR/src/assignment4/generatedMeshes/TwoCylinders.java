package assignment4.generatedMeshes;

import glWrapper.GLPointCloud;
import glWrapper.GLWireframeMesh;

import java.io.IOException;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import meshes.HalfEdgeStructure;
import meshes.PointCloud;
import meshes.WireframeMesh;
import meshes.exception.DanglingTriangleException;
import meshes.exception.MeshNotOrientedException;
import openGL.MyDisplay;

public class TwoCylinders {
	
	public WireframeMesh wf;
	Vector3f dir1, dir2;
	float l;
	float r;
	
	float pointDensity;
	
	Point3f pos1, pos2;
	
	public TwoCylinders(float r, float l){
		wf = new WireframeMesh();
		
		dir1 = new Vector3f(1,0,0);
		pos1 = new Point3f();
		
		dir2 = new Vector3f(0,-1,0);
		pos2 = new Point3f(1,1,0);
		
		pointDensity = 30;
		
		this.r=r;
		this.l =l;
		
		setUp();
	}
	
	
	public void setUp(){
		int perCircle = (int) Math.ceil(r*2* Math.PI *pointDensity);	
		int nCircles = (int) Math.ceil(l *pointDensity);
		
		
		
		Matrix4f rot = new Matrix4f();
		
		Vector3f dirOrth = new Vector3f();
		Vector3f rotated = new Vector3f();
		dirOrth.cross(dir1, dir2);
		dirOrth.normalize();
		dirOrth.scale(r);
		
		
		Point3f[][] points = new Point3f[nCircles][perCircle];
		for(int i = 0; i < nCircles; i++){
			float length = l * i /nCircles;
			for(int j = 0; j < perCircle; j++){
				float angle = (float) (2.f* j * Math.PI / perCircle);
				
				rot.set(new AxisAngle4f(dir1, angle));				
				rot.transform(dirOrth, rotated);
				points[i][j] = new Point3f(pos1);
				
				points[i][j].x +=dir1.x*length + rotated.x;
				points[i][j].y +=dir1.y*length+ rotated.y;
				points[i][j].z +=dir1.z*length+ rotated.z;
				
				
				wf.vertices.add(points[i][j]);
			}
		}
		
		
		Point3f[][] points2 = new Point3f[nCircles][perCircle];
		for(int i = 0; i < nCircles; i++){
			float length = l * i /nCircles;
			for(int j = 0; j < perCircle; j++){
				float angle =  -(float) (2.f* j * Math.PI / perCircle);
				
				rot.set(new AxisAngle4f(dir2, angle));				
				rot.transform(dirOrth, rotated);
				points2[i][j] = new Point3f(pos2);
				
				points2[i][j].x +=dir2.x*length + rotated.x;
				points2[i][j].y +=dir2.y*length+ rotated.y;
				points2[i][j].z +=dir2.z*length+ rotated.z;
				
				
				wf.vertices.add(points2[i][j]);
			}
		}
		
		Point3f[][] connection = new Point3f[nCircles][perCircle];
		
		for(int i = 0; i < nCircles; i++){
			float alpha = (0.f + i+1)/(nCircles+1);
			for(int j = 0; j < perCircle; j++){
				
				connection[i][j] = new Point3f();
				
				connection[i][j].interpolate(points[nCircles-1][j], points2[nCircles-1][j], alpha);
				
				wf.vertices.add(connection[i][j]);
			}
		}
		
		//cylinder1
		for(int i = 0; i < nCircles-1; i++){
			for(int j = 0; j < perCircle; j++){
				int[] fc = {i*perCircle + j, i*perCircle + (j+1)%perCircle, (i+1)*perCircle + (j+1)%perCircle};
				int[] fc2 = {i*perCircle + j, (i+1)*perCircle + (j+1)%perCircle, (i+1)*perCircle + j};
				
				wf.faces.add(fc);
				wf.faces.add(fc2);
				
			}
		}
		
		//cylinder2
		int base = nCircles*perCircle;
		for(int i = 0; i < nCircles-1; i++){
			for(int j = 0; j < perCircle; j++){
				int[] fc = {
						base + i*perCircle + (j+1)%perCircle,
						base + i*perCircle + j, 
						base +(i+1)*perCircle + (j+1)%perCircle};
				int[] fc2 = {
						base +(i+1)*perCircle + (j+1)%perCircle,
						base +i*perCircle + j, 
						base +(i+1)*perCircle + j};
				
				wf.faces.add(fc);
				wf.faces.add(fc2);
				
			}
		}
		
		//middle
		base = 2*nCircles*perCircle;
		for(int i = 0; i < nCircles-1; i++){
			for(int j = 0; j < perCircle; j++){
				int[] fc = {
						base + i*perCircle + j,
						base + i*perCircle + (j+1)%perCircle,
						base +(i+1)*perCircle + (j+1)%perCircle};
				int[] fc2 = {
						base +i*perCircle + j,
						base +(i+1)*perCircle + (j+1)%perCircle,
						base +(i+1)*perCircle + j};
				
				wf.faces.add(fc);
				wf.faces.add(fc2);
				
			}
		}
		
		//connection
		int base_connect = 2*nCircles*perCircle;
		int base_last = (nCircles-1)*perCircle;
		for(int j = 0; j < perCircle; j++){
				int[] fc = {
						base_last +  j,
						base_last + (j+1)%perCircle,
						base_connect + (j+1)%perCircle};
				int[] fc2 = {
						base_last + j,
						base_connect + (j+1)%perCircle,
						base_connect + j};
				
				wf.faces.add(fc);
				wf.faces.add(fc2);
				
		}
		
		//connection
		base_connect = (3*nCircles-1)*perCircle;
		base_last = (2*nCircles-1)*perCircle;
		for(int j = 0; j < perCircle; j++){
				int[] fc = {
						base_last + (j+1)%perCircle,
						base_last +  j,
						base_connect + (j+1)%perCircle};
				int[] fc2 = {
						base_connect + (j+1)%perCircle,
						base_last + j,
						base_connect + j};
				
				wf.faces.add(fc);
				wf.faces.add(fc2);
				
		}
	}
	
	
	public static void main(String[] args) throws MeshNotOrientedException, DanglingTriangleException, IOException{
		TwoCylinders c = new TwoCylinders(0.5f, 0.4f);
		c.setUp();
		
		PointCloud pc = new PointCloud();
		for( Point3f p : c.wf.vertices){
			pc.points.add(p);
		}
		
		
		MyDisplay disp = new MyDisplay();
		GLPointCloud glpc = new GLPointCloud(pc);
		
		disp.addToDisplay(glpc);
		GLWireframeMesh glwf = new GLWireframeMesh(c.wf);
		glwf.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag",
				"shaders/trimesh_flat.geom");
		
		disp.addToDisplay(glwf);
		
		
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(c.wf);
		
		/*Assignment1.smoothen(hs, 5);
		GLHalfedgeStructure glhe = new GLHalfedgeStructure(hs);
		glhe.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag",
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(glhe);
		*/
		
		//Assignment4.implicitFairing(hs, -0.01f);
	}

}
