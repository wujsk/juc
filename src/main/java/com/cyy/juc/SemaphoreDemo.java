package com.cyy.juc;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @program: juc
 * @description: 信号灯
 * @author: 酷炫焦少
 * @create: 2024-11-27 11:30
 **/
public class SemaphoreDemo {
    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(3);
        for (int i = 1; i <= 6; i++) {
            new Thread(() -> {
                try {
                    semaphore.acquire();
                    System.out.println("第" + Thread.currentThread().getName() + "辆车进入");
                    TimeUnit.SECONDS.sleep(3);
                    System.out.println("第" + Thread.currentThread().getName() + "辆车离开");
                    semaphore.release();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, String.valueOf(i)).start();
        }
    }
}
