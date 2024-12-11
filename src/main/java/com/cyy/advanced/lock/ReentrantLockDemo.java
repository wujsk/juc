package com.cyy.advanced.lock;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @program: juc
 * @description: 可重入锁 锁必须是同一个  ReentrantLock和synchronized都是可重入锁 优点：可一定程度避免死锁
 * @author: cyy
 * @create: 2024-12-11 17:13
 * 可重入锁指的是可重复可递归调用的锁，在外层使用锁之后，在内层仍可以使用，并且不会发生死锁，这样的锁就叫做可重入锁
 * 可重入锁类型：隐式：synchronized 无需人工打开关闭锁
 *             显式：ReentrantLock 需要人工打开关闭锁
 **/
public class ReentrantLockDemo {

    public synchronized void m1() {
        System.out.println(Thread.currentThread().getName() + "---come in m1");
        m2();
        System.out.println(Thread.currentThread().getName() + "---come out m1");
    }

    public synchronized void m2() {
        System.out.println(Thread.currentThread().getName() + "---come in m2");
        m3();
        System.out.println(Thread.currentThread().getName() + "---come out m2");
    }

    public synchronized void m3() {
        System.out.println(Thread.currentThread().getName() + "---come in m3");
        System.out.println(Thread.currentThread().getName() + "---come out m3");
    }

    static Lock lock = new ReentrantLock();

    public static void main(String[] args) {
        /*ReentrantLockDemo reentrantLockDemo = new ReentrantLockDemo();
        new Thread(() -> {
            reentrantLockDemo.m1();
        }, "t1").start();*/

        new Thread(() -> {
            lock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + "---come in 外层调用");
                lock.lock();
                try {
                    System.out.println(Thread.currentThread().getName() + "---come in 中层调用");
                    lock.lock();
                    try {
                        System.out.println(Thread.currentThread().getName() + "---come in 内层调用");
                    } finally {
                        lock.unlock();
                        System.out.println(Thread.currentThread().getName() + "---come out 内层调用");
                    }
                } finally {
                    lock.unlock();
                    System.out.println(Thread.currentThread().getName() + "---come out 中层调用");
                }
            } finally {
                lock.unlock();
                System.out.println(Thread.currentThread().getName() + "---come out 外层调用");
            }
        }, "t1").start();

    }

    /**
     * 同步代码块
     */
    private static void reEntryM1() {
        final Object object = new Object();
        new Thread(() -> {
            synchronized (object) {
                System.out.println(Thread.currentThread().getName() + "---外层");
                synchronized (object) {
                    System.out.println(Thread.currentThread().getName() + "---中层");
                    synchronized (object) {
                        System.out.println(Thread.currentThread().getName() + "---内层");
                    }
                }
            }
        }, "t1").start();
    }

}