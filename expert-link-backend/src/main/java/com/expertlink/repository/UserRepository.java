package com.expertlink.repository;

import com.expertlink.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    List<User> findByIsActiveTrue();

    List<User> findByIsActiveFalse();

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") String role);

    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r = :role")
    long countByRole(@Param("role") String role);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r = :userType")
    List<User> findByUserType(@Param("userType") String userType);

    /** 尚未建立专家档案的活跃用户（用于从用户库拉取专家） */
    @Query("""
            SELECT u FROM User u
            WHERE COALESCE(u.isDeleted, false) = false
              AND COALESCE(u.isActive, true) = true
              AND NOT EXISTS (
                  SELECT 1 FROM Expert e
                  WHERE e.owner.id = u.id AND COALESCE(e.isDeleted, false) = false
              )
              AND (
                  :keyword IS NULL OR :keyword = ''
                  OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(COALESCE(u.fullName, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<User> searchActiveUsersWithoutExpertProfile(@Param("keyword") String keyword, Pageable pageable);
}
