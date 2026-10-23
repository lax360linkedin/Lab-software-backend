
package com.labmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestCategoryDTO {

	private Integer id;
	private String categoryName;
	private String description;
	private Integer activeFlag;
	private Long testCount;

	public Long getTestCount() {
		return testCount;
	}

	public void setTestCount(Long testCount) {
		this.testCount = testCount;
	}

	public TestCategoryDTO() {
	}

	public TestCategoryDTO(Integer id, String categoryName, String description, Integer activeFlag) {
		this.id = id;
		this.categoryName = categoryName;
		this.description = description;
		this.activeFlag = activeFlag;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(Integer activeFlag) {
		this.activeFlag = activeFlag;
	}
}
