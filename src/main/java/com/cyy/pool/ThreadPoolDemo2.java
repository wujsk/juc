package com.cyy.pool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @program: juc
 * @description: 自定义线程池
 * @author: 酷炫焦少
 * @create: 2024-11-27 17:29
 **/
public class ThreadPoolDemo2 {
    public static void main(String[] args) {
        // 常驻线程2 最大线程数量5 存活时间 时间单位 阻塞队列 线程工厂 拒绝策略
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2,
                5,
                2L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(3),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
        try {
            for (int i = 1; i <= 10; i++) {
                int num = i;
                threadPool.execute(() -> {
                    System.out.println(Thread.currentThread().getName() + "执行" + num);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭线程池
            threadPool.shutdown();
        }
    }
}
