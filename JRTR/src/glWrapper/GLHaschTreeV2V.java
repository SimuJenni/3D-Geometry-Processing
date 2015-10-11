package glWrapper;

import java.util.ArrayList;
import java.util.Collection;

import javax.media.opengl.GL;
import javax.vecmath.Point3f;

import assignment2.HashOctree;
import assignment2.HashOctreeCell;
import assignment2.HashOctreeVertex;
import meshes.Vertex;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.gl.GLDisplayable.Semantic;
import openGL.objects.Transformation;

public class GLHaschTreeV2V extends GLDisplayable {

	
	public GLHaschTreeV2V(HashOctree tree){
		super(tree.numberofVertices()*6);

		Collection<HashOctreeVertex> verts = tree.getVertices();
		
		ArrayList<Point3f> origins = new ArrayList<Point3f>();
		ArrayList<Point3f> neighbors = new ArrayList<Point3f>();

		for(HashOctreeVertex vert : verts){	
			extractNeighbor(tree, origins, neighbors, vert, 0b100, false);
			extractNeighbor(tree, origins, neighbors, vert, 0b010, false);
			extractNeighbor(tree, origins, neighbors, vert, 0b001, false);
			extractNeighbor(tree, origins, neighbors, vert, 0b100, true);
			extractNeighbor(tree, origins, neighbors, vert, 0b010, true);
			extractNeighbor(tree, origins, neighbors, vert, 0b001, true);
		}
		
		int[] ind = new int[verts.size()*6];
		for(int i = 0; i < ind.length; i++)	{
			ind[i]=i;
		}
		
		this.addElement(toArrayP3f(origins), Semantic.POSITION , 3, "position");
		this.addElement(toArrayP3f(neighbors), Semantic.USERSPECIFIED , 3, "target");
		this.configurePreferredShader("shaders/octreeP2P.vert", "shaders/octreeP2P.frag", "shaders/octreeP2P.geom");
		this.addIndices(ind);

	}

	private void extractNeighbor(HashOctree tree, ArrayList<Point3f> origins, ArrayList<Point3f> neighbors,
			HashOctreeVertex vert, int Obxyz, boolean minus) {
		Point3f origin = vert.position;
		HashOctreeVertex nbrVert;
		if(minus)
			nbrVert = tree.getNbr_v2v(vert, Obxyz);
		else
			nbrVert = tree.getNbr_v2vMinus(vert, Obxyz);

		if(nbrVert == null){
			nbrVert = vert;
		}
		Point3f nbr = nbrVert.position;
		origins.add(origin);
		neighbors.add(nbr);
	}
	
	@Override
	public int glRenderFlag() {
		return GL.GL_POINTS;
	}

	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext, Transformation mvMat) {

	}

	
	private float[] toArrayP3f(ArrayList<Point3f> arrayList) {
		int i = 0;
		float[] points = new float[arrayList.size()*3];
		for(Point3f v: arrayList){
			points[i++] = v.x;
			points[i++] = v.y;
			points[i++] = v.z;
		}
		return points;
	}
}
