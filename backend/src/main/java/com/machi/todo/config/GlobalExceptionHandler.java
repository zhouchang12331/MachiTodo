package com.machi.todo.config;

import com.machi.todo.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理：任何接口里抛出的异常都转成统一 JSON，
 * 前端就能拿到明确的错误信息，而不是一堆白页堆栈。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 参数校验类异常，属于用户输入问题，提示得客气一点 */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException e) {
        return Result.error(e.getMessage());
    }

    /**
     * 静态资源/未知路径没找到（浏览器要的 favicon.ico、接口地址写错等）。
     * 属于正常的 404，不该按服务端故障记 ERROR，否则日志会被刷屏。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResourceFound(NoResourceFoundException e) {
        log.debug("资源不存在：{}", e.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.error("接口不存在：" + e.getResourcePath()));
    }

    /** 其余未预料到的异常统一兜底，同时把堆栈写进日志方便排查 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("接口执行失败", e);
        return Result.error("服务器开小差了：" + e.getMessage());
    }
}
