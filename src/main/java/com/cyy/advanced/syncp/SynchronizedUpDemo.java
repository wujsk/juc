package com.cyy.advanced.syncp;

import org.openjdk.jol.info.ClassLayout;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @program: juc
 * @author: cyy
 * @create: 2025-01-06 16:47
 * @description:
 **/
public class SynchronizedUpDemo {
    public static void main(String[] args) throws InterruptedException {
        // 当一个对象已经计算过identity hashcode，它就无法进入偏向锁状态，跳过偏向锁，直接升级轻量级锁
        Object o = new Object();
        int hashCode = o.hashCode();
        System.out.println(hashCode);

        new Thread(() -> {
            synchronized (o) {
                System.out.println(ClassLayout.parseInstance(o).toPrintable());
                System.out.println(o.hashCode());
            }
        }, "t1").start();
    }

    private static void weightLock() throws InterruptedException {
        Object o = new Object();
        // 0x0000025b60322c62 (fat lock: 0x0000025b60322c62) 确实形成重量级锁了
        new Thread(() -> {
            synchronized (o) {
                System.out.println(ClassLayout.parseInstance(o).toPrintable());
            }
        }, "t1").start();
        TimeUnit.MILLISECONDS.sleep(100);
        new Thread(() -> {
            synchronized (o) {
                System.out.println(ClassLayout.parseInstance(o).toPrintable());
            }
        }, "t1").start();
    }

    private static void lightLock() {
        Object o = new Object();
        // 自旋达到一定次数依旧没有成功，会升级为重量级锁 
        new Thread(() -> {
            synchronized (o) {
                // 0x000000323c1ff238 (thin lock: 0x000000323c1ff238) 确实为轻量锁
                System.out.println(ClassLayout.parseInstance(o).toPrintable());
            }
        }, "t1").start();
    }

    private static void biasedLock() {
        Object o = new Object();

        System.out.println(o.hashCode());
        System.out.println(ClassLayout.parseInstance(o).toPrintable());
    }
}
