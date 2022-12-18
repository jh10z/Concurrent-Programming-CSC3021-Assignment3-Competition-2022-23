package uk.ac.qub.csc3021.graph;

import java.io.*;
import java.lang.ref.Cleaner;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class ParallelContextSimple extends ParallelContext {

    private final int num_threads_;

    public ParallelContextSimple(int num_threads_) {
	    super(num_threads_);
        this.num_threads_ = num_threads_;
    }

    public void terminate() {}

    public void edgemap(SparseMatrix matrix, Relax relax) {
        boolean largeFile = false;

        ThreadReadRelaxMapped[] threads = new ThreadReadRelaxMapped[num_threads_];

        try (RandomAccessFile reader = new RandomAccessFile(matrix.getFile(), "r"); FileChannel channel = reader.getChannel()) {
            //Map File to Memory
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
            byte[] data = new byte[map.remaining()];
            if(map.hasRemaining()) {
                map.get(data);
            } else {
                throw new InterruptedException();
            }

            long buffer = (channel.size() / num_threads_);
            long endPos = buffer;
            for (int i = 0; i < num_threads_; i++) {
                int endIndex = (int)(i * buffer + buffer);
                if(num_threads_ - 1 == i) {
                    endPos = channel.size();
                    endIndex = (int)channel.size();
                }
                ThreadReadRelaxMapped thread = new ThreadReadRelaxMapped(Arrays.copyOfRange(data,
                        (int)(i * buffer), endIndex), matrix, relax,
                        (int)(i * buffer), (int)endPos);
                thread.start();
                threads[i] = thread;
            }

            for (ThreadReadRelaxMapped thread : threads) {
                thread.join();
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            largeFile = true;
        }

        if(largeFile) {
            File file = new File(matrix.getFile());
            ThreadPoolExecutor executor = (ThreadPoolExecutor)Executors.newFixedThreadPool(num_threads_);

            try {
                long bufferSize = 2024 * 1024;
                long currentPos = 0L;
                long taskCount = file.length() / bufferSize; //beware of rounding down

                while(taskCount-- > 0) {
                    if(currentPos + bufferSize > file.length()) {
                        bufferSize = file.length() - currentPos;
                        taskCount = 0;
                    }
                    ThreadReadRelax run = new ThreadReadRelax(currentPos, (int)bufferSize, matrix, relax);
                    executor.submit(run);
                    currentPos += bufferSize;
                }

                executor.shutdown();
                if(!executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS)) {
                    throw new InterruptedException("CSC3021: I couldn't join threads...");
                }

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class ThreadReadRelax implements Runnable {
        private final long pos;
        private final int size;
        private final SparseMatrix matrix;
        private final Relax relax;
        private final File file;

        public ThreadReadRelax (long pos, int size, SparseMatrix matrix, Relax relax) {
            this.pos = pos;
            this.size = size;
            this.matrix = matrix;
            this.relax = relax;
            this.file = new File(matrix.getFile());
        }

        public void run() {
            try (RandomAccessFile reader = new RandomAccessFile(file, "r")) {
                byte[] data = new byte[size];
                reader.seek(pos);
                reader.read(data, 0, size);

                String[] line = new String(data, StandardCharsets.UTF_8).split("\n"); //slow

                int start = pos == 0 ? 3 : 1;
                if(pos + size < matrix.getFile().length()) {
                    reader.seek(pos - line[line.length - 1].length());
                    line[line.length - 1] = reader.readLine();
                }

                matrix.processEdgemapOnInput(relax, Arrays.copyOfRange(line, start, line.length));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class ThreadReadRelaxMapped extends Thread {
        private final byte[] workload;
        private final SparseMatrix matrix;
        private final Relax relax;
        private final int pos;
        private final int size;

        public ThreadReadRelaxMapped (byte[] workload, SparseMatrix matrix, Relax relax, int pos, int size) {
            this.workload = workload;
            this.matrix = matrix;
            this.relax = relax;
            this.pos = pos;
            this.size = size;
        }

        public void run() {
            String[] lines = new String(workload, StandardCharsets.UTF_8).split("\n");
            int start = pos == 0 ? 3 : 1;
            int end = pos + size == matrix.getFile().length() ? lines.length : lines.length - 1;
            matrix.processEdgemapOnInput(relax, Arrays.copyOfRange(lines, start, end));
        }
    }
}
