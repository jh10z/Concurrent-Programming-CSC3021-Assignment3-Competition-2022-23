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
            long bufferSize = 8192L * 64L;
            long currentPos = 0L;
            double taskCount = Math.ceil((double)file.length() / bufferSize);

            while(taskCount-- > 0) {
                if(currentPos + bufferSize > file.length()) {
                    bufferSize = file.length() - currentPos;
                    taskCount = 0;
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

                int start = pos == 0 ? 3 : 1;
                data = discardFirstLine(data, start);

                if(pos + size < file.length()) {
                    data = includeLastLine(data, reader);
                }
                matrix.processEdgemapOnInput(relax, new String(data, StandardCharsets.UTF_8).split("\n"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private byte[] discardFirstLine(byte[] data, int startingLineNum) {
            int start = 0;
            int lineCounter = 0;
            for (int i = 0; i < data.length; i++) {
                if(data[i] == 0xA) {
                    start = i + 1;
                    lineCounter++;
                }
                if(lineCounter == startingLineNum) {
                    break;
                }
            }
            return Arrays.copyOfRange(data, start, data.length);
        }

        private byte[] includeLastLine(byte[] data, RandomAccessFile reader) {
            byte character;
            ArrayList<Byte> endLine = new ArrayList<>();
            try {
                while ((character = reader.readByte()) != 0xA) {
                    endLine.add(character);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            byte[] lines = new byte[data.length + endLine.size()];
            for (int i = 0; i < lines.length; i++) {
                lines[i] = data.length - 1 < i ? data[i] : endLine.get(i - data.length);
            }
            return lines;
        }
    }
}
