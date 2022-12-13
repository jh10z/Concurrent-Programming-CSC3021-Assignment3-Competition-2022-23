package uk.ac.qub.csc3021.graph;

import java.util.ArrayList;

public class ParallelContextSimple extends ParallelContext {

    private final ArrayList<ThreadSimple> threads = new ArrayList<>();
    private static class ThreadSimple extends Thread {
        SparseMatrix matrix;
        Relax relax;
        int from;
        int to;

        public ThreadSimple (SparseMatrix matrix, Relax relax, int start, int end) {
            this.matrix = matrix;
            this.relax = relax;
            this.from = start;
            this.to = end;
        }
        public void run() {
            try {
                matrix.processEdgemapOnInput(relax, from, to);
            } catch(Exception e) {
                System.err.println("Exception: " + e);
            }
        }
    }

    public ParallelContextSimple(int num_threads_) {
	    super(num_threads_);
    }

    public void terminate() {
        for (ThreadSimple thread : threads) {
            thread.interrupt();
        }
    }

    public void edgemap(SparseMatrix matrix, Relax relax) {
        int numOfThreads = getNumThreads();
        int numOfVertices = matrix.getNumVertices();

        int range = numOfVertices / numOfThreads;
        int remainder = numOfVertices % numOfThreads;

        int start = 0, end;
        for (int i = 0; i < numOfThreads; i++) {
            end = start + (remainder-- > 0 ? range + 1 : range);
            ThreadSimple thread = new ThreadSimple(matrix, relax, start, end);
            thread.start();
            threads.add(thread);
            start = end; // reassign for next
        }
        try {
            for (ThreadSimple thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupted Exception: " + e);
        }
    }
}
