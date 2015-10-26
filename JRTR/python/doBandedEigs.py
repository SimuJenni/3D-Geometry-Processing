from scipy.sparse import csr_matrix
import scipy.sparse.linalg as sp
import scipy.linalg as la
import numpy as np

import sys, getopt

def readArray(array_file):
    "returns the 1D array contained iin the file"
    print("Reading : %s" %array_file)
    f= open(array_file, "r")
    b= []
    for line in f:
        b.append(float(line))
    f.close()
    return np.array(b)
    
def doBandedEigs(argv):
    #parse the command line
    file_i = '' #'evdDragon_10000_k2000ifile'
    file_j = ''#'evdDragon_10000_k2000jfile'
    file_val = ''#'evdDragon_10000_k2000vfile'
    file_x_out = ''#'evdDragon_10000_k2000out'
    k = 0
    try:
        opts, args = getopt.getopt(argv, "i:j:v:o:k:",
                                   ["ifile=","jfile=",
                                   "valfile=",
                                   "outPrefix=", "k="])
    except getopt.GetoptError:
        print( 'doBandedEigs.py -i <inputfile> -j <inputfile> -v <inputfile> -o <outputfile> -k <numEvs>')
        sys.exit(2)
    
    for opt, arg in opts:
        if opt in ("-i", "--ifile"):
            file_i = arg
        elif opt in ("-j", "--jfile"):
            file_j = arg
        elif opt in ("-o", "--outPrefix"):
            file_x_out = arg
        elif opt in ("-v", "--valfile"):
            file_val = arg
        elif opt in ("-k", "--k"):
            k = int(arg)
    
    vecfile = file_x_out
    vecfile += '_vecs'
    valfile = file_x_out + '_vals'
    
    print("out: %s, %s\n" % (vecfile, valfile))
    sys.stdout.flush()
    
    
    #read in data and build sparse matrix
    rows = readArray(file_i)
    cols = readArray(file_j)
    vals = readArray(file_val)
    ij = [rows,cols]
    mat = csr_matrix((vals,ij))
    
    #hack to get pos def laplacian
    mat = mat * np.sign(np.average(mat.diagonal()))
    #do a band by band eigenvalue and vector search.
    mn = 1e-3
    mean = -2
    last_mx = -1
    delta = 0
    all_evecs = []
    all_evals = []
    is_first_band = True
    num_evecs_found = 0
    band = 0
    while num_evecs_found < k:
        #Find an overlapping band with 50 eigenvectors.
        delta = (last_mx-mean)*0.8
        while True:
            evals, evecs = sp.eigsh(mat,50,sigma=last_mx+delta,which = 'LM')
            s = np.argsort(evals)
            mn = min(evals)
            delta = delta /2
            if (mn < last_mx or is_first_band):
                break
            else:
                print('redo, too large band step')
           
        #Determine the  new eigenvectors and add them to the rsult
        if is_first_band:
            s = np.array(range(0,evals.size))
        else:
            s = np.argwhere(np.select([evals>last_mx + 1e-6], [evals]))
        all_evecs.append(np.squeeze(evecs[:,s]))
        all_evals.append(np.squeeze(evals[s]))
        
        #If I didnt find any -> abort.
        if(s.size == 0):
            print("Error, could not find more than %i evectors" %num_evecs_found)
            exit(2)
        
        #update stats, prepare next range.
        num_evecs_found += s.size
        last_mx = max(evals)
        mean = np.average(evals)
        is_first_band = False
        print("band: %i, found %i evecs in total.\n" % (band, num_evecs_found))
        sys.stdout.flush()
        band+=1
        
    #write out result
    print("Writing out vectors...")
    f = open(vecfile, 'w')
    sys.stdout.flush()
    band = 0
    for vectorSet in all_evecs:
        print("%i," % band)
    	sys.stdout.flush()
    	band+=1
        for columns in vectorSet.T:
            for x_ij in columns:
                f.write('%f ' %(x_ij))
            f.write('\n')
    f.close()
    print("Writing out eigenvalues, band...")
    sys.stdout.flush()
    f = open(valfile, 'w')
    for vals in all_evals:
        for xi in vals:
            f.write('%f \n' %(xi))
    f.close()
    
    print( 'Evd: success!')
    sys.stdout.flush()

if __name__ == '__main__':
    doBandedEigs(sys.argv[1:])