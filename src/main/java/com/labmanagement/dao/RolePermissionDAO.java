package com.labmanagement.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.labmanagement.entity.RolePermission;
import com.labmanagement.entity.UserRole;

public interface RolePermissionDAO extends JpaRepository<RolePermission, Integer> {

	List<RolePermission> findByRoleNameAndActiveFlag(UserRole roleName, Integer activeFlag);

	Optional<RolePermission> findByRoleNameAndMenuEventId(UserRole roleName, Integer menuEventId);
}