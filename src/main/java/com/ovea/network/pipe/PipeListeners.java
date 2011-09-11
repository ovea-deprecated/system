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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class PipeListeners implements PipeListener {

    private final List<PipeListener> listeners = new CopyOnWriteArrayList<PipeListener>();

    public PipeListeners(PipeListener... listeners) {
        this(Arrays.asList(listeners));
    }

    public PipeListeners(Iterable<? extends PipeListener> listeners) {
        add(listeners);
    }

    public void add(PipeListener listener) {
        listeners.add(listener);
    }

    public void add(PipeListener... listeners) {
        add(Arrays.asList(listeners));
    }

    public void add(Iterable<? extends PipeListener> listeners) {
        for (PipeListener listener : listeners) {
            this.listeners.add(listener);
        }
    }

    @Override
    public void onClose(Pipe pipe) {
        for (PipeListener listener : listeners) {
            listener.onClose(pipe);
        }
    }

    @Override
    public void onConnect(Pipe pipe) {
        for (PipeListener listener : listeners) {
            listener.onConnect(pipe);
        }
    }

    @Override
    public void onBroken(Pipe pipe, BrokenPipeException e) {
        for (PipeListener listener : listeners) {
            listener.onBroken(pipe, e);
        }
    }

    @Override
    public void onInterrupt(Pipe pipe) {
        for (PipeListener listener : listeners) {
            listener.onInterrupt(pipe);
        }
    }
}
