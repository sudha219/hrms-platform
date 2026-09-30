package com.hrms.repository;

import com.hrms.entity.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {
    List<AttendanceRecord> findByCompanyIdAndEmployeeIdAndDate(UUID companyId, UUID employeeId, LocalDate date);
    Optional<AttendanceRecord> findTopByCompanyIdAndEmployeeIdAndDateOrderByCheckInDesc(UUID companyId, UUID employeeId, LocalDate date);
    Page<AttendanceRecord> findByCompanyIdAndEmployeeId(UUID companyId, UUID employeeId, Pageable pageable);
    Page<AttendanceRecord> findByCompanyIdAndDate(UUID companyId, LocalDate date, Pageable pageable);
    Page<AttendanceRecord> findByCompanyId(UUID companyId, Pageable pageable);
}
