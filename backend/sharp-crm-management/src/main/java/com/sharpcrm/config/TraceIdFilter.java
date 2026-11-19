package com.sharpcrm.config;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 链路追踪过滤器
 * <p>
 * 每个请求分配一个 traceId，写进 MDC 供日志格式输出，同时回写到响应头。
 * 排查线上问题时，用户报的「单号」就是这个 id，拿它 grep 日志能捞出这次请求产生的全部记录——
 * 包括异步线程池里打的日志，前提是异步任务提交时把 MDC 一起传过去。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";
    private static final String HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(HEADER);
        if (StrUtil.isBlank(traceId)) {
            traceId = RandomUtil.randomString(16);
        }
        MDC.put(TRACE_ID, traceId);
        response.setHeader(HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            // 线程复用，必须清理，否则下一个请求会串号
            MDC.remove(TRACE_ID);
        }
    }
}
