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
    void onError(Pipe pipe, BrokenPipeException e);

    /**
     * Called if the pipe is interrupted either if the thread is interrupted or by a call to {@link com.ovea.network.pipe.PipeHandle#interrupt()}
     */
    void onInterrrupt(Pipe pipe);

    /**
     * Called if the pipe was closed to due a wait timeout for a call to {@link Pipe#connectAndWait(long, java.util.concurrent.TimeUnit)}
     */
    void onTimeout(Pipe pipe);
}
