package com.ovea.network.pipe;

import java.io.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class ProcessPipe extends Process {

    private final CountDownLatch finished;

    private final Process begin;
    private final Process end;
    private final PipedOutputStream errors = new PipedOutputStream() {
        @Override
        public void close() throws IOException {
            // do not close it
        }
    };

    ProcessPipe(Process left, Process right, Process... suite) {
        finished = new CountDownLatch(2 + suite.length);
        Pipes.connect(left.getInputStream(), right.getOutputStream());
        Pipes.connect(left.getErrorStream(), errors);
        Process prev = right;
        for (Process next : suite) {
            Pipes.connect(prev.getInputStream(), next.getOutputStream());
            Pipes.connect(prev.getErrorStream(), errors);
            prev = next;
        }
        this.begin = left;
        this.end = prev;
    }

    @Override
    public OutputStream getOutputStream() {
        return begin.getOutputStream();
    }

    @Override
    public InputStream getInputStream() {
        return end.getInputStream();
    }

    @Override
    public InputStream getErrorStream() {
        try {
            return new PipedInputStream(errors);
        } catch (IOException e) {
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    @Override
    public int waitFor() throws InterruptedException {
        finished.await();
        return end.waitFor();
    }

    @Override
    public int exitValue() {
        return end.exitValue();
    }

    @Override
    public void destroy() {
        end.destroy();
    }

    private static class ProcessRunner extends Thread {
        private final Process process;
        private volatile boolean finished;

        private ProcessRunner(Process process) {
            this.process = process;
        }

        @Override
        public void run() {
            try {
                process.waitFor();
            } catch (InterruptedException ignored) {
            }
            synchronized (this) {
                notifyAll();
                finished = true;
            }
        }

        synchronized void await(long time, TimeUnit unit) throws InterruptedException {
            if (!finished) {
                wait(unit.toMillis(time));
                if (!finished) {
                    process.destroy();
                    process.waitFor();
                }
            }
        }
    }
}

