package com.cyy.advanced.lock;

import java.util.concurrent.TimeUnit;

/**
 * @program: juc
 * @description: 死锁案例和排查
 * @author: cyy
 * @create: 2024-12-11 17:40
 *
 * 排查方式：1. 先通过jps -l得到java进程目录，jstack PID 查看可能产生死锁的进程
 *         2. win+R输入jconsole 图形化 查看死锁
 **/
public class DeadLockDemo {
    public static void main(String[] args) {
        final Object o1 = new Object();
        final Object o2 = new Object();
        new Thread(() -> {
            synchronized (o1) {
                // 等一下b线程
                try {TimeUnit.SECONDS.sleep(1);} catch (InterruptedException e) {throw new RuntimeException(e);}
                synchronized (o2) {
                    System.out.println(Thread.currentThread().getName() + "未死锁");
                }
            }
        }, "a").start();

        new Thread(() -> {
            synchronized (o2) {
                // 等一下a线程
                try {TimeUnit.SECONDS.sleep(1);} catch (InterruptedException e) {throw new RuntimeException(e);}
                synchronized (o1) {
                    System.out.println(Thread.currentThread().getName() + "未死锁");
                }
            }
        }, "b").start();
    }
}
