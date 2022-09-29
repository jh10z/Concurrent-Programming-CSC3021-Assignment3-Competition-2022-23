package uk.ac.qub.csc3021.graph;

import java.util.concurrent.*;

public class ParallelContextQ3 extends ParallelContext {
    private class ThreadQ3 extends Thread {

	public void run() {
	}
    };

    
    public ParallelContextQ3( int num_threads ) {
	super( num_threads );
    }

    public void terminate() {
    }

    // The constructor for Q3 should create threads, which each remain
    // running throughout the program. They synchronise on a barrier.
    // The main thread executes the iterate method below, which also steps
    // through this barrier.
    public void iterate( SparseMatrix matrix, Relax relax ) {
	// use matrix.iterate( relax, from, to ); in each thread
    }
}
