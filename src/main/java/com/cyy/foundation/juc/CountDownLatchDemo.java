package com.cyy.foundation.juc;

import java.util.concurrent.CountDownLatch;

/**
 * @program: juc
 * @description:
 * @author: 酷炫焦少
 * @create: 2024-11-26 16:42
 **/
public class CountDownLatchDemo {
    public static void main(String[] args) {
        CountDownLatch count = new CountDownLatch(6);
        for (int i = 1; i <= 6; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + "号离开教室");
                count.countDown();
            }, String.valueOf(i)).start();
        }
        try {
            // 主线程在此处等待，直到计数器变为0，也就是所有子线程都执行完了countDown操作
            count.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(Thread.currentThread().getName() + " 班长锁门走了");
    }
}