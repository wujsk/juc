package com.cyy.advanced.completableFuture;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.*;

/**
 * @program: juc
 * @description:
 * @author: 酷炫焦少
 * @create: 2024-11-28 09:59
 **/
public class CompletableFutureDemo {
    /**
     * 创建CompletableFuture有四大核心方法：
     * 不指定线程池，线程池默认是用ForkJoinPool
     * runAsync:1.public static CompletableFuture<Void> runAsync(Runnable runnable)
     *          2.public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor)
     * supplyAsync:1.public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)
     *             2.public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)
     * @param args
     */
    public static void main(String[] args) {

    }

    private static void m5() throws InterruptedException, ExecutionException {
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            return 1;
        });
        // 无需抛出异常
        System.out.println(completableFuture.join());
        // 需要抛出异常
        System.out.println(completableFuture.get());
    }

    private static void m4() {
        /**
         * 你会发现为什么没有打印结果，是因为main线程结束，supplyAsync所用的线程池也会关闭
         * 解决方法: 1.让main线程等一下（显然这并不是很好的方法）
         *          2.可以采用自定义线程池,（切记！切记！切记！线程池一定要关闭，否则线程不会结束）
         *           2.1 个人觉得尽量不可以使用completableFuture.join()，因为会发生阻塞，使用try-finally无疑是最好的决定
         */
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        try {
            CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
                // 暂停几秒
                int result = ThreadLocalRandom.current().nextInt(10);
                System.out.println("result：" + result);
                try {
                    System.out.println(Thread.currentThread().getName() + "---come in");
                    TimeUnit.SECONDS.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (result < 2) {
                    int r = result / 0;
                }
                return 1;
                // r是supplyAsync的结果 e是异常信息
            }, executorService).whenComplete((r, e) -> {
                // 不管有没有这个异常都会走这个方法
                // 如果无异常，打印结果
                if (Objects.isNull(e)) {
                    System.out.println(r);
                }
            }).exceptionally(e -> {
                e.printStackTrace();
                System.out.println("异常情况：" + e.getCause() + ":" + e.getMessage());
                return null;
            });
        } finally {
            executorService.shutdown();
        }
        System.out.println(Thread.currentThread().getName() + "先去忙其他去了");
        // try {
        //     TimeUnit.SECONDS.sleep(3);
        // } catch (InterruptedException e) {
        //     throw new RuntimeException(e);
        // }
    }

    private static void m3() {
        /**
         * main先去忙其他的去了
         // * ForkJoinPool.commonPool-worker-1---come in
         * ForkJoinPool.commonPool-worker-1
         * 1
         * 可以发现采用异步回调的方式
         */
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            // 暂停几秒
            try {
                System.out.println(Thread.currentThread().getName() + "---come in");
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 1;
        });
        System.out.println(Thread.currentThread().getName() + "先去忙其他的去了");
        try {
            System.out.println(completableFuture.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void m2() {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            // 暂停几秒
            try {
                System.out.println(Thread.currentThread().getName());
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 1;
        }, executorService);
        try {
            System.out.println(future.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void m1() {
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
            System.out.println(Thread.currentThread().getName());
            // 暂停几秒
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        try {
            System.out.println(completableFuture.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
