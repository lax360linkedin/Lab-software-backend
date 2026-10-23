package com.labmanagement.dto;

import java.util.ArrayList;
import java.util.List;

public class MenuRequestDTO {

	private String menuName;
	private String link;
	private Integer parentMenuId;
	private Integer activeFlag = 1;
	private List<MenuEventRequestDTO> events = new ArrayList<>();
	public String getMenuName() {
		return menuName;
	}
	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public Integer getParentMenuId() {
		return parentMenuId;
	}
	public void setParentMenuId(Integer parentMenuId) {
		this.parentMenuId = parentMenuId;
	}
	public Integer getActiveFlag() {
		return activeFlag;
	}
	public void setActiveFlag(Integer activeFlag) {
		this.activeFlag = activeFlag;
	}
	public List<MenuEventRequestDTO> getEvents() {
		return events;
	}
	public void setEvents(List<MenuEventRequestDTO> events) {
		this.events = events;
	}

	
}