package assignment6;

import java.util.HashSet;

import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import javax.vecmath.Matrix3f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3f;

import glWrapper.GLUpdatableHEStructure;
import meshes.HEData3d;
import meshes.HalfEdgeStructure;
import meshes.Vertex;
import openGL.MyDisplay;
import openGL.picking.PickingProcessor;
import openGL.picking.TransformedBBox;


/**
 * This class handles call backs from the MyPickingDisplay.java
 * 
 * For interactive deformations, call your methods from here.
 * @author bertholet
 *
 */
public class DeformationPickingProcessor implements PickingProcessor{

	//The Half-edge structure
	private HalfEdgeStructure hs;
	
	//The gl-wrapper for the half-edge structure
	private GLUpdatableHEStructure hs_visualization;
	
	//The two sets selected by the user.
	HashSet<Integer> set1, set2;
	
	//colors to highlight selected regions
	HEData3d colors;
	private Tuple3f color1 = new Vector3f(0.7f, 0.7f, 0.2f);
	private Tuple3f color2 = new Vector3f(0.8f, 0.2f, 0.2f);
	private Tuple3f stdColor = new Vector3f(0.8f, 0.8f, 0.8f);
	
	
	//Encapsulation of the RAPS modeling algorithms
	//the interesting work is delegated to the modeler.
	RAPS_modelling modeler;
	MyDisplay display = null;
	SwingWorker<Void, Void> bgTask= null;
	
	public DeformationPickingProcessor(HalfEdgeStructure hs, 
			GLUpdatableHEStructure vis){
		this.hs = hs;
		this.hs_visualization = vis;
		this.set1 = new HashSet<>();
		this.set2 = new HashSet<>();
		
		colors = new HEData3d(hs);
		for(Vertex v : hs.getVertices()){
			colors.put(v, new Vector3f(0.7f,0.6f,0.5f));
		}
		hs_visualization.add(colors, "color");
		
		modeler = new RAPS_modelling(hs);
		
		//do the deformation in a background thread...
		bgTask = new SwingWorker<Void, Void>(){
			@Override
			protected Void doInBackground() throws Exception {
				modeler.resetRotations();
				modeler.optimalPositions();
				while(this.getState() == StateValue.STARTED){
					try{
						deform_raps();
					}
					catch(Exception e){
						e.printStackTrace();
						return null;
					}
					
				}
				return null;
			}
			
		};
		
		
	}

	public void setDisplay(MyDisplay disp){
		this.display = disp;
	}
	
	
	/**
	 * Put your deformation callbacks here. This method runs in the background
	 * @throws InterruptedException 
	 */
	private synchronized void deform_raps() throws InterruptedException {
		
		//do the deformation magic / delegate it to the modeler...
		hs_visualization.updatePosition();
		this.modeler.deform(3);
		hs_visualization.updatePosition();

	}
	
	
	/**
	 * Callback, called after the user has finished manipulating the
	 * two sets and switched to deformation mode, but before any move(...)
	 * or rotate(...) call is triggered.
	 */
	@Override
	public void prepareMove() {
		
		//update the sets of constrained vertices,
		bgTask.cancel(true);
		modeler.keep(set1);
		modeler.target(set2);
		
		//update the deformation matrix
		//do the cholesky factorization, etc
		modeler.updateL();
		bgTask = new SwingWorker<Void, Void>(){
			@Override
			protected Void doInBackground() throws Exception {
				while(this.getState() == StateValue.STARTED){
					try{
					modeler.resetRotations();
					deform_raps();
					}
					catch(Exception e){
						e.printStackTrace();	
						return null;
					}
				}
				return null;
			}
			
		};
	}

	@Override
	public void move(Vector3f delta, PickTarget target) {
		HashSet<Integer> set = (target == PickTarget.SET1 ? set1: set2);
		
		for(Integer v : set){
			hs.getVertices().get(v).getPos().add(delta);
		}
		
		

		//deform_raps();
		if(bgTask.getState() == StateValue.STARTED){
			System.out.println("Was started already...");
		}
		if(bgTask.getState() == StateValue.PENDING || bgTask.getState() == StateValue.DONE){
			bgTask.execute();	
			
		}
		
//		hs_visualization.updatePosition();

		
	}



	@Override
	public void rotate(Matrix3f rot, PickTarget target) {
		HashSet<Integer> set = (target == PickTarget.SET1 ? set1: set2);
		
		for(Integer v : set){
			rot.transform(hs.getVertices().get(v).getPos());
		}
		
		if(bgTask.getState() == StateValue.STARTED){

		}
		if(bgTask.getState() == StateValue.PENDING || bgTask.getState() == StateValue.DONE){
			bgTask.execute();			
		}
		
//		hs_visualization.updatePosition();
	}

	
	/**
	 * Callback, called when the mode of the picking operation in the 
	 * Picking display changes.
	 */
	@Override
	public void pick(TransformedBBox t, PickOperation op, PickTarget target) {
		bgTask.cancel(true);
		HashSet<Integer> set = (target == PickTarget.SET1 ? set1: set2);
		//collect vertices
		for(Vertex v: hs.getVertices()){
			if(t.contains(v.getPos())){
				switch (op) {
				case ADD:
					set.add(v.index);
					break;
				case REMOVE:
					set.remove(v.index);
				}
			}
		}
		
		recomputeColors();
		hs_visualization.update("color");
	}

	
	/**
	 * update colors to highlight the selected regions
	 */
	private void recomputeColors() {
		for(Vertex v: hs.getVertices()){
			if(set1.contains(v.index)){
				colors.put(v, color1);
			}
			else if(set2.contains(v.index)){
				colors.put(v, color2);
			}
			else{
				colors.put(v, stdColor);
			}
		}
	}

	
	

	


}
