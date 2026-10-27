
package com.labmanagement.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.labmanagement.dao.PatientVisitDAO;
import com.labmanagement.dto.CreateVisitDTO;
import com.labmanagement.dto.PatientVisitDTO;
import com.labmanagement.dto.TestDTO;
import com.labmanagement.entity.Patient;
import com.labmanagement.entity.PatientVisit;
import com.labmanagement.entity.Referral;
import com.labmanagement.entity.User;
import com.labmanagement.entity.UserRole;
import com.labmanagement.service.PatientService;
import com.labmanagement.service.PatientVisitService;
import com.labmanagement.service.ReferralService;
import com.labmanagement.service.RolePermissionService;

@RestController
@RequestMapping("/api/patient/visits")
public class PatientVisitController {

	private final PatientVisitService patientVisitService;
	private final PatientVisitDAO patientVisitDAO;
	private final PatientService patientService;
	private final RolePermissionService rolePermissionService;
	private final ReferralService referralService;

	public PatientVisitController(PatientVisitService patientVisitService, PatientVisitDAO patientVisitDAO,
			PatientService patientService, RolePermissionService rolePermissionService,
			ReferralService referralService) {

		this.patientVisitService = patientVisitService;
		this.patientVisitDAO = patientVisitDAO;
		this.patientService = patientService;
		this.rolePermissionService = rolePermissionService;
		this.referralService = referralService;
	}

	@GetMapping("/patient/{patientId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<PatientVisitDTO>> getPatientVisits(@PathVariable Integer patientId,
			Authentication authentication) {

		checkPermission(authentication, "View Patients");

		Patient patient = patientService.getPatientById(patientId);

		List<PatientVisitDTO> visits = patientVisitService.getVisitsByPatientId(patient.getId()).stream().map(visit -> {
			PatientVisitDTO dto = new PatientVisitDTO();

			dto.setId(visit.getId());
			dto.setPatientCode(visit.getPatient().getCode());

			if (visit.getDoctor() != null) {
				dto.setDoctorCode(visit.getDoctor().getDoctorCode());
				dto.setDoctorName(visit.getDoctor().getDoctorName());
			} else {
				dto.setDoctorCode("SELF");
				dto.setDoctorName("Self");
			}

			dto.setVisitDate(visit.getVisitDate());
			dto.setTests(patientVisitService.getTestsByVisitId(visit.getId(), visit.getPatient().getId()));
			dto.setStatus(visit.getStatus());
			dto.setActiveFlag(visit.getActiveFlag());

			return dto;
		}).toList();

		return ResponseEntity.ok(visits);
	}

	@GetMapping("/{visitId}/tests")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<TestDTO>> getVisitTests(@PathVariable Integer visitId, Authentication authentication) {

		checkPermission(authentication, "View Patients");

		PatientVisit visit = patientVisitDAO.findById(visitId)
				.orElseThrow(() -> new IllegalArgumentException("Visit not found: " + visitId));

		List<TestDTO> tests = patientVisitService.getTestsByVisitId(visit.getId(), visit.getPatient().getId());

		return ResponseEntity.ok(tests);
	}

	private void checkPermission(Authentication authentication, String eventName) {

		if (authentication == null || !authentication.isAuthenticated()
				|| !(authentication.getPrincipal() instanceof User)) {
			throw new AccessDeniedException("Authenticated user required");
		}

		User user = (User) authentication.getPrincipal();

		if (user.getRole() == UserRole.ADMIN) {
			return;
		}

		boolean allowed = rolePermissionService.hasEventAccess(authentication, "PATIENTS", eventName);

		if (!allowed) {
			throw new AccessDeniedException("You do not have permission to perform this action");
		}
	}

	@PostMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<PatientVisitDTO> createVisit(@RequestBody CreateVisitDTO request,
			Authentication authentication) {

		checkPermission(authentication, "Create Patient");

		User user = (User) authentication.getPrincipal();

		PatientVisit savedVisit = patientVisitService.createVisit(request.getPatientCode(), request.getDoctorCode(),
				request.getTestCodes(), user.getId());

		PatientVisitDTO dto = new PatientVisitDTO();

		dto.setId(savedVisit.getId());
		dto.setPatientCode(savedVisit.getPatient().getCode());
		dto.setVisitDate(savedVisit.getVisitDate());
		dto.setStatus(savedVisit.getStatus());
		dto.setActiveFlag(savedVisit.getActiveFlag());

		if (savedVisit.getDoctor() != null) {
			dto.setDoctorCode(savedVisit.getDoctor().getDoctorCode());
			dto.setDoctorName(savedVisit.getDoctor().getDoctorName());
		} else {
			dto.setDoctorCode("SELF");
			dto.setDoctorName("Self");
		}

		dto.setTests(patientVisitService.getTestsByVisitId(savedVisit.getId(), savedVisit.getPatient().getId()));

		return ResponseEntity.status(201).body(dto);
	}

	@GetMapping("/doctor/{doctorCode}/visits")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<PatientVisitDTO>> getVisitsByDoctorCode(@PathVariable String doctorCode,
			Authentication authentication) {

		checkPermission(authentication, "View Doctors");

		List<PatientVisitDTO> response = patientVisitService.getVisitsByDoctorCode(doctorCode).stream().map(visit -> {
			PatientVisitDTO dto = new PatientVisitDTO();

			dto.setId(visit.getId());
			dto.setPatientCode(visit.getPatient().getCode());
			dto.setVisitDate(visit.getVisitDate());
			dto.setStatus(visit.getStatus());
			dto.setActiveFlag(visit.getActiveFlag());

			if (visit.getDoctor() != null) {
				dto.setDoctorCode(visit.getDoctor().getDoctorCode());
				dto.setDoctorName(visit.getDoctor().getDoctorName());
			}

			dto.setTests(patientVisitService.getTestsByVisitId(visit.getId(), visit.getPatient().getId()));

			return dto;
		}).toList();

		return ResponseEntity.ok(response);
	}

	@GetMapping("/referral/history")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<Map<String, Object>>> getReferralHistory(Authentication authentication) {

		checkPermission(authentication, "View Doctors");

		List<Referral> referrals = referralService.getAllActiveReferrals();

		List<Map<String, Object>> response = referrals.stream().map(referral -> {
			var visit = referral.getVisit();
			var patient = visit.getPatient();
			var doctor = visit.getDoctor();

			Map<String, Object> patientDetails = new LinkedHashMap<>();
			patientDetails.put("code", patient.getCode());
			patientDetails.put("patientName", patient.getPatientName());
			patientDetails.put("age", patient.getAge());
			patientDetails.put("gender", patient.getGender());
			patientDetails.put("phone", patient.getPhone());
			patientDetails.put("address", patient.getAddress());

			Map<String, Object> doctorDetails = new LinkedHashMap<>();
			if (doctor != null) {
				doctorDetails.put("doctorCode", doctor.getDoctorCode());
				doctorDetails.put("doctorName", doctor.getDoctorName());
				doctorDetails.put("specialization", doctor.getSpecialization());
				doctorDetails.put("hospitalClinic", doctor.getHospitalClinic());
				doctorDetails.put("phone", doctor.getPhone());
			}

			Map<String, Object> item = new LinkedHashMap<>();
			item.put("referralCode", referral.getReferralCode());
			item.put("patient", patientDetails);
			item.put("doctor", doctorDetails.isEmpty() ? null : doctorDetails);
			item.put("referralDate", referral.getReferralDate());
			item.put("tests", patientVisitService.getTestsByVisitId(visit.getId(), patient.getId()));
			item.put("billAmount", 0);
			item.put("referralStatus", referral.getReferralStatus());
			item.put("reportStatus", referral.getReportStatus());

			return item;
		}).toList();

		return ResponseEntity.ok(response);
	}

	@PatchMapping("/referrals/{referralCode}/status")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Map<String, String>> updateReferralStatus(@PathVariable String referralCode,
			@RequestBody Map<String, String> request, Authentication authentication) {

		checkPermission(authentication, "View Doctors");

		User user = (User) authentication.getPrincipal();

		Referral updatedReferral = referralService.updateReferralStatus(referralCode, request.get("referralStatus"),
				user.getId());

		return ResponseEntity.ok(Map.of("referralCode", updatedReferral.getReferralCode(), "referralStatus",
				updatedReferral.getReferralStatus()));
	}

	@GetMapping("/doctor/{doctorCode}/referrals")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<PatientVisitDTO>> getReferralsByDoctorCode(@PathVariable String doctorCode,
			Authentication authentication) {

		checkPermission(authentication, "View Doctors");

		List<PatientVisitDTO> response = referralService.getActiveReferralsByDoctorCode(doctorCode).stream()
				.map(referral -> {
					var visit = referral.getVisit();
					var patient = visit.getPatient();
					var doctor = visit.getDoctor();

					PatientVisitDTO dto = new PatientVisitDTO();

					dto.setReferralCode(referral.getReferralCode());
					dto.setReferralDate(referral.getReferralDate());
					dto.setReferralStatus(referral.getReferralStatus());
					dto.setReportStatus(referral.getReportStatus());

					dto.setId(visit.getId());
					dto.setPatientCode(patient.getCode());
					dto.setPatientName(patient.getPatientName());
					dto.setVisitDate(visit.getVisitDate());

					if (doctor != null) {
						dto.setDoctorCode(doctor.getDoctorCode());
						dto.setDoctorName(doctor.getDoctorName());
					}

					dto.setTests(patientVisitService.getTestsByVisitId(visit.getId(), patient.getId()));

					dto.setStatus(visit.getStatus());
					dto.setActiveFlag(referral.getActiveFlag());

					return dto;
				}).toList();

		return ResponseEntity.ok(response);
	}

}
