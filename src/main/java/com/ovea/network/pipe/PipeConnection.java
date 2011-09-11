package com.ovea.network.pipe;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public interface PipeConnection {

    /**
     * The connected pipe
     */
    Pipe pipe();

    /**
     * Interrupt the pipe
     */
    void interrupt();

    /**
     * Wait for completion
     *
     * @throws InterruptedException if the wait is interrupted
     * @throws TimeoutException     if the times is up
     * @throws BrokenPipeException  if the pipe is broken
     */
    void await(long time, TimeUnit unit) throws InterruptedException, BrokenPipeException, TimeoutException;

    /**
     * Wait for completion
     *
     * @throws InterruptedException if the wait is interrupted
     * @throws BrokenPipeException  if the pipe is broken
     */
    void await() throws InterruptedException, BrokenPipeException;
}
