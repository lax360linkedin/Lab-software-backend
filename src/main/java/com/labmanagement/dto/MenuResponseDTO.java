package com.labmanagement.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuResponseDTO {

	private String menuCode;
	private String menuName;
	private String link;
	private Integer activeFlag;
	private List<MenuEventResponseDTO> events;

	public String getMenuCode() {
		return menuCode;
	}

	public void setMenuCode(String menuCode) {
		this.menuCode = menuCode;
	}

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

	public Integer getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(Integer activeFlag) {
		this.activeFlag = activeFlag;
	}

	public List<MenuEventResponseDTO> getEvents() {
		return events;
	}

	public void setEvents(List<MenuEventResponseDTO> events) {
		this.events = events;
	}

}