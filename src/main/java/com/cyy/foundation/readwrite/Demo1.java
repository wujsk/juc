package com.cyy.foundation.readwrite;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @program: juc
 * @description: 可重入读写锁 写锁可以降级为读锁 读锁不能升为写锁(写的时候可以读 读的时候不可以写)
 * @author: 酷炫焦少
 * @create: 2024-11-27 16:16
 **/
public class Demo1 {
    public static void main(String[] args) {
        // 创建可重入读写锁
        ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
        // 获取读锁
        ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();
        // 获取写锁
        ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
        // 写锁降级为读锁
        // writeToRead(writeLock, readLock);
        // 读锁升为写锁(产生死锁)
        readToWrite(writeLock, readLock);
        // 以上例子说明写锁可以降级为读锁，读锁不能升为写锁
    }

    private static void readToWrite(ReentrantReadWriteLock.WriteLock writeLock, ReentrantReadWriteLock.ReadLock readLock) {
        readLock.lock();
        System.out.println("reading---");
        writeLock.lock();
        System.out.println("hello world!");
        writeLock.unlock();
        readLock.unlock();
    }

    private static void writeToRead(ReentrantReadWriteLock.WriteLock writeLock, ReentrantReadWriteLock.ReadLock readLock) {
        writeLock.lock();
        System.out.println("hello world!");
        readLock.lock();
        System.out.println("reading---");
        writeLock.unlock();
        readLock.unlock();
    }
}
