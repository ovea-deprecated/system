package com.ovea.network.pipe;

import com.mycila.junit.concurrent.ConcurrentJunitRunner;
import com.mycila.junit.rule.TimeRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

import java.io.ByteArrayOutputStream;
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
@RunWith(ConcurrentJunitRunner.class)
public final class PipeStreamTest {

    @Test
    public void test_identity() throws Exception {
        Pipe pipe1 = new PipeStream("1", new PipedInputStream(), new PipedOutputStream());
        Pipe pipe2 = new PipeStream("1", new PipedInputStream(), new PipedOutputStream());

        assertEquals(pipe1, pipe2);
        assertEquals(pipe1.hashCode(), pipe2.hashCode());
        assertEquals(pipe1.name(), pipe2.name());
        assertEquals(pipe1.toString(), pipe2.toString());

        assertEquals(pipe1.connect(), pipe2.connect());
        assertEquals(pipe1.connect().hashCode(), pipe2.hashCode());
        assertEquals(pipe1.connect().toString(), pipe2.connect().toString());

        pipe1.connect().interrupt();
        pipe2.connect().interrupt();
    }

    @Test
    public void test_connect_handle() throws Exception {
        writer.start();
        PipeConnection connection = pipe.connect();
        try {
            connection.await();
            verify(listener, times(0)).onInterrupt(eq(pipe));
            verify(listener, times(1)).onClose(eq(pipe));
            verify(listener, times(1)).onConnect(eq(pipe));
            verify(listener, times(0)).onBroken(eq(pipe), Matchers.<BrokenPipeException>any());
        } catch (InterruptedException e) {
            fail(e.getMessage());
        } catch (BrokenPipeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void test_connect_handle_double() throws Exception {
        writer.start();
        PipeConnection connection = pipe.connect();
        try {
            connection.await();
            connection.await();
            verify(listener, times(0)).onInterrupt(eq(pipe));
            verify(listener, times(1)).onClose(eq(pipe));
            verify(listener, times(1)).onConnect(eq(pipe));
            verify(listener, times(0)).onBroken(eq(pipe), Matchers.<BrokenPipeException>any());
        } catch (InterruptedException e) {
            fail(e.getMessage());
        } catch (BrokenPipeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void test_connect_handle_timeout_ok() throws Exception {
        writer.start();
        PipeConnection connection = pipe.connect();
        try {
            connection.await(5, TimeUnit.SECONDS);
            verify(listener, times(0)).onInterrupt(eq(pipe));
            verify(listener, times(1)).onClose(eq(pipe));
            verify(listener, times(1)).onConnect(eq(pipe));
            verify(listener, times(0)).onBroken(eq(pipe), Matchers.<BrokenPipeException>any());
        } catch (TimeoutException e) {
            fail(e.getMessage());
        } catch (InterruptedException e) {
            fail(e.getMessage());
        } catch (BrokenPipeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void test_connect_handle_timeout_ok_double() throws Exception {
        writer.start();
        PipeConnection connection = pipe.connect();
        try {
            connection.await(5, TimeUnit.SECONDS);
            connection.await(5, TimeUnit.SECONDS);
            verify(listener, times(0)).onInterrupt(eq(pipe));
            verify(listener, times(1)).onClose(eq(pipe));
            verify(listener, times(1)).onConnect(eq(pipe));
            verify(listener, times(0)).onBroken(eq(pipe), Matchers.<BrokenPipeException>any());
        } catch (TimeoutException e) {
            fail(e.getMessage());
        } catch (InterruptedException e) {
            fail(e.getMessage());
        } catch (BrokenPipeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void test_connect_handle_timedout() throws Exception {
        writer.start();
        try {
            pipe.connect().await(1, TimeUnit.SECONDS);
            fail();
        } catch (TimeoutException e) {
            verify(listener, times(0)).onInterrupt(eq(pipe));
            verify(listener, times(0)).onClose(eq(pipe));
            verify(listener, times(1)).onConnect(eq(pipe));
            verify(listener, times(0)).onBroken(eq(pipe), Matchers.<BrokenPipeException>any());
        } catch (InterruptedException e) {
            fail(e.getMessage());
        } catch (BrokenPipeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void test_connect_handle_timedout_double() throws Exception {
        writer.start();
        try {
            pipe.connect().await(500, TimeUnit.MILLISECONDS);
            fail();
        } catch (TimeoutException e) {
            try {
                pipe.connect().await(500, TimeUnit.MILLISECONDS);
                fail();
            } catch (TimeoutException e1) {
                verify(listener, times(0)).onInterrupt(eq(pipe));
                verify(listener, times(0)).onClose(eq(pipe));
                verify(listener, times(1)).onConnect(eq(pipe));
                verify(listener, times(0)).onBroken(eq(pipe), Matchers.<BrokenPipeException>any());
            } catch (Exception e1) {
                fail();
            }
        } catch (InterruptedException e) {
            fail(e.getMessage());
        } catch (BrokenPipeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void test_connect_handle_interrupted() throws Exception {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    pipe.connect().await(5, TimeUnit.SECONDS);
                    fail();
                } catch (TimeoutException e) {
                    fail(e.getMessage());
                } catch (InterruptedException e) {
                    verify(listener, times(1)).onInterrupt(eq(pipe));
                    verify(listener, times(0)).onClose(eq(pipe));
                    verify(listener, times(1)).onConnect(eq(pipe));
                    verify(listener, times(0)).onBroken(eq(pipe), Matchers.<BrokenPipeException>any());
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

    @Test
    public void test_connect_handle_interrupt() throws Exception {
        writer.start();
        PipeConnection connection = pipe.connect();
        Thread.sleep(1000);
        connection.interrupt();
        verify(listener, times(1)).onInterrupt(eq(pipe));
        verify(listener, times(0)).onClose(eq(pipe));
        verify(listener, times(1)).onConnect(eq(pipe));
        verify(listener, times(0)).onBroken(eq(pipe), Matchers.<BrokenPipeException>any());
    }

    @Test
    public void test_connect_handle_interrupt_double() throws Exception {
        writer.start();
        PipeConnection connection = pipe.connect();
        Thread.sleep(1000);
        connection.interrupt();
        connection.interrupt();
        verify(listener, times(1)).onInterrupt(eq(pipe));
        verify(listener, times(0)).onClose(eq(pipe));
        verify(listener, times(1)).onConnect(eq(pipe));
        verify(listener, times(0)).onBroken(eq(pipe), Matchers.<BrokenPipeException>any());
    }

    /////////////////////////////////////

    PipedOutputStream out = new PipedOutputStream();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PipeListener listener = mock(PipeListener.class);
    Pipe pipe;
    Thread writer = new Thread() {
        @Override
        public void run() {
            try {
                for (int i = 0; i < 4 && !Thread.interrupted(); i++) {
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

    @Rule
    public TimeRule timeRule = new TimeRule();

    @Before
    public void pipeSetup() throws Exception {
        pipe = new PipeStream(new PipedInputStream(out), baos).listenedBy(new PipeListeners(listener));
        assertFalse(pipe.isOpened());
    }

    @After
    public void pipeVerif() throws Exception {
        if (writer.isAlive())
            writer.join();
        assertFalse(pipe.isOpened());
        if (pipe.isClosed()) {
            assertEquals("Hello world !Hello world !Hello world !Hello world !", new String(baos.toByteArray()));
        }
    }

    public static void main(String[] args) {
        JUnitCore.main(PipeStreamTest.class.getName());
    }
}