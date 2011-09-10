package com.ovea.network.pipe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class PipeStream extends PipeSkeleton<InputStream, OutputStream> {

    public PipeStream(InputStream from, OutputStream to) {
        super(from, to);
    }

    public PipeStream(String name, InputStream from, OutputStream to) {
        super(name, from, to);
    }

    @Override
    protected void copy(InputStream from, OutputStream to) throws IOException, BrokenPipeException {
        byte[] buffer = new byte[5];
        int len;
        while (!Thread.currentThread().isInterrupted() && isConnected() && (len = from.read(buffer)) != -1) {
            to.write(buffer, 0, len);
        }
    }
}
