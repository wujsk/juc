package com.cyy.advanced.atomic;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * @program: juc
 * @author: cyy
 * @create: 2025-01-02 13:00
 * @description: 以一种线程安全的方式操作非线程安全对象内的某些字段
 **/
class BankAccount {
    public String name = "CCB";

    //public Integer money = 0;

    // 使用AtomicIntegerFieldUpdater必须加上public volatile
    // 不能定义为Integer类型 只能针对int类型
    public volatile int money = 0;

    public synchronized void add() {
        money += 1000;
    }

    /**
     * 因为对象属性修改类型是抽象类，所以每次使用都必须
     * 使用newUpdater的静态方法进行创建，并需要设置需要更新的类和属性
     */
    AtomicIntegerFieldUpdater<BankAccount> updater =
            AtomicIntegerFieldUpdater.newUpdater(BankAccount.class, "money");

    public void transfer() {
        updater.getAndIncrement(this);
    }

}

public class AtomicIntegerFieldUpdaterDemo {

    public static void main(String[] args) {
        CountDownLatch latch = new CountDownLatch(10);
        BankAccount bankAccount = new BankAccount();
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                bankAccount.transfer();
                latch.countDown();
            }).start();
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println(bankAccount.money);
    }
}
