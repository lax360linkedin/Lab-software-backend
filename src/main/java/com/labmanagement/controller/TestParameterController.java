
package com.labmanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.labmanagement.dto.TestParameterDTO;
import com.labmanagement.entity.TestParameter;
import com.labmanagement.entity.User;
import com.labmanagement.service.TestParameterService;

@RestController
@RequestMapping("/api/test/parameters")
public class TestParameterController {

	private final TestParameterService testParameterService;

	public TestParameterController(TestParameterService testParameterService) {
		this.testParameterService = testParameterService;
	}

	// Create test parameter
	@PostMapping
	@PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'TESTS', 'Manage Test Parameters')")
	public ResponseEntity<TestParameterDTO> createParameter(@RequestBody TestParameterDTO request,
			Authentication authentication) {

		User user = (User) authentication.getPrincipal();

		TestParameter parameter = new TestParameter();
		parameter.setParameterName(request.getParameterName());
		parameter.setUnit(request.getUnit());
		parameter.setReferenceRange(request.getReferenceRange());
		parameter.setUpdatedBy(user.getId());

		TestParameter saved = testParameterService.createParameter(request.getTestId(), parameter);

		return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
	}

	// Get all test parameters
	@GetMapping
	@PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'TESTS', 'View Test Parameters')")
	public ResponseEntity<List<TestParameterDTO>> getAllParameters() {

		List<TestParameterDTO> response = testParameterService.getAllParameters().stream().map(this::toDTO).toList();

		return ResponseEntity.ok(response);
	}

	// Get test parameter by ID
	@GetMapping("/{id}")
	@PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'TESTS', 'View Test Parameters')")
	public ResponseEntity<TestParameterDTO> getParameterById(@PathVariable Integer id) {

		TestParameter parameter = testParameterService.getParameterById(id);

		return ResponseEntity.ok(toDTO(parameter));
	}

	// Get parameters by test ID
	@GetMapping("/by/test/{testId}")
	@PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'TESTS', 'View Test Parameters')")
	public ResponseEntity<List<TestParameterDTO>> getParametersByTestId(@PathVariable Integer testId) {

		List<TestParameterDTO> response = testParameterService.getParametersByTestId(testId).stream().map(this::toDTO)
				.toList();

		return ResponseEntity.ok(response);
	}

	private TestParameterDTO toDTO(TestParameter parameter) {

		TestParameterDTO dto = new TestParameterDTO();

		dto.setId(parameter.getId());
		dto.setTestId(parameter.getTest().getId());
		dto.setParameterName(parameter.getParameterName());
		dto.setUnit(parameter.getUnit());
		dto.setReferenceRange(parameter.getReferenceRange());
		dto.setActiveFlag(parameter.getActiveFlag());

		return dto;
	}
}
