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
    - 1.对象标记（Mark Word）(占用八字节)
    
      ![image-20250106152019424](.\assert\image-20250106152019424.png)
    
      ![image-20250106152118537](.\assert\image-20250106152118537.png)
    
      ![image-20250106152631765](D:\智能设备开发\juc\assert\image-20250106152631765.png)
    
      ![image-20250106154823123](.\assert\image-20250106154823123.png)
    
    - 2.类元信息（又叫类型指针 Class Poniter）（开启指针压缩是占用四字节、反之则占八字节）
    
      - 对象指向他的类元数据的指针，虚拟机通过这个指针来确定这个对象是哪个类的实例。
      - 3.长度（数组类型）
    
  - 实例数据
  
    - 存放类的属性数据信息，包括父类的属性信息。
  
  - 对齐填充
  
    - 虚拟机要求对象起始地址必须是8字节的整数倍。填充数据不是必须存在的，仅仅是为了字节对齐这部分内存按8字节补充对齐。

## 三、Synchronized与锁升级

### 1.Synchronized的升级流程

- Synchronized用的锁是存在Java对象头里的Mark Word中
- 锁升级功能主要依赖于MarkWord中所得标志位和释放偏向锁标志位

### 2.锁的分类

- 偏向锁:MarkWord存储的是偏向的线程ID;
- 轻量锁:MarkWord存储的是指向线程栈中Lock Record的指针;
- 重量锁:MarkWord存储的是指向堆中的monitor对象的指针;

### 3.偏向锁

#### 3.1是什么

- **偏向锁:单线程竞争**
  	当线程A第一次竞争到锁时，通过操作修改Mark Wond中的偏向线程ID、偏向模式。如果不存在其他线程竞争，那么持有偏向锁的线程将永远不需要进行同步

#### 3.2主要作用

- 当一段同步代码一直被同一个线程多次访问，由于只有一个线程那么该线程在后续访问时便念自动获得锁

#### 3.3小结论

Hotspot的作者经过研究发现，大多数情况下:
	多线程的情况下，锁不仅不存在多线程竞争，还存在锁由同一个线程多次获得的情况，
	偏向锁就是在这种情况下出现的，它的出现是为了解决只有在一个线程执行同步时提高性能。
	备注:
		偏向锁会偏向于第一个访问锁的线程，如果在接下来的运行过程中，该锁没有被其他的线程访问，则持有偏向锁的线程将永远不需要触	发同步。也即偏向锁在
		资源没有竞争情况下消除了同步语句，懒的连CAS操作都不做了，直接提高程序性能

#### 3.4重要参数说明

实际上偏向锁在JDK1.6之后是默认开启的，但是启动时间有延迟,
*所以需要添加参数-XX:BiasedLockingStartupDelay=0，让其在程序启动时立刻启动。*
**开启编向锁:**
	-XX: +UseBiasedLocking -Xx:BiasedLockingstartipDeLay=0*
**关闭偏向锁:关闭之后程序默认会直接进入---------->>>>>>>>轻量级锁状态。**
	-XX: -UseBiasedLocking

#### 3.5说明

- 锁的代码块时，不需要再次加锁和释放锁。而是直接会去检查锁的MarkWord里面是不是放的自己的线程ID)。
- 如果相等，表示偏向锁是偏向于当前线程的，就不需要再尝试获得锁了，直到竞争发生才释放锁。以后每次同步，检查锁的偏向线程ID与当前线程ID是否一致，如果一致直接进入同步。无需每次加锁解锁都去CAS更新对象头。如果自始至终使用锁的线程只有一个，很明显偏向锁几乎没有额外开销，性能极高。
- 如果不等，表示发生了竞争，锁已经不是总是偏问于问一个线程」，赵个时恢云云认使用A术百次waINTu主H久Lini程的ID，
- 竞争成功，表示之前的线程不存在了，MarkWord里面的线程ID为新线程的ID，锁不会升级，
  仍然为偏向锁;
- 竞争失败，这时候可能需要升级变为轻量级锁，才能保证线程间公平竞争锁。
- 注意，偏向锁只有遇到其他线程尝试竞争偏向锁时，持有偏向锁的线程才会释放锁，线程是不会主动释放偏向锁的。
- 技术实现:
  一个synchronized方法被一个线程抢到了锁时，那这个方法所在的对象就会在其所在的Mark Word中将偏向锁修改状态位，同时还会有占用前54位来存储线程指针作为标识。若该线程再次访问同一个synchronized方法时，该线程只需去对象头的Mark Word 中去判断一下是否有偏向锁指向本身的ID，无需再进入 Monitor去竞争对象了。

#### 3.6偏向锁的撤销

- 当有另外线程逐步来竞争锁的时候，就不能再使用偏向锁了，要升级为轻量级锁
- 竞争线程尝试CAS更新对象头失败，会等待到全局安坐点（此时不会执行任何代码）撤销偏向锁。
- 偏向锁使用一种等到竞争出现才释放锁的机制，只有当其他线程竞争锁时，持有偏向锁的原来线程才会被撤销。
  - 撤销需要等待全局安全点(该时间点上没有字节码正在执行)，同时检查持有偏向锁的线程是否还在执行:
  - ①第一个线程正在执行synchronized方法(处于同步块)，它还没有执行完，其它线程来抢夺，该偏向锁会被取消掉并出现锁升级。此时轻量级锁由原持有偏向锁的线程持有，继续执行其同步代码，而正在竞争的线程会进入自旋等待获得该轻量级锁。
  - ②第一个线程执行完成synchronized方法(退出同步块)，则将对象头设置成无锁状态并撤销偏向锁，重新偏向。
- ![image-20250108192855239](.\assert\image-20250108192855239.png)

#### 3.7总的流程

![img](.\assert\u=3132532573,266611202&fm=253&fmt=auto&app=138&f=JPEG.jpeg)

#### 3.8偏向锁在Jdk15之后，被废除了

- 偏向锁还是可以使用，	要使用命令

### 4.轻量锁

#### 4.1是什么

- **轻量级锁**:多线程竞争，但是任意时刻最多只有一个线程竞争，即不存在锁竞争太过激烈的情况，也就没有线程阻塞。  

#### 4.2主要作用

- 有线程参与锁的竞争，但是获取锁的冲突事件极短
- 本质就是自旋锁+CAS 

#### 4.3轻量级锁的获取

- 轻量级锁是为了在线程近乎交替执行同步块时提高性能。

- 主要目的:在没有多线程竞争的前提下，通过CAS减少重量级锁使用操作系统互斥量产生的性能消耗，说白了先自旋，不行才升级阻塞。

- 升级时机:当关闭偏向锁功能或多线程竞争偏向锁会导致偏向锁升级为轻量级锁

- 假如线程A已经拿到锁，这时线程B又来抢该对象的锁，由于该对象的锁已经被线程A拿到，当前该锁已是偏向锁了。

- 而线程B在争抢时发现对象头Mark Word中的线程ID不是线程B自己的线程ID(而是线程A)，那线程B就会进行CAS操作希望能获得锁

- 此时线程B操作中有两种情况:
  如果锁获取成功，直接替换Mark Word中的线程ID为B自己的ID(A→B)，重新偏向于其他线程(即将偏向锁交给其他线程，相当于当前线程"被""释放了锁)，该锁会保持偏向锁状态，A线程Over，B线程上位;

  ![image-20250108194110510](.\assert\image-20250108194110510.png)

  如果锁获取失败，则偏向锁升级为轻量级锁(设置偏向锁标识为O并设置锁标志位为O0)，此时轻量级锁由原持有偏向锁的线程持有，继续执行其同步代码，而正在竞争的线程B会进入自旋等待获得该轻量级锁。

#### 4.4补充

![image-20250108194430310](D:\智能设备开发\juc\assert\image-20250108194430310.png)

- **轻量级锁的加锁**
  JM会为每个线程在当前线程的栈帧中创建用于存储锁记录的空间，官方称为**Displaced Mark Word**。若一个线程获得锁时发现是轻量级锁，佘把锁的MarkWord复制到自己的Displaced Mark Word里面。然后线程尝试用CAS将锁的MarkWord替换为指向锁记录的指针。如果成功，当前线程获得锁，如果失败，表示Mark Word已经被替换成了其他线程的锁记录，说明在与其它线程竞争锁，当前线程就尝试使用自旋来获取锁。

  自旋CAS:不断尝试去获取锁，能不升级就不往上捅，尽量不要阻塞

- **轻量级锁的释放**
  在释放锁时，当前线程会使用CAS操作将Displaced Mark Word的内容复制回锁的Mark Word里面。如果没有发生竞争，那么这个复制的操作会成功。如果有其他线程因为自旋多次导致轻量级锁升级成了重量级锁，那么CAS操作会失败，此时会释放锁并唤醒被阻塞的线程。

#### 4.5代码演示

```java
Object o = new Object();
new Thread(() -> {
    synchronized (o) {
        // 0x000000323c1ff238 (thin lock: 0x000000323c1ff238) 确实为轻量锁
        System.out.println(ClassLayout.parseInstance(o).toPrintable());
        // 自旋达到一定次数依旧没有成功，会升级为重量级锁
    }
}).start();
```

#### 4.6自旋次数

**Java6之前**：默认启用，默认情况下自选次数为10次（-XX：PreBlockSpin=10）；或者为自旋线程数超过CPU核数的一半

**Java6之后：自适应自旋锁**

- 自适应自旋锁的大致原理
  线程如果自旋成功了，那下次自旋的最大次数会增加，因为JVM认为既然上次成功了，那么这一次也很大概率会成功。
  反之

  如果很少会自旋成功，那么下次会减少自旋的次数甚至不自旋，避免CPU空转。

#### 4.7轻量锁和偏向锁的区别和不同

- 争夺轻量级锁失败时，自旋尝试抢占锁
- 轻量级锁每次退出同步块都需要释放锁，而偏向锁是在竞争发生时才释放锁

### 5.重量级锁（用户态和内核态的切换）

#### 5.1重量级锁的原理

![image-20250108200044639](.\assert\image-20250108200044639.png)

- Java中synchronized的重量级锁，是基于进入和退出Nonitor对象实现的。在编译时会将同步块的开始位置插入monitor enter指令，在结束位置插入monitor exit指令。
- 当线程执行到monitor enter指令时，会尝试获取对象所对应的Monitor所有权，如果获取到了，即获取到了锁，会在Monitor的owner中存放当前线程的id，这样它将处于锁定状态，除非退出同步块，否则其他线程无法获取到这个Monitor。

#### 5.2代码实现

```Java
Object o = new Object();
// 0x0000025b60322c62 (fat lock: 0x0000025b60322c62) 确实形成重量级锁了
new Thread(() -> {
    synchronized (o) {
        System.out.println(ClassLayout.parseInstance(o).toPrintable());
    }
}, "t1").start();
TimeUnit.MILLISECONDS.sleep(100);
new Thread(() -> {
    synchronized (o) {
        System.out.println(ClassLayout.parseInstance(o).toPrintable());
    }
}, "t1").start();
```

### 6.小总结

- **那么当无锁升级后，hashcode去哪里了**

![image-20250108200930962](.\assert\image-20250108200930962.png)

​	当一个对象已经计算过一致性哈希吗后，它不能升级为偏向锁，只能变成轻量锁。

- **在无锁状态下**，Mark Word中可以存储对象的identity hash code值。当对象的hashCode()方法第一次被调用时，JVM会生成对应的identity hash code值并将该值存储到Mark Word中。
- **对于偏向锁**，在线程获取偏向锁时，会用Thread lD和epoch值覆盖identity hash code所在的位置。如果一个对象的hashCode()方法已经被调用过一次之后，这个对象不能被设置偏向锁。因为如果可以的化，那Mark Word中的identity hash code必然会被偏向线程ld给覆盖，这就会造成同一个对象前后两次调用hashCode()方法得到的结果不一致。
- **升级为轻量级锁时**，JVM会在当前线程的栈帧中创建一个锁记录(Lock Record)空间，用于存储锁对象的Mark Word拷贝，该拷贝中可以包含identity hash code，所以轻量级锁可以和identity hash code共存，哈希码和GC年龄自然保存在此，释放锁后会将这些信息写回到对象头。
- **升级为重量级锁后**，Mark Word保存的重量级锁指针，代表重量级锁的ObjectMonitor类里有字段记录非加锁状态下的Mark Word，锁释放后也会将信息写回到对象头。

#### 6.1代码

```java
// 当一个对象已经计算过identity hashcode，它就无法进入偏向锁状态，跳过偏向锁，直接升级轻量级锁
Object o = new Object();
int hashCode = o.hashCode();
System.out.println(hashCode);

new Thread(() -> {
    synchronized (o) {
        System.out.println(ClassLayout.parseInstance(o).toPrintable());
    }
}, "t1").start();
```

偏向锁过程中遇到一致性哈希计算请求，立马撤销偏向模式，膨胀为重量级锁

#### 6.2小总结

**各种锁的优缺点、synchronized锁升级和升级原理**

![image-20250108202533572](.\assert\image-20250108202533572.png)

![image-20250108202726331](.\assert\image-20250108202726331.png)

#### 6.3 JIT对锁的优化

- **JIT**(即时编译器)

- **锁消除** 

```java
/**
 * @program: juc
 * @author: cyy
 * @create: 2025-01-08 20:29
 * @description: 锁消除
 * 从J工T角度看相当于无视它，synchronized (o)不存在了,这个锁对象并没有被共用扩散到其它线程使用,
 * 极端的说就是根本没有加这个锁对象的底层机器码，消除了锁的使用
 **/
public class LockClearUpDemo {

    static Object object = new Object();

    public void m1() {
        // synchronized (object) {
        //     System.out.println("-------hello LockClearUpDemo");
        // }

        // 锁消除问题，JIT编译器会无视它 synchronized (o)，每次new出来的，不存在了，非正常的
        Object o = new Object();
        synchronized (o) {
            System.out.println("-------hello LockClearUpDemo" + "\t" + o.hashCode() + "\t" + object.hashCode());
        }
    }

    public static void main(String[] args) {
        LockClearUpDemo lockClearUpDemo = new LockClearUpDemo();
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                lockClearUpDemo.m1();
            }, String.valueOf(i + 1)).start();
        }
    }
}

```

- **锁粗化**

  ```java
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
  
  ```

  

![image-20250108204021050](D:\智能设备开发\juc\assert\image-20250108204021050.png)

## 四、AbstractQueuedSynchronizer之AQS 抽象队列同步器

### 1.是什么

- 技术解释

  - 是用来实现锁或者其它同步器组件的公共基础部分的抽象实现，
    是**重量级基础框架及整个JUC体系的基石，主要用于解决锁分配给"谁"的问题官网解释**
  - 整体就是一个抽象的FIFO队列来完成资源获取线程的排队工作，并通过一个**int类变量**表示持有锁的状态

  ![image-20250108210737298](.\assert\image-20250108210737298.png)

### 2.AQS之为什么是JUC基础框架

- 进一步理解锁和同步器的关系
  - **锁是面向锁的使用者**
  - **同步器是面向锁的实现者**
    - Java并发大神DougLee，提出统一规范并简化了锁的实现，**将其抽象出来**屏蔽了同步状态管理、同步队列的管理和维护、阻塞线程排队和通知、唤醒机制等，是一切锁和同步组件实现的--------**公共基础部分**

### 3.能干吗

- 加锁会导致阻塞·
- 解释说明
  - 抢到资源的线程直接使用处理业冬，抢不到资源的必然涉及一种**排队等候机制**。抢占资源失败的线程继续去等待(类似银行业务办理窗口都满了，暂时没有受理窗口的顾客只能去**候客区排队等候**)，但等候线程仍然保留获取锁的可能且获取锁流程仍在继续(候客区的顾客也在等着叫号，轮到了再去受理窗口办理业务)。
  - 既然说到了**排队等候机制**，那么就一定会有某种队列形成，这样的队列是什么数据结构呢?
  - 如果共享资源被占用，**就需要一定的阻塞等待唤醒机制来保证锁分配**。这个机制主要用的是CLH队列的变体实现的，将暂时获取不到锁的线程加入到队列中，这个队列就是AQS同步队列的抽象表现。它将要请求共享资源的线程及自身的等待状态封装成队列的结点对象（**Node**)，通过CAS、自旋以及LockSupport.park()的方式，维护state变量的状态，使并发达到同步的效果。
- 源码说明
  - AQS使用一个volatle的int类型的成员变量来表示同步状态。通过内置的FIFO队列来完成资源获取的排队工作将每条要去抢占资源的线程封装成一个Node节点来实现锁的分配，通过CAS完成对State值的修改。
- ![image-20250109174211534](.\assert\image-20250109174211534.png)

### 4.AQS前置知识

![image-20250109174516591](.\assert\image-20250109174516591.png)

#### 4.1AQS体系内部架构

- AQS自身

  - AQS的int变量
    - AQS的同步状态State成员变量
    - 银行办理业务的受理窗口状态
      - 零就是没人，自由状态可以办理
      - 大于等于1，有人占用窗口，等着去
  - AQS的CLH队列
    - ![image-20250109175110773](.\assert\image-20250109175110773.png)
    - CLH队列（三个大佬名字组成），为一个双向链表。
    - 银行候客区的等待顾客
  - 小总结
    - 有阻塞就需要排队，实现排队必然需要队列
    - state变量+CLH双端队列

- 内部类Node（Node类在AQS类内部）

  - Node的int变量

    - Node的等待状态（jdk8是waitState jdk17是status）成员变量
    - 说人话
      - 等候区其他顾客（其他线程）的等待状态
      - 队列中每个排队的个体就是一个Node

  - Node的内部结构（jdk17）

    - ```java
      // Node status bits, also used as argument and return values
      static final int WAITING   = 1;          // must be 1
      static final int CANCELLED = 0x80000000; // must be negative
      static final int COND      = 2;          // in a condition wait
      
      abstract static class Node {
          volatile Node prev;       // initially attached via casTail
          volatile Node next;       // visibly nonnull when signallable
          Thread waiter;            // visibly nonnull when enqueued
          volatile int status;      // written by owner, atomic bit ops by others
      
          // methods for atomic operations
          final boolean casPrev(Node c, Node v) {  // for cleanQueue
              return U.weakCompareAndSetReference(this, PREV, c, v);
          }
          final boolean casNext(Node c, Node v) {  // for cleanQueue
              return U.weakCompareAndSetReference(this, NEXT, c, v);
          }
          final int getAndUnsetStatus(int v) {     // for signalling
              return U.getAndBitwiseAndInt(this, STATUS, ~v);
          }
          final void setPrevRelaxed(Node p) {      // for off-queue assignment
              U.putReference(this, PREV, p);
          }
          final void setStatusRelaxed(int s) {     // for off-queue assignment
              U.putInt(this, STATUS, s);
          }
          final void clearStatus() {               // for reducing unneeded signals
              U.putIntOpaque(this, STATUS, 0);
          }
      
          private static final long STATUS
              = U.objectFieldOffset(Node.class, "status");
          private static final long NEXT
              = U.objectFieldOffset(Node.class, "next");
          private static final long PREV
              = U.objectFieldOffset(Node.class, "prev");
      }
      ```

      JDK8中Node的属性说明

      ![image-20250109180340786](.\assert\image-20250109180340786.png)

- AQS源码的深度讲解

  - Lock接口的实现类，基本都是通过【聚合】了一个【队列同步器】的子类完成线程访问控制的。

  - ReentrantLockd的原理

    ![image-20250109180927772](.\assert\image-20250109180927772.png)

  - 公平锁源码和非公平锁源码比较

    ```Java
    // 非公平
    protected final boolean tryAcquire(int acquires) {
        if (getState() == 0 && compareAndSetState(0, acquires)) {
            setExclusiveOwnerThread(Thread.currentThread());
            return true;
        }
        return false;
    }
    // 公平
    protected final boolean tryAcquire(int acquires) {
        if (getState() == 0 && !hasQueuedPredecessors() &&
            compareAndSetState(0, acquires)) {
            setExclusiveOwnerThread(Thread.currentThread());
            return true;
        }
        return false;
    }
    ```

    可以明显看出公平锁与非公平锁的lock()方法唯一的区别就在于公平锁在获取同步状态时多了一个限制条件:
    **hasQueuedPredecessors()**
    **hasQueuedPredecessors是公平锁加锁时判断等待队列中是否存在有效节点的方法**

  - 以非公平锁ReentranLock()为例作为突破走起，方法lock()

    - **hasQueuedPredecessors()中判断了是否需要排队**，导致公平锁和非公平锁的差异如下，

    - **公平锁:公平锁讲究先来先到**，线程在获取锁时，如果这个锁的等待队列中已经有线程在等待，那么当前线程就会进入等待队列中;

    - **非公平锁:不管是否有等待队列，如果可以获取锁，则立刻占有锁对象**。也就是说队列的第一个排队线程苏醒后，不一定就是排头的这个线程获得锁，它还是需要参加竞争锁（存在线程竞争的情况下)，后来的线程可能不讲武德插队夺锁了。

      ![image-20250109195616964](.\assert\image-20250109195616964.png)

    - 

## 五、ReentrantLock、ReentrantReadWriteLock、StampedLock讲解
