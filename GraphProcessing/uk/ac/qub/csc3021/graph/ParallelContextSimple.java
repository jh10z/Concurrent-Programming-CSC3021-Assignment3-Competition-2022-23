package uk.ac.qub.csc3021.graph;

import java.io.*;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParallelContextSimple extends ParallelContext {

    private final ArrayList<ThreadSimple> threads = new ArrayList<>();
    private static class ThreadSimple extends Thread {
        private final SparseMatrix matrix;
        private final Relax relax;
        private final List<String> workload;

        public ThreadSimple (SparseMatrix matrix, Relax relax, List<String> workload) {
            this.matrix = matrix;
            this.relax = relax;
            this.workload = workload;
        }
        public void run() {
            try {
                matrix.processEdgemapOnInput(relax, workload);
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

        ArrayList<String> workload = new ArrayList<>();

        try {
            final BufferedReader rd = new BufferedReader(new InputStreamReader(new FileInputStream(matrix.getFile()), StandardCharsets.UTF_8));
            //double tm_start = System.nanoTime();
            for (int i = 0; i < numOfVertices + 3; i++) {
                workload.add(rd.readLine());
            }
            rd.close();
//            double tm_step = (double)(System.nanoTime() - tm_start) * 1e-9;
//            System.err.println("Writing file: " + tm_step + " seconds");

            int start = 0;

            for (int i = 0; i < numOfThreads; i++) {
                int end = start + (remainder-- > 0 ? range + 1 : range);
                ThreadSimple thread = new ThreadSimple(matrix, relax, workload.subList(start + 3, end + 3));
                thread.start();
                threads.add(thread);
                start = end; // reassign for next
            }

            for (ThreadSimple thread : threads) {
                thread.join();
            }

        } catch (FileNotFoundException | InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
