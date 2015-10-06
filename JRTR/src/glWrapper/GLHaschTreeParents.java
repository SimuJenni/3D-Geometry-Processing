package glWrapper;

import java.util.ArrayList;
import java.util.Collection;

import javax.media.opengl.GL;
import javax.vecmath.Point3f;

import assignment2.HashOctree;
import assignment2.HashOctreeCell;
import openGL.gl.GLDisplayable;
import openGL.gl.GLRenderer;
import openGL.gl.GLDisplayable.Semantic;
import openGL.objects.Transformation;

public class GLHaschTreeParents extends GLDisplayable {

	
	public GLHaschTreeParents(HashOctree tree){
		super(tree.numberOfCells());

		Collection<HashOctreeCell> cells = tree.getCells();
		float[] childPoints = new float[cells.size()*3];
		float[] parentPoints = new float[cells.size()*3];

		int index = 0;
		for(HashOctreeCell cell : cells){	
			Point3f child = cell.center;
			HashOctreeCell parentCell = tree.getParent(cell);
			if(parentCell == null){
				parentCell = cell;
			}
			Point3f parent = parentCell.center;

			// Add coordinates to the float arrays
			childPoints[index*3] = child.x;
			childPoints[index*3+1] = child.y;
			childPoints[index*3+2] = child.z;
			parentPoints[index*3] = parent.x;
			parentPoints[index*3+1] = parent.y;
			parentPoints[index*3+2] = parent.z;
			index++;
		}
		
		int[] ind = new int[cells.size()];
		for(int i = 0; i < ind.length; i++)	{
			ind[i]=i;
		}
		
		this.addElement(childPoints, Semantic.POSITION , 3, "position");
		this.addElement(parentPoints, Semantic.USERSPECIFIED , 3, "parent");
		this.configurePreferredShader("shaders/octreeParents.vert", "shaders/octreeParents.frag", "shaders/octreeParents.geom");
		this.addIndices(ind);

	}
	
	@Override
	public int glRenderFlag() {
		return GL.GL_POINTS;
	}

	@Override
	public void loadAdditionalUniforms(GLRenderer glRenderContext, Transformation mvMat) {
		// TODO Auto-generated method stub

	}

}
