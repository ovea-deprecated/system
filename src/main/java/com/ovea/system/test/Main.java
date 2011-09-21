package com.ovea.system.test;

import com.ovea.system.proc.FutureProcess;
import com.ovea.system.trace.Trace;
import com.ovea.system.util.ProcUtils;
import com.sun.jna.Platform;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class Main {

    public static void main(String... args) throws Exception {
        if (args.length == 1 && args[0].equals("-trace")) {
            Trace.main(Main.class.getName());
        } else {
            boolean win = Platform.isWindows() || Platform.isWindowsCE();
            System.out.println(" - PID = " + ProcUtils.getCurrentPID());
            System.out.println(" - Detected OS = " + (win ? "Windows" : "Unix-like"));

            FutureProcess process = launch(win ? "tasklist.exe" : "ps");
            try {
                process.get(1, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                process.cancel(true);
            }
            if (!process.isCancelled())
                process.get();

            process = launch(win ? "cmd.exe" : "sh");
            try {
                process.get(1, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                ProcUtils.kill(process.process());
            }
            process.get();

            process = launch(win ? "cmd.exe" : "sh");
            try {
                process.get(1, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                ProcUtils.kill(process.getPID());
            }
            process.get();
        }
    }

    private static FutureProcess launch(String proc) throws Exception {
        System.out.println(" - Launching " + proc + "...");
        FutureProcess process = new FutureProcess(new ProcessBuilder(proc).start());
        Thread.sleep(1000);
        System.out.println(" - Child PID = " + process.getPID());
        return process;
    }
}
