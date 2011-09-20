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
import java.io.Reader;
import java.io.Writer;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class PipeCharacterStream extends PipeSkeleton<Reader, Writer> {

    public PipeCharacterStream(Reader from, Writer to) {
        super(from, to);
    }

    public PipeCharacterStream(String name, Reader from, Writer to) {
        super(name, from, to);
    }

    @Override
    protected void copy(Reader from, Writer to) throws IOException, BrokenPipeException {
        char[] buffer = new char[8192];
        int len;
        while (canCopy() && (len = from.read(buffer)) != -1) {
            to.write(buffer, 0, len);
        }
    }
}
