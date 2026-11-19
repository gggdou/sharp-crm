package com.sharpcrm.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ResultTest {

    @Test
    void successWithoutData() {
        Result result = Result.success();
        assertEquals(1, result.getCode());
        assertEquals("success", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void successWithData() {
        Object data = new Object();
        Result result = Result.success(data);
        assertEquals(1, result.getCode());
        assertEquals("success", result.getMsg());
        assertSame(data, result.getData());
    }

    @Test
    void successWithCustomMsgAndData() {
        Result result = Result.success("ok", "payload");
        assertEquals(1, result.getCode());
        assertEquals("ok", result.getMsg());
        assertEquals("payload", result.getData());
    }

    @Test
    void errorWithoutMsg() {
        Result result = Result.error();
        assertEquals(0, result.getCode());
        assertEquals("error", result.getMsg());
        assertNull(result.getData());
    }

    @Test
    void errorWithCustomMsg() {
        Result result = Result.error("失败");
        assertEquals(0, result.getCode());
        assertEquals("失败", result.getMsg());
        assertNull(result.getData());
    }
}
