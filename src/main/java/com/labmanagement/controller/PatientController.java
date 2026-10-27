package com.labmanagement.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.labmanagement.dto.PatientDTO;
import com.labmanagement.dto.PatientVisitDTO;
import com.labmanagement.dto.ResponseDTO;
import com.labmanagement.dto.TestDTO;
import com.labmanagement.entity.Patient;
import com.labmanagement.entity.User;
import com.labmanagement.entity.UserRole;
import com.labmanagement.service.PatientService;
import com.labmanagement.service.PatientVisitService;
import com.labmanagement.service.RolePermissionService;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

	private final PatientService patientService;
	private final RolePermissionService rolePermissionService;
	private final PatientVisitService patientVisitService;

	public PatientController(PatientService patientService, RolePermissionService rolePermissionService,
			PatientVisitService patientVisitService) {

		this.patientService = patientService;
		this.rolePermissionService = rolePermissionService;
		this.patientVisitService = patientVisitService;
	}

	// Create patient
	@PostMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<PatientDTO> createPatient(@RequestBody PatientDTO request, Authentication authentication) {

		checkPermission(authentication, "Create Patient");

		User user = getAuthenticatedUser(authentication);

		Patient saved = patientService.createPatient(request, user.getId());

		PatientDTO response = new PatientDTO();
		response.setCode(saved.getCode());
		response.setActiveFlag(saved.getActiveFlag());
		response.setPatientName(saved.getPatientName());
		response.setAge(saved.getAge());
		response.setEmail(saved.getEmail());

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// Get all active patients
	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<PatientDTO>> getAllPatients(Authentication authentication) {

		checkPermission(authentication, "View Patients");

		List<PatientDTO> response = patientService.getAllActivePatients().stream().map(this::toDTO).toList();

		return ResponseEntity.ok(response);
	}

	// Get patient by code
	@GetMapping("/code/{patientCode}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<PatientDTO> getPatientByCode(@PathVariable String patientCode,
			Authentication authentication) {

		checkPermission(authentication, "View Patients");

		Patient patient = patientService.getPatientByCode(patientCode);

		return ResponseEntity.ok(toDTO(patient));
	}

	// Get patient by database ID
	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<PatientDTO> getPatientById(@PathVariable Integer id, Authentication authentication) {

		checkPermission(authentication, "View Patients");

		Patient patient = patientService.getPatientById(id);

		return ResponseEntity.ok(toDTO(patient));
	}

	// Search patients by name
	@GetMapping("/search")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<PatientDTO>> searchPatients(@RequestParam(required = false) String patientName,
			Authentication authentication) {

		checkPermission(authentication, "View Patients");

		List<PatientDTO> response = patientService.searchPatients(patientName).stream().map(this::toDTO).toList();

		return ResponseEntity.ok(response);
	}

	// Update patient details
	@PutMapping("/code/{patientCode}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<PatientDTO> updatePatient(@PathVariable String patientCode, @RequestBody PatientDTO request,
			Authentication authentication) {

		checkPermission(authentication, "Update Patient");

		User user = getAuthenticatedUser(authentication);

		Patient updated = patientService.updatePatientByCode(patientCode, request, user.getId());

		return ResponseEntity.ok(toDTO(updated));
	}

	// Activate or deactivate patient
	@PatchMapping("/code/{patientCode}/status")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ResponseDTO<Void>> updatePatientStatus(@PathVariable String patientCode,
			@RequestParam Integer activeFlag, Authentication authentication) {

		checkPermission(authentication, "Manage Patients");

		User user = getAuthenticatedUser(authentication);

		if (activeFlag == null || (activeFlag != 0 && activeFlag != 1)) {
			throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,
					"activeFlag must be 1 (Active) or 0 (Inactive)");
		}

		patientService.updatePatientStatusByCode(patientCode, activeFlag, user.getId());

		return ResponseEntity.ok(ResponseDTO.success());
	}

	// Get authenticated user
	private User getAuthenticatedUser(Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()
				|| !(authentication.getPrincipal() instanceof User)) {

			throw new AccessDeniedException("Authenticated user required");
		}

		return (User) authentication.getPrincipal();
	}

	// Check role permission
	private void checkPermission(Authentication authentication, String eventName) {

		User user = getAuthenticatedUser(authentication);

		if (user.getRole() == UserRole.ADMIN) {
			return;
		}

		boolean allowed = rolePermissionService.hasEventAccess(authentication, "PATIENTS", eventName);

		if (!allowed) {
			throw new AccessDeniedException("You do not have permission to perform this action");
		}
	}

	// Convert entity to DTO

	private PatientDTO toDTO(Patient patient) {

		PatientDTO dto = new PatientDTO();

		dto.setId(patient.getId());
		dto.setCode(patient.getCode());
		dto.setPatientName(patient.getPatientName());
		dto.setAge(patient.getAge());
		dto.setGender(patient.getGender());
		dto.setPhone(patient.getPhone());
		dto.setAddress(patient.getAddress());
		dto.setEmail(patient.getEmail());

		if (patient.getDoctor() != null) {
			dto.setDoctorCode(patient.getDoctor().getDoctorCode());
			dto.setDoctorName(patient.getDoctor().getDoctorName());
		} else {
			dto.setDoctorCode("SELF");
			dto.setDoctorName("Self");
		}

		dto.setRegistrationDate(patient.getRegistrationDate());
		dto.setActiveFlag(patient.getActiveFlag());
		dto.setUpdatedBy(patient.getUpdatedBy());
		dto.setUpdatedAt(patient.getUpdatedAt());

		List<TestDTO> tests = patientService.getTestsByPatientId(patient.getId());

		dto.setTests(tests);

		BigDecimal totalAmount = tests.stream().map(TestDTO::getPrice).filter(Objects::nonNull).reduce(BigDecimal.ZERO,
				BigDecimal::add);

		List<PatientVisitDTO> visits = patientVisitService.getVisitsByPatientId(patient.getId()).stream().map(visit -> {
			PatientVisitDTO visitDTO = new PatientVisitDTO();

			visitDTO.setId(visit.getId());
			visitDTO.setPatientCode(patient.getCode());

			if (visit.getDoctor() != null) {
				visitDTO.setDoctorCode(visit.getDoctor().getDoctorCode());
				visitDTO.setDoctorName(visit.getDoctor().getDoctorName());
			} else {
				visitDTO.setDoctorCode("SELF");
				visitDTO.setDoctorName("Self");
			}

			visitDTO.setVisitDate(visit.getVisitDate());
			visitDTO.setStatus(visit.getStatus());
			visitDTO.setActiveFlag(visit.getActiveFlag());

			visitDTO.setTests(patientVisitService.getTestsByVisitId(visit.getId(), patient.getId()));

			return visitDTO;
		}).toList();

		dto.setVisits(visits);

		dto.setTotalTestAmount(totalAmount);

		return dto;
	}

}