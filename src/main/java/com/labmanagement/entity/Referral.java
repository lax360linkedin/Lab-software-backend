
package com.labmanagement.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "referrals")
public class Referral {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "referral_code", nullable = false, unique = true, length = 20)
	private String referralCode;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "visit_id", nullable = false, unique = true)
	private PatientVisit visit;

	@CreationTimestamp
	@Column(name = "referral_date", nullable = false, updatable = false)
	private LocalDateTime referralDate;

	@Column(name = "referral_status", nullable = false, length = 30)
	private String referralStatus = "REGISTERED";

	@Column(name = "report_status", nullable = false, length = 30)
	private String reportStatus = "PENDING";

	@Column(name = "active_flag", nullable = false)
	private Integer activeFlag = 1;

	@Column(name = "updated_by", nullable = false)
	private Integer updatedBy;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public Referral() {
	}

	public Integer getId() {
		return id;
	}

	public String getReferralCode() {
		return referralCode;
	}

	public void setReferralCode(String referralCode) {
		this.referralCode = referralCode;
	}

	public PatientVisit getVisit() {
		return visit;
	}

	public void setVisit(PatientVisit visit) {
		this.visit = visit;
	}

	public LocalDateTime getReferralDate() {
		return referralDate;
	}

	public String getReferralStatus() {
		return referralStatus;
	}

	public void setReferralStatus(String referralStatus) {
		this.referralStatus = referralStatus;
	}

	public String getReportStatus() {
		return reportStatus;
	}

	public void setReportStatus(String reportStatus) {
		this.reportStatus = reportStatus;
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
}
