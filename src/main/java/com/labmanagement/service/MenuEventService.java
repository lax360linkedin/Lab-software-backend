
package com.labmanagement.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.labmanagement.dao.MenuEventDAO;
import com.labmanagement.entity.MenuEvent;

@Service
public class MenuEventService {

	private final MenuEventDAO menuEventDAO;

	public MenuEventService(MenuEventDAO menuEventDAO) {
		this.menuEventDAO = menuEventDAO;
	}

	@Transactional(readOnly = true)
	public List<MenuEvent> getActiveEventsByMenuId(Integer menuId) {
		return menuEventDAO.findByMenuIdAndActiveFlag(menuId, 1);
	}

	@Transactional
	public MenuEvent saveMenuEvent(MenuEvent menuEvent) {
		return menuEventDAO.save(menuEvent);
	}

	@Transactional(readOnly = true)
	public MenuEvent getMenuEventById(Integer id) {
		return menuEventDAO.findById(id).filter(event -> Integer.valueOf(1).equals(event.getActiveFlag()))
				.orElseThrow(() -> new IllegalArgumentException("Active menu event not found"));
	}
}
