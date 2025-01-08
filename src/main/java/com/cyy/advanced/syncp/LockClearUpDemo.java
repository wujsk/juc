package com.cyy.advanced.syncp;

/**
 * @program: juc
 * @author: cyy
 * @create: 2025-01-08 20:29
 * @description: 锁消除
 * 从J工T角度看相当于无视它，synchronized (o)不存在了,这个锁对象并没有被共用扩散到其它线程使用,
 * 极端的说就是根本没有加这个锁对象的底层机器码，消除了锁的使用
 **/
public class LockClearUpDemo {

    static Object object = new Object();

    public void m1() {
        // synchronized (object) {
        //     System.out.println("-------hello LockClearUpDemo");
        // }

        // 锁消除问题，JIT编译器会无视它 synchronized (o)，每次new出来的，不存在了，非正常的
        Object o = new Object();
        synchronized (o) {
            System.out.println("-------hello LockClearUpDemo" + "\t" + o.hashCode() + "\t" + object.hashCode());
        }
    }

    public static void main(String[] args) {
        LockClearUpDemo lockClearUpDemo = new LockClearUpDemo();
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                lockClearUpDemo.m1();
            }, String.valueOf(i + 1)).start();
        }
    }
}
