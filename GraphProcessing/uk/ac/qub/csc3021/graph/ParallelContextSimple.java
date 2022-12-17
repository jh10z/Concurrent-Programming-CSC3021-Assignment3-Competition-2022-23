package uk.ac.qub.csc3021.graph;

import java.io.*;
import java.lang.ref.Cleaner;
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

    public void terminate() {

    }

    public void edgemap(SparseMatrix matrix, Relax relax) {
        File file = new File(matrix.getFile());
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(num_threads_);
        Collection<Future<?>> tasks = new LinkedList<>();
        try {
            long bufferSize = 70 * 1204;
            long currentPos = 0;
            double taskCount = Math.ceil((double)file.length() / bufferSize);

            while(taskCount-- > 0) {
                if(currentPos + bufferSize > file.length()) {
                    bufferSize = file.length() - currentPos;
                }
                Runnable run = new ThreadReadRelax(currentPos, bufferSize, matrix.getFile(), matrix, relax);
                tasks.add(executor.submit(run));
                currentPos += bufferSize;
            }
            for (Future<?> currTask : tasks) {
                currTask.get();
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        finally {
            executor.shutdown();
        }
    }

    private static class ThreadReadRelax implements Runnable {
        private final long pos;
        private final long size;
        private final String file;
        private final SparseMatrix matrix;
        private final Relax relax;

        public ThreadReadRelax (long pos, long size, String file, SparseMatrix matrix, Relax relax) {
            this.pos = pos;
            this.size = size;
            this.file = file;
            this.matrix = matrix;
            this.relax = relax;
        }
        public void run() {
            try (RandomAccessFile reader = new RandomAccessFile(file, "r")) {
                byte[] data = new byte[(int)size];
                reader.seek(pos);
                reader.read(data, 0, (int)size);
                String str = new String(data, StandardCharsets.UTF_8);
                String[] line = str.split("\n"); //split string by lines
                reader.seek(pos + size);

                int start = pos == 0 ? 3 : 1; //discard start lines
                if(pos + size < file.length()) {
                    line[line.length - 1] = line[line.length - 1] + reader.readByte() + reader.readLine();
                }
                matrix.processEdgemapOnInput(relax, Arrays.copyOfRange(line, start, line.length));

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
