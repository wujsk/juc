package com.cyy.pool;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * @program: juc
 * @description: Fork/Join分支合并框架
 * @author: 酷炫焦少
 * @create: 2024-11-27 18:21
 **/
class MyTask extends RecursiveTask<Integer> {
    // 拆分 差值不能超过十,计算十以内的计算
    private static final Integer VALUE = 10;
    private Integer begin; //拆分开始值
    private Integer end; // 拆分结束值
    private Integer result; // 结果
    public MyTask(Integer begin, Integer end) {
        this.begin = begin;
        this.end = end;
        this.result = 0;
    }
    @Override
    protected Integer compute() {
        // 判断相加两个值是否超过10
        if (end - begin <= VALUE) {
            for (int i = begin; i <= end; i++) {
                result += i;
            }
        } else {
            // 进一步拆分
            Integer mid = (begin + end) >> 1;
            // 拆分左边
            MyTask myTask1 = new MyTask(begin, mid);
            // 拆分右边
            MyTask myTask2 = new MyTask(mid + 1, end);
            // 调用方法拆分
            myTask1.fork();
            myTask2.fork();
            // 合并结果
            result += myTask1.join() + myTask2.join();
        }
        return result;
    }
}
public class ForkJoinPoolDemo {
    public static void main(String[] args) {
        // 创建myTask对象
        MyTask myTask = new MyTask(1, 100);
        // 创建分支合并池对象
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        ForkJoinTask<Integer> forkJoinTask = forkJoinPool.submit(myTask);
        // 获取最终合并之后结果
        Integer result = null;
        try {
            result = forkJoinTask.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        System.out.println(result);
        // 关闭池对象
        forkJoinPool.shutdown();
    }
}
