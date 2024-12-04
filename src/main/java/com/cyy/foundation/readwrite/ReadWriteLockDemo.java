package com.cyy.foundation.readwrite;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @program: juc
 * @description:
 * @author: 酷炫焦少
 * @create: 2024-11-27 15:43
 **/

// 创建资源类
class MyCache {
    // 创建map
    private volatile Map<String, Object> map = new HashMap<>();
    // 创建读写锁
    private ReadWriteLock rwLock = new ReentrantReadWriteLock();
    // 放数据
    public void put(String key, Object value) {
        Lock lock = rwLock.writeLock();
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getName() + "正在进行写操作" + key);
            TimeUnit.MICROSECONDS.sleep(300);
            // 暂停一会
            map.put(key, value);
            System.out.println(Thread.currentThread().getName() + "已完成写操作" + key);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
    // 取数据
    public Object get(String key) {
        Lock lock = rwLock.readLock();
        lock.lock();
        Object result = null;
        try {
            System.out.println(Thread.currentThread().getName() + "正在进行读操作" + key);
            // 暂停一会
            TimeUnit.MICROSECONDS.sleep(300);
            result = map.get(key);
            System.out.println(Thread.currentThread().getName() + "已完成读操作" + key);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
        return result;
    }
}
public class ReadWriteLockDemo {
    public static void main(String[] args) {
        MyCache myCache = new MyCache();
        // 创建线程放数据
        for (int i = 1; i <= 5; i++) {
            final int num = i;
            new Thread(() -> {
                myCache.put(String.valueOf(num), num);
            }, String.valueOf(i)).start();
        }
        // 创建线程读数据
        for (int i = 1; i <= 5; i++) {
            final int num = i;
            new Thread(() -> {
                System.out.println(myCache.get(String.valueOf(num)));
            }, String.valueOf(i)).start();
        }
    }
}
