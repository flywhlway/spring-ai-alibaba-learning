package com.flywhl.saa.smartcs.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flywhl.saa.smartcs.model.entity.SysUser;

/**
 * sys_user JPA Repository。
 *
 * @author flywhl
 * @since 1.0.0
 */
public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    Optional<SysUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
