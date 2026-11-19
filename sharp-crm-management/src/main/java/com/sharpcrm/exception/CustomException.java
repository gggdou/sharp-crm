package com.sharpcrm.exception;

public class CustomException extends RuntimeException {
    public CustomException() { // 无参构造方法
    }

    public CustomException(String message) { // 有参构造方法，用于传入异常信息
        super(message); // 调用父类的有参构造方法，传入异常信息
    }
}
