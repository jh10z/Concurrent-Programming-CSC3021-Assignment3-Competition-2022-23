/*
 * Use command-line flag -ea for java VM to enable assertions.
 */
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
import java.util.StringTokenizer;

import uk.ac.qub.csc3021.graph.*;

// Main class with main() method. Performs the PageRank computation until
// convergence is reached.
class Driver {
    public static void main( String args[] ) {
	if( args.length < 5 ) {
	    System.err.println( "Usage: java Driver algorithm num-threads outputfile format inputfiles..." );
	    return;
	}

	String algorithm = args[0];
	int num_threads = Integer.parseInt( args[1] );
	String outputFile = args[2];
	String format = args[3];
	String inputFileCOO = null;
	String inputFileCSR = null;
	String inputFileCSC = null;
	for( int i=4; i < args.length; ++i ) {
	    String ext = args[i].substring( args[i].lastIndexOf( "." ) + 1 );
	    if( ext.equals( "csc" ) )
		inputFileCSC = args[i];
	    else if( ext.equals( "csr" ) )
		inputFileCSR = args[i];
	    else if( ext.equals( "coo" ) )
		inputFileCOO = args[i];
	    else if( ext.equals( "csc-csr" ) ) {
		inputFileCSC = args[i];
		inputFileCSR = args[i];
	    } else {
		System.err.println( "argument " + i + " has unrecognized filename extension \"" + ext + "\"" );
		System.err.println( "Note that files must be decompressed before calling this program" );
		System.err.println( "Usage: java Driver algorithm num-threads outputfile format inputfiles..." );
		return;
	    }
	}

	// Tell us what you're doing
	System.err.println( "Format: " + format );
	System.err.println( "Input file CSR: " + inputFileCSR );
	System.err.println( "Input file CSC: " + inputFileCSC );
	System.err.println( "Input file COO: " + inputFileCOO );
	System.err.println( "Algorithm: " + algorithm );
	System.err.println( "Number of threads: " + num_threads );
	System.err.println( "Output file: " + outputFile );

	long tm_start = System.nanoTime();

	SparseMatrix matrix;

	// Step 1. Read in the file
	if( format.equalsIgnoreCase( "CSR" ) ) {
	    matrix = new SparseMatrixCSR( inputFileCSR );
	} else if( format.equalsIgnoreCase( "CSC" ) ) {
	    matrix = new SparseMatrixCSC( inputFileCSC );
	} else if( format.equalsIgnoreCase( "COO" ) ) {
	    matrix = new SparseMatrixCOO( inputFileCOO );
	} else if( format.equalsIgnoreCase( "ICHOOSE" ) ) {
	    // Pick any you want.
	    // matrix = new SparseMatrixCOO( inputFileCOO );
	    // matrix = new SparseMatrixCSR( inputFileCSR );
	    matrix = new SparseMatrixCSC( inputFileCSC );
	} else {
	    System.err.println( "Unknown format '" + format + "'" );
            System.exit(43); // Kattis
	    return; // silence compiler errors
	}

	double tm_input = (double)(System.nanoTime() - tm_start) * 1e-9;
	System.err.println( "Reading input: " + tm_input + " seconds" );
	System.err.println( "Number of vertices: " + matrix.getNumVertices() );
	System.err.println( "Number of edges: " + matrix.getNumEdges() );
	tm_start = System.nanoTime();

	// What facilities for parallel execution do we have?
	// Options:
	// - ParallelContextSingleThread: fully implemented
	// - ParallelContextSimple: needs to be completed by yourself when
	//   asked for in the assignment brief.
	if( format.equalsIgnoreCase( "ICHOOSE" ) )
	    ParallelContextHolder.set( new ParallelContextSimple(num_threads) );
	else
	    ParallelContextHolder.set( new ParallelContextSingleThread() );

	try {
	    if( algorithm.equalsIgnoreCase( "PR" ) ) {
		// Step 2. Calculate PageRank values for the graph
		double PR[] = PageRank.compute( matrix );

		double tm_total = (double)(System.nanoTime() - tm_start) * 1e-9;
		System.err.println( "PageRank: total time: " + tm_total + " seconds" );
		tm_start = System.nanoTime();

		// Step 3. Dump PageRank values to file
		writeToFile( outputFile, PR );

		double tm_write = (double)(System.nanoTime() - tm_start) * 1e-9;
		System.err.println( "Writing file: " + tm_write + " seconds" );
	    } else if( algorithm.equalsIgnoreCase( "CC" ) ) {
		// Step 2. Calculate connected components of the graph
		int CC[] = ConnectedComponents.compute( matrix );

		double tm_total = (double)(System.nanoTime() - tm_start) * 1e-9;
		System.err.println( "Connected Components: total time: " + tm_total + " seconds" );
		tm_start = System.nanoTime();

		// Step 3. Dump component sizes to file
		writeToFile( outputFile, CC );

		double tm_write = (double)(System.nanoTime() - tm_start) * 1e-9;
		System.err.println( "Writing file: " + tm_write + " seconds" );
	    } else if( algorithm.equalsIgnoreCase( "DS" )
		|| algorithm.equalsIgnoreCase( "OPT" ) ) {
		// Step 2. Calculate connected components of the graph
		int CC[] = DisjointSetCC.compute( matrix );

		double tm_total = (double)(System.nanoTime() - tm_start) * 1e-9;
		System.err.println( "Disjoint Set: total time: " + tm_total + " seconds" );
		tm_start = System.nanoTime();

		// Step 3. Dump component sizes to file
		writeToFile( outputFile, CC );

		double tm_write = (double)(System.nanoTime() - tm_start) * 1e-9;
		System.err.println( "Writing file: " + tm_write + " seconds" );
	    } else {
		System.err.println( "Unknown algorithm '" + algorithm + "'" );
		return;
	    }
	} finally {
	    ParallelContextHolder.get().terminate();
	}
	System.err.println( "All done" );
    }

    static void writeToFile( String file, double[] v ) {
	try {
	    OutputStreamWriter os
		= new OutputStreamWriter( new FileOutputStream( file ), "UTF-8" );
	    BufferedWriter wr = new BufferedWriter( os );
	    writeToBuffer( wr, v );
	    wr.close();
	    os.close();
	} catch( FileNotFoundException e ) {
	    System.err.println( "File not found: " + e );
	    return;
	} catch( UnsupportedEncodingException e ) {
	    System.err.println( "Unsupported encoding exception: " + e );
	    return;
	} catch( Exception e ) {
	    System.err.println( "Exception: " + e );
	    return;
	}
    }
    static void writeToBuffer( BufferedWriter buf, double[] v ) {
	PrintWriter out = new PrintWriter( buf );
	for( int i=0; i < v.length; ++i )
	    out.println( i + " " + v[i] );
	out.close();
    }
    static void writeToFile( String file, int[] v ) {
	try {
	    OutputStreamWriter os
		= new OutputStreamWriter( new FileOutputStream( file ), "UTF-8" );
	    BufferedWriter wr = new BufferedWriter( os );
	    writeToBuffer( wr, v );
	    wr.close();
	    os.close();
	} catch( FileNotFoundException e ) {
	    System.err.println( "File not found: " + e );
	    return;
	} catch( UnsupportedEncodingException e ) {
	    System.err.println( "Unsupported encoding exception: " + e );
	    return;
	} catch( Exception e ) {
	    System.err.println( "Exception: " + e );
	    return;
	}
    }
    static void writeToBuffer( BufferedWriter buf, int[] v ) {
	PrintWriter out = new PrintWriter( buf );
	for( int i=0; i < v.length; ++i )
	    out.println( i + " " + v[i] );
	out.close();
    }
}
