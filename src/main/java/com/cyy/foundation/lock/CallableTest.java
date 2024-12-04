package com.cyy.foundation.lock;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

class Thread1 implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        return 1024;
    }
}

public class CallableTest {
    public static void main(String[] args) {
        // FutureTask
        FutureTask<Integer> futureTask1 = new FutureTask<>(new Thread1());
        FutureTask<Integer> futureTask2 = new FutureTask<>(() -> 1025);

    }
}
