package uk.ac.qub.csc3021.graph;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class ParallelContextSimple extends ParallelContext {

    private final ThreadRead[] readThreads;
    private final ThreadRelax[] relaxThreads;
    private final int num_threads_;
    private HashSet<String> workload = new HashSet<>();
    public ParallelContextSimple(int num_threads_) {
	    super(num_threads_);
        this.num_threads_ = num_threads_;
        this.readThreads = new ThreadRead[num_threads_];
        this.relaxThreads = new ThreadRelax[num_threads_];

    }

    public void terminate() {}

    public void edgemap(SparseMatrix matrix, Relax relax) {
        int numOfVertices = matrix.getNumVertices();
        int remainder = numOfVertices % num_threads_;

        File file = new File(matrix.getFile());
        long buffer = file.length() / num_threads_;  // 12 on 4 threads fastest

        long pos = 0;
        long size = (long)(buffer * 1.2);
        for (int i = 0; i < num_threads_; i++) { // 12 on 4 threads fastest
            if(i == num_threads_ - 1) { // 11 on 4 threads fastest
                size = file.length() - pos;
            }
            ThreadRead thread = new ThreadRead(pos, size, matrix.getFile());
            thread.start();
            readThreads[i] = thread;
            pos = (long)((pos + buffer));
        }
        for (int i = 0; i < num_threads_; i++) {
            try {
                readThreads[i].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

            for (int i = 0; i < num_threads_; i++) {
                ThreadRelax thread = new ThreadRelax(matrix, relax, readThreads[i].getLoad());
                thread.start();
                relaxThreads[i] = thread;
            }

            for (int i = 0; i < num_threads_; i++) {
                try {
                    relaxThreads[i].join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }


    private static class ThreadRead extends Thread {
        private final long pos;
        private final long size;
        private final String file;
        private String[] load;

        public ThreadRead (long pos, long size, String file) {
            this.pos = pos;
            this.size = size;
            this.file = file;
        }
        public String[] getLoad() {
            return load;
        }
        public void run() {
            try (RandomAccessFile reader = new RandomAccessFile(file, "r");
                 FileChannel channel = reader.getChannel();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                MappedByteBuffer buff = channel.map(FileChannel.MapMode.READ_ONLY, pos, size);
                String str = "";
                if(buff.hasRemaining()) {
                    byte[] data = new byte[buff.remaining()];
                    buff.get(data);
                    str = new String(data, StandardCharsets.UTF_8);
                }
                String[] line = str.split("\n");
                int start = pos == 0 ? 3 : 1;
                int end = pos + size == file.length() ? line.length : line.length - 1;
                this.load = Arrays.copyOfRange(line, start, end);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class ThreadRelax extends Thread {
        private final SparseMatrix matrix;
        private final Relax relax;
        private final String[] workload;

        public ThreadRelax (SparseMatrix matrix, Relax relax, String[] workload) {
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
}
