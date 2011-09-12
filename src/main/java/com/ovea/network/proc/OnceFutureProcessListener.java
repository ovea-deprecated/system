package com.ovea.network.proc;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class OnceFutureProcessListener implements FutureProcessListener {

    private final AtomicReference<FutureProcessListener> listener;

    public OnceFutureProcessListener(FutureProcessListener listener) {
        this.listener = new AtomicReference<FutureProcessListener>(listener);
    }

    @Override
    public void onComplete(FutureProcess futureProcess) {
        FutureProcessListener listener = this.listener.getAndSet(null);
        if (listener != null) {
            listener.onComplete(futureProcess);
        }
    }

    @Override
    public void onInterrupted(FutureProcess futureProcess) {
        FutureProcessListener listener = this.listener.getAndSet(null);
        if (listener != null) {
            listener.onInterrupted(futureProcess);
        }
    }
}
