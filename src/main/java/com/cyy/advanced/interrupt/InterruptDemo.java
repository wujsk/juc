package com.cyy.advanced.interrupt;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @program: juc
 * @description: 线程中断机制
 * @author: cyy
 * @create: 2024-12-11 18:02
 * 1.一个线程不应该由其他线程来强制中断或停止，而是应该由线程自行停止，自己决定自己命运
 * 2.在java中没有办法立即停止y一条线程，然而停止线程却显得尤为重要，如取消一个耗时操作
 *   因此，Java提供了一种用于停止线程的协商机制--中断，也即中断标识协商机制
 *   中断只是一种协作协商机制，Java没有给中断增加任何语法，中断的过程完全需要程序员自己实现
 *   若要中断一个线程，你需要手动调用该线程的interrupt方法，该方法也仅仅是将线程对象的中断标识设成true;
 *   接着你需要自己写代码不断地检测当前线程的标识位，如果为true，表示别的线程请求这条线程中断，
 *   此时究竞该做什么需要你自己写代码实现。
 *   每个线程对象中都有一个中断标识位，用于表示线程是否被中断;该标识位为true表示中断，为false表示未中断;
 *   通过调用线程对象的interrupt方法将该线程的标识位设为true;可以在别的线程中调用，也可以在自己的线程中调用。
 **/
public class InterruptDemo {

    /**
     * volatile是 Java 中的一个关键字。它主要用于修饰变量，其目的是确保变量在多个线程之间的可见性。
     * 当一个变量被声明为volatile时，Java 虚拟机（JVM）会保证这个变量的值在一个线程中被修改后，其他线程能够立即看到这个修改后的值。
     * 虽然volatile能够保证变量的可见性，但它不能保证原子性。
     * static volatile boolean isStop = false;
     */

    /**
     * AtomicBoolean是 Java 中的原子类，属于java.util.concurrent.atomic包。
     * 它提供了一种原子操作的布尔值，这意味着对AtomicBoolean对象的操作在多线程环境下是线程安全的。
     * static AtomicBoolean isStop = new AtomicBoolean(false);
     */

    public static void main(String[] args) {
        /*
         * 如何中断运行中的进程
         * 1.通过一个volatile变量实现
         * 2.通过AtomicBoolean
         * 3.通过Thread类自带的中断api实例方法实现
         *   在需要中断的线程中不断监听中断状态，一旦发生中断，就执行相应的中断处理业务逻辑stop线程
         *   当前线程的中断标识为true，是不是线程就立即停止？
         *   答：不会立即停止，将继续运行，不会停止
         *   具体来说，当对一个线程，调用interrupt()时:
         *   ①如果线程处于正常活动状态，那么会将该线程的中断标志设置为 true，仅此而已。被设置中断标志的线程将继续正常运行，不受影响。
         *      所以，interrupt()并不能真正的中断线程，需要被调用的线程自己进行配合才行。
         *   ②如果线程处于被阻塞状态（例如处于sleep, wait, join 等状态〉，在别的线程中调用当前线程对象的interrupt方法，
         *      那么线程将立即退出被阻塞状态，并抛出一个InterruptedException异常。
         */
    }

    /**
     * Thread.interrupted()源码解释
     * 测试当前线程是否已中断。该方法清除线程的中断状态。
     * 换句话说，如果要连续调用此方法两次，则第二次调用将返回 false
     * （除非当前线程在第一次调用清除其中断状态之后和第二次调用检查它之前再次中断）.
     * 返回当前状态，并且中断标志位设为false
     * main	false
     * main	false
     * ---- 1
     * ---- 2
     * main	true
     * main	false
     */
    private void m6() {
        System.out.println(Thread.currentThread().getName() + "\t" + Thread.interrupted());
        System.out.println(Thread.currentThread().getName() + "\t" + Thread.interrupted());
        System.out.println("---- 1");
        Thread.currentThread().interrupt(); //中断标志位设置为true
        System.out.println("---- 2");
        System.out.println(Thread.currentThread().getName() + "\t" + Thread.interrupted());
        System.out.println(Thread.currentThread().getName() + "\t" + Thread.interrupted());
    }

    /**
     * jdk8 在抛出异常后，调用isInterrupted为false，说明抛出异常后，中断标志位会变为false
     * jdk17 在抛出异常后，程序会自动停止
     * sleep方法抛出InterruptedException后，中断标识也被清空置为false，
     * 我们在catch没有通过调用th.interrupt()方法再次将中断标识置为true，这就导致无限循环了
     */
    private static void m5() {
        Thread t1 = new Thread(() -> {
            while(true) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println(Thread.currentThread().getName() + "中断标志位: " + Thread.currentThread().isInterrupted() + "程序停止");
                    break;
                }
                try {TimeUnit.MILLISECONDS.sleep(200);} catch (InterruptedException e) {throw new RuntimeException(e);}
                System.out.println("hello InterruptDemo");
            }
        }, "t1");
        t1.start();

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            // 为什么要在异常处，在调用一次？？
            // Thread.currentThread().interrupt();
            e.printStackTrace();
            // throw new RuntimeException(e);
        }

        new Thread(() -> {
            // 查看源码
            t1.interrupt();
        }, "t2").start();
    }

    /**
     * 实例方法interrupt()仅仅是设置线程的中断状态位设置为true，不会停止线程
     * -----9
     * t1线程调用interrupt()后的中断标识01: true
     * -----10
     * 中断不活动的线程(线程已经停止)，不会产生影响。
     * jdk8 中断标识03 在线程停止后，标志位会变成false
     * jdk17 中断标识03 在线程停止后，标志位不会变成false
     */
    private void m4() {
        Thread t1 = new Thread(() -> {
            for (int i = 1; i <= 300; i++) {
                System.out.println("-----" + i);
            }
            System.out.println("t1线程调用interrupt()后的中断标识02: " + Thread.currentThread().isInterrupted());
        }, "t1");
        t1.start();

        System.out.println("t1线程默认的中断标志位: " + t1.isInterrupted()); // false

        // 暂停毫秒
        try {TimeUnit.MILLISECONDS.sleep(2);} catch (InterruptedException e) {throw new RuntimeException(e);}
        t1.interrupt();
        System.out.println("t1线程调用interrupt()后的中断标识01: " + t1.isInterrupted());

        try {TimeUnit.MILLISECONDS.sleep(2000);} catch (InterruptedException e) {throw new RuntimeException(e);}
        System.out.println("t1线程调用interrupt()后的中断标识03: " + t1.isInterrupted());
    }

    private static void m3() {
        Thread t1 = new Thread(() -> {
            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println(Thread.currentThread().getName() + "isInterrupted()被修改为true，程序停止");
                    break;
                }
                System.out.println("t1 -----hello interrupt api");
            }
        }, "t1");
        t1.start();
        // 暂停几秒
        try {TimeUnit.MILLISECONDS.sleep(20);} catch (InterruptedException e) {throw new RuntimeException(e);}
        // t2向t1发出协商，将t1的中断标志位设为ture，希望t1停下来
        new Thread(() -> {
            t1.interrupt();
        }, "t2").start();
    }

    private void m2() {
        AtomicBoolean isStop = new AtomicBoolean(false);
        new Thread(() -> {
            while (true) {
                if (isStop.get()) {
                    System.out.println(Thread.currentThread().getName() + ":isStop被修改为true，那么程序停止");
                    break;
                }
                System.out.println("---hello atomicBoolean");
            }
        }, "t1").start();
        try {TimeUnit.MILLISECONDS.sleep(20);} catch (InterruptedException e) {throw new RuntimeException(e);}
        new Thread(() -> {
            isStop.set(true);
        }, "t2").start();
    }

    // 通过一个volatile变量实现
    volatile boolean isStop = false;
    private void m1() {
        new Thread(() -> {
            while (true) {
                if (isStop) {
                    System.out.println(Thread.currentThread().getName() + ":isStop被修改为true，那么程序停止");
                    break;
                }
                System.out.println("---hello volatile");
            }
        }, "t1").start();
        try {TimeUnit.MILLISECONDS.sleep(20);} catch (InterruptedException e) {throw new RuntimeException(e);}
        new Thread(() -> {
            isStop = true;
        }, "t2").start();
    }
}
