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
import java.util.*;

// This class represents the adjacency matrix of a graph as a sparse matrix
// in coordinate format (COO)
public class SparseMatrixCOO extends SparseMatrix {
    // TODO: variable declarations
    //HashMap<Integer, Integer> cooHashMap;
	Tuple[] cooTuple;

	HashSet<Integer> source_vertices = new HashSet<Integer>();
    int num_vertices; // Number of vertices in the graph
    int num_edges;    // Number of edges in the graph

    public SparseMatrixCOO(String file) {
		try {
			InputStreamReader is = new InputStreamReader(new FileInputStream(file), "UTF-8");
			BufferedReader rd = new BufferedReader(is);
			readFile(rd);
		} catch(FileNotFoundException e) {
			System.err.println("File not found: " + e);
			return;
		} catch(UnsupportedEncodingException e) {
			System.err.println("Unsupported encoding exception: " + e);
			return;
		} catch(Exception e) {
			System.err.println("Exception: " + e);
			return;
		}
    }

    int getNext(BufferedReader rd) throws Exception {
		String line = rd.readLine();
		if(line == null) {
			throw new Exception( "premature end of file" );
		}
		System.out.println("Get Next: " + Integer.parseInt(line));
		return Integer.parseInt(line);
    }

    void getNextPair(BufferedReader rd, int pair[]) throws Exception {
		String line = rd.readLine();
		if(line == null) {
			throw new Exception( "premature end of file");
		}
		StringTokenizer st = new StringTokenizer(line);
		pair[0] = Integer.parseInt(st.nextToken());
		pair[1] = Integer.parseInt(st.nextToken() );
    }

    void readFile(BufferedReader rd) throws Exception {
		String line = rd.readLine();
		//both if statements check if the file is COO in line 1
		if(line == null) {
			throw new Exception("premature end of file");
		}
		if(!line.equalsIgnoreCase("COO")) {
			throw new Exception("file format error -- header");
		}

		num_vertices = getNext(rd); //line 2
		num_edges = getNext(rd); // line 3

		// TODO: Allocate memory for the COO representation
//		int source[] = new int[num_edges];
//		int dest[] = new int[num_edges]; //change to adj matrix for quicker calc I think (change back if other is faster)
//		int[][] matrix = new int[num_vertices][num_vertices];
//		cooHashMap = new HashMap<Integer, Integer>(); //unweighted graph + no insertion order which is fine for coo
		cooTuple = new Tuple[num_edges];

		int edge[] = new int[2]; //Create an edge, edge in COO is source to destination
		for(int i = 0; i < num_edges; i++) { //use this to iterate through rows
			getNextPair(rd, edge);
			// TODO:
			// 	Insert edge with source edge[0] and destination edge[1]
			//PUT CODE HERE
//			cooHashMap.put(edge[0], edge[1]);
//			source[i] = edge[0];
//			dest[i] = edge[1];
//			//matrix[row/dest][col/source]
//			matrix[edge[1]][edge[0]] = 1; //NOT A SPARSE MATRIX???
			cooTuple[i] = new Tuple(edge[0], edge[1]);
			source_vertices.add(edge[0]); //For calc out degree later
			//source_verticesvertices.add(edge[1]);
		}
		//System.out.println("Hash Map: " + cooHashMap.size()); //Doesn't work
//		System.out.println("Tuple: " + cooTuple.length);
//		System.out.println("Should Match: " + num_edges);
		System.out.println(source_vertices.size());
	}

    // Return number of vertices in the graph
    public int getNumVertices() { return num_vertices; }

    // Return number of edges in the graph
    public int getNumEdges() { return num_edges; }

    // Auxiliary function for PageRank calculation
    public void calculateOutDegree(int outdeg[]) {
//		for (int i = 0; i < outdeg.length; i++) {
//			System.out.println(outdeg);
//		}
		// TODO:
		//    Calculate the out-degree for every vertex, i.e., the
		//    number of edges where a vertex appears as a source vertex.
		//PUT CODE HERE
		System.out.println(outdeg.length);
		//int count = 0;
//		for (int i = 0; i < num_edges; i++) {
//			if(vertices.toArray()[i] == cooTuple[i])
//		}
		
		int i = 0;
		for (Integer vertex : source_vertices) {
			outdeg[i] = (int)Arrays.stream(cooTuple).filter(x -> x.source == vertex).count();
			i++;
		}
    }

    public void edgemap(Relax relax) {
		// TODO:
		//    Iterate over all edges in the sparse matrix and calculate
		//    the contribution to the new PageRank value of a destination
		//    vertex made by the corresponding source vertex
		//PUT CODE HERE:

    }

    public void ranged_edgemap(Relax relax, int from, int to) {
		// Only implement for parallel/concurrent processing
		// if you find it useful
		// CHECK IF YOU NEED TO PUT CODE HERE
    }

	public class Tuple {
		public int source;
		public int dest;
		public Tuple(int source, int dest) {
			this.source = source;
			this.dest = dest;
		}
	}
}


