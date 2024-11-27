package com.cyy.blockingQueue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @program: juc
 * @description:
 * @author: 酷炫焦少
 * @create: 2024-11-27 16:40
 **/
public class BlockingQueueDemo {
    public static void main(String[] args) {
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(3);
        // 第一组方法
        // System.out.println(queue.add("a")); // 未满为true 满会IllegalStateException异常
        // System.out.println(queue.element()); // 队尾元素
        // System.out.println(queue.remove()); // 移除输出队尾元素 如果队列中无元素会。会报异常
        // 第二组
        // queue.offer("a"); // offer和poll就是与第一组有会不会报异常的区别
        // queue.poll();
        // 第三组
        // try {
        //     queue.put("a");
        //     queue.put("b");
        //     queue.put("c");
        //     // queue.put("d"); // 会被阻塞
        //     System.out.println(queue.take());
        //     System.out.println(queue.take());
        //     System.out.println(queue.take());
        //     System.out.println(queue.take()); // 会被阻塞
        // } catch (InterruptedException e) {
        //     throw new RuntimeException(e);
        // }
        // 第四组
        // 可以为offer和poll设置阻塞时间，时间超时不再阻塞
    }
}
