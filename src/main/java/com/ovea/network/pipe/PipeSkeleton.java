package com.ovea.network.pipe;

import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public abstract class PipeSkeleton<IN extends Closeable, OUT extends Closeable> implements Pipe {

    private static enum State {READY, CONNECTED, CLOSED}

    private static final PipeListener EMPTY = new PipeListenerAdapter();

    private final AtomicReference<State> state = new AtomicReference<State>(State.READY);
    private final String name;

    private PipeListener listener;
    private IN from;
    private OUT to;

    protected PipeSkeleton(String name, IN from, OUT to) {
        if (from == null) throw new IllegalArgumentException("Missing origin endpoint");
        if (to == null) throw new IllegalArgumentException("Missing destination endpoint");
        if (name == null) throw new IllegalArgumentException("Missing pipe name");
        this.from = from;
        this.to = to;
        this.name = name;
    }

    protected PipeSkeleton(IN from, OUT to) {
        this(UUID.randomUUID().toString(), from, to);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PipeSkeleton that = (PipeSkeleton) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public final boolean isConnected() {
        return state.get() == State.CONNECTED;
    }

    @Override
    public Pipe listenedBy(PipeListener listener) {
        if (listener == null) throw new IllegalArgumentException("Listener cannot be null");
        this.listener = listener;
        return this;
    }

    @Override
    public final void connectAndWait() throws BrokenPipeException, InterruptedException {
        if (state.compareAndSet(State.READY, State.CONNECTED)) {
            listener().onConnect(this);
            try {
                copy(from, to);
            } catch (InterruptedIOException e) {
                closeStreams();
                listener().onInterrrupt(this);
                throw new InterruptedException(e.getMessage());
            } catch (BrokenPipeException e) {
                closeStreams();
                listener().onError(this, e);
                throw e;
            } catch (IOException e) {
                closeStreams();
                BrokenPipeException bpe = new BrokenPipeException(e);
                listener().onError(this, bpe);
                throw bpe;
            }
            closeStreams();
            listener().onClose(this);
        } else {
            throw new BrokenPipeException("Unable to connect pipe");
        }
    }

    @Override
    public void connectAndWait(long time, TimeUnit unit) throws TimeoutException, InterruptedException, BrokenPipeException {
        if (state.compareAndSet(State.READY, State.CONNECTED)) {
            final AtomicBoolean managed = new AtomicBoolean(false);
            FutureTask<Void> task = new FutureTask<Void>(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    listener().onConnect(PipeSkeleton.this);
                    try {
                        copy(from, to);
                    } catch (InterruptedIOException e) {
                        if (!managed.get()) {
                            closeStreams();
                            listener().onInterrrupt(PipeSkeleton.this);
                            throw new InterruptedException(e.getMessage());
                        }
                    } catch (BrokenPipeException e) {
                        if (!managed.get()) {
                            closeStreams();
                            listener().onError(PipeSkeleton.this, e);
                            throw e;
                        }
                    } catch (IOException e) {
                        if (!managed.get()) {
                            closeStreams();
                            BrokenPipeException bpe = new BrokenPipeException(e);
                            listener().onError(PipeSkeleton.this, bpe);
                            throw bpe;
                        }
                    }
                    if (!managed.get()) {
                        closeStreams();
                        listener().onClose(PipeSkeleton.this);
                    }
                    return null;
                }
            });
            Thread thread = new Thread(task, "pipe-" + name + "-thread");
            thread.start();
            try {
                task.get(time, unit);
            } catch (ExecutionException e) {
                Throwable t = e;
                if (e.getCause() != null) {
                    t = e.getCause();
                }
                if (t instanceof RuntimeException)
                    throw (RuntimeException) t;
                if (t instanceof BrokenPipeException)
                    throw (BrokenPipeException) t;
                if (t instanceof InterruptedException)
                    throw (InterruptedException) t;
                if (t instanceof Error)
                    throw (Error) t;
                RuntimeException re = new RuntimeException(t.getMessage(), e);
                re.setStackTrace(t.getStackTrace());
                throw re;
            } catch (InterruptedException e) {
                managed.set(true);
                closeStreams();
                thread.interrupt();
                listener().onInterrrupt(this);
                throw e;
            } catch (TimeoutException e) {
                managed.set(true);
                closeStreams();
                thread.interrupt();
                listener().onTimeout(this);
                throw e;
            }
        } else {
            throw new BrokenPipeException("Unable to connect pipe");
        }
    }

    protected abstract void copy(IN from, OUT to) throws IOException, BrokenPipeException;

    private PipeListener listener() {
        PipeListener l = listener;
        return l == null ? EMPTY : l;
    }

    private void closeStreams() {
        if (state.compareAndSet(State.CONNECTED, State.CLOSED) || state.compareAndSet(State.READY, State.CLOSED)) {
            if (from != null) {
                try {
                    from.close();
                } catch (Exception ignored) {
                }
                from = null;
            }
            if (to != null) {
                try {
                    to.close();
                } catch (Exception ignored) {
                }
                to = null;
            }
        }
    }
}
