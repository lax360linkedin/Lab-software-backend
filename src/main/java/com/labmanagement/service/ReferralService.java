
package com.labmanagement.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.labmanagement.dao.PatientVisitDAO;
import com.labmanagement.dao.ReferralDAO;
import com.labmanagement.entity.PatientVisit;
import com.labmanagement.entity.Referral;

@Service
public class ReferralService {

	private final ReferralDAO referralDAO;
	private final PatientVisitDAO patientVisitDAO;

	public ReferralService(ReferralDAO referralDAO, PatientVisitDAO patientVisitDAO) {
		this.referralDAO = referralDAO;
		this.patientVisitDAO = patientVisitDAO;
	}

	@Transactional
	public Referral createReferral(Integer visitId, Integer updatedBy) {

		if (visitId == null) {
			throw new IllegalArgumentException("Visit ID is required");
		}

		if (updatedBy == null) {
			throw new IllegalArgumentException("Updated by is required");
		}

		PatientVisit visit = patientVisitDAO.findById(visitId)
				.orElseThrow(() -> new IllegalArgumentException("Visit not found: " + visitId));

		// SELF visits should not create referrals.
		if (visit.getDoctor() == null) {
			throw new IllegalArgumentException("Referral cannot be created for a SELF visit");
		}

		// Avoid duplicate referrals for the same visit.
		if (referralDAO.findByVisitId(visitId).isPresent()) {
			throw new IllegalArgumentException("Referral already exists for visit: " + visitId);
		}

		Referral referral = new Referral();
		referral.setVisit(visit);
		referral.setReferralCode(generateReferralCode());
		referral.setReferralStatus("REGISTERED");
		referral.setReportStatus("PENDING");
		referral.setActiveFlag(1);
		referral.setUpdatedBy(updatedBy);

		return referralDAO.save(referral);
	}

	@Transactional(readOnly = true)
	public List<Referral> getAllActiveReferrals() {
		return referralDAO.findByActiveFlagOrderByReferralDateDesc(1);
	}

	private String generateReferralCode() {
		String referralCode;

		do {
			long number = System.currentTimeMillis() % 1_000_000;
			referralCode = "REF-" + String.format("%06d", number);
		} while (referralDAO.existsByReferralCode(referralCode));

		return referralCode;
	}

	@Transactional
	public Referral updateReferralStatus(String referralCode, String referralStatus, Integer updatedBy) {

		if (referralStatus == null || referralStatus.isBlank()) {
			throw new org.springframework.web.server.ResponseStatusException(
					org.springframework.http.HttpStatus.BAD_REQUEST, "Referral status is required");
		}

		String status = referralStatus.trim().toUpperCase();

		if (!java.util.Set.of("REGISTERED", "SAMPLE_COLLECTED", "PROCESSING", "COMPLETED", "CANCELLED")
				.contains(status)) {
			throw new org.springframework.web.server.ResponseStatusException(
					org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid referral status");
		}

		Referral referral = referralDAO.findByReferralCode(referralCode)
				.orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
						org.springframework.http.HttpStatus.NOT_FOUND, "Referral not found: " + referralCode));

		referral.setReferralStatus(status);
		referral.setUpdatedBy(updatedBy);

		return referralDAO.save(referral);
	}

	@Transactional(readOnly = true)
	public List<Referral> getActiveReferralsByDoctorCode(String doctorCode) {
		if (doctorCode == null || doctorCode.isBlank()) {
			throw new IllegalArgumentException("Doctor code is required");
		}

		return referralDAO.findByActiveFlagAndVisit_Doctor_DoctorCodeOrderByReferralDateDesc(1, doctorCode.trim());
	}
}
