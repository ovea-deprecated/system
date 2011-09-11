package com.ovea.network.pipe;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class OncePipeListener implements PipeListener {

    private final AtomicReference<PipeListener> listener;

    public OncePipeListener(PipeListener listener) {
        this.listener = new AtomicReference<PipeListener>(listener);
    }

    @Override
    public void onConnect(Pipe pipe) {
        PipeListener t = listener.get();
        if (t != null) {
            t.onConnect(pipe);
        }
    }

    @Override
    public void onClose(Pipe pipe) {
        PipeListener tl = listener.getAndSet(null);
        if (tl != null) {
            tl.onClose(pipe);
        }
    }

    @Override
    public void onBroken(Pipe pipe, BrokenPipeException e) {
        PipeListener tl = listener.getAndSet(null);
        if (tl != null) {
            tl.onBroken(pipe, e);
        }
    }

    @Override
    public void onInterrupt(Pipe pipe) {
        PipeListener tl = listener.getAndSet(null);
        if (tl != null) {
            tl.onInterrupt(pipe);
        }
    }
}
