package com.hrms.repository;

import com.hrms.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    List<Employee> findByCompanyId(UUID companyId);
    Page<Employee> findByCompanyId(UUID companyId, Pageable pageable);
    Optional<Employee> findByCompanyIdAndId(UUID companyId, UUID id);
    Optional<Employee> findByCompanyIdAndUserId(UUID companyId, UUID userId);
    Optional<Employee> findByCompanyIdAndEmployeeCode(UUID companyId, String employeeCode);
    boolean existsByCompanyIdAndEmployeeCode(UUID companyId, String employeeCode);
    List<Employee> findByCompanyIdAndManagerId(UUID companyId, UUID managerId);

    @Query("SELECT e FROM Employee e WHERE e.companyId = :companyId AND " +
           "(LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.workEmail) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Employee> searchEmployees(@Param("companyId") UUID companyId, @Param("search") String search, Pageable pageable);
}
