package uk.ac.qub.csc3021.graph;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
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
			InputStreamReader is = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
			BufferedReader rd = new BufferedReader(is);
			String line = rd.readLine();

			if(!line.equalsIgnoreCase("CSC" ) && !line.equalsIgnoreCase("CSC-CSR")) {
				throw new Exception("file format error -- header");
			}
			num_vertices = Integer.parseInt(rd.readLine());
			num_edges = Integer.parseInt(rd.readLine());
			rd.close();
		} catch(FileNotFoundException e) {
			System.err.println( "File not found: " + e );
		} catch(UnsupportedEncodingException e) {
			System.err.println( "Unsupported encoding exception: " + e );
		} catch(Exception e) {
			System.err.println( "Exception: " + e );
		}
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

	public void processEdgemapOnInput(Relax relax, String[] workload) {
		for (String s : workload) {
			ArrayList<String> elm = split(s);
			for (int j = 1; j < elm.size(); j++) {
				relax.relax(Integer.parseInt(elm.get(j)), Integer.parseInt(elm.get(0)));
			}
		}
	}
	public ArrayList<String> split(String str) { //Test
		ArrayList<String> elements = new ArrayList<>();
		int pos = 0, end;
		while ((end = str.indexOf(' ', pos)) >= 0) {
			elements.add(str.substring(pos, end));
			pos = end + 1;
		}
		elements.add(str.substring(pos));
		return elements;
	}
}

