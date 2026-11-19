package com.sharpcrm.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.stereotype.Component;

import java.sql.Statement;
import java.util.regex.Pattern;

/**
 * 慢 SQL 监控
 * <p>
 * 在 StatementHandler 层拦截，统计「准备 + 执行 + 结果集」的耗时。
 * 超过阈值的语句打 warn，日志里带上 traceId，可以直接定位到是哪次请求打出来的。
 */
@Slf4j
@Component
@Intercepts({
        @Signature(type = StatementHandler.class, method = "query",
                args = {Statement.class, org.apache.ibatis.session.ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class})
})
public class SlowSqlInterceptor implements Interceptor {

    /** 超过该耗时视为慢 SQL */
    private static final long THRESHOLD_MS = 500;

    private static final Pattern BLANK = Pattern.compile("\\s+");

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            long cost = System.currentTimeMillis() - start;
            if (cost >= THRESHOLD_MS) {
                log.warn("慢SQL {}ms | {}", cost, sqlOf(invocation));
            }
        }
    }

    /**
     * 目标对象可能被多层动态代理包装，逐层剥到真实的 StatementHandler
     */
    private String sqlOf(Invocation invocation) {
        try {
            MetaObject meta = SystemMetaObject.forObject(invocation.getTarget());
            while (meta.hasGetter("h")) {
                meta = SystemMetaObject.forObject(meta.getValue("h"));
            }
            while (meta.hasGetter("target")) {
                meta = SystemMetaObject.forObject(meta.getValue("target"));
            }
            StatementHandler handler = (StatementHandler) meta.getOriginalObject();
            return BLANK.matcher(handler.getBoundSql().getSql()).replaceAll(" ").trim();
        } catch (Exception e) {
            return "<无法解析 SQL>";
        }
    }
}
