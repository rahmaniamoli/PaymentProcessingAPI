package com.mora.paymentprocessingapi.repository;
import com.mora.paymentprocessingapi.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
}