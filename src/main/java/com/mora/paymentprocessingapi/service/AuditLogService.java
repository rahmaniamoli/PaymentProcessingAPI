package com.mora.paymentprocessingapi.service;
import com.mora.paymentprocessingapi.domain.AuditLog;
import com.mora.paymentprocessingapi.repository.AuditLogRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String eventType, String actor, String outcome) {
        repository.save(new AuditLog(eventType, actor, outcome, MDC.get("correlationId")));
    }
}
