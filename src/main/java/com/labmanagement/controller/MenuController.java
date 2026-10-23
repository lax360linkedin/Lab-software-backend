
package com.labmanagement.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.labmanagement.dto.MenuRequestDTO;
import com.labmanagement.dto.MenuResponseDTO;
import com.labmanagement.entity.Menu;
import com.labmanagement.entity.User;
import com.labmanagement.service.MenuService;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

	private final MenuService menuService;

	public MenuController(MenuService menuService) {
		this.menuService = menuService;
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public MenuResponseDTO createMenu(@RequestBody MenuRequestDTO request, Authentication authentication) {

		Integer updatedBy = getLoggedInUserId(authentication);

		Menu savedMenu = menuService.createMenu(request, updatedBy);

		MenuResponseDTO response = new MenuResponseDTO();
		response.setMenuCode(savedMenu.getMenuCode());
		response.setMenuName(savedMenu.getMenuName());
		response.setLink(savedMenu.getLink());
		response.setActiveFlag(savedMenu.getActiveFlag());

		return response;
	}

	private Integer getLoggedInUserId(Authentication authentication) {

		if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
			throw new IllegalStateException("Authenticated user not found");
		}

		User user = (User) authentication.getPrincipal();

		return user.getId();
	}

	// Get all active menus
	// Get all active menus based on logged-in user's permissions
	@GetMapping
	public List<MenuResponseDTO> getAllActiveMenus(Authentication authentication) {
		return menuService.getAllActiveMenuResponses(authentication);
	}

	@PutMapping("/{menuCode}")
	@PreAuthorize("hasRole('ADMIN')")
	public MenuResponseDTO updateMenu(@PathVariable String menuCode, @RequestBody MenuRequestDTO request,
			Authentication authentication) {

		Integer updatedBy = getLoggedInUserId(authentication);

		Menu updatedMenu = menuService.updateMenu(menuCode, request, updatedBy);

		MenuResponseDTO response = new MenuResponseDTO();
		response.setMenuCode(updatedMenu.getMenuCode());
		response.setMenuName(updatedMenu.getMenuName());
		response.setLink(updatedMenu.getLink());
		response.setActiveFlag(updatedMenu.getActiveFlag());

		return response;
	}

	@PatchMapping("/{menuCode}/status")
	@PreAuthorize("hasRole('ADMIN')")
	public MenuResponseDTO updateMenuStatus(@PathVariable String menuCode, @RequestParam Integer activeFlag,
			Authentication authentication) {

		Integer updatedBy = getLoggedInUserId(authentication);

		Menu updatedMenu = menuService.updateMenuStatus(menuCode, activeFlag, updatedBy);

		MenuResponseDTO response = new MenuResponseDTO();
		response.setMenuCode(updatedMenu.getMenuCode());
		response.setMenuName(updatedMenu.getMenuName());
		response.setLink(updatedMenu.getLink());
		response.setActiveFlag(updatedMenu.getActiveFlag());

		return response;
	}

}
