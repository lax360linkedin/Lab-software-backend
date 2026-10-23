
package com.labmanagement.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.labmanagement.dto.RolePermissionResponseDTO;
import com.labmanagement.entity.RolePermission;
import com.labmanagement.entity.UserRole;
import com.labmanagement.service.RolePermissionService;

@RestController
@RequestMapping("/api/role-permissions")
public class RolePermissionController {

	private final RolePermissionService rolePermissionService;

	public RolePermissionController(RolePermissionService rolePermissionService) {
		this.rolePermissionService = rolePermissionService;
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<RolePermissionResponseDTO> savePermission(@RequestBody RolePermissionRequest request,
			Authentication authentication) {

		if (request.getRoleName() == null || request.getMenuEventId() == null || request.getAccessLevel() == null) {
			throw new IllegalArgumentException("Role, menuEventId and accessLevel are required");
		}

		if (!(authentication.getPrincipal() instanceof com.labmanagement.entity.User user)) {
			return ResponseEntity.status(401).build();
		}

		RolePermission saved = rolePermissionService.savePermission(request.getRoleName(), request.getMenuEventId(),
				request.getAccessLevel(), user.getId());

		return ResponseEntity.ok(toResponseDTO(saved));
	}

	@GetMapping("/{roleName}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<RolePermissionResponseDTO>> getPermissionsByRole(@PathVariable UserRole roleName) {

		List<RolePermissionResponseDTO> response = rolePermissionService.getPermissionsByRole(roleName).stream()
				.map(this::toResponseDTO).toList();

		return ResponseEntity.ok(response);
	}

	private RolePermissionResponseDTO toResponseDTO(RolePermission permission) {

		RolePermissionResponseDTO dto = new RolePermissionResponseDTO();

		dto.setId(permission.getId());
		dto.setRoleName(permission.getRoleName().name());
		dto.setMenuEventId(permission.getMenuEvent().getId());
		dto.setMenuCode(permission.getMenuEvent().getMenu().getMenuCode());
		dto.setMenuName(permission.getMenuEvent().getMenu().getMenuName());
		dto.setEventName(permission.getMenuEvent().getEventName());
		dto.setEventType(permission.getMenuEvent().getEventType().name());
		dto.setAccessLevel(permission.getAccessLevel().name());
		dto.setActiveFlag(permission.getActiveFlag());

		return dto;
	}

	public static class RolePermissionRequest {

		private UserRole roleName;
		private Integer menuEventId;
		private RolePermission.AccessLevel accessLevel;

		public UserRole getRoleName() {
			return roleName;
		}

		public void setRoleName(UserRole roleName) {
			this.roleName = roleName;
		}

		public Integer getMenuEventId() {
			return menuEventId;
		}

		public void setMenuEventId(Integer menuEventId) {
			this.menuEventId = menuEventId;
		}

		public RolePermission.AccessLevel getAccessLevel() {
			return accessLevel;
		}

		public void setAccessLevel(RolePermission.AccessLevel accessLevel) {
			this.accessLevel = accessLevel;
		}
	}
}
