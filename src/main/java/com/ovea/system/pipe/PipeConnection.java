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

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public interface PipeConnection {

    /**
     * The connected pipe
     */
    Pipe pipe();

    /**
     * Interrupt the pipe
     */
    void interrupt();

    /**
     * Wait for completion
     *
     * @throws InterruptedException if the wait is interrupted
     * @throws TimeoutException     if the times is up
     * @throws BrokenPipeException  if the pipe is broken
     */
    void await(long time, TimeUnit unit) throws InterruptedException, BrokenPipeException, TimeoutException;

    /**
     * Wait for completion
     *
     * @throws InterruptedException if the wait is interrupted
     * @throws BrokenPipeException  if the pipe is broken
     */
    void await() throws InterruptedException, BrokenPipeException;
}
