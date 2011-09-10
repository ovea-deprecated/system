package com.ovea.network.pipe;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Matchers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.BitSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@RunWith(JUnit4.class)
public final class PipeStreamTest {

    @Test
    public void test_connectAndWait() throws Exception {
        writer.start();
        try {
            pipe.connectAndWait();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        } catch (BrokenPipeException e) {
            fail(e.getMessage());
        }
        verify(listener, times(0)).onTimeout(eq(pipe));
        verify(listener, times(0)).onInterrrupt(eq(pipe));
        verify(listener, times(1)).onClose(eq(pipe));
        verify(listener, times(1)).onConnect(eq(pipe));
        verify(listener, times(0)).onError(eq(pipe), Matchers.<BrokenPipeException>any());
    }

    @Test
    public void test_connectAndWait_interrupted() throws Exception {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    pipe.connectAndWait();
                    fail();
                } catch (BrokenPipeException e) {
                    fail();
                } catch (InterruptedException e) {
                    verify(listener, times(0)).onTimeout(eq(pipe));
                    verify(listener, times(1)).onInterrrupt(eq(pipe));
                    verify(listener, times(0)).onClose(eq(pipe));
                    verify(listener, times(1)).onConnect(eq(pipe));
                    verify(listener, times(0)).onError(eq(pipe), Matchers.<BrokenPipeException>any());
                }
            }
        };
        writer.start();
        t.start();
        Thread.sleep(500);
        t.interrupt();
        t.join();
    }

    @Test
    public void test_connectAndWait_timeout_ok() throws Exception {
        writer.start();
        try {
            pipe.connectAndWait(5, TimeUnit.SECONDS);
            verify(listener, times(0)).onTimeout(eq(pipe));
            verify(listener, times(0)).onInterrrupt(eq(pipe));
            verify(listener, times(1)).onClose(eq(pipe));
            verify(listener, times(1)).onConnect(eq(pipe));
            verify(listener, times(0)).onError(eq(pipe), Matchers.<BrokenPipeException>any());
        } catch (TimeoutException e) {
            fail(e.getMessage());
        } catch (InterruptedException e) {
            fail(e.getMessage());
        } catch (BrokenPipeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void test_connectAndWait_timeout_err() throws Exception {
        writer.start();
        try {
            pipe.connectAndWait(500, TimeUnit.MILLISECONDS);
            fail();
        } catch (TimeoutException e) {
            verify(listener, times(1)).onTimeout(eq(pipe));
            verify(listener, times(0)).onInterrrupt(eq(pipe));
            verify(listener, times(0)).onClose(eq(pipe));
            verify(listener, times(1)).onConnect(eq(pipe));
            verify(listener, times(0)).onError(eq(pipe), Matchers.<BrokenPipeException>any());
        } catch (InterruptedException e) {
            fail(e.getMessage());
        } catch (BrokenPipeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void test_connectAndWait_timeout_interrupted() throws Exception {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    pipe.connectAndWait(5, TimeUnit.SECONDS);
                    fail();
                } catch (TimeoutException e) {
                    fail(e.getMessage());
                } catch (InterruptedException e) {
                    verify(listener, times(0)).onTimeout(eq(pipe));
                    verify(listener, times(1)).onInterrrupt(eq(pipe));
                    verify(listener, times(0)).onClose(eq(pipe));
                    verify(listener, times(1)).onConnect(eq(pipe));
                    verify(listener, times(0)).onError(eq(pipe), Matchers.<BrokenPipeException>any());
                } catch (BrokenPipeException e) {
                    fail(e.getMessage());
                }
            }
        };
        writer.start();
        t.start();
        Thread.sleep(500);
        t.interrupt();
        t.join();
    }

    /*@Test
    public void test_async() throws Exception {
        PipedOutputStream pos = new PipedOutputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Future a;a.get()
        Pipe pipe = new PipeStream(new PipedInputStream(pos), baos);
        pipe.setListener(new PipeListener() {
            @Override
            public void onClose(Pipe pipe) {
                System.out.println("onClose");
            }

            @Override
            public void onConnect(Pipe pipe) {
                System.out.println("onConnect");
            }

            @Override
            public void onError(Pipe pipe, IOException e) {
                System.out.println("onError");
            }
        });

        assertTrue(pipe.isReady());
        assertFalse(pipe.isClosed());
        assertFalse(pipe.isConnected());

        pipe.connectAndWait();

        assertFalse(pipe.isReady());
        assertTrue(pipe.isClosed());
        assertFalse(pipe.isConnected());
        assertEquals("Hello world !", new String(baos.toByteArray()));
    }*/

    PipedOutputStream out = new PipedOutputStream();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PipeListener listener = mock(PipeListener.class);
    Pipe pipe;
    Thread writer = new Thread() {
        @Override
        public void run() {
            try {
                for (int i = 0; i < 4 && !Thread.currentThread().isInterrupted(); i++) {
                    out.write("Hello world !".getBytes());
                    Thread.sleep(500);
                }
                out.close();
            } catch (Exception e) {
                e.printStackTrace(System.out);
                Thread.currentThread().interrupt();
            }
        }
    };
    BitSet states = new BitSet(4);

    @Before
    public void pipeSetup() throws Exception {
        pipe = new PipeStream(new PipedInputStream(out), baos).listenedBy(new PipeListeners(listener, new PipeListener() {
            @Override
            public void onClose(Pipe pipe) {
                states.set(0);
            }

            @Override
            public void onConnect(Pipe pipe) {
                states.set(1);
            }

            @Override
            public void onError(Pipe pipe, BrokenPipeException e) {
                states.set(2);
            }

            @Override
            public void onInterrrupt(Pipe pipe) {
                states.set(3);
            }

            @Override
            public void onTimeout(Pipe pipe) {
                states.set(4);
            }
        }));
        assertFalse(pipe.isConnected());
    }

    @After
    public void pipeVerif() throws Exception {
        assertFalse(pipe.isConnected());
        if (states.get(0)) {
            assertEquals("Hello world !Hello world !Hello world !Hello world !", new String(baos.toByteArray()));
        }
        if (writer.isAlive())
            writer.join();
    }

}