package uk.ac.qub.csc3021.graph;

// Performs the PageRank computation until convergence is reached.
public class PageRank {
    private static class PageRankRelax implements Relax {
	PageRankRelax( int outdeg_[], double d_, double x_[], double y_[] ) {
	    outdeg = outdeg_;
	    d = d_;
	    x = x_;
	    y = y_;
	}

	public void relax( int src, int dst ) {
	    double w = d / (double)outdeg[src];
	    y[dst] += w * x[src];
	}

	int outdeg[];
	double d;
	double x[];
	double y[];
    };

    public static double[] compute( SparseMatrix matrix ) {
	long tm_start = System.nanoTime();

	final int n = matrix.getNumVertices();
	double x[] = new double[n];
	double v[] = new double[n];
	double y[] = new double[n];
	final double d = 0.85; // Leave this value as is
	final double tol = 1e-7; // Leave this value as is
	final int max_iter = 100;
	final boolean verbose = true;
	double delta = 2;
	int iter = 0;

	for( int i=0; i < n; ++i ) {
	    x[i] = v[i] = 1.0 / (double)n;
	    y[i] = 0;
	}

	int outdeg[] = new int[n];
	matrix.calculateOutDegree( outdeg );

	double tm_init = (double)(System.nanoTime() - tm_start) * 1e-9;
	System.err.println( "Initialisation: " + tm_init + " seconds" );
	tm_start = System.nanoTime();

	PageRankRelax PRrelax = new PageRankRelax( outdeg, d, x, y );
	ParallelContext context = ParallelContextHolder.get();

	while( iter < max_iter && delta > tol ) {
	    // Power iteration step.
	    // 1. Transfering weight over out-going links (summation part)
	    context.edgemap( matrix, PRrelax );
	    // 2. Constants (1-d)v[i] added in separately.
	    double w = 1.0 - sum( y, n ); // ensure y[] will sum to 1
	    // System.out.println( "scale with w=" +  w + " add " + (w*v[0]) );
	    for( int i=0; i < n; ++i )
		y[i] += w * v[i];

	    /*
	    for( int i=0; i < n; ++i )
		System.err.println( "Y " + i + " " + y[i] );
	    */

	    // Calculate residual error
	    delta = normdiff( x, y, n );
	    iter++;

	    // Rescale to unit length and swap x[] and y[]
	    w = 1.0 / sum( y, n );
	    for( int i=0; i < n; ++i ) {
		x[i] = y[i] * w;
		y[i] = 0.;
	    }

	    /*
	    for( int i=0; i < n; ++i )
		System.err.println( i + " " + x[i] );
	    */

	    double tm_step = (double)(System.nanoTime() - tm_start) * 1e-9;
	    if( verbose )
		System.err.println( "iteration " + iter + ": residual error="
				    + delta + " xnorm=" + sum(x, n)
				    + " time=" + tm_step + " seconds" );
	    tm_start = System.nanoTime();
	}

	if( delta > tol ) {
	    System.err.println( "Error: solution has not converged." );
	    // We should ignore the result if it has not converged and
	    // return null in this case. However, to help you debugging,
	    // we'll return whatever you came up with.
	    // return null;
	}

	return x;
    }

    public static void validate( SparseMatrix matrix, double[] x ) {
	long tm_start = System.nanoTime();

	final int n = matrix.getNumVertices();
	double v[] = new double[n];
	double y[] = new double[n];
	final double d = 0.85; // Leave this value as is
	final double tol = 1e-7; // Leave this value as is

	for( int i=0; i < n; ++i ) {
	    v[i] = 1.0 / (double)n;
	    y[i] = 0;
	}

	int outdeg[] = new int[n];
	matrix.calculateOutDegree( outdeg );

	double tm_init = (double)(System.nanoTime() - tm_start) * 1e-9;
	System.err.println( "Initialisation: " + tm_init + " seconds" );
	tm_start = System.nanoTime();

	PageRankRelax PRrelax = new PageRankRelax( outdeg, d, x, y );
	ParallelContext context = ParallelContextHolder.get();

	// Perform one step of the power iteration
	context.edgemap( matrix, PRrelax );

	// Constants (1-d)v[i] added in separately.
	double w = 1.0 - sum( y, n );
	for( int i=0; i < n; ++i )
	    y[i] += w * v[i];

	// Calculate residual error
	double delta = normdiff( x, y, n );

	System.err.println( "delta: " + delta );

	if( Double.isNaN( delta ) ) {
	    System.err.println( "Error: NaN value encountered: delta=" + delta );
	    System.exit(43); // Kattis
	} else if( delta > tol ) {
	    System.err.println( "Error: tolerance not met: delta=" + delta );
	    System.exit(43); // Kattis
	} else {
	    System.err.println( "Success." );
	    System.exit(42); // Kattis
	}
    }

    static private double sum( double[] a, int n ) {
	double d = 0.;
	double err = 0.;
	for( int i=0; i < n; ++i ) {
	    // The code below achieves
	    // d += a[i];
	    // but does so with high accuracy
	    double tmp = d;
	    double y = a[i] + err;
	    d = tmp + y;
	    err = tmp - d;
	    err += y;
	}
	return d;
    }

    static private double normdiff( double[] a, double[] b, int n ) {
	double d = 0.;
	double err = 0.;
	for( int i=0; i < n; ++i ) {
	    // The code below achieves
	    // d += Math.abs(b[i] - a[i]);
	    // but does so with high accuracy
	    double tmp = d;
	    double y = Math.abs( b[i] - a[i] ) + err;
	    d = tmp + y;
	    err = tmp - d;
	    err += y;
	}
	return d;
    }
}
