package com.cyy.future;

import java.util.concurrent.*;

/**
 * @program: juc
 * @description:
 * @author: 酷炫焦少
 * @create: 2024-11-28 08:45
 **/
class MyThread1 implements Runnable {
    @Override
    public void run() {
        for (int i = 1; i <= 100; i++) {

        }
    }
}
class MyThread2 implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        Integer result = 0;
        for (int i = 1; i <= 100; i++) {
            result += i;
        }
        return result;
    }
}
// Runnable 无返回值 无异常 Callable有返回值 由异常
public class FutureDemo {
    /**
     * Future 优缺点
     * 优点：会极大的节省实时间
     * 缺点：1.get()会阻塞 get()可以设置过期时间，时间一过，就不再获得，会报异常
     *      2.isDone()会判断FutureTask任务是否完成，需要不断轮询，查看是否完成，会浪费内存
     * 如何解决缺点呢？利用异步回调 此时推出CompletableFuture以声明式处理
     * @param args
     */
    public static void main(String[] args) {

    }

    // 未获得m2的返回值 m1耗时：6013（毫秒）  m2耗时：1（毫秒）
    // 获取m2的返回值 m1耗时：6032 m2耗时：6024
    private static void m2() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        long begin = System.currentTimeMillis();
        FutureTask<Integer> task1 = new FutureTask<>(() -> {
           TimeUnit.SECONDS.sleep(1);
           return 1;
        });
        executorService.submit(task1);
        System.out.println(task1.get());
        FutureTask<Integer> task2 = new FutureTask<>(() -> {
            TimeUnit.SECONDS.sleep(2);
            return 2;
        });
        executorService.submit(task2);
        System.out.println(task2.get());
        FutureTask<Integer> task3 = new FutureTask<>(() -> {
            TimeUnit.SECONDS.sleep(3);
            return 3;
        });
        executorService.submit(task3);
        System.out.println(task3.get());
        long end = System.currentTimeMillis();
        System.out.println("m2耗时：" + (end - begin));
    }

    /**
     * 单线程执行 至少6秒
     */
    private static void m1() {
        long begin = System.currentTimeMillis();
        try {
            TimeUnit.SECONDS.sleep(1);
            TimeUnit.SECONDS.sleep(2);
            TimeUnit.SECONDS.sleep(3 );
        } catch (Exception e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        System.out.println("m1耗时：" + (end - begin));
    }

    private static void testFuture() {
        FutureTask<Integer> task = new FutureTask<>(new MyThread2());
        Thread a = new Thread(task, "A");
        a.start();
        try {
            System.out.println(task.get());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
