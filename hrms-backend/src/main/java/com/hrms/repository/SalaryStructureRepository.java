package com.hrms.repository;

import com.hrms.entity.SalaryStructure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, UUID> {
    Optional<SalaryStructure> findTopByCompanyIdAndEmployeeIdOrderByEffectiveFromDesc(UUID companyId, UUID employeeId);
    List<SalaryStructure> findByCompanyId(UUID companyId);
}
