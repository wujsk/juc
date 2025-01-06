package com.cyy.advanced.threadLocal;

import lombok.Data;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.LongAdder;

/**
 * @program: juc
 * @author: cyy
 * @create: 2025-01-03 12:59
 * @description:
 **/
@Data
class House {
    private int saleCount = 0;

    /*public synchronized void saleCountPlus() {
        saleCount++;
    }*/

    /*ThreadLocal<Integer> saleVolume = new ThreadLocal<>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };*/

    ThreadLocal<Integer> saleVolume = ThreadLocal.withInitial(() -> 0);

    public void saleVolumeByThreadLocal() {
        saleVolume.set(saleVolume.get() + 1);
    }

    public int getSaleVolumeByThreadLocal() {
        return saleVolume.get();
    }
}
public class ThreadLocalDemo {

    private static final int THREAD_NUM = 5;

    public static void main(String[] args) throws InterruptedException {
        House house = new House();
        CountDownLatch latch = new CountDownLatch(THREAD_NUM);
        LongAdder adder = new LongAdder();
        for (int i = 0; i < THREAD_NUM; i++) {
            int count = i;
            new Thread(() -> {
                try {
                    for (int j = 0; j < count + 1; j++) {
                        // house.saleCountPlus();
                        adder.increment();
                        house.saleVolumeByThreadLocal();
                    }
                    System.out.println(Thread.currentThread().getName() + "\t" + "号销售卖出" +
                            house.getSaleVolumeByThreadLocal());
                } finally {
                    house.saleVolume.remove();
                }
                latch.countDown();
            }, String.valueOf(i + 1)).start();
        }
        latch.await();
        System.out.println("共销售出" + adder.sum() + "套房子");
    }
}
