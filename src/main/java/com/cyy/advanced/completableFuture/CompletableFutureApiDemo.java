package com.cyy.advanced.completableFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @program: juc
 * @description:
 * @author: 酷炫焦少
 * @create: 2024-12-04 09:46
 **/
public class CompletableFutureApiDemo {
    public static void main(String[] args) {
        m6();
    }

    private static void m6() {
        // 对计算结果合并
        CompletableFuture<Integer> completableFuture1 = CompletableFuture.supplyAsync(() -> 1);
        // 先完成的，阻塞
        System.out.println(completableFuture1.thenCombine(CompletableFuture.supplyAsync(() -> 2), (f1, f2) -> {
            return f1 + f2;
        }).thenCombine(CompletableFuture.supplyAsync(() -> 3), (a, b) -> {
            return a + b;
        }).join());
    }

    private static void m5() {
        // 对计算速度选用
        CompletableFuture<String> playA = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.MICROSECONDS.sleep(20);
                System.out.println(Thread.currentThread().getName() + "正在执行中");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "A";
        });
        CompletableFuture<String> playB = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.MICROSECONDS.sleep(20);
                System.out.println(Thread.currentThread().getName() + "正在执行中");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "B";
        });
        CompletableFuture<String> result = playA.applyToEither(playB, f -> {
            return f + " is winer";
        });
        // main	-------A is winer
        System.out.println(Thread.currentThread().getName() + "\t" + "-------" + result.join());
    }

    private static void m4() {
        // run会跟着前一个thenRun或者thenRunAsync的线程池一样
        // 而thenRunAsync不会
        ExecutorService threadPool = Executors.newFixedThreadPool(10);
        CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                // 如果main线程先执行，那么根据系统优化策略，main方法会接管这个线程
                TimeUnit.MICROSECONDS.sleep(20);
                System.out.println(Thread.currentThread().getName() + "正在执行中");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 1;
        }, threadPool).thenRunAsync(() -> {
            try {
                TimeUnit.MICROSECONDS.sleep(20);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "正在执行中");
        }, threadPool).thenRunAsync(() -> {
            try {
                TimeUnit.MICROSECONDS.sleep(20);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "正在执行中");
        }).thenRunAsync(() -> {
            try {
                TimeUnit.MICROSECONDS.sleep(20);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "正在执行中");
        }, threadPool).thenRun(() -> {
            try {
                TimeUnit.MICROSECONDS.sleep(20);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + "正在执行中");
        });
        try {
            System.out.println(completableFuture.get(2, TimeUnit.SECONDS));
        } catch (Exception e) {
            e.printStackTrace();
        }
        threadPool.shutdown();
    }

    private static void m3() {
        // 对计算结果进行消费
        CompletableFuture.supplyAsync(() -> {
            return 1;
        }).thenApply(r -> {
            return  r + 2;
            // apply有返回值
        }).thenApply(r -> {
            return r + 3;
           // accept没有返回值
        }).thenAccept(r -> {
            System.out.println(r);
        }).thenRun(() -> {
            System.out.println(Thread.currentThread().getName() + "正在执行中");
            System.out.println("我是一个新线程");
        });

        // 结果为null 说明thenRun不需要之前的结果 需要在thenRun之前消费掉之前的结果
        System.out.println(CompletableFuture.supplyAsync(() -> "123").thenRun(() -> {}).join());
        // run之后 再次accept结果，已经接收不到了
        System.out.println(CompletableFuture.supplyAsync(() -> "123")
                .thenRun(() -> {})
                .thenAccept(System.out::println)
                .join());
        // 消费掉，join没有值了
        System.out.println(CompletableFuture.supplyAsync(() -> "1").thenAccept(System.out::println).join());
    }

    private static void m2() {
        // 对计算结果进行处理
        ExecutorService pool = Executors.newFixedThreadPool(3);
        CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return 1;
        }, pool).handle((r, e) -> {
            // 当前步骤有错，不会进行下一步，如果想要处理这个结果，可以用handle
            int result = 10 /0;
            return r + 2;
        }).thenApply(r -> {
            return r + 3;
        }).whenComplete((r, e) -> {
            if (e == null) {
                System.out.println(r);
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return null;
        });
        // 切记一定要关闭线程池
        pool.shutdown();
        System.out.println(Thread.currentThread().getName() + "先去忙其他去了");
    }

    private static void m1() {
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return "abc";
        });
        try {
            // 1.获取结果api
            // System.out.println(completableFuture.get());
            // System.out.println(completableFuture.get(1, TimeUnit.SECONDS)); // 可以设置过期时间
            // System.out.println(completableFuture.join());
            // 如果还有没计算完成 返回getNow中的参数 如果计算完成了就返回结果
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println(completableFuture.getNow("xxx"));
            // 2. 主动触发计算(如果上面就计算完成的，就返回计算完成的，如果没有，就返回主动计算结果)
            System.out.println(completableFuture.complete("123") + completableFuture.join());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
