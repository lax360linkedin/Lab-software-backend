
package com.labmanagement.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.labmanagement.dao.MenuDAO;
import com.labmanagement.dao.MenuEventDAO;
import com.labmanagement.dto.MenuEventRequestDTO;
import com.labmanagement.dto.MenuEventResponseDTO;
import com.labmanagement.dto.MenuRequestDTO;
import com.labmanagement.dto.MenuResponseDTO;
import com.labmanagement.entity.Menu;
import com.labmanagement.entity.MenuEvent;
import com.labmanagement.util.ReferenceCodeGenerator;

@Service
public class MenuService {

	private final MenuDAO menuDAO;
	private final MenuEventDAO menuEventDAO;
	private final RolePermissionService rolePermissionService;

	public MenuService(MenuDAO menuDAO, MenuEventDAO menuEventDAO, RolePermissionService rolePermissionService) {
		this.menuDAO = menuDAO;
		this.menuEventDAO = menuEventDAO;
		this.rolePermissionService = rolePermissionService;
	}

	@Transactional(readOnly = true)
	public List<Menu> getAllActiveMenus() {
		return menuDAO.findAll().stream().filter(menu -> Integer.valueOf(1).equals(menu.getActiveFlag())).toList();
	}

	@Transactional
	public Menu saveMenu(Menu menu) {
		return menuDAO.save(menu);
	}

	@Transactional(readOnly = true)
	public Menu getMenuById(Integer id) {
		return menuDAO.findById(id).filter(menu -> Integer.valueOf(1).equals(menu.getActiveFlag()))
				.orElseThrow(() -> new IllegalArgumentException("Active menu not found"));
	}

	@Transactional
	public Menu createMenu(MenuRequestDTO request, Integer updatedBy) {

		Menu menu = new Menu();

		menu.setMenuCode(ReferenceCodeGenerator.generate("MNU"));
		menu.setMenuName(request.getMenuName());
		menu.setActiveFlag(request.getActiveFlag());
		menu.setUpdatedBy(updatedBy);

		menu = menuDAO.save(menu);

		if (request.getEvents() != null) {
			for (MenuEventRequestDTO eventRequest : request.getEvents()) {

				MenuEvent event = new MenuEvent();
				event.setMenu(menu);
				event.setEventName(eventRequest.getEventName());

				event.setEventType(MenuEvent.EventType.valueOf(eventRequest.getEventType().toUpperCase()));

				event.setActiveFlag(eventRequest.getActiveFlag() == null ? 1 : eventRequest.getActiveFlag());

				event.setUpdatedBy(updatedBy);

				menuEventDAO.save(event);
			}
		}

		return menu;
	}

	@Transactional(readOnly = true)
	public List<MenuResponseDTO> getAllActiveMenuResponses(Authentication authentication) {

		List<Menu> menus = menuDAO.findAll().stream().filter(menu -> Integer.valueOf(1).equals(menu.getActiveFlag()))
				.toList();

		List<MenuResponseDTO> responses = new ArrayList<>();

		for (Menu menu : menus) {

			List<MenuEvent> menuEvents = menuEventDAO.findByMenuIdAndActiveFlag(menu.getId(), 1);

			List<MenuEventResponseDTO> eventResponses = new ArrayList<>();

			for (MenuEvent event : menuEvents) {

				boolean allowed = rolePermissionService.hasMenuEventVisibility(authentication, event.getId());

				if (!allowed) {
					continue;
				}

				MenuEventResponseDTO eventResponse = new MenuEventResponseDTO();

				eventResponse.setEventId(event.getId());
				eventResponse.setEventName(event.getEventName());
				eventResponse.setEventType(event.getEventType().name());
				eventResponse.setActiveFlag(event.getActiveFlag());

				eventResponses.add(eventResponse);
			}

			MenuResponseDTO response = new MenuResponseDTO();

			response.setMenuCode(menu.getMenuCode());
			response.setMenuName(menu.getMenuName());
			response.setLink(menu.getLink());
			response.setActiveFlag(menu.getActiveFlag());
			response.setEvents(eventResponses);

			responses.add(response);
		}

		return responses;
	}

	@Transactional
	public Menu updateMenu(String menuCode, MenuRequestDTO request, Integer updatedBy) {

		Menu menu = menuDAO.findByMenuCode(menuCode)
				.orElseThrow(() -> new IllegalArgumentException("Menu not found with code: " + menuCode));

		if (request.getMenuName() == null || request.getMenuName().trim().isEmpty()) {
			throw new IllegalArgumentException("Menu name is required");
		}

		String menuName = request.getMenuName().trim();

		boolean duplicateName = menuDAO.findAll().stream().anyMatch(existing -> !existing.getMenuCode().equals(menuCode)
				&& existing.getMenuName().equalsIgnoreCase(menuName));

		if (duplicateName) {
			throw new IllegalArgumentException("Menu name already exists");
		}

		menu.setMenuName(menuName);
		menu.setLink(request.getLink());
		menu.setUpdatedBy(updatedBy);

		if (request.getActiveFlag() != null) {
			if (request.getActiveFlag() != 0 && request.getActiveFlag() != 1) {
				throw new IllegalArgumentException("Active flag must be 1 (Active) or 0 (Inactive)");
			}

			menu.setActiveFlag(request.getActiveFlag());
		}

		menuDAO.save(menu);

		if (request.getEvents() != null) {

			for (MenuEventRequestDTO eventRequest : request.getEvents()) {

				if (eventRequest.getEventName() == null || eventRequest.getEventName().trim().isEmpty()) {
					throw new IllegalArgumentException("Event name is required");
				}

				if (eventRequest.getEventType() == null || eventRequest.getEventType().trim().isEmpty()) {
					throw new IllegalArgumentException("Event type is required");
				}

				MenuEvent.EventType eventType;

				try {
					eventType = MenuEvent.EventType.valueOf(eventRequest.getEventType().trim().toUpperCase());
				} catch (IllegalArgumentException ex) {
					throw new IllegalArgumentException("Invalid event type. Allowed values: SUBMENU, ACTION");
				}

				Integer activeFlag = eventRequest.getActiveFlag() == null ? 1 : eventRequest.getActiveFlag();

				if (activeFlag != 0 && activeFlag != 1) {
					throw new IllegalArgumentException("Event active flag must be 1 or 0");
				}

				String eventId = eventRequest.getEventId();
				MenuEvent event;

				if (eventId == null || eventId.trim().isEmpty()) {

					// Empty eventId: create a new event
					event = new MenuEvent();
					event.setMenu(menu);

				} else {

					// Numeric eventId: update an existing event
					Integer id;

					try {
						id = Integer.valueOf(eventId.trim());
					} catch (NumberFormatException ex) {
						throw new IllegalArgumentException("Event ID must be empty or a valid numeric ID");
					}

					event = menuEventDAO.findByIdAndMenuMenuCode(id, menuCode)
							.orElseThrow(() -> new IllegalArgumentException("Event not found for this menu: " + id));
				}

				event.setEventName(eventRequest.getEventName().trim());
				event.setEventType(eventType);
				event.setActiveFlag(activeFlag);
				event.setUpdatedBy(updatedBy);

				menuEventDAO.save(event);
			}
		}

		return menuDAO.findByMenuCode(menuCode)
				.orElseThrow(() -> new IllegalArgumentException("Menu not found with code: " + menuCode));
	}

	@Transactional
	public Menu updateMenuStatus(String menuCode, Integer activeFlag, Integer updatedBy) {

		Menu menu = menuDAO.findByMenuCode(menuCode)
				.orElseThrow(() -> new IllegalArgumentException("Menu not found with code: " + menuCode));

		if (activeFlag == null || (activeFlag != 0 && activeFlag != 1)) {
			throw new IllegalArgumentException("Active flag must be 1 (Active) or 0 (Inactive)");
		}

		menu.setActiveFlag(activeFlag);
		menu.setUpdatedBy(updatedBy);

		return menuDAO.save(menu);
	}

}
