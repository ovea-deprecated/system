package com.ovea.network.util;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
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

    public static void kill() {
        // SIGTERM is signal #15.
        //TODO
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

    private static final class Killer extends Thread {

        @Override
        public void run() {
            
        }

        
    }
}
