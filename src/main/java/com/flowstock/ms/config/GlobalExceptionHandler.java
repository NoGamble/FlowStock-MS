package com.flowstock.ms.config;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 这个注解告诉 Spring：这个类是全局的异常“捕获站”
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 情况 A：专门处理并发冲突（第一优先级优化）
     * 当两个管理员同时改一个商品，JPA 抛出乐观锁异常时，执行这里
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public String handleConcurrencyError() {
        // 返回给前端的提示，前端可以直接弹窗显示
        return "【系统提示】该商品数据刚刚被他人修改过，请刷新页面后再试。";
    }

    /**
     * 情况 B：处理你在 Service 里手动 throw new RuntimeException("...") 的错误
     * 比如：“库存不足”、“找不到 ID” 等业务提示
     */
    @ExceptionHandler(RuntimeException.class)
    public String handleBusinessError(RuntimeException e) {
        // 这里的 e.getMessage() 就是你在 Service 里写的报错字符串
        return "【业务逻辑错误】" + e.getMessage();
    }

    /**
     * 情况 C：处理其他所有未知的系统错误（比如 SQL 写错了、空指针等）
     * 这是一个“保底”方案，防止后端直接崩掉把代码堆栈吐给用户
     */
    @ExceptionHandler(Exception.class)
    public String handleGeneralError(Exception e) {
        e.printStackTrace(); // 程序员在后台控制台能看到错在哪
        return "【系统异常】服务器冒烟了，请联系管理员查看日志。";
    }

    // 处理死锁
    @ExceptionHandler(CannotAcquireLockException.class)
    public String handleDeadlockException(CannotAcquireLockException e) {
        // 打印简要日志方便排查
        System.err.println("Database Deadlock detected: " + e.getMessage());
        return "【系统繁忙】当前操作的人数较多，请稍后重试。";
    }
}