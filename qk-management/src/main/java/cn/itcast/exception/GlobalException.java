package cn.itcast.exception;

import cn.itcast.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一捕获controller层抛出的异常，返回统一JSON结果
 */
@Slf4j
@RestControllerAdvice // 标识全局异常处理类，对所有@RestController生效
public class GlobalException {

    /**
     * 自定义业务异常处理器
     * @param e 自定义业务异常对象
     * @return 统一返回结果，携带异常提示信息
     */
    @ExceptionHandler(CustomException.class)
    public Result customExceptionHandler(CustomException e){
        return Result.error(e.getMessage());
    }

    /**
     * 通用异常处理器，捕获所有未被上面捕获的Exception
     * @param e 原始异常对象
     * @return 统一返回结果，返回友好提示"系统异常!"
     */
    @ExceptionHandler(Exception.class)
    public Result exceptionHandler(Exception e){
        e.printStackTrace(); // 打印异常堆栈信息
        log.error("异常信息：{}", e.getMessage()); // 打印异常信息
        return Result.error("系统异常!");
    }
}
