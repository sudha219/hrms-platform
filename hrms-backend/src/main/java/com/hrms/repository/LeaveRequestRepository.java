package com.hrms.repository;

import com.hrms.entity.LeaveRequest;
import com.hrms.enums.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {
    Page<LeaveRequest> findByCompanyIdAndEmployeeId(UUID companyId, UUID employeeId, Pageable pageable);
    List<LeaveRequest> findByCompanyIdAndStatus(UUID companyId, LeaveStatus status);
    Optional<LeaveRequest> findByCompanyIdAndId(UUID companyId, UUID id);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.companyId = :companyId AND lr.employee.manager.id = :managerId AND lr.status = :status")
    List<LeaveRequest> findPendingByManager(@Param("companyId") UUID companyId, @Param("managerId") UUID managerId, @Param("status") LeaveStatus status);

    @Query("SELECT COUNT(lr) > 0 FROM LeaveRequest lr WHERE lr.companyId = :companyId AND lr.employee.id = :employeeId " +
           "AND lr.status IN ('PENDING', 'APPROVED') " +
           "AND (:startDate <= lr.endDate AND :endDate >= lr.startDate)")
    boolean hasOverlappingLeave(@Param("companyId") UUID companyId, @Param("employeeId") UUID employeeId,
                                @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
