package sparse.solver;

import java.util.ArrayList;

import no.uib.cipr.matrix.DenseVector;
import sparse.CSRMatrix;
import sparse.SparseTools;
import sparse.solver.LSQR.return_;


/**
 * This is solver which iteratively solves large overdetermined Equation
 * Systems in their least squares sense. The employed method goes under the name "LSQR", 
 * and is a Java port of the LSQR method from Python's SciPy library.
 *  
 * At its core does a bi-conjugate gradient descent
 * on the normal equation, but refrains from computing A^TA. 
 * In principle, all the method needs is a way to compute A^T*v and A*u, that
 * is multiplication by A and by its transposed. Thus, the matrix multiplications could
 * also be described implicitely, such that one could refrain from explicitely building the
 * matrix A.
 * 
 * As it is with many iterative solvers, they are fast and scale well, but do not always 
 * converge. To increase quality, multiresolution schemes and or preconditioners could be used.
 * 
 * @author bertholet
 *
 */
public class LSQRSolver extends Solver {

	int maxIt;
	
	public LSQRSolver() {
		maxIt = -1;
	}
	
	/**
	 * Setting the max iterations to -1 results in the implementation
	 * choosing a large number of iterations, such that typically the solve
	 * stops after reaching some other convergence criterium
	 * @param it
	 */
	public void setMaxIt(int it){
		maxIt = it;
	}
	@Override
	public boolean canSolveOverDetermined() { 
		return true;
	}
	@Override
	public void solve(CSRMatrix mt, ArrayList<Float> b, ArrayList<Float> x) {
	
				
		DenseVector b_ = SparseTools.denseVector(b);
		
		long start = System.currentTimeMillis();
		System.out.println("Solving System ... (" + mt.nRows() + " x " + mt.nCols() + ")" );
		return_ result = LSQR.lsqrMethod(mt, b_, maxIt);
		long stop = System.currentTimeMillis();
		
		System.out.println("Solved in " + (stop-start) + " ms");
		LSQR.printInfo(result);
		DenseVector x_ = result.x;
		
		//copy the result back
		x.clear();
		x.ensureCapacity(x_.size());
		for(int i = 0; i < x_.size(); i++){
			x.add( (float) x_.get(i));
		}
	}

}
