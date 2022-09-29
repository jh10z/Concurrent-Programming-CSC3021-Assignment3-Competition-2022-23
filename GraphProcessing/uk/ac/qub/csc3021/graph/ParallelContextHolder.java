package uk.ac.qub.csc3021.graph;

// This class holds a single instance of a parallel context
public class ParallelContextHolder {
    private static ParallelContext context = null;

    public static void set( ParallelContext context_ ) {
	context = context_;
    }

    public static ParallelContext get() {
	return context;
    }
}
