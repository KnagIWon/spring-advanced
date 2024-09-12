package org.example.expert.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
public class AspectPractice {

    @Pointcut("@annotation(org.example.expert.annotation.TrackTime)")
    private void trackTimeAnnotation() {

    }

    @Around("trackTimeAnnotation()")
    public Object adviceAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request1 = servletRequestAttributes.getRequest();
        Long userId = (Long)request1.getAttribute("userId");
        LocalDateTime requestTime = LocalDateTime.now(); // 요청 시각
        StringBuffer requestURL = request1.getRequestURL();
        String fullURL = requestURL.toString(); // 요청 URL

        String queryString = request1.getQueryString();
        if (queryString != null) {
            fullURL += "?" + queryString;
        }

        try {
            Object result = joinPoint.proceed();
            return result;
        } finally {
            log.info("::: UserID: {}, RequestTime: {}, RequestURL: {}",
                    userId, requestTime, fullURL);
        }
    }
}
