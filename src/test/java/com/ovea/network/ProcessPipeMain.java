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
package com.ovea.network;

import com.ovea.network.pipe.Pipes;
import com.ovea.network.proc.ProcessPipe;

import java.io.IOException;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class ProcessPipeMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        ProcessPipe pipe = Pipes.pipe(
                new ProcessBuilder("C:\\cygwin\\bin\\ls.exe", "-al", "/cygdrive/d/kha/workspace/ovea/project/pipe/src").start(),
                new ProcessBuilder("C:\\cygwin\\bin\\cut.exe", "-cd", "50-").start(),
                new ProcessBuilder("C:\\cygwin\\bin\\grep.exe", "-v", "-Ed", "\"^\\.\\.?$\"").start());
        Pipes.connect("out", pipe.getInputStream(), System.out);
        Pipes.connect("err", pipe.getErrorStream(), System.err);
        pipe.waitFor();
    }
}
