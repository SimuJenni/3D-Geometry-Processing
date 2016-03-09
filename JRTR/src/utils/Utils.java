package utils;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import assignment2.HashOctreeVertex;

public class Utils {
	
	public static void addXCoordFromArray(ArrayList<Point3f> target, ArrayList<Float> source){
		for(int i=0; i<target.size(); i++){
			target.get(i).x = source.get(i);
		}
	}
	
	public static void addYCoordFromArray(ArrayList<Point3f> target, ArrayList<Float> source){
		for(int i=0; i<target.size(); i++){
			target.get(i).y = source.get(i);
		}
	}
	
	public static void addZCoordFromArray(ArrayList<Point3f> target, ArrayList<Float> source){
		for(int i=0; i<target.size(); i++){
			target.get(i).z = source.get(i);
		}
	}

	public static ArrayList<Float> cell2Xcoord(ArrayList<HashOctreeVertex> verts) {
		ArrayList<Float> result = new ArrayList<Float>();
		for(HashOctreeVertex vert : verts){
			Point3f pos = vert.getPosition();
			result.add(pos.x);
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
	
	public static ArrayList<Float> point3f2Xcoord(ArrayList<Point3f> points) {
		ArrayList<Float> result = new ArrayList<Float>();
		for(Point3f point : points){
			result.add(point.x);
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
	
	public static ArrayList<Float> point3f2Zcoord(ArrayList<Point3f> points) {
		ArrayList<Float> result = new ArrayList<Float>();
		for(Point3f point : points){
			result.add(point.z);
		}
		return result;
	}

	public static ArrayList<Float> multArrays(ArrayList<Float> x, ArrayList<Float> y) {
		ArrayList<Float> result = new ArrayList<Float>();
		for(int i=0; i<x.size();i++){
			result.add(x.get(i)*y.get(i));
		}
		return result;
	}
	
	public static ArrayList<Float> addArrays(ArrayList<Float> x, ArrayList<Float> y) {
		ArrayList<Float> result = new ArrayList<Float>(x.size());
		for(int i=0; i<x.size();i++){
			result.add(x.get(i)+y.get(i));
		}
		return result;
	}
	
	
	
}
