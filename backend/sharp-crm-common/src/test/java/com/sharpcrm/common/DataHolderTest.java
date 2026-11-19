package com.sharpcrm.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DataHolderTest {

    private final DataHolder<String> holder = new DataHolder<>();

    @AfterEach
    void cleanUp() {
        holder.remove();
    }

    @Test
    void setGetAndRemoveInSameThread() {
        assertNull(holder.get());
        holder.set("value");
        assertEquals("value", holder.get());
        holder.remove();
        assertNull(holder.get());
    }

    @Test
    void valueIsIsolatedBetweenThreads() throws InterruptedException {
        holder.set("main-thread-value");

        AtomicReference<String> childValue = new AtomicReference<>();
        Thread child = new Thread(() -> childValue.set(holder.get()));
        child.start();
        child.join();

        assertNull(childValue.get(), "子线程不应读取到主线程写入的值");
        assertEquals("main-thread-value", holder.get());
    }
}
