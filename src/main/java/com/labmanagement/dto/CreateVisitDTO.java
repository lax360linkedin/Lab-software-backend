package com.labmanagement.dto;

import java.util.List;

public class CreateVisitDTO {

	private String patientCode;
	private String doctorCode;
	private List<String> testCodes;

	public CreateVisitDTO() {
	}

	public String getPatientCode() {
		return patientCode;
	}

	public void setPatientCode(String patientCode) {
		this.patientCode = patientCode;
	}

	public String getDoctorCode() {
		return doctorCode;
	}

	public void setDoctorCode(String doctorCode) {
		this.doctorCode = doctorCode;
	}

	public List<String> getTestCodes() {
		return testCodes;
	}

	public void setTestCodes(List<String> testCodes) {
		this.testCodes = testCodes;
	}
}