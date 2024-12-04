package com.cyy.foundation.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @program: juc
 * @description: 演示线程的三种分类
 * @author: 酷炫焦少
 * @create: 2024-11-27 17:01
 **/
public class ThreadPoolDemo1 {
    public static void main(String[] args) {
        // 一池五线程
        ExecutorService threadPool1 = Executors.newFixedThreadPool(5);
        // 一池一线程
        ExecutorService threadPool2 = Executors.newSingleThreadExecutor();
        // 一池可扩展线程 根据需求决定
        ExecutorService threadPool3 = Executors.newCachedThreadPool();
        // 五个窗口 十个客户
        try {
            for (int i = 1; i <= 10; i++) {
                int num = i;
                threadPool3.execute(() -> {
                    System.out.println(Thread.currentThread().getName() + "执行" + num);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭线程池
            threadPool3.shutdown();
        }
    }
}
