package sparse.solver;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import assignment4.HSCopyTools;
import sparse.CSRMatrix;
import sparse.LinearSystem;
import utils.Utils;

public abstract class Solver {
	
	/**
	 * x will be used as an initial guess, the result will be stored in x
	 * @param mat
	 * @param b
	 * @param x
	 */
	public abstract void solve(CSRMatrix mat, ArrayList<Float> b,ArrayList<Float> x);
	
	public void solveP3f(CSRMatrix mat, ArrayList<Point3f> b,ArrayList<Point3f> x){
		ArrayList<Float> b_x = Utils.point3f2Xcoord(b);
		ArrayList<Float> b_y = Utils.point3f2Ycoord(b);
		ArrayList<Float> b_z = Utils.point3f2Zcoord(b);
		
		ArrayList<Float> x_x = Utils.point3f2Xcoord(x);
		ArrayList<Float> x_y = Utils.point3f2Ycoord(x);
		ArrayList<Float> x_z = Utils.point3f2Zcoord(x);
		
		this.solve(mat, b_x, x_x);
		this.solve(mat, b_y, x_y);
		this.solve(mat, b_z, x_z);
		
		Utils.addXCoordFromArray(x, x_x);
		Utils.addYCoordFromArray(x, x_y);
		Utils.addZCoordFromArray(x, x_z);
	}

	
	public boolean canSolveOverDetermined(){
		return false;
	}
	
	public void solve(LinearSystem l, ArrayList<Float> x){
		if(l.mat.nCols == l.mat.nRows() || canSolveOverDetermined()){
			solve(l.mat, l.b, x);
		}
		else{
			throw new UnsupportedOperationException("can solve only square mats");
		}
	}
	
	

}
