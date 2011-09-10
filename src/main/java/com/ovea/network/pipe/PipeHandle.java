package com.ovea.network.pipe;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public interface PipeHandle {

    /**
     * The connected pipe
     */
    Pipe pipe();

    /**
     * Interrupt the pipe
     */
    void interrupt();

}
