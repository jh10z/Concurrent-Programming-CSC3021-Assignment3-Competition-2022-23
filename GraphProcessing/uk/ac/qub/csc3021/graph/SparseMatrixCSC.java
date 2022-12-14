package uk.ac.qub.csc3021.graph;

import java.io.*;
import java.util.TreeMap;

// This class represents the adjacency matrix of a graph as a sparse matrix
// in compressed sparse columns format (CSC). The incoming edges for each
// vertex are listed.
public class SparseMatrixCSC extends SparseMatrix {

	int[] index;
	int[] source;
    int num_vertices;
    int num_edges;
	BufferedReader rd;
	String file;

    public SparseMatrixCSC(String file) {
		try {
			InputStreamReader is = new InputStreamReader(new FileInputStream(file), "UTF-8");
			this.rd = new BufferedReader(is);
			this.file = file;
			readFile();
		} catch(FileNotFoundException e) {
			System.err.println( "File not found: " + e );
		} catch(UnsupportedEncodingException e) {
			System.err.println( "Unsupported encoding exception: " + e );
		} catch(Exception e) {
			System.err.println( "Exception: " + e );
		}
    }

    int getNext() throws Exception {
		String line = rd.readLine();
		if(line == null) {
			throw new Exception( "premature end of file" );
		}
		return Integer.parseInt(line);
    }

    void readFile() throws Exception {
		String line = rd.readLine();
		if(line == null) {
			throw new Exception("premature end of file");
		}
		if(!line.equalsIgnoreCase("CSC" ) && !line.equalsIgnoreCase("CSC-CSR")) {
			throw new Exception("file format error -- header");
		}

		num_vertices = getNext();
		num_edges = getNext();
	}

    // Return number of vertices in the graph
    public int getNumVertices() { return num_vertices; }

    // Return number of edges in the graph
    public int getNumEdges() { return num_edges; }

    public void calculateOutDegree(int outdeg[]) {
		for (int i = 0; i < num_edges; i++) {
			outdeg[source[i]] += 1;
		}
    }
    
    public void edgemap(Relax relax) {
		for (int i = 0; i < num_vertices; i++) {
			for (int j = index[i]; j < index[i+1]; j++) {
				relax.relax(source[j], i);
			}
		}
    }

    public void ranged_edgemap(Relax relax, int from, int to) {
		for (int i = from; i < to; i++) {
			for (int j = index[i]; j < index[i+1]; j++) {
				relax.relax(source[j], i);
			}
		}
    }

	public void processEdgemapOnInput(Relax relax, int from, int to) throws Exception {
		for(int i = from; i < to; i++) {
			String[] elm = rd.readLine().split(" ");
			for(int j = 1; j < elm.length; j++) {
				relax.relax(Integer.parseInt(elm[j]), Integer.parseInt(elm[0]));
			}
		}
	}
}

