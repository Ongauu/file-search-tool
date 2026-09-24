package com.crimson.fileSearch.repository;

import com.crimson.fileSearch.entity.Role;
import com.crimson.fileSearch.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
