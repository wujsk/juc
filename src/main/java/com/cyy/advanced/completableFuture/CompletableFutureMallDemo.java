package com.cyy.advanced.completableFuture;

/**
 * @program: juc
 * @description:
 * @author: 酷炫焦少
 * @create: 2024-12-04 08:46
 **/

import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 *  案例说明：电商比价需求，模拟如下情况
 *  1.需求
 *   1.1 同一款商品，同时搜索出同款产品在各大电商平台的售价
 *   1.2 同一款商品，同时搜索出本产品在同一个电商平台下，各个入住卖家售价是多少
 *  2. 输出：出来结果希望是同款商品在不同地方的价格清单列表，返回一个List<String>
 */
@Data
class NetMall{

    private String netMallName;

    public NetMall(String netMallName) {
        this.netMallName = netMallName;
    }

    public double calcPrice(String productName) {
        // 模拟延迟
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return ThreadLocalRandom.current().nextDouble() * 2 + productName.charAt(0);
    }
}
public class CompletableFutureMallDemo {
    static List<NetMall> list = Arrays.asList(
            new NetMall("jd"),
            new NetMall("dangdang"),
            new NetMall("taobao")
    );

    /**
     * 一家一家查
     * @param list
     * @param productName
     * @return
     */
    public static List<String> getPrice(List<NetMall> list, String productName) {
        List<String> prices = list.stream()
                //.map(item -> "《" + productName + "》 in " + item.getNetMallName() + " price is " + item.calcPrice(productName))
                .map(item ->
                        String.format("《" + productName + "》 in %s price is %.2f",
                        item.getNetMallName(),
                        item.calcPrice(productName)))
                .collect(Collectors.toList());
        return prices;
    }

    public static List<String> getPriceByCompletableFuture(List<NetMall> list, String productName) {
        return list.stream().map(item ->
                CompletableFuture.supplyAsync(() ->
                        String.format("《" + productName + "》 in %s price is %.2f",
                                item.getNetMallName(),
                                item.calcPrice(productName)))
        ).collect(Collectors.toList()).stream().map(s -> s.join()).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        long begin = System.currentTimeMillis();
        // List<String> mysql = getPrice(list, "mysql");
        List<String> mysql = getPriceByCompletableFuture(list, "mysql");
        mysql.forEach(item -> System.out.println(item.toString()));
        long end = System.currentTimeMillis();
        System.out.println("m2耗时：" + (end - begin));
    }
}
