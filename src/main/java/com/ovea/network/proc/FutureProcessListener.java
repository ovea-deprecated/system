package com.ovea.network.proc;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public interface FutureProcessListener {
    void onComplete(FutureProcess futureProcess);
    void onInterrupted(FutureProcess futureProcess);
}
