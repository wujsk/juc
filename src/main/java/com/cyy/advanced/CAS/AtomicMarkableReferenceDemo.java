package com.cyy.advanced.CAS;

import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * @program: juc
 * @author: cyy
 * @create: 2025-01-02 12:49
 * @description: 解决是否修改过
 **/
public class AtomicMarkableReferenceDemo {

    private static AtomicMarkableReference<Integer> markableReference = new AtomicMarkableReference<>(100, false);

    public static void main(String[] args) {
        new Thread(() -> {
            boolean marked = markableReference.isMarked();
            System.out.println(Thread.currentThread().getName() + "\t 默认标识" + marked);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            markableReference.compareAndSet(100, 1000, marked, !marked);
        }, "t1").start();

        new Thread(() -> {
            boolean marked = markableReference.isMarked();
            System.out.println(Thread.currentThread().getName() + "\t 默认标识" + marked);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            markableReference.compareAndSet(100, 2000, marked, !marked);
            System.out.println(Thread.currentThread().getName() + "\t 标识" + markableReference.isMarked());
            System.out.println(Thread.currentThread().getName() + "\t 值" + markableReference.getReference());
        }, "t2").start();

    }
}
