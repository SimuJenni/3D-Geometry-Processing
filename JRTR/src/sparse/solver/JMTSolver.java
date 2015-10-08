package sparse.solver;

import java.util.ArrayList;


import sparse.CSRMatrix;
import sparse.SparseTools;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.sparse.BiCG;
import no.uib.cipr.matrix.sparse.BiCGstab;
import no.uib.cipr.matrix.sparse.CG;
import no.uib.cipr.matrix.sparse.CGS;
import no.uib.cipr.matrix.sparse.Chebyshev;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import no.uib.cipr.matrix.sparse.DefaultIterationMonitor;
import no.uib.cipr.matrix.sparse.DiagonalPreconditioner;
import no.uib.cipr.matrix.sparse.GMRES;
import no.uib.cipr.matrix.sparse.ICC;
import no.uib.cipr.matrix.sparse.ILU;
import no.uib.cipr.matrix.sparse.IR;
import no.uib.cipr.matrix.sparse.IterativeSolver;
import no.uib.cipr.matrix.sparse.IterativeSolverNotConvergedException;
import no.uib.cipr.matrix.sparse.OutputIterationReporter;
import no.uib.cipr.matrix.sparse.Preconditioner;
import no.uib.cipr.matrix.sparse.QMR;

/**
 * Wrapper for the conjugent gradient and stabilized biconjugate gradient
 * Java matrix toolkit algorithms. in Ax =  b, b is taken as an initial guess.
 * @author Alf
 *
 */
public class JMTSolver extends Solver{
	
	final int cg = 0, bcstab = 1;  
	public enum solverType {BiCG, BiCGstab, CG, CGS, CHEBYSHEV, GMRES, IR, QMR };
	solverType type;
	
	public JMTSolver(){
		this.type = solverType.BiCGstab;//solverType.BiCGstab;
	}
	
	public JMTSolver(solverType type){
		this.type = type;
	}
	
	
	private IterativeSolver solver(solverType t, DenseVector b_){
		switch (t) {
		case BiCG:
			return new BiCG(b_);
		case BiCGstab:	
			return new BiCGstab(b_);
		case CG:
			return new CG(b_);
		case CGS:
			return new CGS(b_);
		//case CHEBYSHEV:
			//only if symm & pos def
		//	return new Chebyshev(b_,);
		case GMRES:
			return new GMRES(b_);
		case IR:
			return new IR(b_);
		case QMR:
			return new QMR(b_);
		default:
			return new BiCGstab(b_);
		}
	}
	/**
	 * solves A x = b and takes b as an initial guess.
	 * @param m
	 * @param x
	 */
	public void solve(CSRMatrix m, ArrayList<Float> b, ArrayList<Float> x){
		System.out.println("Starting the solver...");
		DenseVector b_ = SparseTools.denseVector(b);
		DenseVector x_ = b_.copy();
		
		CompRowMatrix mat = SparseTools.createCRMatrix(m);
		
		//construct the specified JMT solver
		IterativeSolver solver =  solver(type, b_);
		
		//this preconditioner works best/ leads most consistently to convergence.
		Preconditioner M = new DiagonalPreconditioner(mat.numRows());///*new ICC(mat.copy());	// new ILU(mat.copy());
		//ILUT SSOR AMG
		M.setMatrix(mat);
		solver.setPreconditioner(M);
		//solver.getIterationMonitor().setIterationReporter(new OutputIterationReporter());
		((DefaultIterationMonitor) solver.getIterationMonitor()).setRelativeTolerance(1e-3);
		((DefaultIterationMonitor) solver.getIterationMonitor()).setMaxIterations(2000);
		
		//try to solve the equation
		try {
			solver.solve(mat, b_, x_);
		} catch (IterativeSolverNotConvergedException e) {
			System.err.println("Iterative Solver did not converge");
		}
		
		//copy the result back
		for(int i = 0; i < x_.size(); i++){
			x.set(i, (float) x_.get(i));
		}
	}
	
	/**
	 * solves A x = b and takes b as an initial guess.
	 * @param m
	 * @param x
	 */
	public void solve(CSRMatrix m, ArrayList<Float> b, ArrayList<Float> x, ArrayList<Float> initial_guess){
		System.out.println("Starting the solver...");
		DenseVector b_ = SparseTools.denseVector(b);
		DenseVector x_ = SparseTools.denseVector(initial_guess);//b_.copy();
		
		CompRowMatrix mat = SparseTools.createCRMatrix(m);
		
		//construct the specified JMT solver
		IterativeSolver solver = solver(type, b_);//(type == bcstab? new BiCGstab(b_): new CG(b_) );
		
		//this preconditioner works best/ leads most consistently to convergence.
		Preconditioner M = new DiagonalPreconditioner(mat.numRows());///*new ICC(mat.copy());	// new ILU(mat.copy());
		M.setMatrix(mat);
		solver.setPreconditioner(M);
		//solver.getIterationMonitor().setIterationReporter(new OutputIterationReporter());
		((DefaultIterationMonitor) solver.getIterationMonitor()).setRelativeTolerance(1e-3);
		((DefaultIterationMonitor) solver.getIterationMonitor()).setMaxIterations(2000);
		
		//try to solve the equation
		try {
			solver.solve(mat, b_, x_);
		} catch (IterativeSolverNotConvergedException e) {
			System.err.println("Iterative Solver did not converge");
		}
		
		//copy the result back
		for(int i = 0; i < x_.size(); i++){
			x.set(i, (float) x_.get(i));
		}
	}

}
