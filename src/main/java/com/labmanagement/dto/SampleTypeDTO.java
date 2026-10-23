
package com.labmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SampleTypeDTO {

	private Integer id;
	private String sampleTypeName;
	private String description;
	private Integer activeFlag;
	private Long testCount;

	public Long getTestCount() {
		return testCount;
	}

	public void setTestCount(Long testCount) {
		this.testCount = testCount;
	}

	public SampleTypeDTO() {
	}

	public SampleTypeDTO(Integer id, String sampleTypeName, String description, Integer activeFlag) {
		this.id = id;
		this.sampleTypeName = sampleTypeName;
		this.description = description;
		this.activeFlag = activeFlag;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSampleTypeName() {
		return sampleTypeName;
	}

	public void setSampleTypeName(String sampleTypeName) {
		this.sampleTypeName = sampleTypeName;
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
