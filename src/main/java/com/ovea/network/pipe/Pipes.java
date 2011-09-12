package com.ovea.network.pipe;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class Pipes {
    private Pipes() {
    }

    /* create */

    public static Pipe create(InputStream in, OutputStream out) {
        return new PipeByteStream(in, out);
    }

    public static Pipe create(String name, InputStream in, OutputStream out) {
        return new PipeByteStream(name, in, out);
    }

    public static Pipe create(Reader in, Writer out) {
        return new PipeCharacterStream(in, out);
    }

    public static Pipe create(String name, Reader in, Writer out) {
        return new PipeCharacterStream(name, in, out);
    }

    public static Pipe create(ReadableByteChannel in, WritableByteChannel out) {
        return new PipeByteChannel(in, out);
    }

    public static Pipe create(String name, ReadableByteChannel in, WritableByteChannel out) {
        return new PipeByteChannel(name, in, out);
    }

    /* connects*/

    public static PipeConnection connect(InputStream in, OutputStream out) {
        return create(in, out).connect();
    }

    public static PipeConnection connect(String name, InputStream in, OutputStream out) {
        return create(name, in, out).connect();
    }

    public static PipeConnection connect(Reader in, Writer out) {
        return create(in, out).connect();
    }

    public static PipeConnection connect(String name, Reader in, Writer out) {
        return create(name, in, out).connect();
    }

    public static PipeConnection connect(ReadableByteChannel in, WritableByteChannel out) {
        return create(in, out).connect();
    }

    public static PipeConnection connect(String name, ReadableByteChannel in, WritableByteChannel out) {
        return create(name, in, out).connect();
    }

    /* process */

    public static ProcessPipe pipe(Process left, Process right, Process... suite) {
        return new ProcessPipe(left, right, suite);
    }
}
