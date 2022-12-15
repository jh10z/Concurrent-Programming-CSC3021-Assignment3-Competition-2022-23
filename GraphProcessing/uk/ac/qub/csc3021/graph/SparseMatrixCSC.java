package uk.ac.qub.csc3021.graph;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

// This class represents the adjacency matrix of a graph as a sparse matrix
// in compressed sparse columns format (CSC). The incoming edges for each
// vertex are listed.
public class SparseMatrixCSC extends SparseMatrix {

	int[] index;
	int[] source;
    int num_vertices = 3072627;
    int num_edges = 234370166;
	//BufferedReader rd;
	String file;

    public SparseMatrixCSC(String file) {
		try {
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
    void readFile() throws Exception {
		InputStreamReader is = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
		BufferedReader rd = new BufferedReader(is);
		for (int i = 0; i < 3; i++) {
			String line = rd.readLine();
			if(line == null) {
				throw new Exception("premature end of file");
			}
			if(i==0 && !line.equalsIgnoreCase("CSC" ) && !line.equalsIgnoreCase("CSC-CSR")) {
				throw new Exception("file format error -- header");
			}
			if(i==1) num_vertices = Integer.parseInt(line);
			if(i==2) num_edges = Integer.parseInt(line);
		}
		rd.close();
	}

    // Return number of vertices in the graph
    public int getNumVertices() { return num_vertices; }

    // Return number of edges in the graph
    public int getNumEdges() { return num_edges; }

	public String getFile() { return file; }

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

	public void processEdgemapOnInput(Relax relax, List<String> workload) {
		//double tm_start = System.nanoTime();

		for (int i = 0; i < workload.size(); i++) {
			String[] elm = workload.get(i).split(" ");
			for (int j = 1; j < elm.length; j++) {
				relax.relax(Integer.parseInt(elm[j]), Integer.parseInt(elm[0]));
			}
		}
//		double tm_step = (double)(System.nanoTime() - tm_start) * 1e-9;
//		System.err.println("emap processing time=" + tm_step + " seconds");

	}
}

