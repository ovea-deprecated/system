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
    public void onError(Pipe pipe, BrokenPipeException e) {
    }

    @Override
    public void onInterrrupt(Pipe pipe) {
    }

    @Override
    public void onTimeout(Pipe pipe) {
    }
}
