package com.cyy.advanced.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Ticket {
    private int number = 50;
    // 默认非公平锁
    Lock lock = new ReentrantLock(true);

    public void sale() {
        lock.lock();
        try {
            if (number > 0) {
                System.out.println(Thread.currentThread().getName() + "卖出第" + number-- + "张票，还剩下：" + number);
            }
        } finally {
            lock.unlock();
        }
    }
}

/**
 * @program: juc
 * @description: 预埋伏AQS
 * @author: cyy
 * @create: 2024-12-11 16:59
 **/
public class FairAndUnFairLock {
    public static void main(String[] args) {
        Ticket ticket = new Ticket();
        new Thread(() -> {
           for (int i = 0; i < 50; i++) {
               ticket.sale();
           }
        }, "a").start();
        new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                ticket.sale();
            }
        }, "b").start();
        new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                ticket.sale();
            }
        }, "c").start();
    }
}
