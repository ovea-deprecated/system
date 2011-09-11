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
package com.ovea.network.tunnel;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class OnceTunnelListener implements TunnelListener {

    private final AtomicReference<TunnelListener> listener;

    public OnceTunnelListener(TunnelListener listener) {
        this.listener = new AtomicReference<TunnelListener>(listener);
    }

    @Override
    public void onConnect(Tunnel tunnel) {
        TunnelListener t = listener.get();
        if (t != null) {
            t.onConnect(tunnel);
        }
    }

    @Override
    public void onClose(Tunnel tunnel) {
        TunnelListener tl = listener.getAndSet(null);
        if (tl != null) {
            tl.onClose(tunnel);
        }
    }

    @Override
    public void onBroken(Tunnel tunnel, BrokenTunnelException e) {
        TunnelListener tl = listener.getAndSet(null);
        if (tl != null) {
            tl.onBroken(tunnel, e);
        }
    }

    @Override
    public void onInterrupt(Tunnel tunnel) {
        TunnelListener tl = listener.getAndSet(null);
        if (tl != null) {
            tl.onInterrupt(tunnel);
        }
    }
}
