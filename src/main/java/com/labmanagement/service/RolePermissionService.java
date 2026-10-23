
package com.labmanagement.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.labmanagement.dao.MenuEventDAO;
import com.labmanagement.dao.RolePermissionDAO;
import com.labmanagement.entity.MenuEvent;
import com.labmanagement.entity.RolePermission;
import com.labmanagement.entity.User;
import com.labmanagement.entity.UserRole;

@Service
public class RolePermissionService {

	private final RolePermissionDAO rolePermissionDAO;
	private final MenuEventDAO menuEventDAO;

	public RolePermissionService(RolePermissionDAO rolePermissionDAO, MenuEventDAO menuEventDAO) {

		this.rolePermissionDAO = rolePermissionDAO;
		this.menuEventDAO = menuEventDAO;
	}

	@Transactional
	public RolePermission savePermission(UserRole roleName, Integer menuEventId, RolePermission.AccessLevel accessLevel,
			Integer updatedBy) {

		if (roleName == null || menuEventId == null || accessLevel == null || updatedBy == null) {
			throw new IllegalArgumentException("Role, event, access level and updatedBy are required");
		}

		if (roleName == UserRole.ADMIN) {
			throw new IllegalArgumentException("Admin permissions are automatic");
		}

		MenuEvent menuEvent = menuEventDAO.findById(menuEventId)
				.orElseThrow(() -> new IllegalArgumentException("Menu event not found: " + menuEventId));

		RolePermission permission = rolePermissionDAO.findByRoleNameAndMenuEventId(roleName, menuEventId)
				.orElseGet(RolePermission::new);

		permission.setRoleName(roleName);
		permission.setMenuEvent(menuEvent);
		permission.setAccessLevel(accessLevel);
		permission.setActiveFlag(1);
		permission.setUpdatedBy(updatedBy);

		return rolePermissionDAO.save(permission);
	}

	@Transactional(readOnly = true)
	public List<RolePermission> getPermissionsByRole(UserRole roleName) {
		return rolePermissionDAO.findByRoleNameAndActiveFlag(roleName, 1);
	}

	@Transactional(readOnly = true)
	public boolean hasAccess(UserRole roleName, Integer menuEventId, RolePermission.AccessLevel requiredAccess) {

		return getPermissionsByRole(roleName).stream()
				.anyMatch(permission -> permission.getMenuEvent().getId().equals(menuEventId)
						&& permission.getAccessLevel() == requiredAccess);
	}

	@Transactional(readOnly = true)
	public boolean hasEventAccess(org.springframework.security.core.Authentication authentication, String menuCode,
			String eventName) {

		// Step 1: Validate user authentication and input
		if (authentication == null || !authentication.isAuthenticated()
				|| !(authentication.getPrincipal() instanceof com.labmanagement.entity.User user)
				|| user.getActiveFlag() == null || user.getActiveFlag() != 1 || user.getRole() == null
				|| menuCode == null || eventName == null) {

			return false;
		}

		// Step 2: Admin automatically gets FULL access
		if (user.getRole() == UserRole.ADMIN) {
			return true;
		}

		// Step 3: Check configured permissions for other roles
		return getPermissionsByRole(user.getRole()).stream()
				.filter(permission -> permission.getMenuEvent() != null && permission.getMenuEvent().getMenu() != null)
				.anyMatch(permission -> {

					var event = permission.getMenuEvent();

					// Verify menu code and event name
					if (!menuCode.equals(event.getMenu().getMenuCode()) || !eventName.equals(event.getEventName())) {
						return false;
					}

					var access = permission.getAccessLevel();

					if (access == null || access == RolePermission.AccessLevel.NONE) {
						return false;
					}

					// FULL access
					if (access == RolePermission.AccessLevel.FULL) {
						return true;
					}

					// View events
					if (eventName.startsWith("View ")) {
						return access == RolePermission.AccessLevel.VIEW
								|| access == RolePermission.AccessLevel.VIEW_SELECT
								|| access == RolePermission.AccessLevel.MANAGE_VIEW
								|| access == RolePermission.AccessLevel.VIEW_SHARE_PRINT
								|| access == RolePermission.AccessLevel.LIMITED_VIEW
								|| access == RolePermission.AccessLevel.TECHNICAL;
					}

					// Manage events
					if (eventName.startsWith("Manage ")) {
						return access == RolePermission.AccessLevel.MANAGE_VIEW
								|| access == RolePermission.AccessLevel.TECHNICAL;
					}

					// Create, update, accept, reject and register events
					if (eventName.startsWith("Update ") || eventName.startsWith("Accept ")
							|| eventName.startsWith("Reject ") || eventName.startsWith("Create ")
							|| eventName.startsWith("Register ")) {

						return access == RolePermission.AccessLevel.MANAGE_VIEW
								|| access == RolePermission.AccessLevel.TECHNICAL;
					}

					// Generate events
					if (eventName.startsWith("Generate ")) {
						return access == RolePermission.AccessLevel.VIEW_SHARE_PRINT
								|| access == RolePermission.AccessLevel.TECHNICAL;
					}

					return false;
				});
	}

	@Transactional(readOnly = true)
	public boolean hasMenuEventVisibility(Authentication authentication, String menuCode, String eventName) {

		if (authentication == null || !authentication.isAuthenticated()
				|| !(authentication.getPrincipal() instanceof com.labmanagement.entity.User user)
				|| user.getActiveFlag() == null || user.getActiveFlag() != 1 || user.getRole() == null
				|| menuCode == null || eventName == null) {
			return false;
		}

		// Admin can see all active menu events
		if (user.getRole() == UserRole.ADMIN) {
			return true;
		}

		// Check the role's active permissions
		return getPermissionsByRole(user.getRole()).stream()
				.filter(permission -> permission.getMenuEvent() != null && permission.getMenuEvent().getMenu() != null)
				.anyMatch(permission -> {

					var event = permission.getMenuEvent();
					var access = permission.getAccessLevel();

					return menuCode.equals(event.getMenu().getMenuCode()) && eventName.equals(event.getEventName())
							&& access != null && access != RolePermission.AccessLevel.NONE;
				});
	}

	@Transactional(readOnly = true)
	public boolean hasMenuEventVisibility(Authentication authentication, Integer menuEventId) {

		// Validate logged-in user
		if (authentication == null || !authentication.isAuthenticated()
				|| !(authentication.getPrincipal() instanceof User user) || user.getActiveFlag() == null
				|| user.getActiveFlag() != 1 || user.getRole() == null || menuEventId == null) {

			return false;
		}

		// Admin can access all active menu events
		if (user.getRole() == UserRole.ADMIN) {
			return true;
		}

		// Check permissions for logged-in user's role
		return getPermissionsByRole(user.getRole()).stream()
				.anyMatch(permission -> permission.getMenuEvent() != null
						&& menuEventId.equals(permission.getMenuEvent().getId()) && permission.getAccessLevel() != null
						&& permission.getAccessLevel() != RolePermission.AccessLevel.NONE);
	}

	public RolePermission updatePermissionStatus(Integer id, Integer activeFlag) {

		RolePermission permission = rolePermissionDAO.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Role permission not found: " + id));

		permission.setActiveFlag(activeFlag);

		return rolePermissionDAO.save(permission);
	}

}
