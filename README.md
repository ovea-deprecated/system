Pipe any streams, socket, process, ...

## Stream Pipes

    // create a pipe
    Pipe pipe = Pipes.create("optional  pipe name", myInputStream, myOutputStream).listenedBy(myOptionalPipeListener);

    // get the connection handle
    PipeConnection connection = pipe.connect();

    // then you can do
    connection.await();
    connection.await(1, SECONDS);
    connection.interrupt();

## Process Pipes

    ProcessPipe pipe = Pipes.pipe(
            new ProcessBuilder("ls", "-al", "/workspace/ovea/project/pipe/src").start(),
            new ProcessBuilder("cut", "-c", "50-").start(),
            new ProcessBuilder("grep", "-v", "-E", "\"^\\.\\.?$\"").start());
    Pipes.connect("out", pipe.getInputStream(), System.out);
    Pipes.connect("err", pipe.getErrorStream(), System.err);
    int retCode = pipe.waitFor();

## Socket Tunnels

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
