package com.cyy.advanced.threadLocal;

import java.lang.ref.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

/**
 * @program: juc
 * @author: cyy
 * @create: 2025-01-03 14:24
 * @description:
 **/
class MyObject {
    // 这个方法一般不用复写，只是为了写一个案例
    @Override
    protected void finalize() throws Throwable {
        try {
            System.out.println("invoked finalize method~~~");
        } finally {
            super.finalize();
        }
    }
}
public class ReferenceDemo {
    public static void main(String[] args) throws InterruptedException {
        ThreadLocal<String> t1 = new ThreadLocal<>();
        t1.set("123");
        System.out.println(t1.get());
    }

    /**
     * 1.虚引用必须和引用队列(Reference Queue)联合使用
     * 2.PhantomReference的get方法总是返回null
     * 3.处理监控通知使用
     * @throws InterruptedException
     */
    private static void phantomReference() throws InterruptedException {
        MyObject myObject = new MyObject();
        System.out.println(myObject);
        ReferenceQueue<MyObject> referenceQueue = new ReferenceQueue<>();
        PhantomReference<MyObject> phantomReference = new PhantomReference<>(myObject, referenceQueue);

        List<byte[]> list = new ArrayList<>();
        new Thread(() -> {
           while (true) {
               list.add(new byte[5 * 1024 * 1024]);
               try {
                   TimeUnit.MILLISECONDS.sleep(500);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
               System.out.println(phantomReference.get() + "\t list add ok");
           }
        }).start();

        new Thread(() -> {
            while (true) {
                Reference<? extends MyObject> poll = referenceQueue.poll();
                if (poll != null) {
                    System.out.println(poll.get());
                    System.out.println("有数据加入虚引用队列中了");
                    break;
                }
            }
        }).start();
    }

    /**
     * 对于弱引用对象来说，只要垃圾回收机制一运行，不管JVM的内存空间是否足够，都会回收对象占用的内存。
     * @throws InterruptedException
     */
    private static void weakReference() throws InterruptedException {
        WeakReference<MyObject> weakReference = new WeakReference<>(new MyObject());
        System.out.println(weakReference.get());

        System.gc();
        TimeUnit.SECONDS.sleep(1);
        System.out.println(weakReference.get());
    }

    /**
     * 内存充足的时候 不会被回收
     * 内存不充足的时候 会被回收
     *
     * 使用场景：比如图片，或一段时间就需要重新下载。 例如手机，杀后台。
     * @throws InterruptedException
     */
    private static void softReference() throws InterruptedException {
        // MyObject myObject = new MyObject(); 不可以直接new出来，直接new出来会变成强引用
        SoftReference<MyObject> softReference = new SoftReference<>(new MyObject());

        System.gc();
        TimeUnit.SECONDS.sleep(1);

        // 内存充足
        System.out.println(softReference.get());

        // 需要添加-Xms10m -Xmx10m
        try {
            byte[] bytes = new byte[20 * 1024 * 1024];
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(softReference.get());
    }

    /**
     * 强应用为默认支持状态
     * 根据垃圾回收机制进行回收
     * @throws InterruptedException
     */
    private static void strongReference() throws InterruptedException {
        MyObject myObject = new MyObject();
        System.out.println(myObject);

        myObject = null;
        System.gc(); // 人工开启GC

        TimeUnit.SECONDS.sleep(1);
        System.out.println(myObject);
    }
}
