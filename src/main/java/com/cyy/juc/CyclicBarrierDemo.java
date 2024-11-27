package com.cyy.juc;

import java.util.concurrent.CyclicBarrier;

/**
 * @program: juc
 * @description: 集齐七颗龙珠，召唤神龙
 * @author: 酷炫焦少
 * @create: 2024-11-26 16:58
 **/
public class CyclicBarrierDemo {
    private static final int NUMBER = 7;
    public static void main(String[] args) {
        CyclicBarrier barrier = new CyclicBarrier(NUMBER, () -> {
            System.out.println("集齐七颗龙珠，召唤神龙");
        });
        for (int i = 1; i <= NUMBER; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + "颗龙珠集齐");
                try {
                    // 等待
                    barrier.await();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, String.valueOf(i)).start();
        }
    }
}
