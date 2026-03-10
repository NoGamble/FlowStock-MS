package com.flowstock.ms;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FlowStockMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowStockMsApplication.class, args);
    }

    @Bean
    public CommandLineRunner testJPA(InventoryRepository repo) {
        return args -> {
            System.out.println("---- 开始读取数据库数据 ----");
            repo.findAll().forEach(item -> {
                System.out.println("商品名称: " + item.getItemName() + " | 数量: " + item.getQuantity());
            });
            System.out.println("---- 读取结束 ----");
        };
    }
}
