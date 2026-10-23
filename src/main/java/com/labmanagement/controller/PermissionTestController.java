
package com.labmanagement.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/permission-test")
public class PermissionTestController {

	@GetMapping("/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> adminTest() {
		return ResponseEntity.ok("ADMIN access granted");
	}

	@GetMapping("/receptionist")
	@PreAuthorize("hasRole('RECEPTIONIST')")
	public ResponseEntity<String> receptionistTest() {
		return ResponseEntity.ok("RECEPTIONIST access granted");
	}

	@GetMapping("/technician")
	@PreAuthorize("hasRole('LAB_TECHNICIAN')")
	public ResponseEntity<String> technicianTest() {
		return ResponseEntity.ok("LAB_TECHNICIAN access granted");
	}

	// Database role permission test
	@GetMapping("/patients")
	@PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'PATIENTS', 'View Patients')")
	public ResponseEntity<String> patientPermissionTest() {
	    return ResponseEntity.ok("Patient view permission granted");
	}

	@GetMapping("/register-patient")
	@PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'PATIENTS', 'Register Patient')")
	public ResponseEntity<String> registerPatientPermissionTest() {
		return ResponseEntity.ok("Patient registration permission granted");
	}

}
