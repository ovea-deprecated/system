package com.ovea.network;

import com.ovea.network.pipe.Pipes;

import java.io.IOException;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class ProcessPipeMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        Process end = Pipes.pipe(
                new ProcessBuilder("C:\\cygwin\\bin\\ls.exe", "-al", "/cygdrive/d/kha/workspace/ovea/project/pipe/src").start(),
                new ProcessBuilder("C:\\cygwin\\bin\\cut.exe", "-cd", "50-").start(),
                new ProcessBuilder("C:\\cygwin\\bin\\grep.exe", "-v", "-E", "\"^\\.\\.?$\"").start());
        Pipes.connect(end.getInputStream(), System.out);
        Pipes.connect(end.getErrorStream(), System.err);
        end.waitFor();
    }
}
