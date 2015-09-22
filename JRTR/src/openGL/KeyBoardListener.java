package openGL;

import glWrapper.GLHalfedgeStructure;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import openGL.gl.GLDisplayable;
import openGL.interfaces.SceneManagerIterator;
import openGL.objects.SimpleSceneManager;

public class KeyBoardListener implements KeyListener {
	
	private SimpleSceneManager manager;
	private MyDisplay display;

	public KeyBoardListener(SimpleSceneManager sceneManager, MyDisplay myDisplay) {
		manager = sceneManager;
		display = myDisplay;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyChar()){
			case 'd':
				switchDisplayData();
				break;
			case 'f':
				simpleSmooth();
				break;
			default:
				break;
		}

	}

	private void simpleSmooth() {
		SceneManagerIterator itr = manager.iterator();
		while(itr.hasNext()){
			GLDisplayable data = itr.next().getShape().getVertexData();
			if(data instanceof GLHalfedgeStructure){
				((GLHalfedgeStructure) data).simpleSmooth();
			}
		}
		display.updateDisplay();
	}

	private void switchDisplayData() {
		SceneManagerIterator itr = manager.iterator();
		while(itr.hasNext()){
			GLDisplayable data = itr.next().getShape().getVertexData();
			if(data instanceof GLHalfedgeStructure){	
				((GLHalfedgeStructure) data).displayData = !((GLHalfedgeStructure) data).displayData;
			}
		}
		display.updateDisplay();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}
