
package com.labmanagement.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.labmanagement.service.RolePermissionService;

@Component("menuPermissionChecker")
public class MenuPermissionChecker {

	private final RolePermissionService rolePermissionService;

	public MenuPermissionChecker(RolePermissionService rolePermissionService) {
		this.rolePermissionService = rolePermissionService;
	}

	public boolean hasPermission(Authentication authentication, String menu, String event) {

		return rolePermissionService.hasEventAccess(authentication, menu, event);
	}
}
