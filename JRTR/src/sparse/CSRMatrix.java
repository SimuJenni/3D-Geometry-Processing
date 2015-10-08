package sparse;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import sparse.CSRMatrix.col_val;


/**
 * A sparse matrix container in CSR format, to allow Solver Library independent
 * Matrix definition and computations
 *
 * @author bertholet
 *
 */
public class CSRMatrix {
	
	// row[i] will be the compresse representation of the ith row of the matrix.
	// Each row is represented by a list of column/value pairs.
	private ArrayList<ArrayList<col_val>> rows;
	
	//public int nRows; //rows
	public int nCols; //columns
	
	
	/**
	 * Construct an empty rows x cols matrix
	 * @param rows
	 * @param cols
	 */
	public CSRMatrix( int rows, int cols){
		this.nCols = cols;
		this.rows = new ArrayList<>(rows);
		
		for(int i = 0; i < rows; i++){
			this.rows.add( new ArrayList<col_val>());
		}
	}
	
	public int nRows(){
		return rows.size();		
	}
	
	public int nCols(){
		return nCols;
	}
	
	/**
	 * number of stored entries
	 * @return
	 */
	public int numberofEntries() {
		int num = 0;
		for(ArrayList<col_val> row: rows){
			num+= row.size();
		}
		return num;
	}
	
	/**
	 * Get a specific Row
	 * @param idx
	 * @return
	 */
	public ArrayList<col_val> getRow(int idx){
		return rows.get(idx);
	}
	
	/**
	 * Get the last row of this matrix
	 * @return
	 */
	public ArrayList<col_val> lastRow(){
		return rows.get(rows.size()-1);
	}
	
	/**
	 * Get all Rows
	 * @return
	 */
	public ArrayList<ArrayList<col_val>> getRows(){
		return rows;
	}
		
	/**
	 * get the last existing entry of a specific row.
	 * Returns null if the row is empty.
	 * @param row
	 * @return
	 */
	public col_val lastElement(int row){
		if(rows.get(row).size() == 0){
			return null;
		}
		return rows.get(row).get(rows.get(row).size()-1);
	}
	
	/**
	 * Append an empty row to this matrix. This changes the matrix dimenstions.
	 */
	public void appendRow() {
		this.rows.add(new ArrayList<col_val>());
	}
	
	/**
	 * Getter method. If iterating over the matrix entries
	 * it will be better to directly iterate over each row,
	 * obtained by getRow(row), rather than accessing the
	 * entries with this getter.
	 * @param row
	 * @param col
	 * @return
	 */
	public float get(int row, int col) {
		ArrayList<col_val> r = rows.get(row);
		for(col_val c : r){
			if(c.col == col){
				return c.val;
			}
			else if (c.col > col){
				return 0;
			}
		}
		return 0;
	}
	
	/**
	 * Additionally assert that the entry exists.
	 * @param row
	 * @param col
	 * @return
	 */
	public float get_AssertExisting(int row, int col) {
		ArrayList<col_val> r = rows.get(row);
		for(col_val c : r){
			if(c.col == col){
				return c.val;
			}
			else if (c.col > col){
				assert(false);
				return 0;
			}
		}
		assert(false);
		return 0;
	}
	
	/**
	 * General purpose method to set entries. 
	 * Note that it is most efficient to fill in rows in a sorted manner, that is
	 * by setting entries in the order of their column indices (in that case set has O(1)). 
	 * Else a new entry needs to be inserted in an array list, resulting in a complexity of O(rowSize);
	 * @param row
	 * @param col
	 * @param val
	 */
	public void set(int row, int col, float val){
		assert(col < nCols);
		assert(row < nRows());
		
		//the new entry
		col_val c = new col_val(col, val);
		//first, fastest option: the matrix is being filled in row by row. 
		if(rows.get(row).size() == 0 || lastElement(row).col < col){
			rows.get(row).add(c);
			return;
		}
		
		//else search for the entry
		int indx = Collections.binarySearch(rows.get(row), c);
		//if inexistent: add it
		if(indx<0){
			rows.get(row).add(-(indx+1),c);
		}
		//else set the entry
		else{
			assert(rows.get(row).get(indx).col == col);
			rows.get(row).get(indx).val = val;
		}
		
	}
	
	/**
	 * Sorts and appends scale * row to this matrix
	 * @param row
	 * @param scale
	 */
	public void appendRow(ArrayList<col_val> row, float scale) {
		Collections.sort(row);
		assert(row.get(row.size()-1).col < nCols);
		
		this.rows.add(row);
		ArrayList<col_val> r = lastRow();
		for(col_val v: r){
			v.val *= scale;
		}
	}
	

	/**
	 * append the matrix  scale * mat to the actual matrix
	 * @param mat
	 * @param scale
	 */
	public void append(CSRMatrix mat, float scale) {
		assert(mat.nCols == nCols);
		for(ArrayList<col_val> row: mat.rows){
			this.appendRow(row, scale);
			
		}
	}
	
	/**
	 * Compute the transposed of this matrix
	 * @return
	 */
	public CSRMatrix transposed(){
		CSRMatrix mat_T = new CSRMatrix(nCols, nRows());
		
		int row = 0;
		for(ArrayList<col_val> r: rows){

			for(col_val c_v : r){
				mat_T.rows.get(c_v.col).add(new col_val(row, c_v.val));
			}
			row++;
		}
		
		for(ArrayList<col_val> r: mat_T.rows){
			Collections.sort(r);
		}
		
		return mat_T;
	}
	
	
	/**
	 * Compute A + B and store it in this matrix
	 * @param A
	 * @param B
	 */
	public void add(CSRMatrix A, CSRMatrix B){
		assert(B.nCols == A.nCols && B.nRows() == A.nRows());
		//will not work if this is either A or B.
		assert(this != A && this != B);
		
		this.nCols = A.nCols;
		this.rows.clear();
		
		int idx1, idx2;
		ArrayList<col_val> row1, row2, rowRes;
		for(int row = 0; row < A.nRows(); row++){
			idx1 = 0;
			idx2 = 0;
			
			this.appendRow();
			rowRes = this.lastRow();
			row1 = B.rows.get(row);
			row2 = A.rows.get(row);
			
			while(idx1 < row1.size() && idx2 < row2.size()){
				if(row1.get(idx1).col < row2.get(idx2).col){
					rowRes.add(new col_val(row1.get(idx1++)));
				}
				else if(row1.get(idx1).col > row2.get(idx2).col){
					rowRes.add(new col_val(row2.get(idx2++)));
				}
				else{
					rowRes.add(new col_val(row1.get(idx1).col, 
							row1.get(idx1++).val + row2.get(idx2++).val));
				}
			}
			while(idx1 < row1.size()){
				rowRes.add(new col_val(row1.get(idx1++)));
			}
			while(idx2 < row2.size()){
				rowRes.add(new col_val(row2.get(idx2++)));
			}
			
			
		}
	}
	
	
	/**
	 * Multiply this matrix with the vector other and write the result into result.
	 * @param other
	 * @param result
	 */
	public void mult(ArrayList<Float> other, ArrayList<Float> result){
		assert(other.size() == nCols);
		result.clear();
		result.ensureCapacity(nRows());
		
		
		float res;
		for(ArrayList<col_val> row : rows){
			res = 0;
			for(col_val c : row){
				res += c.val * other.get(c.col);
			}
			result.add(res);
		}

	}
	
	

		
	/**
	 * scale the matrix by some factor
	 * @param scale
	 */
	public void scale(float scale) {
		for(ArrayList<col_val> row: rows){
			for(col_val el : row){
				el.val *=scale;
			}
		}
	}
	
	/**
	 * Plain matrix multiplication.
	 * @param other
	 * @param result
	 */
	public void mult(CSRMatrix other, CSRMatrix result){
		assert(this.nCols == other.nRows());
		//for multiplication the columns of other need to be accessed fast,
		//A CSR matrix only allows fast row access, therefore the other matrix is transposed.
		CSRMatrix otherT = other.transposed();
		
		//prepare the result matrix
		result.rows.clear();
		result.nCols = other.nCols;
		
		int this_rows = this.nRows();
		ArrayList<col_val> myRow, otherCol;
		int help_idx;
		float newEntry; 
		boolean hasEntry;
		for(int i = 0; i < this_rows; i++){
			result.appendRow();
			for(int j = 0; j < other.nCols; j++){
				help_idx = 0;
				myRow = this.getRow(i);
				otherCol = otherT.getRow(j);
				newEntry = 0;
				hasEntry = false;
				
				//iterate over the entries of the row. Check if the other column has a corresponding entry
				//if it has add A(ik)*A(kj) to new entry
				for(col_val myEntry : myRow){
					while(help_idx < otherCol.size() && myEntry.col >otherCol.get(help_idx).col){
						help_idx++;
					}
					if(otherCol.size() <= help_idx){
						break;
					}
					else if(otherCol.get(help_idx).col == myEntry.col){
						hasEntry = true;
						newEntry += otherCol.get(help_idx).val * myEntry.val;
					}
				}
				if(hasEntry){
					result.set(i,j, newEntry);
				}
			}
		}
	}
	
	public boolean hasNans(){
		for(ArrayList<col_val> row : this.rows){
			for(col_val c : row){
				if(c.val * 0 != 0){
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Check if the matrix is malformed, that is:
	 * It has Nans or infinite values, entries  with invalid column indices, duplicate entries
	 * Or rows that are not sorted by column index.
	 * @param verbose
	 * @return
	 */
	public boolean checkMatrix(boolean verbose){
		int rowIdx = 0;
		for(ArrayList<col_val> row : this.rows){
			col_val lastC = new col_val(-1,0);
			for(col_val c : row){
				if(c.val * 0 != 0){
					if(verbose){
						System.out.println("Matrix has Nans or Infs in Row " + rowIdx);
					}
					return false;
				}
				else if(c.col <0 || c.col>= nCols ){
					if(verbose){
						System.out.println("Matrix has entries with out of bound column index in Row " + rowIdx);
					}
					return false;
				}
				else if(c.col == lastC.col){
					if(verbose){
						System.out.println("Matrix has duplicate entries in Row " + rowIdx);
					}
					return false;
				}
				else if(c.col < lastC.col){
					if(verbose){
						System.out.println("Matrix row is not sorted in Row " + rowIdx);
					}
					return false;
				}
				lastC = c;
			}
			rowIdx++;
		}
		return true;
	}
	/**
	 * Contains an integer, denoting the column of the entry, and a float, denoting the
	 * value of the entry.
	 * @author Alf
	 *
	 */
	public static class col_val implements Comparable<col_val>{
		public int col;
		public float val;
		
		public col_val(int column, float value) {
			this.col = column;
			this.val  = value;
		}
		public col_val(col_val o) {
			this.col = o.col;
			this.val = o.val;
		}
		
		@Override
		public int compareTo(col_val o) {
			return 
				this.col < o.col ? -1 :
				this.col == o.col ? 0 :
					1;
		}
		
		public String toString(){
			return "("+ this.col + "," + this.val + ") ";
		}
	}

/* ********************************************************************************/
/////////////////////////////////////////////////////////////////////////////////////
// Do not pass. Ugly ,optimized, uncommented code ahead.	
// Do not ignore this warning.
/////////////////////////////////////////////////////////////////////////////////////
/* ********************************************************************************/
	
	//magic speedup number.
	static final int numBits2 = 6104;
	
	/**
	 * Multiply two matrices using multiple threads.
	 * 
	 * The method is sped up by multiplying two lower resolution bit matrices
	 * and then pruning the computations accordingly.
	 * 
	 * Do not look at the code please. Please.
	 * @param other
	 * @param result
	 */
	public void multParallel(CSRMatrix other, CSRMatrix result){
		assert(this.nCols == other.nRows());
		assert(this!= result && other != result);
		CSRMatrix otherT = other.transposed();
		
		result.rows.clear();
		for(int i = 0; i < this.nRows(); i++){
			result.appendRow();
		}

		
		int idx = 0;
		

		//This BitSet array stores for columns what rows are nonzero
		BitSet[] rowsWithCol = new BitSet[numBits2];
		for(int i = 0; i < numBits2; i++){
			rowsWithCol[i] = new BitSet(numBits2);
		}
		
		idx = 0;
		int step = (int) Math.ceil((1.f*otherT.nRows() +1) / numBits2);
		//int max = numBits2*step;
		for(ArrayList<col_val> row: otherT.rows){
			
			assert(idx/step <= numBits2);
			for(col_val c_v: row){
				rowsWithCol[(int)(((long) c_v.col) * numBits2 / otherT.nCols)].set(idx / step);
			}
			
			
			idx++;
		}
		
		
		
		ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors()-1));
		RowServer rowServer = new RowServer(this.nRows());
		
		for(int i = 0; i < 30; i++){
			executor.execute(new MultThread(rowServer, otherT, result, null/*B*/, rowsWithCol, step));
		}
		
		executor.shutdown();
		
		try {
			executor.awaitTermination(10, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		result.nCols = other.nCols;
	}
	
	/**
	 * To allow multithreaded matrix multiplications
	 * @author Alf
	 *
	 */
	private class MultThread implements Runnable{
		
		private CSRMatrix result;
		private CSRMatrix otherT;
		private RowServer rowServer;
		BitSet sparsityA, temp, intersectedRows;
		//BitSet[] sparsityB;
		private BitSet[] rows_col;
		private int row_col_step;

		public MultThread(RowServer rowServer, CSRMatrix otherT, CSRMatrix result, 
				BitSet[] sparsityPattern_other, BitSet[] rowsWithCol, int step){
			this.rowServer = rowServer;
			this.otherT = otherT;
			this.result = result;
			
			sparsityA = new BitSet(numBits2);
			temp = new BitSet(numBits2);
			//sparsityB = sparsityPattern_other;
			this.rows_col = rowsWithCol; 
			this.row_col_step = step;
			this.intersectedRows = new BitSet(numBits2);
		}
		
		@Override
		public void run(){
			ArrayList<col_val> Arow, Bcol;
			ArrayList<col_val> resultRow;
			boolean exists;
			int sz1, sz2, idxA, idxB;
			float value;
			
			
			//thread gets a new rowmultiplication task, while there are tasks left
			for(int row = rowServer.getRow(); row >=0; row = rowServer.getRow()){
				resultRow = result.rows.get(row);
				Arow = rows.get(row);
				
				sparsityA.clear();
				for(col_val c_v: Arow){
					sparsityA.set((int)(((long) c_v.col )* numBits2 / otherT.nCols));
				}

				
				intersectedRows.clear();
				for(int i = sparsityA.nextSetBit(0); i>=0; i = sparsityA.nextSetBit(i+1)){
					intersectedRows.or(rows_col[i]);
					
					assert(rows_col[i].cardinality() <= numBits2);
				}
				
				//for(int j = 0; j < otherT.nRows; j++){
				for(int i = intersectedRows.nextSetBit(0); i>=0; i = intersectedRows.nextSetBit(i+1)){
					assert(i < numBits2);
					
					for(int j = i*row_col_step; 
							j < (i+1)*row_col_step && j <  otherT.nRows(); 
							j++){//*/
						
						Bcol = otherT.rows.get(j);
						if(Arow.get(Arow.size() -1).col < Bcol.get(0).col ||
								Bcol.get(Bcol.size() -1).col < Arow.get(0).col){
							continue;
						}
					
						sz1 = Arow.size(); sz2 = Bcol.size();
						
						exists = false;
						value = idxA = idxB =0;
						while(idxA < sz1 && idxB < sz2){
							if(Arow.get(idxA).col < Bcol.get(idxB).col){
								idxA++;
							}
							else if(Arow.get(idxA).col > Bcol.get(idxB).col){
								idxB++;
							}
							else{
								value += Arow.get(idxA).val * Bcol.get(idxB).val;
								exists = true;
								idxA++;idxB++;
							}
						}
						
						if(exists){
							resultRow.add(new col_val(j,value));
							
						}
					}//iterate over relevant subset of js.	\
				}//end iterate over filled blocks			/
			}//end getTask loop.
		}//end run()
	}//end class
	
	
	/**
	 * Allow cpu-paralelized matrix multiplication.
	 * @author Alf
	 *
	 */
	private class RowServer{
		int nextRow;
		int numRows;
		
		RowServer(int numRows){
			this.nextRow = 0;
			this.numRows = numRows;
		}
		
		public synchronized int getRow(){
			if(nextRow < numRows){
				int result = nextRow;
				nextRow++;
				if(nextRow % 10000 == 0){
					System.out.println("do Row " + nextRow + " out of " + numRows + "!");
				}
				return result;
			}
			return -1;
		}
	}

	public CSRMatrix clone() {
		CSRMatrix cpy = new CSRMatrix(nRows(), nCols);
		int i = 0;
		for(ArrayList<col_val> row: rows){
			ArrayList<col_val> newRow = cpy.rows.get(i++);
			for(col_val c: row){
				newRow.add(new col_val(c));
			}
		}
		return cpy;
	}



	
}


