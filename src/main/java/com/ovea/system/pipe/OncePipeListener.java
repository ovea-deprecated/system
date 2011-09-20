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

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class OncePipeListener implements PipeListener {

    private final AtomicReference<PipeListener> listener;

    public OncePipeListener(PipeListener listener) {
        this.listener = new AtomicReference<PipeListener>(listener);
    }

    @Override
    public void onConnect(Pipe pipe) {
        PipeListener t = listener.get();
        if (t != null) {
            t.onConnect(pipe);
        }
    }

    @Override
    public void onClose(Pipe pipe) {
        PipeListener tl = listener.getAndSet(null);
        if (tl != null) {
            tl.onClose(pipe);
        }
    }

    @Override
    public void onBroken(Pipe pipe, BrokenPipeException e) {
        PipeListener tl = listener.getAndSet(null);
        if (tl != null) {
            tl.onBroken(pipe, e);
        }
    }

    @Override
    public void onInterrupt(Pipe pipe) {
        PipeListener tl = listener.getAndSet(null);
        if (tl != null) {
            tl.onInterrupt(pipe);
        }
    }
}
