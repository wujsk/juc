package com.cyy.advanced.CAS;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @program: juc
 * @description:
 * @author: cyy
 * @create: 2024-12-16 19:14
 * 自旋锁
 * CAS是实现自旋锁的基础，CAS利用CPU指令保证了操作的原子性，以达到锁的效果，至于自旋呢，看字面意思也很明白，自己旋转。
 * 是指尝试获取锁的线程不会立即阻塞，而是采用循环的方式去尝试获取锁，当线程发现锁被占用时，会不断循环判断锁的状态，直到获取。
 * 这样的好处是减少线程上下文切换的消耗，缺点是循环会消耗CPU
 *
 * 题目:实现一个自旋锁，复习CAS思想
 * 自旋锁好处:循环比较获取没有类似wait的阻读。
 * 通过CAS操作完成自旋锁，A线程先进来调用myLock方法自己持有锁5秒钟，B随后进来后发现当前有线程持有锁，
 * 所以只能通过自旋等待，直到A释放锁后B随后抢到。
 **/
public class SpinLockDemo {
    public static void main(String[] args) {
        Lock lock = new ReentrantLock();
        new Thread(() -> {
            lock.lock();
            try {
                System.out.println(Thread.currentThread().getName() + "抢到锁");
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
                System.out.println(Thread.currentThread().getName() + "释放锁");
            }
        }, "A").start();
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        new Thread(() -> {
            while (!lock.tryLock()) {
                System.out.println(Thread.currentThread().getName() + "自旋中");
            }
            lock.lock();
            System.out.println(Thread.currentThread().getName() + "抢到锁");
        }, "B").start();
    }
}
