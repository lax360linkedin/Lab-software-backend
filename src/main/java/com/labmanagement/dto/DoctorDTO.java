
package com.labmanagement.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.labmanagement.entity.Doctor.ReferralType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorDTO {

	private Integer id;
	private String doctorName;
	private String doctorCode;
	private byte[] doctorPhoto;
	private String specialization;
	private String hospitalClinic;
	private String phone;
	private String email;
	private String address;
	private ReferralType referralType;
	private Integer commissionApplicable;
	private BigDecimal referralCommission;
	private String notes;
	private Integer activeFlag;
	private LocalDateTime updatedAt;

	// Doctor card summary fields
	private Long totalReferredPatients;
	private BigDecimal totalReferredBilling;
	private String rating;

	public DoctorDTO() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDoctorName() {
		return doctorName;
	}

	public void setDoctorName(String doctorName) {
		this.doctorName = doctorName;
	}

	public String getDoctorCode() {
		return doctorCode;
	}

	public void setDoctorCode(String doctorCode) {
		this.doctorCode = doctorCode;
	}

	public byte[] getDoctorPhoto() {
		return doctorPhoto;
	}

	public void setDoctorPhoto(byte[] doctorPhoto) {
		this.doctorPhoto = doctorPhoto;
	}

	public String getSpecialization() {
		return specialization;
	}

	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}

	public String getHospitalClinic() {
		return hospitalClinic;
	}

	public void setHospitalClinic(String hospitalClinic) {
		this.hospitalClinic = hospitalClinic;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public ReferralType getReferralType() {
		return referralType;
	}

	public void setReferralType(ReferralType referralType) {
		this.referralType = referralType;
	}

	public Integer getCommissionApplicable() {
		return commissionApplicable;
	}

	public void setCommissionApplicable(Integer commissionApplicable) {
		this.commissionApplicable = commissionApplicable;
	}

	public BigDecimal getReferralCommission() {
		return referralCommission;
	}

	public void setReferralCommission(BigDecimal referralCommission) {
		this.referralCommission = referralCommission;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Integer getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(Integer activeFlag) {
		this.activeFlag = activeFlag;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Long getTotalReferredPatients() {
		return totalReferredPatients;
	}

	public void setTotalReferredPatients(Long totalReferredPatients) {
		this.totalReferredPatients = totalReferredPatients;
	}

	public BigDecimal getTotalReferredBilling() {
		return totalReferredBilling;
	}

	public void setTotalReferredBilling(BigDecimal totalReferredBilling) {
		this.totalReferredBilling = totalReferredBilling;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}
}
