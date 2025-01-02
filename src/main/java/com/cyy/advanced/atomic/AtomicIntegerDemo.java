package com.cyy.advanced.atomic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * @program: juc
 * @description:
 * @author: cyy
 * @create: 2024-12-16 19:59
 **/
public class AtomicIntegerDemo {

    public static final int SIZE = 50;

    public static void main(String[] args) {

        // CountDownLatch countDownLatch = new CountDownLatch(SIZE);

        MyNumber number = new MyNumber();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 1; i <= SIZE; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < 1000; j++) {
                    number.addPlus();
                }
            });
            futures.add(future);
            // new Thread(() -> {
            //     try {
            //         for (int j = 0; j < 1000; j++) {
            //             number.addPlus();
            //         }
            //     } finally {
            //         countDownLatch.countDown();
            //     }
            // }, "t" + i).start();
        }

        // 等待上面50个线程全部执行完再打印
        // try {TimeUnit.SECONDS.sleep(2);} catch (InterruptedException e) {throw new RuntimeException(e);}

        // try {countDownLatch.await();} catch (InterruptedException e) {throw new RuntimeException(e);}
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            System.out.println(Thread.currentThread().getName() + "\t" + number.atomicInteger.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

class MyNumber {
    public AtomicInteger atomicInteger = new AtomicInteger();

    public Integer addPlus() {
        return atomicInteger.getAndIncrement();
    }
}

class Solution {
    public int strStr(String haystack, String needle) {
        boolean contains = haystack.contains(needle);
        if (contains) {
            return haystack.indexOf(needle);
        } else {
            return -1;
        }
    }
}