package uk.ac.qub.csc3021.graph;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.TreeMap;

// This class represents the adjacency matrix of a graph as a sparse matrix
// in compressed sparse columns format (CSC). The incoming edges for each
// vertex are listed.
public class SparseMatrixCSC extends SparseMatrix {

	public class ThreadSimple extends Thread {

		public ThreadSimple() {

		}
		public void run() {

		}

	}

    // TODO: variable declarations
	int[] index;
	int[] source;
    int num_vertices; // Number of vertices in the graph
    int num_edges;    // Number of edges in the graph
	int num_threads;

    public SparseMatrixCSC(String file) {
		this.num_threads = ParallelContextHolder.get().getNumThreads();
		try {
			InputStreamReader is = new InputStreamReader(new FileInputStream(file), "UTF-8");
			BufferedReader rd = new BufferedReader(is);
			readFile(rd);
		} catch(FileNotFoundException e) {
			System.err.println( "File not found: " + e );
			return;
		} catch(UnsupportedEncodingException e) {
			System.err.println( "Unsupported encoding exception: " + e );
			return;
		} catch(Exception e) {
			System.err.println( "Exception: " + e );
			return;
		}
    }

    int getNext(BufferedReader rd) throws Exception {
		String line = rd.readLine();
		if(line == null) {
			throw new Exception( "premature end of file" );
		}
		return Integer.parseInt(line);
    }

    void readFile(BufferedReader rd) throws Exception {

		String line = rd.readLine();
		if(line == null) {
			throw new Exception("premature end of file");
		}
		if(!line.equalsIgnoreCase("CSC" ) && !line.equalsIgnoreCase("CSC-CSR")) {
			throw new Exception("file format error -- header");
		}

		num_vertices = getNext(rd);
		num_edges = getNext(rd);

		// TODO: allocate data structures
		index = new int[num_vertices + 1];
		source = new int[num_edges];

		index[0] = 0;
		for(int i = 0; i < num_vertices; i++) {
			line = rd.readLine();
			if(line == null) {
				throw new Exception("premature end of file");
			}
			String elm[] = line.split(" ");
			assert Integer.parseInt(elm[0]) == i : "Error in CSC file";

			index[i + 1] = (index[i] + elm.length) - 1; //index[1] = 0 + 93
			for(int j = 1; j < elm.length; j++) {
				int src = Integer.parseInt(elm[j]);
				// TODO:
				//    Record an edge from source src to destination i
				source[index[i] + (j - 1)] = src;
			}
		}
	}

    // Return number of vertices in the graph
    public int getNumVertices() { return num_vertices; }

    // Return number of edges in the graph
    public int getNumEdges() { return num_edges; }

    // Auxiliary function for PageRank calculation
    public void calculateOutDegree(int outdeg[]) {
		// TODO:
		//    Calculate the out-degree for every vertex, i.e., the
		//    number of edges where a vertex appears as a source vertex.
		for (int i = 0; i < num_edges; i++) { //go through all connections and check source reoocursion like COO
			outdeg[source[i]] += 1;
		}
    }
    
    public void edgemap(Relax relax) {
		// TODO:
		//    Iterate over all edges in the sparse matrix and call "relax"
		//    on each edge.
		for (int i = 0; i < num_vertices; i++) { //go through source
			for (int j = index[i]; j < index[i+1]; j++) { //if it falls in index its part of node
				relax.relax(source[j], i);
			}
		}
		System.out.println(num_threads);
    }

    public void ranged_edgemap(Relax relax, int from, int to) {
		// Only implement for parallel/concurrent processing
		// if you find it useful
		// TODO:
		//    Iterate over partition indicated by from...to and calculate
		//    the contribution to the new PageRank value of a destination
		//    vertex made by the corresponding source vertex
		for (int i = from; i < to; i++) { //go through source
			for (int j = index[i]; j < index[i+1]; j++) { //if it falls in index its part of node
				relax.relax(source[j], i);
			}
		}
    }
}

