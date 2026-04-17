package com.expertlink.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "domains")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Domain extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "english_name", length = 100)
    private String englishName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "level")
    private Integer level = 1;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    @Column(name = "color_code", length = 20)
    private String colorCode;

    @Column(name = "expert_count")
    private Integer expertCount = 0;

    @Column(name = "project_count")
    private Integer projectCount = 0;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    @JsonIgnore
    private Domain parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private Set<Domain> children = new HashSet<>();

    @ManyToMany(mappedBy = "domains")
    @JsonIgnore
    @Builder.Default
    private Set<Expert> experts = new HashSet<>();

    @OneToMany(mappedBy = "domain")
    @JsonIgnore
    @Builder.Default
    private Set<Project> projects = new HashSet<>();

    /** 领域行管（可多选 / AB 角），由超级管理员配置 */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "domain_stewards",
            joinColumns = @JoinColumn(name = "domain_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "ownedExperts", "createdProjects",
            "reviews", "sentMessages", "receivedMessages", "notifications"})
    @Builder.Default
    private Set<User> stewards = new HashSet<>();

    // Helper methods
    public void addChild(Domain child) {
        children.add(child);
        child.setParent(this);
        child.setParentId(this.getId());
        child.setLevel(this.getLevel() + 1);
    }

    public void removeChild(Domain child) {
        children.remove(child);
        child.setParent(null);
        child.setParentId(null);
        child.setLevel(1);
    }
}