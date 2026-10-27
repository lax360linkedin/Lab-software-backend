package com.labmanagement.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.labmanagement.dao.DoctorDAO;
import com.labmanagement.dao.PatientDAO;
import com.labmanagement.dao.PatientTestDAO;
import com.labmanagement.dao.PatientVisitDAO;
import com.labmanagement.dao.TestDAO;
import com.labmanagement.dto.PatientDTO;
import com.labmanagement.entity.Doctor;
import com.labmanagement.entity.Patient;
import com.labmanagement.entity.PatientTest;
import com.labmanagement.entity.PatientVisit;
import com.labmanagement.entity.Test;

@Service
public class PatientService {

	private final PatientDAO patientDAO;
	private final DoctorDAO doctorDAO;
	private final PatientTestDAO patientTestDAO;
	private final TestDAO testDAO;
	private final PatientVisitDAO patientVisitDAO;

	public PatientService(PatientDAO patientDAO, DoctorDAO doctorDAO, PatientTestDAO patientTestDAO, TestDAO testDAO,
			PatientVisitDAO patientVisitDAO) {
		this.patientDAO = patientDAO;
		this.doctorDAO = doctorDAO;
		this.patientTestDAO = patientTestDAO;
		this.testDAO = testDAO;
		this.patientVisitDAO = patientVisitDAO;
	}

	@Transactional
	public Patient createPatient(PatientDTO dto, Integer updatedBy) {

		validatePatient(dto);

		if (updatedBy == null) {
			throw new IllegalArgumentException("updatedBy is required");
		}

		List<String> testCodes = dto.getTestCodes();

		if (testCodes == null || testCodes.isEmpty()) {
			throw new IllegalArgumentException("At least one test is required");
		}

		Set<String> uniqueTestCodes = new HashSet<>();

		for (String testCode : testCodes) {

			if (testCode == null || testCode.isBlank()) {
				throw new IllegalArgumentException("Test code cannot be blank");
			}

			if (!uniqueTestCodes.add(testCode.trim())) {
				throw new IllegalArgumentException("Duplicate test selection: " + testCode);
			}
		}

		String patientCode = dto.getCode();

		if (patientCode != null && !patientCode.isBlank()) {

			patientCode = patientCode.trim();

			if (patientDAO.existsByCode(patientCode)) {
				throw new IllegalArgumentException("Patient code already exists");
			}

		} else {
			patientCode = generatePatientCode();
		}

		Doctor doctor = null;

		if (dto.getDoctorCode() == null || dto.getDoctorCode().isBlank()) {
			throw new IllegalArgumentException("Doctor code or SELF is required");
		}

		String doctorCode = dto.getDoctorCode().trim();

		if (!doctorCode.equalsIgnoreCase("SELF")) {

			doctor = doctorDAO.findByDoctorCode(doctorCode)
					.orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorCode));

			if (doctor.getActiveFlag() == null || doctor.getActiveFlag() != 1) {
				throw new IllegalArgumentException("Selected doctor is inactive");
			}
		}

		// Validate selected tests before saving the patient.
		for (String testCode : uniqueTestCodes) {

			Test test = testDAO.findByTestCode(testCode)
					.orElseThrow(() -> new IllegalArgumentException("Test not found: " + testCode));

			if (test.getActiveFlag() == null || test.getActiveFlag() != 1) {
				throw new IllegalArgumentException("Selected test is inactive: " + testCode);
			}
		}

		Patient patient = new Patient();

		patient.setCode(patientCode);
		patient.setPatientName(dto.getPatientName().trim());
		patient.setAge(dto.getAge());
		patient.setGender(dto.getGender());
		patient.setPhone(dto.getPhone().trim());
		patient.setAddress(dto.getAddress());
		patient.setEmail(normalizeEmail(dto.getEmail()));
		patient.setDoctor(doctor);
		patient.setActiveFlag(1);
		patient.setUpdatedBy(updatedBy);

		Patient savedPatient = patientDAO.save(patient);

		PatientVisit visit = new PatientVisit();

		visit.setPatient(savedPatient);
		visit.setDoctor(doctor);
		visit.setStatus("REGISTERED");
		visit.setActiveFlag(1);
		visit.setUpdatedBy(updatedBy);

		patientVisitDAO.save(visit);

		// Save selected tests with prices from the database.
		for (String testCode : uniqueTestCodes) {

			Test test = testDAO.findByTestCode(testCode)
					.orElseThrow(() -> new IllegalArgumentException("Test not found: " + testCode));

			PatientTest patientTest = new PatientTest();

			patientTest.setPatient(savedPatient);
			patientTest.setVisit(visit);
			patientTest.setTest(test);
			patientTest.setPrice(test.getPrice());
			patientTest.setActiveFlag(1);
			patientTest.setUpdatedBy(updatedBy);

			patientTestDAO.save(patientTest);
		}

		return savedPatient;
	}

	@Transactional(readOnly = true)
	public List<Patient> getAllActivePatients() {
		return patientDAO.findByActiveFlag(1);
	}

	@Transactional(readOnly = true)
	public Patient getPatientByCode(String patientCode) {

		if (patientCode == null || patientCode.isBlank()) {
			throw new IllegalArgumentException("Patient code is required");
		}

		return patientDAO.findByCode(patientCode.trim())
				.orElseThrow(() -> new IllegalArgumentException("Patient not found with code: " + patientCode));
	}

	@Transactional(readOnly = true)
	public Patient getPatientById(Integer id) {

		if (id == null) {
			throw new IllegalArgumentException("Patient ID is required");
		}

		return patientDAO.findById(id).orElseThrow(() -> new IllegalArgumentException("Patient not found: " + id));
	}

	@Transactional
	public Patient updatePatientByCode(String patientCode, PatientDTO dto, Integer updatedBy) {

		if (patientCode == null || patientCode.isBlank()) {
			throw new IllegalArgumentException("Patient code is required");
		}

		if (updatedBy == null) {
			throw new IllegalArgumentException("updatedBy is required");
		}

		Patient existing = patientDAO.findByCode(patientCode.trim())
				.orElseThrow(() -> new IllegalArgumentException("Patient not found with code: " + patientCode));

		validatePatient(dto);

		if (dto.getDoctorCode() == null || dto.getDoctorCode().isBlank()) {
			throw new IllegalArgumentException("Doctor code or SELF is required");
		}

		String doctorCode = dto.getDoctorCode().trim();
		Doctor doctor = null;

		if (!doctorCode.equalsIgnoreCase("SELF")) {

			doctor = doctorDAO.findByDoctorCode(doctorCode)
					.orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorCode));

			if (doctor.getActiveFlag() == null || doctor.getActiveFlag() != 1) {
				throw new IllegalArgumentException("Selected doctor is inactive");
			}
		}

		// Preserve the existing patient code.
		existing.setPatientName(dto.getPatientName().trim());
		existing.setAge(dto.getAge());
		existing.setGender(dto.getGender());
		existing.setPhone(dto.getPhone().trim());
		existing.setAddress(dto.getAddress());
		existing.setEmail(normalizeEmail(dto.getEmail()));
		existing.setDoctor(doctor);
		existing.setUpdatedBy(updatedBy);

		return patientDAO.save(existing);
	}

	@Transactional
	public Patient updatePatientStatusByCode(String patientCode, Integer activeFlag, Integer updatedBy) {

		if (patientCode == null || patientCode.isBlank()) {
			throw new IllegalArgumentException("Patient code is required");
		}

		validateStatus(activeFlag);

		if (updatedBy == null) {
			throw new IllegalArgumentException("updatedBy is required");
		}

		Patient patient = patientDAO.findByCode(patientCode.trim())
				.orElseThrow(() -> new IllegalArgumentException("Patient not found with code: " + patientCode));

		patient.setActiveFlag(activeFlag);
		patient.setUpdatedBy(updatedBy);

		return patientDAO.save(patient);
	}

	@Transactional(readOnly = true)
	public List<Patient> searchPatients(String patientName) {

		if (patientName == null || patientName.isBlank()) {
			return getAllActivePatients();
		}

		return patientDAO.findByPatientNameContainingIgnoreCase(patientName.trim());
	}

	@Transactional
	public Patient updatePatientStatus(Integer id, Integer activeFlag, Integer updatedBy) {

		if (id == null) {
			throw new IllegalArgumentException("Patient ID is required");
		}

		validateStatus(activeFlag);

		if (updatedBy == null) {
			throw new IllegalArgumentException("updatedBy is required");
		}

		Patient patient = patientDAO.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Patient not found: " + id));

		patient.setActiveFlag(activeFlag);
		patient.setUpdatedBy(updatedBy);

		return patientDAO.save(patient);
	}

	private void validatePatient(PatientDTO dto) {

		if (dto == null || dto.getPatientName() == null || dto.getPatientName().isBlank() || dto.getAge() == null
				|| dto.getAge() <= 0 || dto.getGender() == null || dto.getPhone() == null || dto.getPhone().isBlank()) {

			throw new IllegalArgumentException("Patient name, valid age, gender and phone are required");
		}
	}

	private void validateStatus(Integer activeFlag) {

		if (activeFlag == null || (activeFlag != 0 && activeFlag != 1)) {
			throw new IllegalArgumentException("Active flag must be 1 (Active) or 0 (Inactive)");
		}
	}

	private String normalizeEmail(String email) {

		if (email == null || email.isBlank()) {
			return null;
		}

		return email.trim();
	}

	private String generatePatientCode() {

		String patientCode;

		do {
			long nextNumber = System.currentTimeMillis() % 1_000_000;
			patientCode = "PT-" + String.format("%06d", nextNumber);
		} while (patientDAO.existsByCode(patientCode));

		return patientCode;
	}

	@Transactional(readOnly = true)
	public List<com.labmanagement.dto.TestDTO> getTestsByPatientId(Integer patientId) {

		return patientTestDAO.findByPatientIdAndActiveFlag(patientId, 1).stream().map(patientTest -> {
			Test test = patientTest.getTest();

			com.labmanagement.dto.TestDTO dto = new com.labmanagement.dto.TestDTO();

			dto.setId(test.getId());
			dto.setTestCode(test.getTestCode());
			dto.setTestName(test.getTestName());
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

			// Use the price saved during patient registration.
			dto.setPrice(patientTest.getPrice());
			dto.setActiveFlag(test.getActiveFlag());

			return dto;
		}).toList();
	}

}