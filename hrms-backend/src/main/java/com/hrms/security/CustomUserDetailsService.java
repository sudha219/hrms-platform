package com.hrms.security;

import com.hrms.entity.CompanyMembership;
import com.hrms.entity.User;
import com.hrms.repository.CompanyMembershipRepository;
import com.hrms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CompanyMembershipRepository membershipRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        List<CompanyMembership> memberships = membershipRepository.findByUserId(user.getId());
        CompanyMembership primaryMembership = memberships.isEmpty() ? null : memberships.get(0);

        return UserPrincipal.create(user, primaryMembership);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserByIdAndCompany(UUID userId, UUID companyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        CompanyMembership membership = null;
        if (companyId != null) {
            membership = membershipRepository.findByCompanyIdAndUserId(companyId, userId).orElse(null);
        } else {
            List<CompanyMembership> memberships = membershipRepository.findByUserId(user.getId());
            membership = memberships.isEmpty() ? null : memberships.get(0);
        }

        return UserPrincipal.create(user, membership);
    }
}
