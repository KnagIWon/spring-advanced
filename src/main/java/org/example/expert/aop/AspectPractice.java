package org.example.expert.aop;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.example.expert.domain.common.dto.AuthUser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Aspect
public class AspectPractice {

    private final HttpServletRequest request;

    public AspectPractice(HttpServletRequest request) {
        this.request = request;
    }

    @Pointcut("@annotation(org.example.expert.annotation.TrackTime)")
    private void trackTimeAnnotation() {

    }

    @Around("trackTimeAnnotation()")
    public Object adviceAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        long startTime = System.currentTimeMillis();
        HttpServletRequest request1 = servletRequestAttributes.getRequest();
        Long userId = (Long)request1.getAttribute("userId");

        String requestUrl = request.getRequestURI(); // 요청 URL
        LocalDateTime requestTime = LocalDateTime.now(); // 요청 시각

        try {
            Object result = joinPoint.proceed();
            return result;
        } finally {
            // 측정 완료
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            log.info("::: ExecutionTime: {}ms, UserID: {}, RequestTime: {}, RequestURL: {}",
                    executionTime, userId, requestTime, requestUrl);
        }
    }
}
