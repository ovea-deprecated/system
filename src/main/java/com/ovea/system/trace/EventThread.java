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
package com.ovea.system.trace;

import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class processes incoming JDI events and displays them
 *
 * @author Robert Field
 */
public class EventThread extends Thread {

    static String nextBaseIndent = ""; // Starting indent for next thread

    private final VirtualMachine vm;   // Running VM
    private final String[] excludes;   // Packages to exclude
    private final PrintWriter writer;  // Where output goes
    private boolean connected = true;  // Connected to VM
    private boolean vmDied = true;     // VMDeath occurred

    // Maps ThreadReference to ThreadTrace instances
    private Map<ThreadReference, ThreadTrace> traceMap = new HashMap<ThreadReference, ThreadTrace>();

    EventThread(VirtualMachine vm, String[] excludes, PrintWriter writer) {
        super("event-handler");
        this.vm = vm;
        this.excludes = excludes;
        this.writer = writer;
    }

    /**
     * Run the event handling thread.
     * As long as we are connected, get event sets off
     * the queue and dispatch the events within them.
     */
    @Override
    public void run() {
        EventQueue queue = vm.eventQueue();
        while (connected) {
            try {
                EventSet eventSet = queue.remove();
                EventIterator it = eventSet.eventIterator();
                while (it.hasNext()) {
                    handleEvent(it.nextEvent());
                }
                eventSet.resume();
            } catch (InterruptedException exc) {
                // Ignore
            } catch (VMDisconnectedException discExc) {
                handleDisconnectedException();
                break;
            }
        }
    }

    /**
     * Create the desired event requests, and enable
     * them so that we will get events.
     *
     * @param watchFields Do we want to watch assignments to fields
     */
    void setEventRequests(boolean watchFields) {
        EventRequestManager mgr = vm.eventRequestManager();

        // want all exceptions
        ExceptionRequest excReq = mgr.createExceptionRequest(null, true, true);
        // suspend so we can step
        excReq.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        excReq.enable();

        MethodEntryRequest menr = mgr.createMethodEntryRequest();
        for (String exclude : excludes) {
            menr.addClassExclusionFilter(exclude);
        }
        menr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        menr.enable();

        MethodExitRequest mexr = mgr.createMethodExitRequest();
        for (String exclude : excludes) {
            mexr.addClassExclusionFilter(exclude);
        }
        mexr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
        mexr.enable();

        ThreadDeathRequest tdr = mgr.createThreadDeathRequest();
        // Make sure we sync on thread death
        tdr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        tdr.enable();

        if (watchFields) {
            ClassPrepareRequest cpr = mgr.createClassPrepareRequest();
            for (String exclude : excludes) {
                cpr.addClassExclusionFilter(exclude);
            }
            cpr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
            cpr.enable();
        }
    }

    /**
     * This class keeps context on events in one thread.
     * In this implementation, context is the indentation prefix.
     */
    class ThreadTrace {
        final ThreadReference thread;
        final String baseIndent;
        static final String threadDelta = "                     ";
        StringBuffer indent;

        ThreadTrace(ThreadReference thread) {
            this.thread = thread;
            this.baseIndent = nextBaseIndent;
            indent = new StringBuffer(baseIndent);
            nextBaseIndent += threadDelta;
            println("====== " + thread.name() + " ======");
        }

        private void println(String str) {
            writer.print(indent);
            writer.println(str);
        }

        void methodEntryEvent(MethodEntryEvent event) {
            println(event.method().name() + "  --  " + event.method().declaringType().name());
            indent.append("| ");
        }

        void methodExitEvent() {
            indent.setLength(indent.length() - 2);
        }

        void fieldWatchEvent(ModificationWatchpointEvent event) {
            Field field = event.field();
            Value value = event.valueToBe();
            println("    " + field.name() + " = " + value);
        }

        void exceptionEvent(ExceptionEvent event) {
            println("Exception: " + event.exception() + " catch: " + event.catchLocation());

            // Step to the catch
            EventRequestManager mgr = vm.eventRequestManager();
            StepRequest req = mgr.createStepRequest(thread,
                StepRequest.STEP_MIN,
                StepRequest.STEP_INTO);
            req.addCountFilter(1);  // next step only
            req.setSuspendPolicy(EventRequest.SUSPEND_ALL);
            req.enable();
        }

        // Step to exception catch
        void stepEvent(StepEvent event) {
            // Adjust call depth
            int cnt = 0;
            indent = new StringBuffer(baseIndent);
            try {
                cnt = thread.frameCount();
            } catch (IncompatibleThreadStateException ignored) {
            }
            while (cnt-- > 0) {
                indent.append("| ");
            }

            EventRequestManager mgr = vm.eventRequestManager();
            mgr.deleteEventRequest(event.request());
        }

        void threadDeathEvent() {
            indent = new StringBuffer(baseIndent);
            println("====== " + thread.name() + " end ======");
        }
    }

    /**
     * Returns the ThreadTrace instance for the specified thread,
     * creating one if needed.
     */
    ThreadTrace threadTrace(ThreadReference thread) {
        ThreadTrace trace = traceMap.get(thread);
        if (trace == null) {
            trace = new ThreadTrace(thread);
            traceMap.put(thread, trace);
        }
        return trace;
    }

    /**
     * Dispatch incoming events
     */
    private void handleEvent(Event event) {
        if (event instanceof ExceptionEvent) {
            exceptionEvent((ExceptionEvent) event);
        } else if (event instanceof ModificationWatchpointEvent) {
            fieldWatchEvent((ModificationWatchpointEvent) event);
        } else if (event instanceof MethodEntryEvent) {
            methodEntryEvent((MethodEntryEvent) event);
        } else if (event instanceof MethodExitEvent) {
            methodExitEvent((MethodExitEvent) event);
        } else if (event instanceof StepEvent) {
            stepEvent((StepEvent) event);
        } else if (event instanceof ThreadDeathEvent) {
            threadDeathEvent((ThreadDeathEvent) event);
        } else if (event instanceof ClassPrepareEvent) {
            classPrepareEvent((ClassPrepareEvent) event);
        } else if (event instanceof VMStartEvent) {
            vmStartEvent();
        } else if (event instanceof VMDeathEvent) {
            vmDeathEvent();
        } else if (event instanceof VMDisconnectEvent) {
            vmDisconnectEvent();
        } else {
            throw new Error("Unexpected event type");
        }
    }

    /**
     * A VMDisconnectedException has happened while dealing with
     * another event. We need to flush the event queue, dealing only
     * with exit events (VMDeath, VMDisconnect) so that we terminate
     * correctly.
     */
    synchronized void handleDisconnectedException() {
        EventQueue queue = vm.eventQueue();
        while (connected) {
            try {
                EventSet eventSet = queue.remove();
                EventIterator iter = eventSet.eventIterator();
                while (iter.hasNext()) {
                    Event event = iter.nextEvent();
                    if (event instanceof VMDeathEvent) {
                        vmDeathEvent();
                    } else if (event instanceof VMDisconnectEvent) {
                        vmDisconnectEvent();
                    }
                }
                eventSet.resume(); // Resume the VM
            } catch (InterruptedException exc) {
                // ignore
            }
        }
    }

    private void vmStartEvent() {
        writer.println("-- VM Started --");
    }

    // Forward event for thread specific processing
    private void methodEntryEvent(MethodEntryEvent event) {
        threadTrace(event.thread()).methodEntryEvent(event);
    }

    // Forward event for thread specific processing
    private void methodExitEvent(MethodExitEvent event) {
        threadTrace(event.thread()).methodExitEvent();
    }

    // Forward event for thread specific processing
    private void stepEvent(StepEvent event) {
        threadTrace(event.thread()).stepEvent(event);
    }

    // Forward event for thread specific processing
    private void fieldWatchEvent(ModificationWatchpointEvent event) {
        threadTrace(event.thread()).fieldWatchEvent(event);
    }

    void threadDeathEvent(ThreadDeathEvent event) {
        ThreadTrace trace = traceMap.get(event.thread());
        if (trace != null) {  // only want threads we care about
            trace.threadDeathEvent();   // Forward event
        }
    }

    /**
     * A new class has been loaded.
     * Set watchpoints on each of its fields
     */
    private void classPrepareEvent(ClassPrepareEvent event) {
        EventRequestManager mgr = vm.eventRequestManager();
        List<Field> fields = event.referenceType().visibleFields();
        for (Field field : fields) {
            ModificationWatchpointRequest req = mgr.createModificationWatchpointRequest(field);
            for (String exclude : excludes) {
                req.addClassExclusionFilter(exclude);
            }
            req.setSuspendPolicy(EventRequest.SUSPEND_NONE);
            req.enable();
        }
    }

    private void exceptionEvent(ExceptionEvent event) {
        ThreadTrace trace = traceMap.get(event.thread());
        if (trace != null) {  // only want threads we care about
            trace.exceptionEvent(event);      // Forward event
        }
    }

    public void vmDeathEvent() {
        vmDied = true;
        writer.println("-- The application exited --");
    }

    public void vmDisconnectEvent() {
        connected = false;
        if (!vmDied) {
            writer.println("-- The application has been disconnected --");
        }
    }
}
