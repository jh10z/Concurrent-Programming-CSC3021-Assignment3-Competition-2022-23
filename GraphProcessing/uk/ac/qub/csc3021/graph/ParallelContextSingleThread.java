package uk.ac.qub.csc3021.graph;

public class ParallelContextSingleThread extends ParallelContext {
    public ParallelContextSingleThread() {
	// We only use one thread in this case
	super( 1 );
    }

    // Terminate all threads (easy if we create no threads!)
    public void terminate() { }

    // Call into the iterate method and visit all edges
    public void edgemap( SparseMatrix matrix, Relax relax ) {
	matrix.edgemap( relax );
    }
}
