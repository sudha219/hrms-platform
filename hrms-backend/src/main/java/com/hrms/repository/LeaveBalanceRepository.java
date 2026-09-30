package com.hrms.repository;

import com.hrms.entity.LeaveBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, UUID> {
    List<LeaveBalance> findByCompanyIdAndEmployeeIdAndYear(UUID companyId, UUID employeeId, Integer year);
    Optional<LeaveBalance> findByCompanyIdAndEmployeeIdAndLeaveTypeIdAndYear(UUID companyId, UUID employeeId, UUID leaveTypeId, Integer year);
}
