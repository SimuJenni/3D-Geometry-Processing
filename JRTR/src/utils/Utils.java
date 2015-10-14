package utils;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import assignment2.HashOctreeVertex;

public class Utils {

	public static ArrayList<Float> cell2Xcoord(ArrayList<HashOctreeVertex> verts) {
		ArrayList<Float> result = new ArrayList<Float>();
		for(HashOctreeVertex vert : verts){
			Point3f pos = vert.getPosition();
			result.add(pos.x);
		}
		return result;
	}

	public static ArrayList<Float> point3f2Xcoord(ArrayList<Point3f> points) {
		ArrayList<Float> result = new ArrayList<Float>();
		for(Point3f point : points){
			result.add(point.x);
		}
		return result;
	}
	
	public static ArrayList<Float> cell2Ycoord(ArrayList<HashOctreeVertex> verts) {
		ArrayList<Float> result = new ArrayList<Float>();
		for(HashOctreeVertex vert : verts){
			Point3f pos = vert.getPosition();
			result.add(pos.y);
		}
		return result;
	}

	public static ArrayList<Float> point3f2Ycoord(ArrayList<Point3f> points) {
		ArrayList<Float> result = new ArrayList<Float>();
		for(Point3f point : points){
			result.add(point.y);
		}
		return result;
	}

}
