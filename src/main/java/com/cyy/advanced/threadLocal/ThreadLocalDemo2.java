package com.cyy.advanced.threadLocal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @program: juc
 * @author: cyy
 * @create: 2025-01-03 13:24
 * @description: 【强制】必须回收自定义的ThreadLocal 变量，尤其在线程池场景下，线程经常会被复用，
 * 如果不清理自定义的 ThreadLocal变量，可能会影响后续业务逻辑和造成内存泄露等问题。尽量在代理中使用try-finally块进行回收。
 **/
class MyData {

    public ThreadLocal<Integer> threadLocal = ThreadLocal.withInitial(() -> 0);

    public void add() {
        threadLocal.set(threadLocal.get() + 1);
    }

}

public class ThreadLocalDemo2 {
    public static void main(String[] args) {
        MyData data = new MyData();
        ExecutorService threadPool = Executors.newFixedThreadPool(3);
        try {
            for (int i = 0; i < 10; i++) {
                threadPool.submit(() -> {
                    try {
                        Integer beforeInteger = data.threadLocal.get();
                        data.add();
                        Integer afterInteger = data.threadLocal.get();
                        System.out.println(Thread.currentThread().getName() +
                                "\t beforeInt:" + beforeInteger + "\t afterInt:" + afterInteger);
                    } finally {
                        data.threadLocal.remove();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }
}
