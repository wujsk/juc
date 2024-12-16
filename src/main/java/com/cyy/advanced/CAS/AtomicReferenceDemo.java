package com.cyy.advanced.CAS;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @program: juc
 * @description: 原子引用
 * @author: cyy
 * @create: 2024-12-16 19:10
 **/
public class AtomicReferenceDemo {
    public static void main(String[] args) {
        AtomicReference<User> atomicReference = new AtomicReference<>();
        User u1 = new User("z3", 22);
        User u2 = new User("l4", 28);

        atomicReference.set(u1);

        System.out.println(atomicReference.compareAndExchange(u1, u2) + "\t" + atomicReference.get().toString());
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class User{
    private String username;

    private int age;
}
