package com.ovea.network.tunnel;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class OnceTunnelListener implements TunnelListener {

    private final AtomicReference<TunnelListener> listener;

    public OnceTunnelListener(TunnelListener listener) {
        this.listener = new AtomicReference<TunnelListener>(listener);
    }

    @Override
    public void onConnect(Tunnel tunnel) {
        TunnelListener t = listener.get();
        if (t != null) {
            t.onConnect(tunnel);
        }
    }

    @Override
    public void onClose(Tunnel tunnel) {
        TunnelListener tl = listener.getAndSet(null);
        if (tl != null) {
            tl.onClose(tunnel);
        }
    }

    @Override
    public void onBroken(Tunnel tunnel, BrokenTunnelException e) {
        TunnelListener tl = listener.getAndSet(null);
        if (tl != null) {
            tl.onBroken(tunnel, e);
        }
    }

    @Override
    public void onInterrupt(Tunnel tunnel) {
        TunnelListener tl = listener.getAndSet(null);
        if (tl != null) {
            tl.onInterrupt(tunnel);
        }
    }
}
