package uk.ac.qub.csc3021.graph;

public class ParallelContextSimple extends ParallelContext {
    private class ThreadSimple extends Thread {
        public void run(SparseMatrix matrix, Relax relax, int from, int to) {
            matrix.ranged_edgemap(relax, from, to);
        }
    };

    public ParallelContextSimple(int num_threads_) {
	    super(num_threads_);
    }

    public void terminate() {

    }

    // The edgemap method for Q3 should create threads, which each process
    // one graph partition, then wait for them to complete.
    public void edgemap(SparseMatrix matrix, Relax relax) {
	    // use matrix.ranged_edgemap(relax, from, to); in each thread
        int numOfThreads = getNumThreads();
        int numOfVerts = matrix.getNumVertices();
        int range = numOfVerts / numOfThreads + 1;
        int remainder = numOfVerts % numOfThreads;

        for (int i = 0; i < numOfThreads; i++) {
            int start = i * range;
            int end = (i + 1) * range;
            if(i == numOfThreads - 1 && remainder > 0) { end = numOfVerts; }
            ThreadSimple thread = new ThreadSimple();
            thread.run(matrix, relax, start, end);
        }
    }
}
