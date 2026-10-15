package com.mora.paymentprocessingapi.config;

import com.mora.paymentprocessingapi.service.AuditEvent;
import com.mora.paymentprocessingapi.service.AuditLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    private final AuditLogService auditLogService;

    public AuditAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Around("@annotation(auditEvent)")
    public Object audit(ProceedingJoinPoint joinPoint, AuditEvent auditEvent) throws Throwable {
        String actor = currentActor();

        try {
            Object result = joinPoint.proceed();
            recordSafely(auditEvent.value(), actor, "SUCCESS");
            return result;
        } catch (Throwable ex) {
            recordSafely(auditEvent.value(), actor, "FAILURE");
            throw ex;
        }
    }

    private String currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated())
            return "anonymous";


        return authentication.getName();
    }

    private void recordSafely(String event, String actor, String outcome) {
        try {
            auditLogService.record(event, actor, outcome);
        } catch (RuntimeException ex) {
            log.error("Unable to persist audit event: event={}, outcome={}", event, outcome, ex);
        }
    }
}
