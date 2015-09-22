package glWrapper;

import java.util.ArrayList;
import java.util.Iterator;

import javax.media.opengl.GL;
import javax.vecmath.Point3f;

import meshes.Face;
import meshes.HEData1d;
import meshes.HEData3d;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.gl.GLDisplayable.Semantic;
import openGL.gl.interactive.GLUpdateable;
import openGL.objects.Transformation;

public class GLHalfedgeStructure extends GLUpdateable {
	
	private HalfEdgeStructure halfEdgeStruct;
	private HEData3d attributes3d;
	ArrayList<Vertex> preVerts;

	public boolean displayData;

	public GLHalfedgeStructure(HalfEdgeStructure newHalfEdgeStruct) {
		super(newHalfEdgeStruct.getVertices().size());
		halfEdgeStruct = newHalfEdgeStruct;
		preVerts = halfEdgeStruct.getVertices();
		
		//Add Vertices
		float[] verts = new float[halfEdgeStruct.getVertices().size()*3];
		int[] ind = new int[halfEdgeStruct.getFaces().size()*3];
		
		//copy the data to the allocated arrays
		copyToArrayP3f(halfEdgeStruct.getVertices() , verts);
		copyToArray(halfEdgeStruct.getFaces() , ind);
		
		//The class GLVertexData provides the methods addElement(...), which will
		//cause the passed array to be sent to the graphic card
		//The array passed with the semantic POSITION will always be associated
		//to the position variable in the GL shaders, while arrays passed with the
		//USERSPECIFIED semantic will be associated to the name passed in the last argument
		//
		this.addElement(verts, Semantic.POSITION , 3, "position");
		//Here the position coordinates are passed a second time to the shader as color
		this.addElement(verts, Semantic.USERSPECIFIED , 3, "color");
		
		//pass the index array which has to be conformal to the glRenderflag returned, here GL_Triangles
		this.addIndices(ind);
		
	}
	
	public GLHalfedgeStructure(HalfEdgeStructure newHalfEdgeStruct, HEData1d data) {
		this(newHalfEdgeStruct);
		ArrayList<Vertex> verts = newHalfEdgeStruct.getVertices();
		float[] attr = new float[verts.size()];
		for(int i=0; i<verts.size(); i++){
			attr[i] = (float) data.get(verts.get(i));
		}
		this.addElement(attr, Semantic.USERSPECIFIED , 1, "attribute1d");
	}
	
	public GLHalfedgeStructure(HalfEdgeStructure newHalfEdgeStruct, HEData3d data) {
		this(newHalfEdgeStruct);
		attributes3d = data;
		add3dattr();
	}
	
	private void add3dattr(){
		ArrayList<Vertex> verts = halfEdgeStruct.getVertices();
		float[] attr = new float[verts.size()*3];
		for(int i=0; i<verts.size(); i++){
			attr[i*3] = attributes3d.get(verts.get(i)).x;
			attr[i*3+1] = attributes3d.get(verts.get(i)).y;
			attr[i*3+2] = attributes3d.get(verts.get(i)).z;
		}
		this.addElement(attr, Semantic.USERSPECIFIED , 3, "attribute3d");
	}


	/**
	 * Return the gl render flag to inform opengl that the indices/positions describe
	 * triangles
	 */
	@Override
	public int glRenderFlag() {
		return GL.GL_TRIANGLES;
	}


	/**
	 * No additional uniform variabes are passed to the shader.
	 */
	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext,
			Transformation mvMat) {
		
		//additional uniforms can be loaded using the function
		//glRenderContext.setUniform(name, val);
		
		//Such uniforms can be accessed in the shader by declaring them as
		// uniform <type> name;
		//where type is the appropriate type, e.g. float / vec3 / mat4 etc.
		//this method is called at every rendering pass.
		if(displayData)
			glRenderContext.setUniform("displayData", 1);
		else
			glRenderContext.setUniform("displayData", 0);

	}
	
	/**
	 * Helper method that copies the face information to the ind array
	 * @param arrayList
	 * @param ind
	 */
	private void copyToArray(ArrayList<Face> arrayList, int[] ind) {
		int i = 0;
		for(Face f : arrayList){
			Iterator<Vertex> it = f.iteratorFV();
			while(it.hasNext()){
				ind[i] = it.next().index;
				i++;
			}
		}
	}
	
	/**
	 * Helper method that copies the vertices arraylist to the verts array
	 * @param arrayList
	 * @param verts
	 */
	private void copyToArrayP3f(ArrayList<Vertex> arrayList, float[] verts) {
		int i = 0;
		for(Vertex v: arrayList){
			Point3f vertPos = v.getPos();
			verts[i++] = vertPos.x;
			verts[i++] = vertPos.y;
			verts[i++] = vertPos.z;
		}
	}
	
	public void simpleSmooth(){
		halfEdgeStruct.simpleSmooth();
		float[] verts = this.getDataBuffer("position");
		copyToArrayP3f( halfEdgeStruct.getVertices(), verts);
		this.scheduleUpdate("position");
	}

}
