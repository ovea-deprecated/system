package com.ovea.network;

import com.ovea.network.pipe.Pipes;
import com.ovea.network.proc.FutureProcess;
import com.ovea.network.util.ProcUtils;
import com.sun.jna.Platform;
import org.hyperic.jni.ArchLoaderException;
import org.hyperic.jni.ArchNotSupportedException;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public final class Main {
    public static void main(String[] args) throws IOException, InterruptedException, ArchLoaderException, ArchNotSupportedException {
        System.out.println(" - PID = " + ProcUtils.getCurrentPID());

        Process process;
        if (Platform.isWindows() || Platform.isWindowsCE()) {
            System.out.println(" - Detected OS = Windows");
            System.out.println(" - Launching 'tasklist.exe'...");
            process = new ProcessBuilder("tasklist.exe").start();
        } else {
            System.out.println(" - Detected OS = Unix based");
            System.out.println(" - Launching 'ps'...");
            process = new ProcessBuilder("ps").start();
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Pipes.connect("out", process.getInputStream(), baos);
        Pipes.connect("err", process.getErrorStream(), baos);
        System.out.println(" - Child PID = " + ProcUtils.getPID(process));
        process.waitFor();
        System.out.println(new String(baos.toByteArray()));

        process = new ProcessBuilder("cmd.exe").start();
        Thread.sleep(1000);
        new FutureProcess(process).cancel(true);

        SigarLoader loader = new SigarLoader(Sigar.class);
        System.out.println(loader.findJarPath(loader.getLibName()));
        System.out.println(loader.getLibraryName());
        System.out.println(loader.getPackageName() + ".path");

        System.out.println(System.getProperty("java.library.path"));
        System.out.println(Sigar.getSigNum("SIGINT "));
    }
}
