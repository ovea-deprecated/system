package com.ovea.network;

import com.ovea.network.pipe.Pipes;
import com.ovea.network.pipe.ProcessPipe;

import java.io.IOException;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
final class ProcessPipeMain2 {
    public static void main(String[] args) throws IOException, InterruptedException {
        ProcessPipe pipe = Pipes.pipe(
                new ProcessBuilder("C:\\cygwin\\bin\\ls.exe", "-al", "/cygdrive/d/kha/workspace/ovea/project/pipe/src").start(),
                new ProcessBuilder("C:\\cygwin\\bin\\cut.exe", "-c", "50-").start(),
                new ProcessBuilder("C:\\cygwin\\bin\\grep.exe", "-v", "-E", "\"^\\.\\.?$\"").start());
        Pipes.connect("out", pipe.getInputStream(), System.out);
        Pipes.connect("err", pipe.getErrorStream(), System.err);
        pipe.waitFor();
    }
}
