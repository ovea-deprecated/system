package com.ovea.network.tunnel;

import com.ovea.network.pipe.*;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class Tunnel {

    private static enum State {OPENED, CLOSED, INTERRUPTED, BROKEN}

    private static final TunnelListener EMPTY = new TunnelListenerAdapter();

    private final AtomicReference<State> state = new AtomicReference<State>(null);
    private final CountDownLatch latch = new CountDownLatch(2);
    private final PipeConnection up;
    private final PipeConnection down;
    private final String name;

    private Tunnel(String name, Pipe up, Pipe down, final TunnelListener listener) {
        this.name = name;
        this.up = up.listenedBy(new Listener(down, listener)).connect();
        this.down = down.listenedBy(new Listener(up, listener)).connect();
    }

    public boolean isOpened() {
        return state.get() == State.OPENED;
    }

    public boolean isClosed() {
        return state.get() == State.CLOSED;
    }

    public boolean isBroken() {
        return state.get() == State.BROKEN;
    }

    public boolean isInterrupted() {
        return state.get() == State.INTERRUPTED;
    }

    public void interrupt() {
        up.interrupt();
        down.interrupt();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tunnel tunnel = (Tunnel) o;
        return down.equals(tunnel.down) && up.equals(tunnel.up);
    }

    @Override
    public int hashCode() {
        int result = up.hashCode();
        result = 31 * result + down.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return name;
    }

    public void await(long time, TimeUnit unit) throws InterruptedException, BrokenTunnelException, TimeoutException {
        latch.await(time, unit);
        //TODO
    }

    public void await() throws InterruptedException, BrokenTunnelException {
        latch.await();
        //TODO
    }

    private class Listener implements PipeListener {

        private final Pipe other;
        private final TunnelListener listener;

        private Listener(Pipe other, TunnelListener listener) {
            this.other = other;
            this.listener = listener;
        }

        @Override
        public void onConnect(Pipe pipe) {
            if (state.compareAndSet(null, State.OPENED)) {
                listener.onConnect(Tunnel.this);
            }
        }

        @Override
        public void onClose(Pipe pipe) {
            if (state.compareAndSet(null, State.CLOSED) || state.compareAndSet(State.OPENED, State.CLOSED)) {
                other.connect().interrupt();
                listener.onClose(Tunnel.this);
            }
            latch.countDown();
        }

        @Override
        public void onBroken(Pipe pipe, BrokenPipeException e) {
            if (state.compareAndSet(null, State.BROKEN) || state.compareAndSet(State.OPENED, State.BROKEN)) {
                other.connect().interrupt();
                listener.onBroken(Tunnel.this, new BrokenTunnelException(e));
            }
            latch.countDown();
        }

        @Override
        public void onInterrupt(Pipe pipe) {
            if (state.compareAndSet(null, State.INTERRUPTED) || state.compareAndSet(State.OPENED, State.INTERRUPTED)) {
                other.connect().interrupt();
                listener.onInterrupt(Tunnel.this);
            }
            latch.countDown();
        }
    }

    public static Tunnel connect(Socket left, Socket right) throws IOException {
        return connect(left, right, EMPTY);
    }

    public static Tunnel connect(final Socket left, final Socket right, TunnelListener listener) throws IOException {
        if (left == null) throw new IllegalArgumentException("Missing left socket");
        if (right == null) throw new IllegalArgumentException("Missing right socket");
        if (listener == null) throw new IllegalArgumentException("Missing tunnel listener");
        String l = left.getInetAddress().getHostAddress() + ":" + left.getPort();
        String r = right.getInetAddress().getHostAddress() + ":" + right.getPort();
        return new Tunnel(
                l + "<=>" + r,
                new PipeByteStream(l + "=>" + r, left.getInputStream(), right.getOutputStream()),
                new PipeByteStream(r + "=>" + l, right.getInputStream(), left.getOutputStream()),
                new TunnelListeners(new TunnelListenerAdapter() {
                    @Override
                    public void onClose(Tunnel tunnel) {
                        try {
                            left.close();
                        } catch (IOException ignored) {
                        }
                        try {
                            right.close();
                        } catch (IOException ignored) {
                        }
                    }

                    @Override
                    public void onBroken(Tunnel tunnel, BrokenTunnelException e) {
                        onClose(tunnel);
                    }

                    @Override
                    public void onInterrupt(Tunnel tunnel) {
                        onClose(tunnel);
                    }
                }, listener));
    }
}
