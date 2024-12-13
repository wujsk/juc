package com.cyy.advanced.JMM;

import java.util.concurrent.TimeUnit;

/**
 * @program: juc
 * @description:
 * @author: cyy
 * @create: 2024-12-13 15:27
 * 重排序：
 *  重排序是指编译器和处理器为了优化程序性能而对指令序列进行重新排序的一种手段，有时候会改变程序语句的先后顺序
 *  不存在数据依赖性，可以重排序
 *    存在数据依赖行，不可以重排序
 *   但重排后的指令绝对不能改变原有的串行语句！这点在并发设计中必须重点考虑！
 * volatile
 * 特点：
 *  1.可见性
 *  2.有序性（禁重排）
 * 内存语义：
 *  1.当写一个volatile变量时，JMM会把该线程对应的本地内存中的共享变量值立即刷新h回主内存中。
 *  2.当读一个volatile变量时，JMM会把该线程对应的本地内存设置为无效，重新回到主内存中读取最新共享变量。
 *  所以volatile的写内存语义是直接刷新到主内存中，读内存语义是直接从主内存中读取。
 * 如何保证可见和有序？
 *  内存屏障Memory Barrier
 * 内存屏障
 *  是什么？
 *    1.内存屏障（也称内存栅栏，屏障指令等，是一类同步屏障指令，是CPU或编译器在对内存随机访问的操作中的一个同步点，
 *      使得此点之前的所有读写操作都执行后才可以开始执行此点之后的操作)，避免代码重排疗。
 *      内存屏障其实就是一种JVM指令，Java内存模型的重排规则会要求Java编译器在生成JVM指令时插入特定的内存屏障指令，
 *      通过这些内存屏障指令，可见性和有序性(禁重排)，但volatile无法保证原子性。
 *    2.内存屏障之前的所有写操作都要回写到主内存，
 *    3.内存屏障之后的所有读操作都能获得内存屏障之前的所有写操作的最新结果(实现了可见性)。
 *    4.写屏障〈Store Memory Barrier):告诉处理器在写屏障之前将所有存储在缓存(store bufferes)中的数据同步到主内存。
 *      也就是说当看到Store屏障指令，就必须把该指令之前所有写入指令执行完毕才能继续往下执行。
 *    5.读屏障(Load Memory Barrier):处理器在读屏障之后的读操作，都在读屏障之后执行。
 *      也就是说在Load屏障指令之后就能够保证后面的读取数据指令一定能够读取到最新的数据。
 *    6.因此重排序时，不允许把内存屏障之后的指令重排序到内存屏障之前。
 *      一句话:对一个volatile变量的写,先行发生于任意后续对这个volatile变量的读，也叫写后读。
 *   内存屏障种类：
 *     粗分两种：
 *      1.读屏障(Load Barrier)：在读指令之前插入读屏障，让工作内存或CPU高速缓存当中的缓存数据失效，重新回到主内存中获取最新数据。
 *      2.写屏障(Store Barrier)：在写指令之后插入写屏障，强制把写缓冲区的数据刷回到主内存中。
 *     细分四种：
 *      1.LoadLoad
 *      2.StoreStore
 *      3.LoadStore
 *      4.StoreLoad
 *  什么叫保证有序性？
 *    禁重排：通过内存屏障禁重排
 *    1.重排序有可能影响程序的执行和实现因此，我们有时候希望告诉JVM你别“自作聪明”给我重排序，我这里不需要排序，听主人的。
 *    2.对于编译器的重排序，JMM会根据重排序的规则，禁止特定类型的编译器重排序。
 *    3.对于处理器的重排序，Java编译器在生成指令序列的适当位置，插入内存屏障指令，来禁止特定类型的处理器排序。
 *  happens-before之volatile变量规则
 *    当第一个操作为volatile读时，不论第二个操作是什么，都不能重排疗。这个操作保证了volatilei读之后的操作不会被重排到volatilei读之前。
 *    当第二个操作为volatle写时，不论第一个操作是什么，都不能重排序。这个操作保证了volatile写之前的操作不会被重排到volatile写之后。
 *    当第一个操作为volatile写时，第二个操作为volatile读时，不能重排。
 *  JMM就将内存屏障插入策略分为4种规则
 **/
public class VolatileTest {

    static volatile boolean flag = true;

    public static void main(String[] args) {
        MyNumber myNumber = new MyNumber();
        for (int i = 1; i <= 10; i++) {
            new Thread(() -> {
                for (int j = 1; j <= 1000; j++) {
                    myNumber.addPlus();
                }
            }, "t" + i).start();
        }
        try {TimeUnit.SECONDS.sleep(1);} catch (InterruptedException e) {throw new RuntimeException(e);}

        // main num:9972
        System.out.println(Thread.currentThread().getName() + " num:" + myNumber.num);
    }

    static class MyNumber {
        volatile int num;

        // int num;

        public void addPlus() {
            num++;
        }

        // public synchronized void addPlus() {
        //     num++;
        // }
    }

    //保证可见性
    private static void m1() {
        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "\t ---come in");
            // static boolean flag = true; 主线程设置为false，终止不了，说明没有可见性
            // static volatile boolean flag = true; 抓线程设置为false，终止了，说明volatile 有可见性
            while(flag) {

            }
            System.out.println(Thread.currentThread().getName() + "\t ---come out");
        }, "t1").start();

        try {TimeUnit.SECONDS.sleep(1);} catch (InterruptedException e) {throw new RuntimeException(e);}

        flag = false;
    }
}
