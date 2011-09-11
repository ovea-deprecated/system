Pipe any streams, socket, process, ...

## Pipes

    // create a pipe
    Pipe pipe = new PipeStream("optional  pipe name", myInputStream, myOutputStream).listenedBy(myOptionalPipeListener);

    // get the connection handle
    PipeConnection connection = pipe.connect();

    // then you can do
    connection.await();
    connection.await(1, SECONDS);
    connection.interrupt();

## Tunnels

    Socket socket1 = new Socket("localhost", 2000);
    Socket socket2 = new Socket("localhost", 2222);
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
