package sparse.solver;

import java.util.ArrayList;


import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector.Norm;
import no.uib.cipr.matrix.io.VectorInfo;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import sparse.CSRMatrix;
import sparse.SparseTools;
import sparse.Timer;
import sparse.CSRMatrix.col_val;
import sparse.solver.LSQR.return_;

/**<h1>Sparse Equations and Least Squares.</h1>
<p>The original Fortran code was written by C. C. Paige and M. A. Saunders as
described in
C. C. Paige and M. A. Saunders, <i>LSQR: An algorithm for sparse linear
equations and sparse least squares,</i> TOMS 8(1), 43--71 (1982).
C. C. Paige and M. A. Saunders, <i>Algorithm 583; LSQR: Sparse linear
equations and least-squares problems,</i> TOMS 8(2), 195--209 (1982).</p>
It is licensed under the following BSD license:
Copyright (c) 2006, Systems Optimization Laboratory
All rights reserved.
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
    * Neither the name of Stanford University nor the names of its
      contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
<p>
The Fortran code was translated to Python for use in CVXOPT by Jeffery
Kline with contributions by Mridul Aanjaneya and Bob Myhill.
Adapted for SciPy by Stefan van der Walt. Copied/Ported to Java by Peter Bertholet.
</p>*/

public class LSQR {

	static double eps = Math.ulp(0.0);//np.finfo(np.float64).eps
	
	private static class c_s_r{
		double c, s, r;
	}
	
	/** Stable implementation of Givens rotation.
    <h3>Notes</h3>
    The routine 'SymOrtho' was added for numerical stability. This is
    recommended by S.-C. Choi in [1].  It removes the unpleasant potential of
    "1/eps" in some important places
    <h3>References</h3>
      [1] S.-C. Choi, "Iterative Methods for Singular Linear Equations
           and Least-Squares Problems", Dissertation,
           http://www.stanford.edu/group/SOL/dissertations/sou-cheng-choi-thesis.pdf
    */
	private static void _sym_ortho( double a,  double b, c_s_r return_){

	    if (b == 0){
	    	return_.c = Math.signum(a);
	    	return_.s = 0; return_.r = Math.abs(a);
	    }
	     else if (a == 0){
	    	 return_.c = 0;
	    	 return_.s = Math.signum(b); return_.r = Math.abs(b);
	     }
	     else if (Math.abs(b) > Math.abs(a)){
	    	double tau = a / b;
	    	return_.s = Math.signum(b) / Math.sqrt(1 + tau * tau);
	    	return_.c = return_.s * tau;
	    	return_.r = b / return_.s;
	     }
	    else{
	    	double tau = b / a;
	    	return_.c = Math.signum(a) / Math.sqrt(1+tau*tau);
	    	return_.s =return_.c * tau;
	    	return_.r = a / return_.c;
	    }  
	      
	    return;
	}

	public static class return_{
		DenseVector x;
		int istop;
		int itn;
		double r1norm, r2norm, anorm, acond, arnorm, xnorm;
		DenseVector var;
		return_(DenseVector x_, int istop_, int itn_, double r1norm_,
				double r2norm_, double anorm_, double acond_, double arnorm_,
				double xnorm_, DenseVector var_){
			x = x_; istop = istop_; itn = itn_; r1norm = r1norm_;
			r2norm = r2norm_; anorm = anorm_; acond = acond_; arnorm = arnorm_;
			xnorm = xnorm_; var = var_;
		}
	}
	
	
	 /**Find the least-squares solution to a large, sparse, linear system
    of equations.
    <p>
    The function solves ``Ax = b``  or  ``min ||b - Ax||^2`` or
    ``min ||Ax - b||^2 + d^2 ||x||^2``.
    The matrix A may be square or rectangular (over-determined or
    under-determined), and may have any rank.
    </p>:
   	  <li>1. Unsymmetric equations --    solve  A*x = b</li>
      <li>2. Linear least squares  --    solve  A*x = b in the least-squares sense</li>
      <li>3. Damped least squares  --    solve  (   A    )*x = ( b )
                                            ( damp*I )     ( 0 )
                                     in the least-squares sense </li>
                                     
    <h3>Parameters </h3>
    (the parameters are not all exposed by this Java method, but easily could be)
    ----------
   <li> A : {sparse matrix, ndarray, LinearOperator}</li>
        Representation of an m-by-n matrix.  It is required that
        the linear operator can produce ``Ax`` and ``A^T x``.
    <li>b : array_like, shape (m,) </li>
        Right-hand side vector ``b``.
    <li>damp : float</li>
        Damping coefficient.
    <li>atol, btol : float, optional</li>
        Stopping tolerances. If both are 1.0e-9 (say), the final
        residual norm should be accurate to about 9 digits.  (The
        final x will usually have fewer correct digits, depending on
        cond(A) and the size of damp.)
    <li>conlim : float, optional</li>
        Another stopping tolerance.  lsqr terminates if an estimate of
        ``cond(A)`` exceeds `conlim`.  For compatible systems ``Ax =
        b``, `conlim` could be as large as 1.0e+12 (say).  For
        least-squares problems, conlim should be less than 1.0e+8.
        Maximum precision can be obtained by setting ``atol = btol =
        conlim = zero``, but the number of iterations may then be
        excessive.
    <li>iter_lim : int, optional</li>
        Explicit limitation on number of iterations (for safety).
    <li>show : bool, optional</li>
        Display an iteration log.
    <li>calc_var : bool, optional</li>
        Whether to estimate diagonals of ``(A'A + damp^2*I)^{-1}``.
    <h3>Returns (also not all exposed)</h3>
    <li>x : ndarray of float</li>
        The final solution.
    <li>istop : int</li>
        Gives the reason for termination.
        1 means x is an approximate solution to Ax = b.
        2 means x approximately solves the least-squares problem.
    <li>itn : int</li>
        Iteration number upon termination.
    <li>r1norm : float</li>
        ``norm(r)``, where ``r = b - Ax``.
    <li>r2norm : float</li>
        ``sqrt( norm(r)^2  +  damp^2 * norm(x)^2 )``.  Equal to `r1norm` if
        ``damp == 0``.
    <li>anorm : float</li>
        Estimate of Frobenius norm of ``Abar = [[A]; [damp*I]]``.
    <li>acond : float</li>
        Estimate of ``cond(Abar)``.
    <li>arnorm : float</li>
        Estimate of ``norm(A'*r - damp^2*x)``.
    <li>xnorm : float</li>
        ``norm(x)``
    <li>var : ndarray of float</li>
        If ``calc_var`` is True, estimates all diagonals of
        ``(A'A)^{-1}`` (if ``damp == 0``) or more generally ``(A'A +
        damp^2*I)^{-1}``.  This is well defined if A has full column
        rank or ``damp > 0``.  (Not sure what var means if ``rank(A)
        < n`` and ``damp = 0.``)
    <h3>Notes</h3>
    
    <p>LSQR uses an iterative method to approximate the solution.  The
    number of iterations required to reach a certain accuracy depends
    strongly on the scaling of the problem.  Poor scaling of the rows
    or columns of A should therefore be avoided where possible.
    For example, in problem 1 the solution is unaltered by
    row-scaling.  If a row of A is very small or large compared to
    the other rows of A, the corresponding row of ( A  b ) should be
    scaled up or down.</p>
    <p>In problems 1 and 2, the solution x is easily recovered
    following column-scaling.  Unless better information is known,
    the nonzero columns of A should be scaled so that they all have
    the same Euclidean norm (e.g., 1.0).</p>
    <p>In problem 3, there is no freedom to re-scale if damp is
    nonzero.  However, the value of damp should be assigned only
    after attention has been paid to the scaling of A.
    The parameter damp is intended to help regularize
    ill-conditioned systems, by preventing the true solution from
    being very large.  Another aid to regularization is provided by
    the parameter acond, which may be used to terminate iterations
    before the computed solution becomes very large.</p>
    <p>If some initial estimate ``x0`` is known and if ``damp == 0``,
    one could proceed as follows:</p>
      <li>1. Compute a residual vector ``r0 = b - A*x0``.</li>
      <li>2. Use LSQR to solve the system  ``A*dx = r0``.</li>
      <li>3. Add the correction dx to obtain a final solution ``x = x0 + dx``.</li>
    <p>This requires that ``x0`` be available before and after the call
    to LSQR.  To judge the benefits, suppose LSQR takes k1 iterations
    to solve A*x = b and k2 iterations to solve A*dx = r0.
    If x0 is "good", norm(r0) will be smaller than norm(b).
    If the same stopping tolerances atol and btol are used for each
    system, k1 and k2 will be similar, but the final solution x0 + dx
    should be more accurate.  The only way to reduce the total work
    is to use a larger stopping tolerance for the second system.
    If some value btol is suitable for A*x = b, the larger value
    btol*norm(b)/norm(r0)  should be suitable for A*dx = r0.</p>
    <p>Preconditioning is another way to reduce the number of iterations.
    If it is possible to solve a related system ``M*x = b``
    efficiently, where M approximates A in some helpful way (e.g. M -
    A has low rank or its elements are small relative to those of A),
    LSQR may converge more rapidly on the system ``A*M(inverse)*z =
    b``, after which x can be recovered by solving M*x = z.
    If A is symmetric, LSQR should not be used!</p>
    <p>Alternatives are the symmetric conjugate-gradient method (cg)
    and/or SYMMLQ.  SYMMLQ is an implementation of symmetric cg that
    applies to any symmetric A and will converge more rapidly than
    LSQR.  If A is positive definite, there are other implementations
    of symmetric cg that require slightly less work per iteration than
    SYMMLQ (but will take the same number of iterations).</p>
    <h3>References</h3>
    <p> [1] C. C. Paige and M. A. Saunders (1982a).
           "LSQR: An algorithm for sparse linear equations and
           sparse least squares", ACM TOMS 8(1), 43-71.</p>
    <p> [2] C. C. Paige and M. A. Saunders (1982b).
           "Algorithm 583.  LSQR: Sparse linear equations and least
           squares problems", ACM TOMS 8(2), 195-209.</p>
    <p> [3] M. A. Saunders (1995).  "Solution of sparse rectangular
           systems using LSQR and CRAIG", BIT 35, 588-604.</p>
    """
	 * @param maxIt */
	public static return_ lsqrMethod(CSRMatrix A_csr, DenseVector b, int maxIt) 
	{
		
		Timer prequel = new Timer(), mainLoop = new Timer(), matVecMultPre = new Timer(),
				matVecMultMain = new Timer(), vecOpMain = new Timer(), 
				allocPre = new Timer(), allocMain = new Timer();
		
		int  n = A_csr.nCols();/// A.numColumns();
	    allocPre.start();
	    double data_n[][] = new double[5][n];
	    double data_m[][] = new double[2][b.size()];
	    allocPre.stop();		
		prequel.start();
		
		
		//These should be parameters
		double damp=0.0, atol=1e-8, btol=1e-8, conlim=1e8;
	    int iter_lim=(maxIt < 0? 2*A_csr.nCols() : maxIt); //upper bound
	    
	    boolean calc_var=false;
	
	    	    
	    DenseVector v = new DenseVector(data_n[0],false);//n);
	    DenseVector x = new DenseVector(data_n[1],false);//new DenseVector(n);
	    DenseVector w = new DenseVector(data_n[2],false);//new DenseVector(n);
	    DenseVector var = new DenseVector(data_n[3],false);//new DenseVector(n);
	    DenseVector tmp_dimV = new DenseVector(data_n[4],false);//new DenseVector(n);
	    DenseVector u = new DenseVector(data_m[0],false);//b.copy();
	    u.set(b);
	    DenseVector tmp_dimU = new DenseVector(data_m[1],false);//new DenseVector(u.size());
	    
	    c_s_r res_ = new c_s_r();
	    
	    //iteration, stopping reason,
	    int itn = 0, istop = 0;
	    double ctol = 0;
	    if (conlim > 0){
	    	ctol = 1/conlim;
	    }
	    double anorm = 0, acond = 0, dampsq = Math.pow(damp,2);
	    double ddnorm = 0, res2 = 0, xnorm = 0;
	    double xxnorm = 0, z = 0, cs2 = -1,sn2 = 0;
	
	  
	    //Set up the first vectors u and v for the bidiagonalization.
	    //These satisfy  beta*u = b,  alfa*v = A'u.
	    double alfa = 0;
	    double beta = u.norm(Norm.Two);

	    if (beta > 0){
	        //u = (1/beta) * u
	    	u.scale(1/beta);
	        //v = A^T*u
	    	matVecMultPre.start();
	   // 	ATransp.mult(u, v);
	   // 	A.transMult(u, v);
	    	transMult(A_csr, u, v);
	    	matVecMultPre.stop();
	        alfa = v.norm(Norm.Two);
	    }
	    if (alfa > 0){
	        v.scale(1/alfa);
	        w = v.copy();
	    }
	    double rhobar = alfa;
	    double phibar = beta;
	    double bnorm = beta;
	    double rnorm = beta;
	    double r1norm = rnorm;
	    double r2norm = rnorm;
	
	    // Reverse the order here from the original matlab code because
	    // there was an error on return when arnorm==0
	    double arnorm = alfa * beta;
	    if (arnorm == 0){
	        //print(msg[0]);
	        return new return_( x, istop, itn, r1norm, r2norm, anorm, acond, arnorm, xnorm, var);
	    }
	
	    prequel.stop();
	    mainLoop.start();
	    //Main iteration loop.
	    while( itn < iter_lim){
	        itn = itn + 1;
	        
	        //     Perform the next step of the bidiagonalization to obtain the
	        //     next  beta, u, alfa, v.  These satisfy the relations
	        //                beta*u  =  a*v   -  alfa*u,
	        //                alfa*v  =  A'*u  -  beta*v.
	
	        //u = A.matvec(v) - alfa * u;
	        matVecMultMain.start();
	        //A.mult(v, tmp_dimU);
	        mult(A_csr,v,tmp_dimU);
	        matVecMultMain.stop();
	        
	        vecOpMain.start();
	        tmp_dimU.add(-alfa, u);
	        u.set(tmp_dimU);
	        
	        beta = u.norm(Norm.Two);
	        vecOpMain.stop();
	        
	        if (beta > 0){
	            //u = (1/beta) * u;
	        	vecOpMain.start();
	        	u.scale(1/beta);
	            anorm = Math.sqrt(anorm*anorm + alfa*alfa + beta*beta + damp*damp);
	            vecOpMain.stop();
	            //v = ATransp.rmatvec(u) - beta * v;
	            matVecMultMain.start();
	            //ATransp.mult(u,tmp_dimV);
	            //A.transMult(u, tmp_dimV);
	            transMult(A_csr,u, tmp_dimV);
	            matVecMultMain.stop();
	            vecOpMain.start();
	            tmp_dimV.add(-beta, v);
	            v.set(tmp_dimV);
	            
	            alfa = v.norm(Norm.Two);
	            if( alfa > 0){
	            	v.scale(1 / alfa);
	            }
	            vecOpMain.stop();
	        }
	        allocMain.start();
	        // Use a plane rotation to eliminate the damping parameter.
	        // This alters the diagonal (rhobar) of the lower-bidiagonal matrix.
	        double rhobar1 = Math.sqrt(rhobar*rhobar + damp*damp);
	        double cs1 = rhobar / rhobar1;
	        double sn1 = damp / rhobar1;
	        double psi = sn1 * phibar;
	        phibar = cs1 * phibar;
	
	        // Use a plane rotation to eliminate the subdiagonal element (beta)
	        // of the lower-bidiagonal matrix, giving an upper-bidiagonal matrix.
	        //cs, sn, rho = _sym_ortho(rhobar1, beta)
	        _sym_ortho(rhobar1, beta, res_);
	        double cs, sn, rho;
	        cs = res_.c; sn = res_.s; rho = res_.r;
	        
	        double theta = sn * alfa;
	        rhobar = -cs * alfa;
	        double phi = cs * phibar;
	        phibar = sn * phibar;
	        double tau = sn * phi;
	
	        // Update x and w.
	        double t1 = phi / rho;
	        double t2 = -theta / rho;
	      //dk = (1 / rho) * w;
	        
	        DenseVector dk = w.copy();
	        allocMain.stop();
	        
	        vecOpMain.start();
	        dk.scale(1/rho);
	
	        //x = x + t1 * w;
	        x.add(t1, w);
	
	        //w = v + t2 * w;
	        tmp_dimV.set(w);
	        w.set(v); w.add(t2, tmp_dimV);
	        ddnorm = ddnorm + Math.pow(dk.norm(Norm.Two), 2);
	
	        vecOpMain.stop();
	        /*if(calc_var){
	            var = var + dk**2;
	        }*/
	        
	        // Use a plane rotation on the right to eliminate the
	        // super-diagonal element (theta) of the upper-bidiagonal matrix.
	        // Then use the result to estimate norm(x).
	        double delta = sn2 * rho;
	        double gambar = -cs2 * rho;
	        double rhs = phi - delta * z;
	        double zbar = rhs / gambar;
	        xnorm = Math.sqrt(xxnorm + zbar*zbar);
	        double gamma = Math.sqrt(gambar*gambar + theta*theta);
	        cs2 = gambar / gamma;
	        sn2 = theta / gamma;
	        z = rhs / gamma;
	        xxnorm = xxnorm + z*z;
	
	       //# Test for convergence.
	        // First, estimate the condition of the matrix  Abar,
	        // and the norms of  rbar  and  Abar'rbar.
	        acond = anorm * Math.sqrt(ddnorm);
	        double res1 = phibar*phibar;
	        res2 = res2 + psi*psi;
	        rnorm = Math.sqrt(res1 + res2);
	        arnorm = alfa * Math.abs(tau);
	
	        // Distinguish between
	        //    r1norm = ||b - Ax|| and
	        //    r2norm = rnorm in current code
	        //           = sqrt(r1norm^2 + damp^2*||x||^2).
	        //    Estimate r1norm from
	        //    r1norm = sqrt(r2norm^2 - damp^2*||x||^2).
	        // Although there is cancellation, it might be accurate enough.
	        double r1sq = rnorm*rnorm - dampsq * xxnorm;
	        r1norm = Math.sqrt(Math.abs(r1sq));
	        if (r1sq < 0){
	        	r1norm = -r1norm;
	        }
	        r2norm = rnorm;
	
	        // Now use these norms to estimate certain other quantities,
	        // some of which will be small near a solution.
	        double test1 = rnorm / bnorm;
	        double test2 = arnorm / (anorm * rnorm + eps);
	        double test3 = 1 / (acond + eps);
	        t1 = test1 / (1 + anorm * xnorm / bnorm);
	        double rtol = btol + atol * anorm * xnorm / bnorm;
	
	        // The following tests guard against extremely small values of
	        // atol, btol  or  ctol.  (The user may have set any or all of
	        // the parameters  atol, btol, conlim  to 0.)
	        // The effect is equivalent to the normal tests using
	        // atol = eps,  btol = eps,  conlim = 1/eps.
	        if (itn >= iter_lim)
	            istop = 7;
	        if (1 + test3 <= 1)
	            istop = 6;
	        if (1 + test2 <= 1)
	            istop = 5;
	        if( 1 + t1 <= 1)
	            istop = 4;
	
	        // Allow for tolerances set by the user.
	        if (test3 <= ctol)
	            istop = 3;
	        if (test2 <= atol)
	            istop = 2;
	        if (test1 <= rtol)
	            istop = 1;
	
	        if (istop != 0){
	        	break;
	        }
	    }
	    mainLoop.stop();
	
	    System.out.println("Timings \n"
	    		+ "\tPre: " + prequel.total() + "\t\t\tmain " + mainLoop.total() + 
	    		"\n\t %vecmult " + matVecMultPre.total() * 1.0/prequel.total()
	    		+ "\t\t%vecmult " + matVecMultMain.total() * 1.0/mainLoop.total()
				+ "\n\t %alloc " + allocPre.total() * 1.0/prequel.total()
				+ "\t\t%alloc " + allocMain.total() * 1.0/mainLoop.total()
				+ "\n\t\t\t\t\t %other op " + vecOpMain.total() * 1.0/mainLoop.total()
	    		);
	    return new return_( x, istop, itn, r1norm, r2norm, anorm, acond, arnorm, xnorm, var);
}

	private static void mult(CSRMatrix a, DenseVector x, DenseVector result) {
		assert(x.size() == a.nCols);
		assert(result.size() == a.nRows());
		
		int nrows = a.nRows();
		double res;
		int rc, rowSz;
		double[] xd = x.getData();
		for(int r = 0; r < nrows; r++){
			ArrayList<col_val> row = a.getRow(r);
			res = 0;
			rowSz = row.size();
			for(rc = 0; rc <rowSz; rc++){
				res += row.get(rc).val * xd[row.get(rc).col];
			}
			result.set(r, res);
		}
	}
	
	private static void transMult(CSRMatrix a, DenseVector x, DenseVector result) {
		assert(x.size() == a.nRows());
		assert(result.size() == a.nCols);
		
		result.zero();
		int nrows = a.nRows();
		double[] rd = result.getData();
		double[] xd = x.getData();
		int rc, rowSz;
		double xd_r;
		for(int r = 0; r < nrows; r++){
			ArrayList<col_val> row = a.getRow(r);
			rowSz = row.size();
			xd_r = xd[r];
			for(rc = 0; rc <rowSz; rc++){
				rd[row.get(rc).col] += row.get(rc).val * xd_r;
			}
		}
	}
	
	public static void printInfo(return_ result) {
		 String[] msg = {"The exact solution is  x = 0",
        "Ax - b is small enough, given atol, btol",
        "The least-squares solution is good enough, given atol",
        "The estimate of cond(Abar) has exceeded conlim",
        "Ax - b is small enough for this machine",
        "The least-squares solution is good enough for this machine",
        "Cond(Abar) seems to be too large for this machine",
        "The iteration limit has been reached"};
		 
		 System.out.println("LSQR summary\n" +
		 "Termination reason: " + msg[result.istop] + "\n" +
		 "its " + result.itn + "\t r1norm " + result.r1norm + "\tr2norm " + result.r2norm + "\n" +
		  "cond " + result.acond );
		 /*   if show:
	        print(' ')
	        print('LSQR            Least-squares solution of  Ax = b')
	        str1 = 'The matrix A has %8g rows  and %8g cols' % (m, n)
	        str2 = 'damp = %20.14e   calc_var = %8g' % (damp, calc_var)
	        str3 = 'atol = %8.2e                 conlim = %8.2e' % (atol, conlim)
	        str4 = 'btol = %8.2e               iter_lim = %8g' % (btol, iter_lim)
	        print(str1)
	        print(str2)
	        print(str3)
	        print(str4)*/ 
	}

}