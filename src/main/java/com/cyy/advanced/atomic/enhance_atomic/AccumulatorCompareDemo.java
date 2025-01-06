package com.cyy.advanced.atomic.enhance_atomic;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * @program: juc
 * @author: cyy
 * @create: 2025-01-02 14:13
 * @description:
 **/
class ClickNumber {
    public int number = 0;

    public synchronized void click() {
        number++;
    }

    AtomicLong atomicLong = new AtomicLong();
    public void clickByAtomicLong() {
        atomicLong.getAndIncrement();
    }

    LongAdder longAdder = new LongAdder();
    public void clickByLongAdder() {
        longAdder.increment();
    }

    LongAccumulator longAccumulator = new LongAccumulator((x, y) -> x + y, 0);
    public void clickByAccumulator() {
        longAccumulator.accumulate(1);
    }

}
public class AccumulatorCompareDemo {
    public static void main(String[] args) throws InterruptedException {
        ClickNumber number = new ClickNumber();
        CountDownLatch latch = new CountDownLatch(50);
        long begin = System.currentTimeMillis();
        System.out.println("开始点赞");
        for (int i = 0; i < 50; i++) {
            new Thread(() -> {
                for (int j = 0; j < 1000000; j++) {
                    // number.click(); 点赞结束花费:3951	50000000
                    // number.clickByAtomicLong(); 点赞结束花费:1129	50000000
                    // number.clickByLongAdder(); 点赞结束花费:122	50000000
                    // number.clickByAccumulator(); 点赞结束花费:144	50000000
                }
                latch.countDown();
            }).start();
        }
        latch.await();
        System.out.println("点赞结束花费:" + (System.currentTimeMillis() - begin) + "\t" + number.number);
    }
}
