package com.hrms.repository;

import com.hrms.entity.CompanyMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership, UUID> {
    List<CompanyMembership> findByUserId(UUID userId);
    List<CompanyMembership> findByCompanyId(UUID companyId);
    Optional<CompanyMembership> findByCompanyIdAndUserId(UUID companyId, UUID userId);
}
