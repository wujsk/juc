package com.cyy.advanced.lock;


/**
 * @program: juc
 * @description:
 * @author: cyy
 * @create: 2024-12-11 15:53
 **/
public class LockSyncDemo {

    Object object = new Object();

    public void m1() {
        synchronized (object) {
            System.out.println("---hello synchronized code block---");
        }
    }

    public void m2() {
        synchronized (object) {
            System.out.println("---hello synchronized code block---");
            throw new RuntimeException("----exp");
        }
    }

    public synchronized void m3() {
        System.out.println("---hello synchronized m3---");
    }

    public static synchronized void m4() {
        System.out.println("---hello synchronized m4---");
    }

    public static void main(String[] args) {

    }
}
