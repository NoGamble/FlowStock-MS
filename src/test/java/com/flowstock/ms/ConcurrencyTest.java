package com.flowstock.ms.service;

import com.flowstock.ms.entity.Inventory;
import com.flowstock.ms.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ConcurrencyTest {

    @Autowired
    private StockMovementService stockMovementService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    public void testStockOutConcurrency() throws InterruptedException {
        // 1. 准备一条初始数据
        Inventory item = new Inventory();
        item.setItemName("并发测试商品");
        item.setCurrentQuantity(100);
        // 虽然你不关注这两个字段，但为了满足数据库非空约束，随便塞个值
        item.setSkuCode("TEST-" + System.currentTimeMillis());
        item.setUnit("个");

        item = inventoryRepository.save(item);
        Long itemId = item.getId();

        // 2. 设置并发环境
        int threadCount = 2; // 模拟两个管理员同时操作
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        // CountDownLatch 确保两个线程几乎在同一瞬间“起跑”
        CountDownLatch latch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                try {
                    latch.await(); // 等待发令枪响
                    stockMovementService.processOutbound(itemId, 10);
                    successCount.incrementAndGet();
                } catch (ObjectOptimisticLockingFailureException e) {
                    failCount.incrementAndGet();
                    System.out.println("检测到乐观锁冲突！");
                } catch (Exception e) {
                    System.out.println("其他异常: " + e.getMessage());
                }
            });
        }

        // 3. 发令枪响，开始并发
        latch.countDown();
        executor.shutdown();
        Thread.sleep(2000); // 给线程一点执行时间

        // 4. 验证结果
        Inventory updatedItem = inventoryRepository.findById(itemId).get();
        System.out.println("==== 测试结果 ====");
        System.out.println("成功次数: " + successCount.get());
        System.out.println("失败次数: " + failCount.get());
        System.out.println("最终库存: " + updatedItem.getCurrentQuantity());
        System.out.println("==================");

        // 验证逻辑：
        // 如果乐观锁生效，成功次数应该是 1，库存应该是 90。
        // 如果没生效（丢失更新），成功次数会是 2，库存会变成错误的 80。
        assertEquals(1, successCount.get(), "应该只有一个人操作成功");
        assertEquals(90, updatedItem.getCurrentQuantity(), "库存数值不符合预期");
    }
}