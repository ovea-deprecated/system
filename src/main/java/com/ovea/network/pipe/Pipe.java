package com.ovea.network.pipe;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public interface Pipe {
    /**
     * Pipe name, used in {@link #toString()}, {@link #equals(Object)} and {@link #hashCode()}
     */
    String name();

    /**
     * Set a pipe listener to get events
     */
    Pipe listenedBy(PipeListener listener);

    /**
     * Pipe the streams and returns immediatelly the connection handle. If the connection is already made returns the existing handle.
     *
     * @return {@link PipeConnection} object to be able to interrupt or wait for the pipe to finish
     */
    PipeConnection connect();

    boolean isReady();
    boolean isOpened();
    boolean isClosed();
    boolean isBroken();
    boolean isInterrupted();
}
