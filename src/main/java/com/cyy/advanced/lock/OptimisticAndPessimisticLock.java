package com.cyy.advanced.lock;

import java.util.concurrent.TimeUnit;

/**
 * @program: juc
 * @description: 乐观锁和悲观锁
 * @author: 酷炫焦少
 * @create: 2024-12-04 22:28
 **/
public class OptimisticAndPessimisticLock {
    /**
     * synchronized和Lock的实现类都是悲观锁：认为自己在使用用户数据的时候，一定有人来修改用户数据。
     * 乐观锁：认为自己在使用用户数据的时候，不会被别人修改。
     * 乐观锁判断规则：1.版本号机制 Version
     *         2.CAS（compare-and-swap）算法，Java中原子类的递增操作就是基于CAS算法的。乐观锁会产生ABA问题。
     * @param args
     */

    /**
     * 谈谈你对多线程锁的理解，8锁案例说明
     * 口诀： 线程 操作 资源类
     * 8锁案例说明：
     * 同步指的是加不加synchronized
     *  1. 标准访问有ab两个线程，请问先发送邮件还是短信？ // 先邮件后短信
     *  2. sendEmail添加延迟，先发短信还是先发邮件？ // 先邮件后短信
     *  3. 添加一个普通的hello方法，先执行hello还是先执行sendEmail？ 先hello再sendEmail
     *  4. 现在有两部手机，一个发短信，一个发邮件，请问先发邮件还是发短信？ // 先短信后邮件
     *  5. 两个静态同步方法，一个手机，请问先执行发邮件还是发短信？ // 先发邮件再发短信
     *  6. 两个静态同步方法，两个手机，请问先执行发邮件还是发短信？ // 先发邮件再发短信
     *  7. 一个静态同步方法，一个普通同步方法，一个手机，请问先执行发邮件还是发短信？ // 先发短信再发邮件
     *  8. 一个静态同步方法，一个普通同步方法，两个手机，请问先执行发邮件还是发短信？ // 先发短信再发邮件
     *  笔记总结: 不同锁争的资源时机不同
     *  1-2 锁对象（this 对象的实例化）
     *  一个对象里面如果有多个synchronized方法，某一个时刻内，只要一个线程去调用其中的一个synchronized方法了,
     *  其它的线程都只能等待，换句话说，某一个时刻内，只能有唯一的一个线程去访问这些synchronized方法
     *  锁的是当前对象this，被锁定后，其它的线程都不能进入到当前对象的其它的synchronized方法
     *  3
     *  hello方法没有加锁，没有进行资源争夺
     *  4
     *  由于上方1-2知道是对象锁，所以两个对象，所以锁的对象不同，所以不会发生锁竞争，不会发生阻塞。
     *  5-6 类锁
     *  对于普通同步方法，锁的是当前实例对象，通常指的是this，具体的一部部手机，所有普通同步方法用的都是同一把锁 --实例对象本身
     *  对于静态同步方法，锁的是当前类的class对象，如Phone.class唯一模板
     *  对于同步方法块，锁的是synchronized括号内的对象
     *  7-8
     *  当一个线程试图访问同步代码时，他首先必须得到锁,正常退出或抛出异常时必须释放锁
     *
     *  所有普通同步方法用的都是同一把锁--实例对象本身 就是new出来的具体实例对象本身，本类this
     *  也就是说如果一个实例对象的普通方法获取锁后，该实例对象的其他普通方法必须等待获取锁的方法释放锁后才能获取锁
     *
     *  所有的静态同步方法用的也是同一把锁--类对象本身，就是我们说过的唯一模板Class
     *  具体实例对象this和唯一模板Class，这两把锁是不同的对象，所以静态同步方法与普通同步方法之间是不会有竞态条件的
     *  但是一旦一个静态同步方法获取锁后，其他静态同步方法都必须等待该方法释放锁后才能获取锁
     */
    // 资源类
    static class Phone {

        public static synchronized void sendEmail() {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("---发邮件---");
        }

        public synchronized void sendSMS() {
            System.out.println("---发短信---");
        }

        public void hello() {
            System.out.println("---hello---");
        }
    }

    public static void main(String[] args) {
        // 先打印发邮件后打印发短信
        Phone phone = new Phone();
        Phone phone2 = new Phone();
        new Thread(() -> {
            phone.sendEmail(); // 1,2,3
            // phone.sendEmail(); // 4, 5
            // phone.sendEmail(); // 6
            // phone.sendEmail(); // 7
            phone.sendEmail(); // 8
        }, "t1").start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        new Thread(() -> {
            phone.sendSMS(); // 1,2
            // phone.hello(); // 3
            // phone2.sendSMS(); // 4
            // phone.sendSMS(); // 5
            // phone2.sendSMS(); // 6
            // phone.sendSMS(); // 7
            // phone2.sendSMS();
        }, "t2").start();
    }

}
