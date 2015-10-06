package glWrapper;

import java.util.ArrayList;
import java.util.Collection;

import javax.media.opengl.GL;
import javax.vecmath.Point3f;

import assignment2.HashOctree;
import assignment2.HashOctreeCell;
import meshes.Vertex;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.gl.GLDisplayable.Semantic;
import openGL.objects.Transformation;

public class GLHaschTreeC2C extends GLDisplayable {

	
	public GLHaschTreeC2C(HashOctree tree){
		super(tree.numberOfCells()*6);

		Collection<HashOctreeCell> cells = tree.getCells();
		
		ArrayList<Point3f> origins = new ArrayList<Point3f>();
		ArrayList<Point3f> neighbors = new ArrayList<Point3f>();

		for(HashOctreeCell cell : cells){	
			extractNeighbor(tree, origins, neighbors, cell, 0b100, false);
			extractNeighbor(tree, origins, neighbors, cell, 0b010, false);
			extractNeighbor(tree, origins, neighbors, cell, 0b001, false);
			extractNeighbor(tree, origins, neighbors, cell, 0b100, true);
			extractNeighbor(tree, origins, neighbors, cell, 0b010, true);
			extractNeighbor(tree, origins, neighbors, cell, 0b001, true);
		}
		
		int[] ind = new int[cells.size()*6];
		for(int i = 0; i < ind.length; i++)	{
			ind[i]=i;
		}
		
		this.addElement(toArrayP3f(origins), Semantic.POSITION , 3, "position");
		this.addElement(toArrayP3f(neighbors), Semantic.USERSPECIFIED , 3, "target");
		this.configurePreferredShader("shaders/octreeC2C.vert", "shaders/octreeC2C.frag", "shaders/octreeC2C.geom");
		this.addIndices(ind);

	}

	private void extractNeighbor(HashOctree tree, ArrayList<Point3f> origins, ArrayList<Point3f> neighbors,
			HashOctreeCell cell, int Obxyz, boolean minus) {
		Point3f origin = cell.center;
		HashOctreeCell nbrCell;
		if(minus)
			nbrCell = tree.getNbr_c2c(cell, Obxyz);
		else
			nbrCell = tree.getNbr_c2cMinus(cell, Obxyz);

		if(nbrCell == null){
			nbrCell = cell;
		}
		Point3f nbr = nbrCell.center;
		origins.add(origin);
		neighbors.add(nbr);
	}
	
	@Override
	public int glRenderFlag() {
		return GL.GL_POINTS;
	}

	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext, Transformation mvMat) {
		// TODO Auto-generated method stub

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
