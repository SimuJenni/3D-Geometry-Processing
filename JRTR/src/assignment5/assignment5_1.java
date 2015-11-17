package assignment5;

import java.util.Iterator;

import glWrapper.GLHalfedgeStructure;
import meshes.HalfEdge;
import meshes.HalfEdgeStructure;
import meshes.WireframeMesh;
import meshes.reader.ObjReader;
import openGL.MyDisplay;

public class assignment5_1 {
	
	public static void main(String[] args) throws Exception{
		WireframeMesh wf = ObjReader.read("objs/dragon.obj", true);
		HalfEdgeStructure hs = new HalfEdgeStructure();
		hs.init(wf);

		smallEdgeCollapse(hs, 0.02f);
		
		MyDisplay disp = new MyDisplay();
		
		GLHalfedgeStructure mesh = new GLHalfedgeStructure(hs);
		
		mesh.configurePreferredShader("shaders/trimesh_flat.vert", 
				"shaders/trimesh_flat.frag", 
				"shaders/trimesh_flat.geom");
		disp.addToDisplay(mesh);
	}

	/*
	 * Collapses all edges smaller than epsilon in the HalfEdgeStructure hs
	 */
	private static void smallEdgeCollapse(HalfEdgeStructure hs, float epsilon) {
		HalfEdgeCollapse collapser = new HalfEdgeCollapse(hs);
		boolean converged = false;
		int iter = 1;
		int maxIter = 10;
		while(!converged){
			System.out.println("Iteration "+iter+"/"+maxIter);
			int count = 0;
			Iterator<HalfEdge> eItr = hs.iteratorE(); 
			while(eItr.hasNext()){
				HalfEdge edge = eItr.next();
				if(collapser.isEdgeDead(edge) || !HalfEdgeCollapse.isEdgeCollapsable(edge)
						|| collapser.isCollapseMeshInv(edge, edge.end().getPos())){
					continue;
				}
				else if(edge.getLength()<epsilon){
					collapser.collapseEdge(edge);
					count++;
				}
			}
			System.out.println("Collapsed "+count+" edges");
			collapser.finish();
			converged = count==0 || iter>=maxIter;
			iter++;
		}
	}

}
