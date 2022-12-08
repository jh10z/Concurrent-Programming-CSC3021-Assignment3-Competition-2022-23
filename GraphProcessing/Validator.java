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
class Validator {
    public static void main( String args[] ) {
	if( args.length < 3 ) {
	    System.err.println( "Usage: java Validator format inputfile algorithm" );
	    System.exit(43); // Kattis
	}

	String format = args[0];
	String inputFile = args[1];
	String algorithm = args[2];

	// Tell us what you're doing
	System.err.println( "Format: " + format );
	System.err.println( "Input file: " + inputFile );
	System.err.println( "Algorithm: " + algorithm );

	long tm_start = System.nanoTime();

	SparseMatrix matrix;

	if( format.equalsIgnoreCase( "CSR" ) ) {
	    matrix = new SparseMatrixCSR( inputFile );
	} else if( format.equalsIgnoreCase( "CSC" ) ) {
	    matrix = new SparseMatrixCSC( inputFile );
	} else if( format.equalsIgnoreCase( "COO" ) ) {
	    matrix = new SparseMatrixCOO( inputFile );
	} else {
	    System.err.println( "Unknown format '" + format + "'" );
	    System.exit(43); // Kattis
	    return;
	}

	double tm_input = (double)(System.nanoTime() - tm_start) * 1e-9;
	System.err.println( "Reading input: " + tm_input + " seconds" );
	tm_start = System.nanoTime();

	// What facilities for parallel execution do we have?
	ParallelContextHolder.set( new ParallelContextSingleThread() );
	try {
	    if( algorithm.equalsIgnoreCase( "PR" ) ) {
		// Read computed values
		int n = matrix.getNumVertices();
		double values[] = new double[n];
		readFromStdin( values, n );
		
		// Validate PageRank values for the graph
		PageRank.validate( matrix, values );
	    } else if( algorithm.equalsIgnoreCase( "CC" ) ) {
		// We are hard-coding the solutions here:
		if( inputFile.matches( ".*rMatGraph_J_5_100.*" ) ) {
		    // Read computed values
		    int n = 4;
		    int cc[] = new int[n];
		    readFromStdin( cc, n );
		
		    boolean v = ( cc[0] == 125 || cc[0] == 1 )
			&& ( cc[1] == 125 || cc[1] == 1 )
			&& ( cc[2] == 125 || cc[2] == 1 )
			&& ( cc[3] == 125 || cc[3] == 1 );
		    boolean total128 = cc[0] + cc[1] + cc[2] + cc[3] == 128;
		    if( !v || !total128 ) {
			System.err.println( "Error: component sizes wrong" );
			System.exit(43); // Kattis
		    } else {
			System.err.println( "Success." );
			System.exit(42); // Kattis
		    }
		} else if( inputFile.matches( ".*orkut_undir.*" ) ) {
		    // Read computed values
		    int n = 187;
		    int cc[] = new int[n];
		    readFromStdin( cc, n );
		
		    int k = 0;
		    int v = matrix.getNumVertices();
		    boolean correct = true;
		    for( int i=0; i < n; ++i ) {
			if( cc[i] != 1 && cc[i] != 3072441 ) {
			    correct = false;
			    break;
			}
			k += cc[i];
		    }
		    if( k != v )
			correct = false;
		    if( !correct ) {
			System.err.println( "Error: component sizes wrong: sum to " + k + " vertices: " + v );
			System.exit(43); // Kattis
		    } else {
			System.err.println( "Success." );
			System.exit(42); // Kattis
		    }
		} else {
		    System.err.println( "Error: no hardcoded data for '"
					+ inputFile + "'" );
		    System.exit(43); // Kattis
		}
	    } else {
		System.err.println( "Error: unknown algorithm '" + algorithm + "'" );
		System.exit(43); // Kattis
	    }
	} finally {
	    ParallelContextHolder.get().terminate();
	}
	// Should not get here
	System.exit(43); // Kattis
    }

    static void readFromStdin( double[] v, int n ) {
	try {
	    InputStreamReader is
		= new InputStreamReader( System.in, "UTF-8" );
	    BufferedReader rd = new BufferedReader( is );
	    readFromBuffer( rd, v, n );
	    is.close();
	} catch( FileNotFoundException e ) {
	    System.err.println( "File not found: " + e );
	    System.exit( 43 );
	} catch( UnsupportedEncodingException e ) {
	    System.err.println( "Unsupported encoding exception: " + e );
	    System.exit( 43 );
	} catch( Exception e ) {
	    System.err.println( "Exception: " + e );
	    System.exit( 43 );
	}
    }
    
    static void readFromFile( String file, double[] v, int n ) {
	try {
	    InputStreamReader is
		= new InputStreamReader( new FileInputStream( file ), "UTF-8" );
	    BufferedReader rd = new BufferedReader( is );
	    readFromBuffer( rd, v, n );
	} catch( FileNotFoundException e ) {
	    System.err.println( "File not found: " + e );
	    System.exit( 43 );
	} catch( UnsupportedEncodingException e ) {
	    System.err.println( "Unsupported encoding exception: " + e );
	    System.exit( 43 );
	} catch( Exception e ) {
	    System.err.println( "Exception: " + e );
	    System.exit( 43 );
	}
    }

    static void readFromBuffer( BufferedReader rd, double[] v, int n )
	throws Exception {
	for( int i=0; i < n; ++i ) {
	    String line = rd.readLine();
	    if( line == null )
		throw new Exception( "premature end of file (values)" );

	    StringTokenizer st = new StringTokenizer( line );
	    int idx = Integer.parseInt( st.nextToken() );
	    if( i != idx )
		throw new Exception( "error in order of vertices: "
				     + idx + " found; expected " + i );
	    v[i] = Double.parseDouble( st.nextToken() );
	    if( Double.isNaN( v[i] ) ) {
		System.err.println( "NaN value read for position " + i );
		System.exit( 43 );
	    }
	}
    }
    
    static void readFromStdin( int[] v, int n ) {
	try {
	    InputStreamReader is
		= new InputStreamReader( System.in, "UTF-8" );
	    BufferedReader rd = new BufferedReader( is );
	    readFromBuffer( rd, v, n );
	} catch( FileNotFoundException e ) {
	    System.err.println( "File not found: " + e );
	    System.exit( 43 );
	} catch( UnsupportedEncodingException e ) {
	    System.err.println( "Unsupported encoding exception: " + e );
	    System.exit( 43 );
	} catch( Exception e ) {
	    System.err.println( "Exception: " + e );
	    System.exit( 43 );
	}
    }
    
    static void readFromFile( String file, int[] v, int n ) {
	try {
	    InputStreamReader is
		= new InputStreamReader( new FileInputStream( file ), "UTF-8" );
	    BufferedReader rd = new BufferedReader( is );
	    readFromBuffer( rd, v, n );
	} catch( FileNotFoundException e ) {
	    System.err.println( "File not found: " + e );
	    System.exit( 43 );
	} catch( UnsupportedEncodingException e ) {
	    System.err.println( "Unsupported encoding exception: " + e );
	    System.exit( 43 );
	} catch( Exception e ) {
	    System.err.println( "Exception: " + e );
	    System.exit( 43 );
	}
    }

    static void readFromBuffer( BufferedReader rd, int[] v, int n )
	throws Exception {
	for( int i=0; i < n; ++i ) {
	    String line = rd.readLine();
	    if( line == null )
		throw new Exception( "premature end of file (values)" );

	    StringTokenizer st = new StringTokenizer( line );
	    int idx = Integer.parseInt( st.nextToken() );
	    if( i != idx )
		throw new Exception( "error in order of vertices: "
				     + idx + " found; expected " + i );
	    v[i] = Integer.parseInt( st.nextToken() );
	}
    }
}
