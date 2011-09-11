package com.ovea.network.pipe;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public interface PipeListener {

    /**
     * Called when a pipe is closed correctly after the stream ends
     */
    void onClose(Pipe pipe);

    /**
     * Called when the pipe is connected
     */
    void onConnect(Pipe pipe);

    /**
     * Called if a stream is broken
     */
    void onBroken(Pipe pipe, BrokenPipeException e);

    /**
     * Called if the pipe is interrupted either if the thread is interrupted or by a call to {@link PipeConnection#interrupt()}
     */
    void onInterrupt(Pipe pipe);

}
