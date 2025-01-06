# JUC

## 一、ThreadLocal

### 1.是什么

- ThreadLocal提供线程局部变量。这些变量与正常的变量**不同**，因为每一个线程在访问TheadLocal实例的时候（通过其get或set方法)**都有自己的、独立初始化的变量副本**。ThreadLocal实例通常是类中的私有静态字段，使用它的目的是希望将状态（例如，用户ID或事务ID）与线程关联起来。

### 2.干什么

- 实现**每一个线程都有自己专属的本地变量副本**(自己用自己的变量不麻烦别人，不和其他人共享，人人有份，人各一份)
- 主要解决了让每个线程绑定自己的值，通过使用get)和set()方法，获取默认值或将其值更改为当前线程所存的副本的值**从而避免了线程安全问题**，比如我们之前讲解的8锁案例，资源类是使用同一部手机，多个线程抢夺同一部手机使用，假如人手一份是不是天下太平? ?

![image-20250103125517377](.\assert\image-20250103125517377.png)

### 3.api介绍及实战

```java
@Data
class House {
    private int saleCount = 0;

    /*public synchronized void saleCountPlus() {
        saleCount++;
    }*/

    /*ThreadLocal<Integer> saleVolume = new ThreadLocal<>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };*/

    ThreadLocal<Integer> saleVolume = ThreadLocal.withInitial(() -> 0);

    public void saleVolumeByThreadLocal() {
        saleVolume.set(saleVolume.get() + 1);
    }

    public int getSaleVolumeByThreadLocal() {
        return saleVolume.get();
    }
}
public class ThreadLocalDemo {

    private static final int THREAD_NUM = 5;

    public static void main(String[] args) throws InterruptedException {
        House house = new House();
        CountDownLatch latch = new CountDownLatch(THREAD_NUM);
        LongAdder adder = new LongAdder();
        for (int i = 0; i < THREAD_NUM; i++) {
            int count = i;
            new Thread(() -> {
                /*for (int j = 0; j < count + 1; j++) {
                    // house.saleCountPlus();
                    adder.increment();
                    house.saleVolumeByThreadLocal();
                }
                System.out.println(Thread.currentThread().getName() + "\t" + "号销售卖出" +
                        house.getSaleVolumeByThreadLocal());*/
                // 养成好习惯记得要释放
                try {
                    for (int j = 0; j < count + 1; j++) {
                        // house.saleCountPlus();
                        adder.increment();
                        house.saleVolumeByThreadLocal();
                    }
                    System.out.println(Thread.currentThread().getName() + "\t" + "号销售卖出" +
                            house.getSaleVolumeByThreadLocal());
                } finally {
                    house.saleVolume.remove();
                }
                latch.countDown();
            }, String.valueOf(i + 1)).start();
        }
        latch.await();
        System.out.println("共销售出" + adder.sum() + "套房子");
    }
}
```

- 阿里巴巴规范中，会经常使用线程池，会出现线程复用的情况，如果ThreadLocal不remove会出现**内存泄漏**情况

  ```java
  class MyData {
  
      public ThreadLocal<Integer> threadLocal = ThreadLocal.withInitial(() -> 0);
  
      public void add() {
          threadLocal.set(threadLocal.get() + 1);
      }
  
  }
  
  public class ThreadLocalDemo2 {
      public static void main(String[] args) {
          MyData data = new MyData();
          ExecutorService threadPool = Executors.newFixedThreadPool(3);
          try {
              for (int i = 0; i < 10; i++) {
                  threadPool.submit(() -> {
                      try {
                          Integer beforeInteger = data.threadLocal.get();
                          data.add();
                          Integer afterInteger = data.threadLocal.get();
                          System.out.println(Thread.currentThread().getName() +
                                  "\t beforeInt:" + beforeInteger + "\t afterInt:" + afterInteger);
                      } finally {
                          data.threadLocal.remove();
                      }
                  });
              }
          } catch (Exception e) {
              e.printStackTrace();
          } finally {
              threadPool.shutdown();
          }
      }
  }
  ```

### 4.ThreadLocal源码分析

### 5.ThreadLocal内存泄漏问题

### 6.四大引用

#### 	6.1强引用

#### 	6.2软引用

#### 	6.3弱引用

#### 	6.4虚引用

​	**1.虚引用必须和引用队列(ReferenceQueue)联合使用**
​		虚引用需要java.lang.ref.PhantomReference类来实现,顾名思义，就是形同虚设，与其他几种引用都不同，虚引用并不会决定对象的生命周期。如果一个对象仅持有虚引用，那么它就和没有任何引用一样，在任何时候都可能被垃圾回收器回收，它不能单独使用也不能通过它访问对象，虚引用必须和引用队列(ReferenceQueue)联合使用。
​	**2.PhantomReference的get方法总是返回null**
​		虚引用的主要作用是跟踪对象被垃圾回收的状态。仅仅是提供了一种确保对象被finalize以后做某些事情的通知机制。
​		PhantomReference的get方法总是返回null，因此无法访问对应的引用对象。
​	**3.处理监控通知使用**
​		换句话说，设置虚引用关联对象的唯一目的，就是在这个对象被收集器回收的时候收到一个系统通知或者后续添加进一步的处理，用来实现比finalize机制更灵活的回收操作。

### 7.ThreadLocal之为什么源代码要用弱引用

![image-20250105212208676](D:\智能设备开发\juc\assert\image-20250105212208676.png)

- 当我们为threadLocal变量赋值，实际上就是当前的Entry(threadLocal实例为key，值为value)往这个threadLocaMap中存放。Entry中的key是弱引用，当threadLocal外部强引用被置为nullit=rnul,那么系统GC的时候，根据可达性分析，这个threadLocal实例就没有任何一条链路能够引用到它，这个ThreadLocal势必会被回收。这样一来，**ThreadLocalMap中就会出现key'snull的Enty，就没有办法访问这些key为snul的Entryivalue,如果当前线程再迟迟不结束的话，这些key/onull的Entryi的value就会一直存在一条强引用链:Thread Ref -> Thread -> ThreaLocalMap -> Entry>value永远无法回收，造成内存泄漏。**
- 当然，如果当前thread运行结束，threacLocal，threadLocalMap,.Entry没有引用链可达，在垃圾回收的时候都会被系统进行回收。
- 但在实际使用中我们**有时候会用线程池去维护我们的线程**，比如在Executors.newFixedThreadPool()时创建线程的时候，为了复用线程是不会结束的，所以threadLocal内存泄漏就值得我们小心。

### 8.ThreadLocal之清除脏Entry

![image-20250105212458531](.\assert\image-20250105212458531.png)

- **ThreadLocalMap**使用**ThreadLocal**的弱引用作为**key**，如果一个**ThreadLocal**没有外部强引用引用他，那么系统gc的时候，这个**ThreadLocal**势必会被回收，这样一来，**ThreadLocalMap中就会出现key为null的Entry**，**就没有办法访问这些key为null的Entry的value**，如果当前线程再**迟迟不结束**的话(比如正好用在线程池)，这些**key为nll的Entryi的value就会一直存在一条强引用链**。
- 虽然弱引用，保证了**key**指向的**ThreadLocal**对象能被及时回收，但是**v**指向的**value**对象是需要**TheadLocalMap**调用**get**、**set**发现**key为null时才会去回收整个entry、value**，**因此弱引用不能100%保证内存不泄露**。**我们要在不使用某个ThreadLocal对象后，手动调川remoev方法来删除它**，尤其是在线程池中，不仅仅是内存泄露的问题，因为线程池中的线程是重复使用的，意味着这个线程的**ThreadLocalMap**对象也是重复使用的，如果我们不手动调用**remove**方法，那么后面的线程就有可能获取到上个线程**遗留下来的value值**，造成**bug**。
- 从前面源代码中的**set,getEntry,remove**方法看出，在**threadLocal**的生命周期里，针对**threadLocal**存在的内存泄漏
  的问题，都会通过**expungeStaleEntry, cleanSomeSlots,replaceStaleEntry**这三个方法清理掉key为null的**脏entry**.

> 注：在 Java 中，如果一个强引用对象被赋值为`null`，这个对象在垃圾回收器运行时是会被回收的（前提是没有其他引用类型，如软引用、弱引用或虚引用等还指向这个对象）。expungeStaleEntry, cleanSomeSlots,replaceStaleEntry这三个方法是将key为null的数据中的value（强引用）设为null，就会被垃圾回收机制回收。

### 9.ThreadLocal最佳实践总结

![image-20250105213903270](.\assert\image-20250105213903270.png)

![image-20250105213927464](.\assert\image-20250105213927464.png)

## 二、Java对象内存布局和对象头

### 1.Java对象内存布局

- 在HotSpot虚拟机里，对象在堆内存中的存储布局可以划分为三个部分:对象头（Header)、实例数据（Instance Data）和对齐填充(Padding) 。（保证八个字节的倍数）

  ![image-20250105215635774](.\assert\image-20250105215635774.png)

  - 对象头
    - 1.对象标记（Mark Word）
    - 2.类元信息（又叫类型指针 Class Poniter）

## 三、Synchronized与锁升级

## 四、AbstractQueuedSynchronizer之AQS

## 五、ReentrantLock、ReentrantReadWriteLock、StampedLock讲解
