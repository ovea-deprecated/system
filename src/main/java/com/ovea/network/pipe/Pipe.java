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
package com.ovea.network.pipe;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public interface Pipe {
    /**
     * Pipe name, used in {@link #toString()}
     */
    String name();

    /**
     * Set a pipe listener to get events
     */
    Pipe listenedBy(PipeListener listener);

    /**
     * Pipe the streams and returns immediatelly the connection handle. If the connection is already made returns the existing handle.
     *
     * @return {@link PipeConnection} object to be able to interrupt or wait for the pipe to finish
     */
    PipeConnection connect();

    boolean isReady();
    boolean isOpened();
    boolean isClosed();
    boolean isBroken();
    boolean isInterrupted();
}
