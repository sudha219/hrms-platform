package com.hrms.repository;

import com.hrms.entity.PayrollRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollRunRepository extends JpaRepository<PayrollRun, UUID> {
    Page<PayrollRun> findByCompanyId(UUID companyId, Pageable pageable);
    Optional<PayrollRun> findByCompanyIdAndId(UUID companyId, UUID id);
    Optional<PayrollRun> findByCompanyIdAndPeriodStartAndPeriodEnd(UUID companyId, LocalDate periodStart, LocalDate periodEnd);
}
