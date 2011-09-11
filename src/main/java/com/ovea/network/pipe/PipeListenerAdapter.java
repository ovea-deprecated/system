package com.ovea.network.pipe;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class PipeListenerAdapter implements PipeListener {
    @Override
    public void onClose(Pipe pipe) {
    }

    @Override
    public void onConnect(Pipe pipe) {
    }

    @Override
    public void onBroken(Pipe pipe, BrokenPipeException e) {
    }

    @Override
    public void onInterrupt(Pipe pipe) {
    }
}
