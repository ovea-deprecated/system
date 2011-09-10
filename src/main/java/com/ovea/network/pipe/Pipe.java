package com.ovea.network.pipe;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public interface Pipe {
    /**
     * Pipe name, used in {@link #toString()}, {@link #equals(Object)} and {@link #hashCode()}
     */
    String name();

    /**
     * True if the pipe is connected
     */
    boolean isConnected();

    /**
     * Set a pipe listener to get events
     */
    Pipe listenedBy(PipeListener listener);

    /**
     * Pipe the streams and wait for completion
     *
     * @throws BrokenPipeException  if the pipe is broken or cannot be connected
     * @throws InterruptedException if the current thread was interrupted while waiting
     */
    void connectAndWait() throws BrokenPipeException, InterruptedException;

    /**
     * Pipe the streams and wait for completion for a maximum amount of time
     *
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws TimeoutException     if the wait timed out
     * @throws BrokenPipeException  if the pipe is broken or cannot be connected
     */
    void connectAndWait(long time, TimeUnit unit) throws TimeoutException, InterruptedException, BrokenPipeException;

    /**
     * Pipe the streams and returns immediatelly
     *
     * @return {@link PipeHandle} object to be able to interrupt or wait for the pipe to finish
     * @throws PipeStateException if the pipe is not ready
     */
    //PipeHandle connect() throws PipeStateException;


}
