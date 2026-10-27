
package com.labmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.labmanagement.entity.Patient.Gender;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PatientDTO {

	private Integer id;
	private String code;
	private String patientName;
	private Integer age;
	private Gender gender;
	private String phone;
	private String address;
	private String email;

	private Integer doctorId;
	private String doctorCode;
	private String doctorName;
	private List<String> testCodes;
	private List<PatientVisitDTO> visits;

	public List<PatientVisitDTO> getVisits() {
		return visits;
	}

	public void setVisits(List<PatientVisitDTO> visits) {
		this.visits = visits;
	}

	public List<String> getTestCodes() {
		return testCodes;
	}

	public void setTestCodes(List<String> testCodes) {
		this.testCodes = testCodes;
	}

	private List<TestDTO> tests;
	private BigDecimal totalTestAmount;

	private LocalDateTime registrationDate;
	private Integer activeFlag;
	private Integer updatedBy;
	private LocalDateTime updatedAt;

	public PatientDTO() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Integer getDoctorId() {
		return doctorId;
	}

	public void setDoctorId(Integer doctorId) {
		this.doctorId = doctorId;
	}

	public String getDoctorCode() {
		return doctorCode;
	}

	public void setDoctorCode(String doctorCode) {
		this.doctorCode = doctorCode;
	}

	public String getDoctorName() {
		return doctorName;
	}

	public void setDoctorName(String doctorName) {
		this.doctorName = doctorName;
	}

	public List<TestDTO> getTests() {
		return tests;
	}

	public void setTests(List<TestDTO> tests) {
		this.tests = tests;
	}

	public BigDecimal getTotalTestAmount() {
		return totalTestAmount;
	}

	public void setTotalTestAmount(BigDecimal totalTestAmount) {
		this.totalTestAmount = totalTestAmount;
	}

	public LocalDateTime getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(LocalDateTime registrationDate) {
		this.registrationDate = registrationDate;
	}

	public Integer getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(Integer activeFlag) {
		this.activeFlag = activeFlag;
	}

	public Integer getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(Integer updatedBy) {
		this.updatedBy = updatedBy;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
