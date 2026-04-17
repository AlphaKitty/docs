package com.expertlink.service;

import com.expertlink.domain.Expert;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpertPrivacyService {

    private static final Set<String> ELEVATED = Set.of(
            "ROLE_SUPER_ADMIN",
            "ROLE_DOMAIN_STEWARD",
            "ROLE_DEPT_ADMIN",
            "ROLE_EXPERT_USER"
    );

    public boolean shouldMask(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return true;
        }
        Set<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        boolean elevated = authorities.stream().anyMatch(ELEVATED::contains);
        if (elevated) {
            return false;
        }
        return authorities.contains("ROLE_REGULAR_USER") || authorities.contains("ROLE_VISITOR");
    }

    public Expert maskForRead(Expert expert, Authentication authentication) {
        if (expert == null || !shouldMask(authentication)) {
            return expert;
        }
        Expert m = new Expert();
        BeanUtils.copyProperties(expert, m,
                "skills", "domains", "projectExperiences", "reviews", "owner", "primaryDomain");
        m.setName(maskName(expert.getName()));
        m.setEnglishName(maskName(expert.getEnglishName()));
        m.setPhoneNumber(null);
        m.setEmail(null);
        m.setWechatId(null);
        m.setIdNumber(null);
        if (expert.getSkills() != null) {
            m.setSkills(new HashSet<>(expert.getSkills()));
        }
        if (expert.getDomains() != null) {
            m.setDomains(new HashSet<>(expert.getDomains()));
        }
        m.setOwner(expert.getOwner());
        m.setPrimaryDomain(expert.getPrimaryDomain());
        return m;
    }

    public Page<Expert> maskPage(Page<Expert> page, Authentication authentication) {
        if (!shouldMask(authentication)) {
            return page;
        }
        Pageable pageable = page.getPageable();
        var content = page.getContent().stream()
                .map(e -> maskForRead(e, authentication))
                .toList();
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    static String maskName(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        String n = name.trim();
        if (n.length() <= 1) {
            return "*";
        }
        if (n.length() == 2) {
            return n.charAt(0) + "*";
        }
        return n.charAt(0) + "*" + n.charAt(n.length() - 1);
    }
}
