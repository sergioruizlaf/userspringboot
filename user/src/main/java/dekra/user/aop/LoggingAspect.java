package dekra.user.aop;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Log4j2
@Aspect
@Component
public class LoggingAspect {

    @Before("@annotation(dekra.user.aop.LogRequest)")
    public void logRequestExecution(JoinPoint joinPoint) {
        String method = joinPoint.getSignature().getName();
        String params = Arrays.toString(joinPoint.getArgs());
        log.info("Method [" + method + "] gets called with parameters " + params);
    }

}