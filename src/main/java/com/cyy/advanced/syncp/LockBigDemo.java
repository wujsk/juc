package com.cyy.advanced.syncp;

/**
 * @program: juc
 * @author: cyy
 * @create: 2025-01-08 20:37
 * @description: 锁粗化
 * 假如方法中首尾相接，前后相邻的都是同一个锁对象，那IT编译器就会把这几个synchronized块合并成一个大块,
 * 加粗加大范围，一次申请锁使用即可，避免次次的申请和释放锁，提升了性能
 **/
public class LockBigDemo {

    static Object object = new Object();

    public static void main(String[] args) {
        new Thread(() -> {
            synchronized (object) {
                System.out.println("1111");
            }
            synchronized (object) {
                System.out.println("2222");
            }
            synchronized (object) {
                System.out.println("3333");
            }
            synchronized (object) {
                System.out.println("4444");
            }

            // 以上变成
            synchronized (object) {
                System.out.println("1111");
                System.out.println("2222");
                System.out.println("3333");
                System.out.println("4444");
            }
        }, String.valueOf("t1")).start();
    }
}
