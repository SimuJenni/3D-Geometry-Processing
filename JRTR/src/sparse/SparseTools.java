package sparse;

import java.util.ArrayList;

import cern.colt.matrix.impl.SparseDoubleMatrix2D;

import sparse.CSRMatrix.col_val;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.CompRowMatrix;

public class SparseTools {
	
	
	
	public static SparseDoubleMatrix2D createColtMatrix(CSRMatrix m){
		
		SparseDoubleMatrix2D mat = new SparseDoubleMatrix2D(
				m.nRows(), m.nCols, m.numberofEntries(), 0.2, 0.5);
		
		int r = 0;
		for(ArrayList<col_val> row : m.getRows()){
			for(col_val c: row){
				mat.setQuick(r, c.col, c.val);
			}
			r++;
		}
		
		return mat;
	}
	
	
	
	/**
	 * the java matrix toolkit library might be good, but the matrix
	 * construction sux.
	 * @param def
	 * @return
	 */
	public static CompRowMatrix createCRMatrix(CSRMatrix def){
						
		int[][] nz = new int[def.nRows()][];
		
		ArrayList<col_val> temp;
		for(int i = 0; i < nz.length; i++){
			temp = def.getRows().get(i);
			
			nz[i] = new int[temp.size()];
			int j = 0;
			for(col_val val : temp){
				nz[i][j++] = val.col;
			}
		}
				
		CompRowMatrix mat = new CompRowMatrix(def.nRows(), def.nCols, nz);
		
		
		boolean emptyDiag=false, hasDiagElement;
		for(int i = 0; i < nz.length; i++){
			hasDiagElement = true;
			for(col_val val: def.getRows().get(i)){
				mat.set(i, val.col, val.val);
				hasDiagElement = hasDiagElement || i==val.col;
			}
			if(!hasDiagElement){
				System.out.println("Kein diag. Element");
			}
			emptyDiag = emptyDiag || !hasDiagElement;
		}
				
		//System.out.println("mat cast: " +nzStruct.total() + "\t"+ alloc.total() + "\t" + fillIn.total());
		return mat;
		
	}
	
	
	public static ArrayList<Float> arrayList(DenseVector x) {
		ArrayList<Float> result = new ArrayList<>();
		for(int i = 0; i < x.size(); i++){
			result.add((float) x.get(i));
		}
		return result;
	}
	
	public static DenseVector denseVector(ArrayList<Float> b) {
		DenseVector res = new DenseVector(b.size());
		int idx = 0;
		for(float val: b){
			res.set(idx++, val);
		}
		
		return res;
	}

}
