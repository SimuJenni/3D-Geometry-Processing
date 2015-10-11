package sparse.solver;

import java.util.ArrayList;

import sparse.CSRMatrix;
import sparse.SparseTools;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;


/**
 * Solver seems to be made for dense matrices, even thought it accepts sparse matrices
 * so it will get slowowowowow for large sparse matrices
 * @author bertholet
 *
 */
public class ColtSolver extends Solver {

	


	@Override
	public void solve(CSRMatrix mt, ArrayList<Float> b, ArrayList<Float> x) {
		// TODO Auto-generated method stub
		SparseDoubleMatrix2D mat = SparseTools.createColtMatrix(mt) ;
		System.out.println("final matrix created");
		
		
		System.out.println("Starting svd");
		
		DoubleMatrix2D b2 = new DenseDoubleMatrix2D(b.size(),1);
		for(int i = 0; i < b.size(); i++){
			b2.set(i, 0, b.get(i));
		}
		
		Algebra alg = Algebra.DEFAULT;
		DoubleMatrix2D res = alg.solve(mat, b2);
		
		
		for(int i = 0; i < res.size(); i++){
			x.set(i, (float)res.get(i,0));
			
		}
	}
}
