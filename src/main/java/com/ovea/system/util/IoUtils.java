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
package com.ovea.system.util;

import java.io.*;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class IoUtils {

    private IoUtils() {
    }

    public static void close(Closeable... closeables) {
        if (closeables != null && closeables.length > 0) {
            for (Closeable closeable : closeables) {
                if (closeable != null) {
                    try {
                        closeable.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }

    public static InputStream uncloseable(final InputStream in) {
        return new InputStream() {
            @Override
            public int read() throws IOException {
                return in.read();
            }

            @Override
            public void close() throws IOException {
                // nothing
            }
        };
    }

    public static OutputStream uncloseable(final OutputStream stream) {
        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                stream.write(b);
            }

            @Override
            public void close() throws IOException {
                // nothing
            }
        };
    }

    public static Reader uncloseable(final Reader stream) {
        return new Reader() {
            @Override
            public int read(char[] cbuf, int off, int len) throws IOException {
                return stream.read(cbuf, off, len);
            }

            @Override
            public void close() throws IOException {
                // nothing
            }
        };
    }

    public static Writer uncloseable(final Writer stream) {
        return new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                stream.write(cbuf, off, len);
            }

            @Override
            public void flush() throws IOException {
                stream.flush();
            }

            @Override
            public void close() throws IOException {
                // nothing
            }
        };
    }

}
