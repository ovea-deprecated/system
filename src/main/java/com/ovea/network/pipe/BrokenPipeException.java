package com.ovea.network.pipe;

import java.io.IOException;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class BrokenPipeException extends Exception {
    public BrokenPipeException(IOException e) {
        super(e.getMessage(), e);
    }

    public BrokenPipeException(String message) {
        super(message);
    }
}
