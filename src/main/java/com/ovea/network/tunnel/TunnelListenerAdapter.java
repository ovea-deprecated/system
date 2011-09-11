package com.ovea.network.tunnel;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class TunnelListenerAdapter implements TunnelListener {
    @Override
    public void onConnect(Tunnel tunnel) {
    }

    @Override
    public void onClose(Tunnel tunnel) {
    }

    @Override
    public void onBroken(Tunnel tunnel, BrokenTunnelException e) {
    }

    @Override
    public void onInterrupt(Tunnel tunnel) {
    }
}
