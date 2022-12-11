package uk.ac.qub.csc3021.graph;

import java.util.ArrayList;

public class ParallelContextSimple extends ParallelContext {

    private ArrayList<ThreadSimple> threads = new ArrayList<ThreadSimple>();
    private class ThreadSimple extends Thread {
        public void run(SparseMatrix matrix, Relax relax, int from, int to) {
            matrix.ranged_edgemap(relax, from, to);
        }
    };

    public ParallelContextSimple(int num_threads_) {
	    super(num_threads_);
    }

    public void terminate() {
        for (ThreadSimple thread : threads) {
            thread.interrupt();
        }
    }

    // The edgemap method for Q3 should create threads, which each process
    // one graph partition, then wait for them to complete.
    public void edgemap(SparseMatrix matrix, Relax relax) {
	    // use matrix.ranged_edgemap(relax, from, to); in each thread
        int numOfThreads = getNumThreads();
        int numOfVertices = matrix.getNumVertices();
        //
        int range = numOfVertices / numOfThreads;
        int remainder = numOfVertices % numOfThreads;

        int start = 0, end = 0;
        for (int i = 0; i < numOfThreads; i++) {
            end = start + (remainder-- > 0 ? range + 1 : range); // distribute remainder at earliest if there is any left
            ThreadSimple thread = new ThreadSimple();
            thread.run(matrix, relax, start, end);
            threads.add(thread);
            start = end; // reassign for next
        }
        try {
            for (ThreadSimple thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            System.out.println(e.toString());
        }
    }
}
