package com.itheima.bigevent.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itheima.bigevent.anno.OpLog;
import com.itheima.bigevent.mapper.OperationLogMapper;
import com.itheima.bigevent.pojo.OperationLog;
import com.itheima.bigevent.pojo.Result;
import com.itheima.bigevent.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Map;

@Aspect
@Component
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public OperationLogAspect(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Around("@annotation(com.itheima.bigevent.anno.OpLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        Exception ex = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            ex = e;
            throw e;
        } finally {
            try {
                saveLog(joinPoint, result, ex);
            } catch (Exception ignore) {
                // 日志记录失败不影响主流程
            }
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, Object result, Exception ex) throws JsonProcessingException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        OpLog annotation = method.getAnnotation(OpLog.class);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        OperationLog log = new OperationLog();

        // 用户信息
        Map<String, Object> userMap = ThreadLocalUtil.get();
        if (userMap != null) {
            Object id = userMap.get("id");
            Object username = userMap.get("username");
            if (id instanceof Integer) {
                log.setUserId((Integer) id);
            }
            if (username != null) {
                log.setUsername(username.toString());
            }
        }

        // 注解信息
        if (annotation != null) {
            log.setModule(annotation.module());
            log.setOperation(annotation.operation());
        }

        // 请求相关
        if (request != null) {
            log.setRequestMethod(request.getMethod());
            log.setRequestUri(request.getRequestURI());
            log.setIp(request.getRemoteAddr());
        }
        log.setClassMethod(signature.getDeclaringTypeName() + "." + signature.getName());

        // 参数
        Object[] args = joinPoint.getArgs();
        try {
            log.setRequestParams(objectMapper.writeValueAsString(args));
        } catch (JsonProcessingException e) {
            log.setRequestParams("[unserializable]");
        }

        // 结果
        if (result instanceof Result<?> r) {
            log.setResultCode(r.getCode());
            log.setResultMessage(r.getMessage());
        } else if (ex != null) {
            log.setResultCode(1);
            log.setErrorMessage(ex.getMessage());
        } else {
            log.setResultCode(0);
        }

        if (ex != null && log.getErrorMessage() == null) {
            log.setErrorMessage(ex.getMessage());
        }

        operationLogMapper.insert(log);
    }
}

