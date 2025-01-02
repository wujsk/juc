package com.cyy.advanced.atomic;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @program: juc
 * @author: cyy
 * @create: 2025-01-02 13:30
 * @description:
 * 需求：
 * 多线程并发调用一个类的初始方法，如果未被初始化，将执行初始化工作
 * 要求初始化只能执行一次，只有一个线程执行成功
 **/
class User {

    public volatile Boolean init = false;

    AtomicReferenceFieldUpdater<User, Boolean> updater =
            AtomicReferenceFieldUpdater.newUpdater(User.class, Boolean.class, "init");

    public void init() {
        updater.compareAndSet(this, Boolean.FALSE, Boolean.TRUE);
    }
}
public class AtomicReferenceFieldUpdaterDemo {

    // public static Object lock = new Object();

    public static void main(String[] args) {
        // if (user == null) {
        //     synchronized (lock) {
        //         if (user == null) {
        //             user = new User();
        //         }
        //     }
        // }
        // System.out.println(user);
        User user = new User();
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
               if (user.updater.compareAndSet(user, Boolean.FALSE, Boolean.TRUE)) {
                   System.out.println(Thread.currentThread().getName() + "\t" + "start init");
                   user.init();
                   System.out.println(Thread.currentThread().getName() + "\t" + "init over");
               } else {
                   System.out.println(Thread.currentThread().getName() + "\t" + "其他线程正在执行初始化工作");
               }
            }, String.valueOf(i + 1)).start();
        }
    }
}
