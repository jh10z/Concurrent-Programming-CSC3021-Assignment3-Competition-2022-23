package uk.ac.qub.csc3021.graph;

// Calculate the strongly connected components by propagating labels.
// This algorithm only works correctly for undirected graphs
public class ConnectedComponents {
    private static class CCRelax implements Relax {
	CCRelax( int x_[], int y_[] ) {
	    x = x_;
	    y = y_;
	}

	public void relax( int src, int dst ) {
	    // y[dst] = Math.min( y[dst], x[src] );
	    y[dst] = Math.min( y[dst], y[src] );
	    // return y[dst] == 0;
	}

	int x[];
	int y[];
    };

    public static int[] compute( SparseMatrix matrix ) {
	long tm_start = System.nanoTime();

	final int n = matrix.getNumVertices();
	int x[] = new int[n];
	int y[] = new int[n];
	final boolean verbose = true;
	final int max_iter = 100;
	int iter = 0;
	boolean change = true;

	for( int i=0; i < n; ++i ) {
	    x[i] = y[i] = i; // Each vertex is assigned a unique label
	}

	final int degree[] = new int[n];
	matrix.calculateOutDegree( degree );
	int lg = 0;
	for( int i=1; i < n; ++i ) {
	    if( degree[i] > degree[lg] )
		lg = i;
	}
	// Swap initial values
	x[0] = y[0] = lg;
	x[lg] = y[lg] = 0;

	CCRelax CCrelax = new CCRelax( x, y );

	double tm_init = (double)(System.nanoTime() - tm_start) * 1e-9;
	System.err.println( "Initialisation: " + tm_init + " seconds" );
	tm_start = System.nanoTime();

	ParallelContext context = ParallelContextHolder.get();

	while( iter < max_iter && change ) {
	    // 1. Assign same label to connected vertices
	    context.edgemap( matrix, CCrelax );
	    // 2. Check changes and copy data over for new pass
	    change = false;
	    for( int i=0; i < n; ++i ) {
		if( x[i] != y[i] ) {
		    x[i] = y[i];
		    change = true;
		}
	    }

	    double tm_step = (double)(System.nanoTime() - tm_start) * 1e-9;
	    if( verbose )
		System.err.println( "iteration " + iter
				    + " time=" + tm_step + " seconds" );
	    tm_start = System.nanoTime();
	    ++iter;
	}

	// Post-process the labels

	// 1. Count number of components
	//    and map component IDs to narrow domain
	int ncc = 0;
	int remap[] = new int[n];
	for( int i=0; i < n; ++i ) {
	    // "unswap" values such that we can identify representatives
	    if( x[i] == 0 )
		x[i] = lg;
	    else if( x[i] == lg )
		x[i] = 0;
	    if( x[i] == i )
		remap[i] = ncc++;
	}

	if( verbose )
	    System.err.println( "Number of components: " + ncc );

	// 2. Calculate size of each component
	int sizes[] = new int[ncc];
	for( int i=0; i < n; ++i ) {
	    ++sizes[remap[x[i]]];
	}

	if( verbose )
	    System.err.println( "ConnectedComponents: " + ncc + " components" );


	return sizes;
    }
}
