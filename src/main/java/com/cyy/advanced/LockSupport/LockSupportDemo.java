package com.cyy.advanced.LockSupport;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @program: juc
 * @description:
 * @author: cyy
 * @create: 2024-12-12 15:48
 **/
public class LockSupportDemo {
    public static void main(String[] args) {
        /**
         * 用来创建锁和其他同步类的基本线程阻塞原语。
         *
         * 此类以及每个使用它的线程与一个许可关联（从 Semaphore 类的意义上说）。如果该许可可用，并且可在进程中使用，则调用 park 将立即返回；
         * 否则可能 阻塞。
         * 如果许可尚不可用，则可以调用 unpark 使其可用。（但与 Semaphore 不同的是，许可不能累积，并且最多只能有一个许可。）
         *
         * LockSupport解决了之前唤醒线程必须要在等待线程之前，且不需要线程必须要持有锁才可以。
         */
        Thread t1 = new Thread(() -> {
            // 暂停几秒线程
            // try {TimeUnit.SECONDS.sleep(1);} catch (InterruptedException e) {throw new RuntimeException(e);}

            System.out.println(Thread.currentThread().getName() + "\t ----come in");
            // 等待许可证的发放，如果没有许可证，将等待。
            // 此时需要两个许可证
            LockSupport.park();
            // LockSupport.park();
            System.out.println(Thread.currentThread().getName() + "\t ----被发放许可证");
        }, "t1");
        t1.start();

        // 暂停几秒线程
        try {TimeUnit.SECONDS.sleep(1);} catch (InterruptedException e) {throw new RuntimeException(e);}

        new Thread(() -> {
            // 给t1发放许可证
            System.out.println(Thread.currentThread().getName() + "\t ----发放许可证");
            // 但是许可证不能累计，只能有一个
            LockSupport.unpark(t1);
            // LockSupport.unpark(t1);
        }, "t2").start();

    }

    /**
     * 有两种异常情况
     * 1.没有锁代码块 解决办法：线程先要获得并持有锁，必须在锁块中
     * t1	 ----come in
     * Exception in thread "t1" java.lang.RuntimeException: java.lang.IllegalMonitorStateException
     * 	at com.cyy.advanced.LockSupport.LockSupportDemo.lambda$main$0(LockSupportDemo.java:25)
     * 	at java.base/java.lang.Thread.run(Thread.java:833)
     * Caused by: java.lang.IllegalMonitorStateException
     * 	at java.base/java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.enableWait(AbstractQueuedSynchronizer.java:1516)
     * 	at java.base/java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:1611)
     * 	at com.cyy.advanced.LockSupport.LockSupportDemo.lambda$main$0(LockSupportDemo.java:22)
     * 	... 1 more
     * Exception in thread "t2" java.lang.IllegalMonitorStateException
     * 	at java.base/java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.signal(AbstractQueuedSynchronizer.java:1473)
     * 	at com.cyy.advanced.LockSupport.LockSupportDemo.lambda$main$1(LockSupportDemo.java:37)
     * 	at java.base/java.lang.Thread.run(Thread.java:833)
     * 	2.唤醒线程在等待线程之前, 等待线程不能被唤醒 解决办法：必须要先等待后唤醒，线程才能够被唤醒
     * 	t2	 ----发出通知
     *  t1	 ----come in
     */
    private void m2() {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        new Thread(() -> {
            // try {TimeUnit.SECONDS.sleep(1);} catch (InterruptedException e) {throw new RuntimeException(e);}
            lock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + "\t ----come in");
                condition.await();
                System.out.println(Thread.currentThread().getName() + "\t ----被唤醒");
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }, "t1").start();

        // 暂停几秒线程
        try {TimeUnit.SECONDS.sleep(1);} catch (InterruptedException e) {throw new RuntimeException(e);}

        new Thread(() -> {
            lock.lock();
            try {
                condition.signal();
                System.out.println(Thread.currentThread().getName() + "\t ----发出通知");
            } finally {
                lock.unlock();
            }
        }, "t2").start();
    }

    /**
     * 两种异常情况
     * 1.将同步代码块注释掉，结果发生异常,只有获取了锁，才能等待或唤醒 解决办法：线程先要获得并持有锁，必须在锁块中
     * t1	 ----come in
     * Exception in thread "t1" java.lang.IllegalMonitorStateException: current thread is not owner
     * 	at java.base/java.lang.Object.wait(Native Method)
     * 	at java.base/java.lang.Object.wait(Object.java:338)
     * 	at com.cyy.advanced.LockSupport.LockSupportDemo.lambda$main$0(LockSupportDemo.java:18)
     * 	at java.base/java.lang.Thread.run(Thread.java:833)
     * Exception in thread "t2" java.lang.IllegalMonitorStateException: current thread is not owner
     * 	at java.base/java.lang.Object.notify(Native Method)
     * 	at com.cyy.advanced.LockSupport.LockSupportDemo.lambda$main$1(LockSupportDemo.java:31)
     * 	at java.base/java.lang.Thread.run(Thread.java:833)
     *
     * 	wait和notify只有你在获取锁，或者理解为进入特定屋子之后，才有资格进入等待池的屋子
     * 	2.将唤醒线程和等待线程调换位置 解决办法：必须要先等待后唤醒，线程才能够被唤醒
     * 	导致t1线程不能被唤醒
     * 	t2	 ----发出通知
     *  t1	 ----come in
     */
    private void m1() {
        Object objectLock = new Object();
        new Thread(() -> {
            // try {TimeUnit.SECONDS.sleep(1);} catch (InterruptedException e) {throw new RuntimeException(e);}
            synchronized (objectLock) {
                System.out.println(Thread.currentThread().getName() + "\t ----come in");
                try {
                    objectLock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(Thread.currentThread().getName() + "\t ----被唤醒");
            }
        }, "t1").start();

        // 暂停几秒线程
        try {TimeUnit.SECONDS.sleep(1);} catch (InterruptedException e) {throw new RuntimeException(e);}

        new Thread(() -> {
            synchronized (objectLock) {
                objectLock.notify();
                System.out.println(Thread.currentThread().getName() + "\t ----发出通知");
            }
        }, "t2").start();
    }
}
