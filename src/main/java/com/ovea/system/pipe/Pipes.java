/**
 * Copyright (C) 2011 Ovea <dev@ovea.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ovea.system.pipe;

import com.ovea.system.proc.ProcessPipe;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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

    public static ProcessPipe pipe(Process first, Process next, Process... others) {
        if (first == null) throw new IllegalArgumentException("Missing first process");
        if (next == null) throw new IllegalArgumentException("Missing second process");
        List<Process> processes = new LinkedList<Process>();
        processes.add(first);
        processes.add(next);
        if (others != null && others.length > 0) {
            processes.addAll(Arrays.asList(others));
        }
        return new ProcessPipe(processes);
    }
}
