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
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class PipeByteChannel extends PipeSkeleton<ReadableByteChannel, WritableByteChannel> {

    public PipeByteChannel(ReadableByteChannel from, WritableByteChannel to) {
        super(from, to);
    }

    public PipeByteChannel(String name, ReadableByteChannel from, WritableByteChannel to) {
        super(name, from, to);
    }

    @Override
    protected void copy(ReadableByteChannel from, WritableByteChannel to) throws IOException, BrokenPipeException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(64 * 1024);
        while (from.read(buffer) != -1) {
            buffer.flip();
            to.write(buffer);
            buffer.compact();
        }
        buffer.flip();
        while (buffer.hasRemaining()) {
            to.write(buffer);
        }
    }
}
