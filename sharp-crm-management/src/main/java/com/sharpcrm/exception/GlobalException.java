package com.sharpcrm.exception;

import com.sharpcrm.common.Result;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一捕获 controller 层抛出的异常，返回统一 JSON 结果
 */
@Slf4j
@RestControllerAdvice
public class GlobalException {

    /**
     * 自定义业务异常处理器
     */
    @ExceptionHandler(CustomException.class)
    public Result customExceptionHandler(CustomException e) {
        return Result.error(e.getMessage());
    }

    /**
     * @RequestBody + @Valid 校验失败：返回字段级错误，前端可直接定位到具体输入框
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("；"));
        log.warn("参数校验失败：{}", message);
        return Result.error(message);
    }

    /**
     * 表单/查询参数绑定校验失败
     */
    @ExceptionHandler(BindException.class)
    public Result handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("；"));
        log.warn("参数绑定失败：{}", message);
        return Result.error(message);
    }

    /**
     * 方法参数上的约束校验失败（如 @PathVariable 上的 @Min）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("；"));
        log.warn("参数约束校验失败：{}", message);
        return Result.error(message);
    }

    /**
     * 请求体格式错误（JSON 语法错、日期格式不匹配等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败：{}", e.getMessage());
        return Result.error("请求参数格式不正确");
    }

    /**
     * 缺少必填的查询参数
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result handleMissingParam(MissingServletRequestParameterException e) {
        return Result.error("缺少必要参数：" + e.getParameterName());
    }

    /**
     * 路径变量类型不匹配，例如 /clues/abc
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.error("参数 " + e.getName() + " 类型不正确");
    }

    /**
     * 通用异常处理器，兜底未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public Result exceptionHandler(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统异常!");
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + "：" + error.getDefaultMessage();
    }
}
