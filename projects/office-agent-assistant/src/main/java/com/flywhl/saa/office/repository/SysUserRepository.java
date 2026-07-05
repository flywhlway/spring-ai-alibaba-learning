package com.flywhl.saa.office.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.flywhl.saa.office.model.entity.SysUser;
public interface SysUserRepository extends JpaRepository<SysUser, Long> {
    Optional<SysUser> findByUsername(String username);
    boolean existsByUsername(String username);
}

