package com.labmanagement.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "doctors")
public class Doctor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "doctor_name", nullable = false, length = 150)
	private String doctorName;

	@Column(name = "doctor_code", unique = true, length = 50)
	private String doctorCode;

	@Lob
	@Column(name = "doctor_photo", columnDefinition = "LONGBLOB")
	private byte[] doctorPhoto;

	@Column(nullable = false, length = 150)
	private String specialization;

	@Column(name = "hospital_clinic", length = 150)
	private String hospitalClinic;

	@Column(length = 20)
	private String phone;

	@Column(length = 150)
	private String email;

	@Column(length = 500)
	private String address;

	@Enumerated(EnumType.STRING)
	@Column(name = "referral_type", nullable = false, length = 30)
	private ReferralType referralType = ReferralType.EXTERNAL_REFERRAL;

	@Column(name = "commission_applicable", nullable = false)
	private Integer commissionApplicable = 0;

	@Column(name = "referral_commission", precision = 10, scale = 2)
	private BigDecimal referralCommission = BigDecimal.ZERO;

	@Lob
	private String notes;

	@Column(name = "active_flag", nullable = false)
	private Integer activeFlag = 1;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "updated_by", nullable = false)
	private Integer updatedBy;

	public enum ReferralType {
		EXTERNAL_REFERRAL, INTERNAL_REFERRAL, HOSPITAL_REFERRAL, CLINIC_REFERRAL
	}

	public Doctor() {
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

	public Integer getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(Integer updatedBy) {
		this.updatedBy = updatedBy;
	}
}