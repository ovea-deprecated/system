/**
 * Copyright (C) 2011 Ovea <dev@ovea.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ovea.network.pipe;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class ProcessPipe extends Process {

    private static final AtomicLong threadGroups = new AtomicLong();

    private final InputStream inputStream;
    private final InputStream errorStream;
    private final OutputStream outputStream;
    private final CountDownLatch finished;
    private final SharedErrorStream sharedErrorStream = new SharedErrorStream();
    private final AtomicReference<Integer> exitValue = new AtomicReference<Integer>();
    private final Queue<Thread> threads = new LinkedList<Thread>();
    private final Queue<PipeConnection> pipes = new LinkedList<PipeConnection>();

    ProcessPipe(List<? extends Process> processes) {
        processes = new ArrayList<Process>(processes);
        finished = new CountDownLatch(processes.size());
        ThreadGroup group = new ThreadGroup(Thread.currentThread().getThreadGroup(), getClass().getSimpleName() + "-" + threadGroups.getAndIncrement() + " (" + processes.size() + ")");
        for (int i = 0; i < processes.size(); i++) {
            Process current = processes.get(i);
            Thread thread = new Thread(group, new RunnableProcess(current, i == processes.size() - 1));
            thread.start();
            threads.add(thread);
            if (i > 0) {
                pipes.add(Pipes.connect(processes.get(i - 1).getInputStream(), current.getOutputStream()));
            }
        }
        this.outputStream = processes.get(0).getOutputStream();
        this.inputStream = processes.get(processes.size() - 1).getInputStream();
        try {
            this.errorStream = new PipedInputStream(sharedErrorStream);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Output stream of the first process in the pipe
     */
    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Input stream of the last process in the pipe
     */
    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Error stream combination of all process error streams
     */
    @Override
    public InputStream getErrorStream() {
        return errorStream;
    }

    @Override
    public int waitFor() throws InterruptedException {
        if (Thread.interrupted())
            throw new InterruptedException();
        try {
            finished.await();
            return exitValue();
        } catch (InterruptedException e) {
            destroy();
            throw e;
        }
    }

    public int waitFor(long time, TimeUnit unit) throws InterruptedException, TimeoutException {
        if (Thread.interrupted())
            throw new InterruptedException();
        try {
            if (finished.await(time, unit)) {
                return exitValue();
            } else {
                throw new TimeoutException();
            }
        } catch (InterruptedException e) {
            destroy();
            throw e;
        }
    }

    @Override
    public int exitValue() throws IllegalThreadStateException {
        Integer i = exitValue.get();
        if (i == null) {
            throw new IllegalThreadStateException("Pipe as not completed");
        }
        return i;
    }

    @Override
    public void destroy() {
        while (!pipes.isEmpty()) {
            pipes.poll().interrupt();
        }
        for (Thread thread : threads) {
            thread.interrupt();
        }
        try {
            while (!threads.isEmpty()) {
                threads.poll().join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private final class RunnableProcess implements Runnable {
        private final Process process;
        private final boolean isLast;

        private RunnableProcess(Process process, boolean isLast) {
            this.process = process;
            this.isLast = isLast;
        }

        @Override
        public void run() {
            try {
                int end = process.waitFor();
                if (isLast) {
                    exitValue.compareAndSet(null, end);
                }
                sharedErrorStream.append(process.getErrorStream());
            } catch (InterruptedException ignored) {
            } finally {
                if (isLast) {
                    try {
                        sharedErrorStream.close();
                    } catch (IOException ignored) {
                    }
                }
                finished.countDown();
            }
        }
    }

    private static final class SharedErrorStream extends PipedOutputStream {

        private final Lock lock = new ReentrantLock();

        void append(InputStream stream) throws InterruptedException {
            try {
                int c;
                lock.lockInterruptibly();
                while ((c = stream.read()) != -1) {
                    write(c);
                }
            } catch (IOException ignored) {
            } finally {
                lock.unlock();
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
