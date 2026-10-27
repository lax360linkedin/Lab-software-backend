
package com.labmanagement.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.labmanagement.dao.DoctorDAO;
import com.labmanagement.dao.PatientDAO;
import com.labmanagement.dao.PatientTestDAO;
import com.labmanagement.dao.PatientVisitDAO;
import com.labmanagement.dao.TestDAO;
import com.labmanagement.dto.TestDTO;
import com.labmanagement.entity.Doctor;
import com.labmanagement.entity.Patient;
import com.labmanagement.entity.PatientTest;
import com.labmanagement.entity.PatientVisit;
import com.labmanagement.entity.Test;

@Service
public class PatientVisitService {

	private final PatientVisitDAO patientVisitDAO;
	private final PatientTestDAO patientTestDAO;
	private final DoctorDAO doctorDAO;
	private final PatientDAO patientDAO;
	private final TestDAO testDAO;
	private final ReferralService referralService;

	public PatientVisitService(PatientVisitDAO patientVisitDAO, PatientTestDAO patientTestDAO, DoctorDAO doctorDAO,
			PatientDAO patientDAO, TestDAO testDAO, ReferralService referralService) {

		this.patientVisitDAO = patientVisitDAO;
		this.patientTestDAO = patientTestDAO;
		this.doctorDAO = doctorDAO;
		this.patientDAO = patientDAO;
		this.testDAO = testDAO;
		this.referralService = referralService;
	}

	@Transactional(readOnly = true)
	public List<PatientVisit> getVisitsByPatientId(Integer patientId) {

		return patientVisitDAO.findByPatientIdOrderByVisitDateDesc(patientId);
	}

	@Transactional(readOnly = true)
	public List<TestDTO> getTestsByVisitId(Integer visitId, Integer patientId) {

		List<PatientTest> patientTests = patientTestDAO.findByVisitIdAndPatientIdAndActiveFlag(visitId, patientId, 1);

		return patientTests.stream().map(patientTest -> {

			Test test = patientTest.getTest();

			TestDTO dto = new TestDTO();

			dto.setId(test.getId());
			dto.setTestCode(test.getTestCode());
			dto.setTestName(test.getTestName());
			dto.setPrice(patientTest.getPrice());
			dto.setActiveFlag(test.getActiveFlag());
			dto.setMethod(test.getMethod());
			dto.setTurnaroundTime(test.getTurnaroundTime());

			if (test.getCategory() != null) {
				dto.setCategoryId(test.getCategory().getId());
				dto.setCategoryName(test.getCategory().getCategoryName());
			}

			if (test.getSampleType() != null) {
				dto.setSampleTypeId(test.getSampleType().getId());
				dto.setSampleTypeName(test.getSampleType().getSampleTypeName());
			}

			return dto;
		}).toList();
	}

	@Transactional
	public PatientVisit createVisit(String patientCode, String doctorCode, List<String> testCodes, Integer updatedBy) {

		// 1. Validate patient code
		if (patientCode == null || patientCode.isBlank()) {
			throw new IllegalArgumentException("Patient code is required");
		}

		patientCode = patientCode.trim();

		// 2. Validate updatedBy
		if (updatedBy == null) {
			throw new IllegalArgumentException("Updated by is required");
		}

		// 3. Validate test list
		if (testCodes == null || testCodes.isEmpty()) {
			throw new IllegalArgumentException("At least one test is required");
		}

		// 4. Fetch existing patient
		final String codeToFind = patientCode;

		Patient patient = patientDAO.findByCode(codeToFind)
				.orElseThrow(() -> new IllegalArgumentException("Patient not found: " + codeToFind));

		// 5. Validate doctor code
		if (doctorCode == null || doctorCode.isBlank()) {
			throw new IllegalArgumentException("Doctor code is required");
		}

		String doctorCodeToFind = doctorCode.trim();
		Doctor doctor = null;

		// SELF visits do not have an assigned doctor
		if (!doctorCodeToFind.equalsIgnoreCase("SELF")) {

			doctor = doctorDAO.findByDoctorCode(doctorCodeToFind)
					.orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorCodeToFind));

			if (doctor.getActiveFlag() == null || doctor.getActiveFlag() != 1) {
				throw new IllegalArgumentException("Doctor is inactive");
			}
		}

		// 6. Validate test codes and remove duplicates
		Set<String> uniqueCodes = new LinkedHashSet<>();

		for (String testCode : testCodes) {

			if (testCode == null || testCode.isBlank()) {
				throw new IllegalArgumentException("Test code cannot be null or blank");
			}

			String trimmedTestCode = testCode.trim();

			if (!uniqueCodes.add(trimmedTestCode)) {
				throw new IllegalArgumentException("Invalid or duplicate test code: " + trimmedTestCode);
			}
		}

		// 7. Fetch and validate all selected tests
		List<Test> selectedTests = uniqueCodes.stream().map(testCode -> testDAO.findByTestCode(testCode)
				.orElseThrow(() -> new IllegalArgumentException("Test not found: " + testCode))).toList();

		for (Test test : selectedTests) {

			if (test.getActiveFlag() == null || test.getActiveFlag() != 1) {
				throw new IllegalArgumentException("Test is inactive: " + test.getTestCode());
			}
		}

		// 8. Create and save the patient visit
		PatientVisit visit = new PatientVisit();

		visit.setPatient(patient);
		visit.setDoctor(doctor);
		visit.setStatus("REGISTERED");
		visit.setActiveFlag(1);
		visit.setUpdatedBy(updatedBy);

		PatientVisit savedVisit = patientVisitDAO.save(visit);

		// 9. Save selected tests against the visit
		for (Test test : selectedTests) {

			PatientTest patientTest = new PatientTest();

			patientTest.setPatient(patient);
			patientTest.setVisit(savedVisit);
			patientTest.setTest(test);
			patientTest.setPrice(test.getPrice());
			patientTest.setActiveFlag(1);
			patientTest.setUpdatedBy(updatedBy);

			patientTestDAO.save(patientTest);
		}

		// 10. Create referral only when a doctor is assigned
		if (savedVisit.getDoctor() != null) {
			referralService.createReferral(savedVisit.getId(), updatedBy);
		}

		// 11. Return the saved visit
		return savedVisit;
	}

	@Transactional(readOnly = true)
	public List<PatientVisit> getVisitsByDoctorCode(String doctorCode) {

		if (doctorCode == null || doctorCode.isBlank()) {
			throw new IllegalArgumentException("Doctor code is required");
		}

		String code = doctorCode.trim();

		// Verify doctor exists
		doctorDAO.findByDoctorCode(code).orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + code));

		return patientVisitDAO.findByDoctor_DoctorCodeAndActiveFlagOrderByVisitDateDesc(code, 1);
	}

}
