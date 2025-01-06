package com.cyy.advanced.objectHead;

import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.vm.VM;

/**
 * @program: juc
 * @author: cyy
 * @create: 2025-01-06 15:54
 * @description: JOL:Java Object Layout
 *
 * 1.默认配置，启动了压缩指针，-XX:+UseCompressedClassPointers
 **/
public class JOLDemo {
    public static void main(String[] args) {
        // System.out.println(VM.current().details());

        // 所有对象分配的字节都是8的倍数
        // System.out.println(VM.current().objectAlignment()); // 8

        // Object o = new Object();
        // System.out.println(ClassLayout.parseInstance(o).toPrintable());

        System.out.println(ClassLayout.parseClass(Customer.class).toPrintable());
    }
}

class Customer {
    int id;

    boolean flag;
}
