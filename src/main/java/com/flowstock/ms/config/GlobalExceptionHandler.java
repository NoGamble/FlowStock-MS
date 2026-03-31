package com.flowstock.ms.config;

import com.flowstock.ms.dto.Result;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.transaction.TransactionSystemException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 情况 A：专门处理并发冲突（第一优先级优化）
     * 当两个管理员同时改一个商品，JPA 抛出乐观锁异常时，执行这里
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public Result<Void> handleOptimisticLock(ObjectOptimisticLockingFailureException e) {
        return Result.error(409, "数据已被他人修改，请刷新页面重试");
    }

    /**
     * 情况 B：处理你在 Service 里手动 throw new RuntimeException("...") 的错误
     * 比如：“库存不足”、“找不到 ID” 等业务提示
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntime(RuntimeException e) {
        return Result.error(500, e.getMessage());
    }

    /**
     * 情况 C：处理其他所有未知的系统错误（比如 SQL 写错了、空指针等）
     * 这是一个“保底”方案，防止后端直接崩掉把代码堆栈吐给用户
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        e.printStackTrace(); // 记得打印日志，方便你调试
        return Result.error(500, "系统未知错误，请联系管理员");
    }

    // 处理死锁
    @ExceptionHandler(CannotAcquireLockException.class)
    public Result<Void> handleDeadlock(CannotAcquireLockException e) {
        return Result.error(408, "服务器繁忙（锁竞争），请稍后重试");
    }
}