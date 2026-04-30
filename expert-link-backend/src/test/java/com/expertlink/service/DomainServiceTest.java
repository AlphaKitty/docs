package com.expertlink.service;

import com.expertlink.domain.Domain;
import com.expertlink.repository.DomainRepository;
import com.expertlink.repository.ExpertRepository;
import com.expertlink.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomainServiceTest {

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private ExpertRepository expertRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DomainService domainService;

    @Test
    void updateClearsParentWhenParentIdIsNull() {
        Domain parent = Domain.builder()
                .name("父领域")
                .description("父领域描述")
                .parentId(null)
                .level(1)
                .isActive(true)
                .build();
        parent.setId(10L);

        Domain existing = Domain.builder()
                .name("子领域")
                .description("子领域描述")
                .parentId(10L)
                .parent(parent)
                .level(2)
                .isActive(true)
                .build();
        existing.setId(20L);
        parent.getChildren().add(existing);

        Domain update = Domain.builder()
                .name("子领域")
                .description("已清空父级")
                .parentId(null)
                .isActive(true)
                .build();

        when(domainRepository.findById(20L)).thenReturn(Optional.of(existing));
        when(domainRepository.save(existing)).thenReturn(existing);

        domainService.update(20L, update);

        ArgumentCaptor<Domain> captor = ArgumentCaptor.forClass(Domain.class);
        verify(domainRepository).save(captor.capture());
        Domain saved = captor.getValue();
        assertNotNull(saved);

        assertNull(saved.getParentId());
        assertNull(saved.getParent());
        assertEquals(1, saved.getLevel());
    }

    @Test
    void collectWithAncestorDomainIds_includesParentChain() {
        Domain parent = Domain.builder().name("父").build();
        parent.setId(1L);
        parent.setParentId(null);
        Domain child = Domain.builder().name("子").build();
        child.setId(2L);
        child.setParentId(1L);
        when(domainRepository.findAll()).thenReturn(List.of(parent, child));

        Set<Long> out = domainService.collectWithAncestorDomainIds(Set.of(2L));

        assertEquals(Set.of(1L, 2L), out);
    }
}
