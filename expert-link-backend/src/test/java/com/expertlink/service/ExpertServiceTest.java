package com.expertlink.service;

import com.expertlink.domain.Domain;
import com.expertlink.domain.Expert;
import com.expertlink.domain.User;
import com.expertlink.repository.DomainRepository;
import com.expertlink.repository.ExpertDesignationRepository;
import com.expertlink.repository.ExpertRepository;
import com.expertlink.repository.SkillRepository;
import com.expertlink.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExpertServiceTest {

    private final ExpertRepository expertRepository = mock(ExpertRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final DomainRepository domainRepository = mock(DomainRepository.class);
    private final DomainService domainService = mock(DomainService.class);
    private final SkillRepository skillRepository = mock(SkillRepository.class);
    private final ExpertDesignationRepository expertDesignationRepository = mock(ExpertDesignationRepository.class);

    private final ExpertService expertService = new ExpertService(
            expertRepository,
            userRepository,
            domainRepository,
            domainService,
            skillRepository,
            expertDesignationRepository
    );

    @Test
    void findByDomainIdsOnlyReturnsEligibleRoleExperts() {
        when(domainService.collectSubtreeDomainIds(Set.of(10L))).thenReturn(Set.of(10L, 11L));

        User expertOwner = User.builder().roles(Set.of("EXPERT_USER")).build();
        expertOwner.setId(1L);
        User regularOwner = User.builder().roles(Set.of("REGULAR_USER")).build();
        regularOwner.setId(2L);
        User superAdminOwner = User.builder().roles(Set.of("SUPER_ADMIN")).build();
        superAdminOwner.setId(3L);

        Domain primary10 = Domain.builder().name("d10").build();
        primary10.setId(10L);
        Domain primary11 = Domain.builder().name("d11").build();
        primary11.setId(11L);

        Expert expertUser = Expert.builder().name("Expert User").owner(expertOwner).primaryDomain(primary10).build();
        expertUser.setId(1L);
        Expert regularUser = Expert.builder().name("Regular User").owner(regularOwner).primaryDomain(primary10).build();
        regularUser.setId(2L);
        Expert superAdmin = Expert.builder().name("Super Admin").owner(superAdminOwner).primaryDomain(primary11).build();
        superAdmin.setId(3L);
        when(expertRepository.findByDomainIds(anySet())).thenReturn(List.of(expertUser, regularUser, superAdmin));

        List<Expert> result = expertService.findByDomainIds(Set.of(10L));

        assertEquals(2, result.size());
        assertEquals(Set.of(1L, 3L), result.stream().map(Expert::getId).collect(java.util.stream.Collectors.toSet()));
    }
}
