package com.labmanagement.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.labmanagement.dao.DoctorDAO;
import com.labmanagement.dao.ReferralDAO;
import com.labmanagement.dto.DoctorDTO;
import com.labmanagement.entity.Doctor;

@Service
public class DoctorService {

	private final DoctorDAO doctorDAO;
	private final ReferralDAO referralDAO;

	public DoctorService(DoctorDAO doctorDAO, ReferralDAO referralDAO) {
		this.doctorDAO = doctorDAO;
		this.referralDAO = referralDAO;
	}

	@Transactional
	public Doctor createDoctor(DoctorDTO dto, Integer updatedBy) {

		validateDoctor(dto);

		if (updatedBy == null) {
			throw new IllegalArgumentException("updatedBy is required");
		}

		String doctorCode = dto.getDoctorCode();

		if (doctorCode != null && !doctorCode.isBlank()) {
			doctorCode = doctorCode.trim();

			if (doctorDAO.existsByDoctorCode(doctorCode)) {
				throw new IllegalArgumentException("Doctor code already exists");
			}
		} else {
			doctorCode = generateDoctorCode();
		}

		Doctor doctor = new Doctor();

		doctor.setDoctorName(dto.getDoctorName().trim());
		doctor.setDoctorCode(doctorCode);
		doctor.setDoctorPhoto(dto.getDoctorPhoto());
		doctor.setSpecialization(dto.getSpecialization().trim());
		doctor.setHospitalClinic(dto.getHospitalClinic());
		doctor.setPhone(dto.getPhone());
		doctor.setEmail(dto.getEmail());
		doctor.setAddress(dto.getAddress());
		doctor.setReferralType(
				dto.getReferralType() != null ? dto.getReferralType() : Doctor.ReferralType.EXTERNAL_REFERRAL);
		doctor.setCommissionApplicable(dto.getCommissionApplicable() != null ? dto.getCommissionApplicable() : 0);
		doctor.setReferralCommission(
				dto.getReferralCommission() != null ? dto.getReferralCommission() : java.math.BigDecimal.ZERO);
		doctor.setNotes(dto.getNotes());
		doctor.setActiveFlag(1);
		doctor.setUpdatedBy(updatedBy);

		return doctorDAO.save(doctor);
	}

	@Transactional(readOnly = true)
	public Doctor getDoctorByCode(String doctorCode) {

		if (doctorCode == null || doctorCode.isBlank()) {
			throw new IllegalArgumentException("Doctor code is required");
		}

		return doctorDAO.findByDoctorCode(doctorCode.trim())
				.orElseThrow(() -> new RuntimeException("Doctor not found with code: " + doctorCode));
	}

	@Transactional
	public Doctor updateDoctorByCode(String doctorCode, DoctorDTO dto, Integer updatedBy) {

		if (doctorCode == null || doctorCode.isBlank()) {
			throw new IllegalArgumentException("Doctor code is required");
		}

		Doctor existing = doctorDAO.findByDoctorCode(doctorCode.trim())
				.orElseThrow(() -> new RuntimeException("Doctor not found with code: " + doctorCode));

		validateDoctor(dto);

		// Preserve the existing unique doctor code.
		dto.setDoctorCode(existing.getDoctorCode());

		existing.setDoctorName(dto.getDoctorName());
		existing.setDoctorPhoto(dto.getDoctorPhoto());
		existing.setSpecialization(dto.getSpecialization());
		existing.setHospitalClinic(dto.getHospitalClinic());
		existing.setPhone(dto.getPhone());
		existing.setEmail(dto.getEmail());
		existing.setAddress(dto.getAddress());
		existing.setReferralType(dto.getReferralType());

		if (dto.getCommissionApplicable() != null) {
			existing.setCommissionApplicable(dto.getCommissionApplicable());
		}

		if (dto.getReferralCommission() != null) {
			existing.setReferralCommission(dto.getReferralCommission());
		}

		existing.setNotes(dto.getNotes());
		existing.setUpdatedBy(updatedBy);

		return doctorDAO.save(existing);
	}

	@Transactional
	public Doctor updateDoctorStatusByCode(String doctorCode, Integer activeFlag, Integer updatedBy) {

		if (doctorCode == null || doctorCode.isBlank()) {
			throw new IllegalArgumentException("Doctor code is required");
		}

		if (activeFlag == null || (activeFlag != 0 && activeFlag != 1)) {
			throw new IllegalArgumentException("Active flag must be 1 (Active) or 0 (Inactive)");
		}

		if (updatedBy == null) {
			throw new IllegalArgumentException("Updated by is required");
		}

		Doctor doctor = doctorDAO.findByDoctorCode(doctorCode.trim())
				.orElseThrow(() -> new RuntimeException("Doctor not found with code: " + doctorCode));

		doctor.setActiveFlag(activeFlag);
		doctor.setUpdatedBy(updatedBy);

		return doctorDAO.save(doctor);
	}

	@Transactional(readOnly = true)
	public List<Doctor> getAllActiveDoctors() {
		return doctorDAO.findByActiveFlag(1);
	}

	@Transactional(readOnly = true)
	public Doctor getDoctorById(Integer id) {
		return doctorDAO.findById(id).orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + id));
	}

	@Transactional(readOnly = true)
	public List<Doctor> searchDoctors(String doctorName) {

		if (doctorName == null || doctorName.isBlank()) {
			return getAllActiveDoctors();
		}

		return doctorDAO.findByDoctorNameContainingIgnoreCase(doctorName.trim());
	}

	@Transactional
	public Doctor updateDoctorStatus(Integer id, Integer activeFlag, Integer updatedBy) {

		if (activeFlag == null || (activeFlag != 0 && activeFlag != 1)) {
			throw new IllegalArgumentException("activeFlag must be 1 or 0");
		}

		if (updatedBy == null) {
			throw new IllegalArgumentException("updatedBy is required");
		}

		Doctor doctor = doctorDAO.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + id));

		doctor.setActiveFlag(activeFlag);
		doctor.setUpdatedBy(updatedBy);

		return doctorDAO.save(doctor);
	}

	private void validateDoctor(DoctorDTO dto) {

		if (dto == null || dto.getDoctorName() == null || dto.getDoctorName().isBlank()
				|| dto.getSpecialization() == null || dto.getSpecialization().isBlank()) {

			throw new IllegalArgumentException("Doctor name and specialization are required");
		}

		if (dto.getCommissionApplicable() != null && dto.getCommissionApplicable() != 0
				&& dto.getCommissionApplicable() != 1) {
			throw new IllegalArgumentException("commissionApplicable must be 1 or 0");
		}

		if (dto.getReferralCommission() != null && dto.getReferralCommission().signum() < 0) {
			throw new IllegalArgumentException("Referral commission cannot be negative");
		}

		if (dto.getCommissionApplicable() != null && dto.getCommissionApplicable() == 0
				&& dto.getReferralCommission() != null && dto.getReferralCommission().signum() > 0) {
			throw new IllegalArgumentException("Commission amount cannot be set when commission is not applicable");
		}
	}

	private String generateDoctorCode() {

		String doctorCode;

		do {
			long nextNumber = System.currentTimeMillis() % 1_000_000;
			doctorCode = "DR-" + String.format("%06d", nextNumber);
		} while (doctorDAO.existsByDoctorCode(doctorCode));

		return doctorCode;
	}

	@Transactional
	public void updateDoctorPhoto(String doctorCode, byte[] photo, Integer updatedBy) {

		if (doctorCode == null || doctorCode.isBlank()) {
			throw new IllegalArgumentException("Doctor code is required");
		}

		if (photo == null || photo.length == 0) {
			throw new IllegalArgumentException("Doctor photo is required");
		}

		if (updatedBy == null) {
			throw new IllegalArgumentException("Updated by is required");
		}

		Doctor doctor = doctorDAO.findByDoctorCode(doctorCode.trim())
				.orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

		doctor.setDoctorPhoto(photo);
		doctor.setUpdatedBy(updatedBy);

		doctorDAO.save(doctor);
	}

	@Transactional(readOnly = true)
	public DoctorDTO getDoctorCardByCode(String doctorCode) {

		Doctor doctor = getDoctorByCode(doctorCode);

		DoctorDTO dto = new DoctorDTO();
		dto.setId(doctor.getId());
		dto.setDoctorName(doctor.getDoctorName());
		dto.setDoctorCode(doctor.getDoctorCode());
		dto.setSpecialization(doctor.getSpecialization());
		dto.setHospitalClinic(doctor.getHospitalClinic());
		dto.setPhone(doctor.getPhone());
		dto.setEmail(doctor.getEmail());
		dto.setAddress(doctor.getAddress());
		dto.setReferralType(doctor.getReferralType());
		dto.setActiveFlag(doctor.getActiveFlag());
		dto.setUpdatedAt(doctor.getUpdatedAt());

		long totalVisits = referralDAO.countByVisit_Doctor_DoctorCodeAndActiveFlag(doctor.getDoctorCode(), 1);

		dto.setTotalReferredPatients(totalVisits);
		dto.setTotalReferredBilling(java.math.BigDecimal.ZERO);
		dto.setRating("Not rated");

		return dto;
	}

	@Transactional(readOnly = true)
	public List<DoctorDTO> getDoctorReferralSummary() {
		return getAllActiveDoctors().stream().map(doctor -> getDoctorCardByCode(doctor.getDoctorCode())).toList();
	}

}