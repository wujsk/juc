package com.cyy.advanced.atomic.enhance_atomic;

import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * @program: juc
 * @author: cyy
 * @create: 2025-01-02 13:58
 * @description:
 * LongAdder在无竞争的情况，跟AtomicLong一样，对同一个base进行操作，
 * 当出现竞争关系时则是采用化整为零分散热点的做法，用空间换时间，用一个数组cells，将一个value拆分进这个数组clls。
 * 多个线程需要同时对value进行操作时候，可以对线程id进行hash得到hash值，再根据hash值映射到这个数组cells的某个下标，
 * 再对该下标所对应的值进行自增操作。当所有线程操作完毕，将数组cells的所有值和base都加起来作为最终结果。
 **/
public class LongAdderApiDemo {
    public static void main(String[] args) {
        LongAdder adder = new LongAdder();
        adder.increment();
        adder.increment();
        adder.increment();
        System.out.println(adder.sum());

        LongAccumulator longAccumulator = new LongAccumulator((x, y) -> x + y, 0);
        longAccumulator.accumulate(1);
        longAccumulator.accumulate(3);
        System.out.println(longAccumulator.get());
    }
}
