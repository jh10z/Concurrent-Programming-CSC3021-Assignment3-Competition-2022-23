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

    private ThreadRead[] readThreads;
    private ThreadRelax[] relaxThreads;
    private final int num_threads_;
    private HashSet<String> workload = new HashSet<>();
    public ParallelContextSimple(int num_threads_) {
	    super(num_threads_);
        this.num_threads_ = num_threads_;
        this.readThreads = new ThreadRead[num_threads_];
        this.relaxThreads = new ThreadRelax[num_threads_];

    }

    public void terminate() {
//        for (int i = 0; i < num_threads_; i++) {
//            threads[i].interrupt();
//        }
    }

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
        //System.out.println(workload.size());

//        int[] lineDistribution = new int[num_threads_ + 1];
//        int numOfVertices = matrix.getNumVertices();
//        int remainder = numOfVertices % num_threads_;
//
//        int start = 0;
//        for (int i = 0; i < num_threads_; i++) {
//            int end = start + (numOfVertices / num_threads_) + (remainder-- > 0 ? 1 : 0);
//            lineDistribution[i] = start;
//            lineDistribution[i+1] = end;
//            start = end; // reassign for next
//        }
//
//        for (int num : lineDistribution) {
//            System.out.println("lmao: " + num);
//        }
//
//        File file = new File(matrix.getFile());
//        long buffer = file.length() / 2;
//        ThreadRead t = new ThreadRead(0, buffer, numOfVertices, matrix.getFile(), 1 );
//        t.start();
//        threads[0] = t;
//        ThreadRead thread2 = new ThreadRead(buffer - 500, file.length() - (file.length() / 2) + 500, numOfVertices, matrix.getFile(), 2 );
//        thread2.start();
//        threads[1] = thread2;
//        for (int i = 0; i < 2; i++) {
//            try {
//                threads[i].join();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        String[] array1 = t.getWorkload();
//        String[] array2 =  thread2.getWorkload();
//
//        int aLen = array1.length;
//        int bLen = array2.length;
//        String[] workload = new String[aLen + bLen];
//
//        System.arraycopy(array1, 0, workload, 0, aLen);
//        System.arraycopy(array2, 0, workload, aLen, bLen);
//            BufferedReader rd = new BufferedReader(new InputStreamReader(new FileInputStream(matrix.getFile()), StandardCharsets.UTF_8), 8 * 1024);
//            for (int i = 0; i < numOfVertices + 3; i++) {
//                workload[i] = rd.readLine();
//            }
//            rd.close();

//            File file = new File(matrix.getFile());
//            long pos = 25;
//            long buffer = file.length() / 12;
//
//            for (int i = 0; i < num_threads_; i++) {
//                ThreadRead read = new ThreadRead(pos, buffer, numOfVertices);
//                read.start();
//                pos += buffer - (buffer / 10);
//                if(i==10) buffer = file.length() - pos;
//                threads[i] = read;
//            }
//
//            for (int i = 0; i < num_threads_; i++) {
//                threads[i].join();
//            }

//            int start = 0;
//            for (int i = 0; i < num_threads_; i++) {
//                int end = start + (numOfVertices / num_threads_) + (remainder-- > 0 ? 1 : 0);
//                ThreadRelax thread = new ThreadRelax(matrix, relax, start + 3, end + 3, workload);
//                thread.start();
//                relaxThreads[i] = thread;
//                start = end; // reassign for next
//            }
//
//            for (int i = 0; i < num_threads_; i++) {
//                try {
//                    relaxThreads[i].join();
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }

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

//    private static class ThreadRelax extends Thread {
//        private final SparseMatrix matrix;
//        private final Relax relax;
//        private final String[] workload;
//        private final int start;
//        private final int end;
//
//        public ThreadRelax (SparseMatrix matrix, Relax relax, int start, int end, String[] workload) {
//            this.matrix = matrix;
//            this.relax = relax;
//            this.workload = workload;
//            this.start = start;
//            this.end = end;
//        }
//        public void run() {
//            try {
//                matrix.processEdgemapOnInput(relax, Arrays.copyOfRange(workload, start, end));
//            } catch(Exception e) {
//                System.err.println("Exception: " + e);
//            }
//        }
//    }

//    private static class ThreadRead extends Thread {
//        private final long pos;
//        private final long size;
//        private String[] workload;
//        private String file;
//        private final int thread;
//
//        public ThreadRead (long pos, long size, int numOfVerts, String file, int thread) {
//            this.pos = pos;
//            this.size = size;
//            this.file = file;
//            this.thread = thread;
//        }
//        public String[] getWorkload() {
//            return workload;
//        }
//        public void run() {
//            try (RandomAccessFile reader = new RandomAccessFile("C:\\Users\\Astray\\Desktop\\orkut_undir.csc-csr", "r");
//                 FileChannel channel = reader.getChannel();
//                 ByteArrayOutputStream ignored = new ByteArrayOutputStream()) {
//
//                    MappedByteBuffer buff = channel.map(FileChannel.MapMode.READ_ONLY, pos, size);
//                    String str = "";
//                    if(buff.hasRemaining()) {
//                        byte[] data = new byte[buff.remaining()];
//                        buff.get(data);
//                        str = new String(data, StandardCharsets.UTF_8);
//                    }
//                    String[] lines = str.split("\n");
//                    if(thread == 1) {
//                        workload = Arrays.copyOfRange(lines, 0,lines.length-1);
//                    } else if(thread == 2) {
//                        workload = Arrays.copyOfRange(lines, 1,lines.length);
//                    }
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
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
