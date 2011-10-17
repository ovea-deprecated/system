/**
 * Copyright (C) 2011 Ovea <dev@ovea.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ovea.system.test;

import com.ovea.system.proc.FutureProcess;
import com.ovea.system.trace.Trace;
import com.ovea.system.util.Platform;
import com.ovea.system.util.ProcUtils;

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
            System.out.println(" - PID = " + ProcUtils.currentPID());
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
                ProcUtils.terminate(process.process());
            }
            process.get();

            process = launch(win ? "cmd.exe" : "sh");
            try {
                process.get(1, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                ProcUtils.terminate(process.pid());
            }
            process.get();
        }
    }

    private static FutureProcess launch(String proc) throws Exception {
        System.out.println(" - Launching " + proc + "...");
        FutureProcess process = new FutureProcess(new ProcessBuilder(proc).start());
        Thread.sleep(1000);
        System.out.println(" - Child PID = " + process.pid());
        return process;
    }
}
