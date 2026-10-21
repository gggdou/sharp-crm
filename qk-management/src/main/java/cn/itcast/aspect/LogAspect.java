package cn.itcast.aspect;

import cn.hutool.json.JSONUtil;
import cn.itcast.common.DataHolder;
import cn.itcast.pojo.OperateLog;
import cn.itcast.service.OperateLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 操作日志切面类
 * 作用：使用AOP环绕通知，统一拦截controller层新增、修改、删除接口，自动记录操作日志存入数据库
 */
@Aspect // 标记当前类为切面类
@Component // 交给Spring容器管理
public class LogAspect {

    // 注入操作日志service，用于保存日志数据
    @Autowired
    private OperateLogService operateLogService;

    // 注入DataHolder对象
    @Autowired
    private DataHolder dataHolder;

    /**
     * 环绕通知：拦截controller中以add、update、delete开头的方法
     * 切点表达式：匹配cn.itcast.controller包下所有Controller类，方法名以add / update / delete开头的所有方法
     * 环绕通知：可以在目标方法执行前、执行后、异常时做处理，能拿到返回值，适合记录操作耗时
     * @param joinPoint 连接点对象：封装目标方法的信息（类名、方法名、参数等）
     * @return 目标方法执行后的返回结果
     * @throws Throwable 抛出目标方法产生的异常
     */
    @Around("execution(* cn.itcast.controller.*Controller.add*(..))" +
            " || execution(* cn.itcast.controller.*Controller.update*(..))" +
            " || execution(* cn.itcast.controller.*Controller.delete*(..))")
    public Object recordLog(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录目标方法执行的开始时间戳
        long start = System.currentTimeMillis();
        // 保存目标方法原始返回值，初始为null
        Object proceed = null;

        try {
            // 执行目标Controller方法，拿到返回结果
            proceed = joinPoint.proceed();
            // 将执行结果返回给接口调用方
            return proceed;
        } finally {
            /*
             finally块：无论目标方法正常执行 / 抛出异常，都会执行日志记录
             即使接口报错，也能把这条操作日志保存下来
             */
            // 获取目标方法签名（包含类名、方法名等信息）
            Signature signature = joinPoint.getSignature();

            // 构建操作日志实体对象
            OperateLog operateLog = new OperateLog();
            // 设置操作发生时间
            operateLog.setOperateTime(LocalDateTime.now());
            // 设置目标方法所在类全限定名
            operateLog.setClassName(signature.getDeclaringTypeName());
            // 设置目标方法名称
            operateLog.setMethodName(signature.getName());

            // 获取目标方法传入的所有请求参数
            Object[] args = joinPoint.getArgs();
            // 参数转JSON字符串存入日志，无参数则存null
            operateLog.setMethodParams(args == null ? null : JSONUtil.toJsonStr(args));
            // 接口返回结果转JSON存入日志，无返回值则存null
            operateLog.setReturnValue(proceed == null ? null : JSONUtil.toJsonStr(proceed));

            // 获取方法执行结束时间戳
            long end = System.currentTimeMillis();
            // 计算方法执行耗时（毫秒）
            operateLog.setCostTime(end - start);

            Map<String, Object> claims = (Map<String, Object>) dataHolder.get();

            Integer userId = Integer.parseInt(claims.get("id").toString());

            operateLog.setOperateUserId(userId);

            // 调用service，保存操作日志到数据库
            operateLogService.saveOrUpdate(operateLog);
        }
    }
}
