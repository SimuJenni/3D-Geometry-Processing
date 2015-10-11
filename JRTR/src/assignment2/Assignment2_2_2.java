package assignment2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.vecmath.Point3f;

import glWrapper.GLHaschTreeParents;
import glWrapper.GLHashtree;
import glWrapper.GLHashtree_Vertices;
import glWrapper.GLPointCloud;
import meshes.PointCloud;
import meshes.reader.ObjReader;
import meshes.reader.PlyReader;
import openGL.MyDisplay;


public class Assignment2_2_2 {
	
public static void main(String[] args) throws IOException{
				
		//these Demos will violate assertions as long as the MortonCodes class are
		//implemented.  
		//(enabling Assertions recommended! in Eclipse menu: run->run configuration->arguments-> vm argument: -ea)
//		hashTreeDemo(ObjReader.readAsPointCloud("./objs/dragon.obj", true));
		hashTreeDemo(PlyReader.readPointCloud("./objs/octreeTest.ply", true));
				
	}
	
	public static void hashTreeDemo(PointCloud pc){		
		HashOctree tree = new HashOctree(pc,4,1,1f);
		Collection<HashOctreeCell> cells = tree.getCells();
		ArrayList<Point3f> points = new ArrayList<Point3f>();
		for(HashOctreeCell cell : cells){
			points.add(cell.center);
		}
		PointCloud pointCloud = new PointCloud();
		pointCloud.points = points;
		
		MyDisplay display = new MyDisplay();
		GLPointCloud glPC = new GLPointCloud(pointCloud);
//		GLPointCloud glPC = new GLPointCloud(pc);

		GLHashtree glOT = new GLHashtree(tree);
		GLHaschTreeParents glHP = new GLHaschTreeParents(tree);
		
		glOT.configurePreferredShader("shaders/octree.vert", 
				"shaders/octree.frag", 
				"shaders/octree.geom");
		
		
		display.addToDisplay(glOT);
		display.addToDisplay(glHP); // parent Connections
		display.addToDisplay(glPC);
	}

}
