package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    @Before("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillAspect(JoinPoint joinPoint) {
        log.info("进行公共字段的填充");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill annotation = signature.getMethod().getAnnotation(AutoFill.class);
        Object[] arg = joinPoint.getArgs();
        if (arg == null || arg.length == 0)
            return;
        Object entity = arg[0];
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        try {
            Method setUpdateTime = entity.getClass().getMethod("setUpdateTime", LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getMethod("setUpdateUser", Long.class);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if(annotation.value() == OperationType.INSERT){
            try {
                Method setCreateTime = entity.getClass().getMethod("setCreateTime", LocalDateTime.class);
                Method setCreateUser = entity.getClass().getMethod("setCreateUser", Long.class);
                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }
}
