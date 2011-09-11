package com.ovea.network.pipe;

import com.ovea.network.tunnel.BrokenTunnelException;
import com.ovea.network.tunnel.Tunnel;
import com.ovea.network.tunnel.TunnelListener;

import java.io.IOException;
import java.net.Socket;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class SocketTunnelMain {
    public static void main(String[] args) throws IOException {
        // start 2 netcat daemons first before running this class
        final Socket socket1 = new Socket("localhost", 2000);
        final Socket socket2 = new Socket("localhost", 2222);
        Tunnel tunnel = Tunnel.connect(socket1, socket2, new TunnelListener() {
            @Override
            public void onConnect(Tunnel tunnel) {
                System.out.println("onConnect - " + tunnel);
            }

            @Override
            public void onClose(Tunnel tunnel) {
                System.out.println("onClose - " + tunnel);
            }

            @Override
            public void onBroken(Tunnel tunnel, BrokenTunnelException e) {
                System.out.println("onBroken - " + tunnel);
            }

            @Override
            public void onInterrupt(Tunnel tunnel) {
                System.out.println("onInterrupt - " + tunnel);
            }
        });
        try {
            tunnel.await();
        } catch (InterruptedException e) {
            e.printStackTrace(System.out);
        } catch (BrokenTunnelException e) {
            e.printStackTrace(System.out);
        }
    }
}
