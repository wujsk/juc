package com.cyy.lock;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @program: juc
 * @description:
 * @author: 酷炫焦少
 * @create: 2024-11-26 09:20
 **/
public class ThreadDemo4 {
    public static void main(String[] args) {
        // list线程不安全
        // List<String> list = new ArrayList<>(); 不安全
        // List<String> list = new Vector<>(); 安全 不常用
        // List<String> list = Collections.synchronizedList(new ArrayList<>()); 安全 不常用
        List<String> list = new CopyOnWriteArrayList<>(); // 安全 常用
        for(int i = 0; i < 10 ; i++) {
            new Thread(() -> {
                list.add("111");
                System.out.println(list);
            }, String.valueOf(i)).start();
        }
        Set<String> set = new CopyOnWriteArraySet<>();
        Map<String, String> map = new ConcurrentHashMap<String, String>();
    }
}
