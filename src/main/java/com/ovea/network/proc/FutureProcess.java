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
package com.ovea.network.proc;

import java.util.concurrent.*;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class FutureProcess implements Future<Integer> {

    private static final FutureProcessListener EMPTY = new FutureProcessListenerAdapter();

    private final Process process;
    private final Future<Integer> exitCode;

    public FutureProcess(Process process) {
        this(process, EMPTY);
    }

    public FutureProcess(final Process process, final FutureProcessListener listener) {
        FutureTask<Integer> task = new FutureTask<Integer>(new Callable<Integer>() {
            @Override
            public Integer call() throws InterruptedException {
                return process.waitFor();
            }
        }) {
            @Override
            protected void done() {
                try {
                    get();
                    listener.onComplete(FutureProcess.this);
                } catch (InterruptedException e) {
                    listener.onInterrupted(FutureProcess.this);
                } catch (CancellationException e) {
                    listener.onInterrupted(FutureProcess.this);
                } catch (ExecutionException e) {
                    // can only be an interrupted exception
                    listener.onInterrupted(FutureProcess.this);
                } finally {
                    try {
                        process.destroy();
                    } catch (Throwable ignored) {
                    }
                }
            }
        };
        this.process = process;
        this.exitCode = task;
        new Thread(task).start();
    }

    public Process process() {
        return process;
    }

    // delegates

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return exitCode.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return exitCode.isCancelled();
    }

    @Override
    public boolean isDone() {
        return exitCode.isDone();
    }

    @Override
    public Integer get() throws InterruptedException, ExecutionException {
        return exitCode.get();
    }

    @Override
    public Integer get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return exitCode.get(timeout, unit);
    }
}
