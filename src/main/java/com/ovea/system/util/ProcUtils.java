package com.ovea.system.util;

import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;
import org.hyperic.jni.ArchNotSupportedException;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarLoader;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class ProcUtils {

    private ProcUtils() {
    }

    public static long getPID(Process process) {
        String cName = process.getClass().getName();
        if (cName.equals("java.lang.UNIXProcess")) {
            /* get the PID on unix/linux systems */
            try {
                Field f = process.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                return f.getInt(process);
            } catch (Throwable e) {
                throw new IllegalStateException("Unable to recover PID from Unix process" + process);
            }
        } else if (cName.equals("java.lang.ProcessImpl") || cName.equals("java.lang.Win32Process")) {
            /* determine the pid on windows plattforms */
            try {
                Field f = process.getClass().getDeclaredField("handle");
                f.setAccessible(true);
                long handl = f.getLong(process);
                WinNT.HANDLE handle = new WinNT.HANDLE();
                handle.setPointer(Pointer.createConstant(handl));
                return Kernel32.INSTANCE.GetProcessId(handle);
            } catch (Throwable e) {
                throw new IllegalStateException("Unable to recover PID from Windows process" + process);
            }
        } else {
            throw new IllegalArgumentException("Process type not supported: " + process.getClass().getName());
        }
    }

    public static long getCurrentPID() {
        // lazy instanciation
        return RuntimePID.get();
    }

    public static void kill(Process process) {
        long pid = getPID(process);
        String cName = process.getClass().getName();
        if (cName.equals("java.lang.UNIXProcess")) {
            /* get the PID on unix/linux systems */
            try {
                Method destroyProcess = process.getClass().getDeclaredMethod("destroyProcess", int.class);
                destroyProcess.setAccessible(true);
                destroyProcess.invoke(null, (int) pid);
            } catch (Throwable e) {
                kill(pid);
            }
        } else if (cName.equals("java.lang.ProcessImpl") || cName.equals("java.lang.Win32Process")) {
            /* determine the pid on windows plattforms */
            try {
                Field f = process.getClass().getDeclaredField("handle");
                f.setAccessible(true);
                Method destroyProcess = process.getClass().getDeclaredMethod("terminateProcess", long.class);
                destroyProcess.setAccessible(true);
                destroyProcess.invoke(null, f.getLong(process));
            } catch (Throwable e) {
                kill(pid);
            }
        } else {
            throw new IllegalArgumentException("Process type not supported: " + process.getClass().getName());
        }
    }

    public static void kill(long pid) {
        if (Platform.isWindows() || Platform.isWindowsCE()) {
            WinNT.HANDLE handle = Kernel32.INSTANCE.OpenProcess(0x0001, true, (int) pid);
            Kernel32.INSTANCE.TerminateProcess(handle, 0);
        } else {
            UnixKiller.kill(pid);
        }
    }

    private static final class RuntimePID {
        private static final long pid;

        static {
            /* tested on: */
            /* - windows xp sp 2, java 1.5.0_13 */
            /* - mac os x 10.4.10, java 1.5.0 */
            /* - debian linux, java 1.5.0_13 */
            /* all return pid@host, e.g 2204@antonius */
            String processName = ManagementFactory.getRuntimeMXBean().getName();
            Matcher matcher = Pattern.compile("^([0-9]+)@.+$", Pattern.CASE_INSENSITIVE).matcher(processName);
            if (matcher.matches()) {
                pid = Long.parseLong(matcher.group(1));
            } else {
                throw new IllegalStateException("Unable to recover PID from process identifier: " + processName);
            }
        }

        public static long get() {
            return pid;
        }
    }

    private static final class UnixKiller {

        private static final String SIGAR_VERSION = "-" + System.getProperty("ovea.system.sigar.version", "1.6.4");
        private static final Sigar SIGAR;

        static {
            try {
                File tmp = new File(System.getProperty("java.io.tmpdir"), "ovea-system-sigar");
                if (!tmp.exists()) {
                    tmp.mkdirs();
                }
                SigarLoader loader = new SigarLoader(Sigar.class);
                File lib = new File(tmp, loader.getLibraryName());
                File lockFile = new File(lib + ".lock");
                FileChannel channel = null;
                FileLock lock = null;

                try {
                    channel = new RandomAccessFile(lockFile, "rw").getChannel();
                    lock = channel.lock();
                    if (!lib.exists()) {
                        InputStream in = null;
                        OutputStream out = null;
                        try {
                            String name = loader.getLibName();
                            if (name == null) {
                                name = loader.getDefaultLibName();
                            }
                            name = SigarLoader.getLibraryPrefix() + name + SIGAR_VERSION + SigarLoader.getLibraryExtension();
                            in = loader.getClassLoader().getResourceAsStream(name);
                            if (in == null) {
                                throw new IllegalStateException("SIGAR library " + name + " not found in classpath");
                            }
                            in = new BufferedInputStream(in);
                            out = new BufferedOutputStream(new FileOutputStream(lib));
                            byte[] buffer = new byte[8192];
                            int c;
                            while ((c = in.read(buffer)) != -1) {
                                out.write(buffer, 0, c);
                            }
                        } finally {
                            if (out != null)
                                try {
                                    out.close();
                                } catch (IOException ignored) {
                                }
                            if (in != null)
                                try {
                                    in.close();
                                } catch (IOException ignored) {
                                }
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                } finally {
                    if (lock != null)
                        try {
                            lock.release();
                        } catch (IOException ignored) {
                        }
                    if (channel != null)
                        try {
                            channel.close();
                        } catch (IOException ignored) {
                        }
                    lockFile.delete();
                }
                System.setProperty(loader.getPackageName() + ".path", tmp.getAbsolutePath());
                SIGAR = new Sigar();
                Sigar.load();
            } catch (ArchNotSupportedException e) {
                throw new RuntimeException(e.getMessage(), e);
            } catch (SigarException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    SIGAR.close();
                }
            });
        }

        public static void kill(long pid) {
            int signal = Sigar.getSigNum("SIGTERM");
            try {
                SIGAR.kill(pid, signal);
            } catch (SigarException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
}
