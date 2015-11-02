package utils;

import java.util.ArrayList;

import javax.vecmath.Point3f;

public class VecMath {

	public static float dist(Point3f p1, Point3f p2){
		p1.distance(p2);
		return p1.distance(p2);
	}
	
	/**
	 * Computes a+s*b
	 * @param vertOrig
	 * @param vertSmoothed
	 * @param s
	 */
	public static  ArrayList<Point3f> scaleAdd(ArrayList<Point3f> vertOrig, ArrayList<Point3f> vertSmoothed, float s){
		 ArrayList<Point3f> result = new  ArrayList<Point3f>();
		assert(vertOrig.size()==vertSmoothed.size());
		for(int i=0; i<vertOrig.size();i++){
			Point3f p = new Point3f(vertSmoothed.get(i));
			p.scale(s);
			p.add(vertOrig.get(i));
			result.add(p);
		}
		return result;
	}
	
}
