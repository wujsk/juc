package com.cyy.advanced.CAS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * @program: juc
 * @description:
 * @author: cyy
 * @create: 2024-12-16 19:36
 **/
public class AtomicStampedDemo {
    public static void main(String[] args) {
        // 用于解决 ABA 问题
        Book b1 = new Book(1, "java");
        Book b2 = new Book(2, "python");
        AtomicStampedReference<Book> stampedReference = new AtomicStampedReference<>(b1, 1);
        System.out.println(stampedReference.getReference() + "\t" + stampedReference.getStamp());

        boolean b = stampedReference.compareAndSet(b1, b2, stampedReference.getStamp(), stampedReference.getStamp() + 1);
        System.out.println(b + "\t" + stampedReference.getReference() + "\t" + stampedReference.getStamp());

        boolean c = stampedReference.compareAndSet(b2, b1, stampedReference.getStamp(), stampedReference.getStamp() + 1);
        System.out.println(c + "\t" + stampedReference.getReference() + "\t" + stampedReference.getStamp());
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Book {
    private int id;

    private String bookName;
}
