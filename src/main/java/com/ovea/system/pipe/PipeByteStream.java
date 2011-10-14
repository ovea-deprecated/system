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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class PipeByteStream extends PipeSkeleton<InputStream, OutputStream> {

    private final int bufferSize;

    public PipeByteStream(InputStream from, OutputStream to) {
        super(from, to);
        this.bufferSize = 8192;
    }

    public PipeByteStream(String name, InputStream from, OutputStream to, int bufferSize) {
        super(name, from, to);
        this.bufferSize = bufferSize;
    }

    @Override
    protected void copy(InputStream from, OutputStream to) throws IOException, BrokenPipeException {
        byte[] buffer = new byte[bufferSize];
        int len;
        while (canCopy() && (len = from.read(buffer)) != -1) {
            to.write(buffer, 0, len);
        }
    }
}
