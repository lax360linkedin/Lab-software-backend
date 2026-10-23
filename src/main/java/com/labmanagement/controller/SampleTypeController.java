
package com.labmanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.labmanagement.dto.SampleTypeDTO;
import com.labmanagement.entity.SampleType;
import com.labmanagement.entity.User;
import com.labmanagement.service.SampleTypeService;

@RestController
@RequestMapping("/api/test/sample/types")
public class SampleTypeController {

	private final SampleTypeService sampleTypeService;

	public SampleTypeController(SampleTypeService sampleTypeService) {
		this.sampleTypeService = sampleTypeService;
	}

	// Create sample type
	@PostMapping
	@PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'TESTS', 'Manage Sample Types')")
	public ResponseEntity<SampleTypeDTO> createSampleType(@RequestBody SampleTypeDTO request,
			Authentication authentication) {

		User user = (User) authentication.getPrincipal();

		SampleType sampleType = new SampleType();
		sampleType.setSampleTypeName(request.getSampleTypeName());
		sampleType.setDescription(request.getDescription());
		sampleType.setUpdatedBy(user.getId());

		SampleType saved = sampleTypeService.createSampleType(sampleType);

		return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
	}

	// Get all sample types
	@GetMapping
	@PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'TESTS', 'View Sample Types')")
	public ResponseEntity<List<SampleTypeDTO>> getAllSampleTypes() {

		List<SampleTypeDTO> response = sampleTypeService.getAllSampleTypes().stream().map(sampleType -> {
			SampleTypeDTO dto = toDTO(sampleType);

			dto.setTestCount(sampleTypeService.getActiveTestCount(sampleType.getId()));

			return dto;
		}).toList();

		return ResponseEntity.ok(response);
	}

	// Get sample type by ID
	@GetMapping("/{id}")
	@PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'TESTS', 'View Sample Types')")
	public ResponseEntity<SampleTypeDTO> getSampleTypeById(@PathVariable Integer id) {

		SampleType sampleType = sampleTypeService.getSampleTypeById(id);

		return ResponseEntity.ok(toDTO(sampleType));
	}

	// Update sample type
	@PutMapping("/{id}")
	@PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'TESTS', 'Manage Sample Types')")
	public ResponseEntity<SampleTypeDTO> updateSampleType(@PathVariable Integer id, @RequestBody SampleTypeDTO request,
			Authentication authentication) {

		User user = (User) authentication.getPrincipal();

		SampleType sampleType = new SampleType();
		sampleType.setSampleTypeName(request.getSampleTypeName());
		sampleType.setDescription(request.getDescription());
		sampleType.setUpdatedBy(user.getId());

		SampleType updated = sampleTypeService.updateSampleType(id, sampleType);

		return ResponseEntity.ok(toDTO(updated));
	}

	// Update sample type status
	@PatchMapping("/{id}/status")
	@PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'TESTS', 'Manage Sample Types')")
	public ResponseEntity<SampleTypeDTO> updateStatus(@PathVariable Integer id, @RequestParam Integer activeFlag,
			Authentication authentication) {

		User user = (User) authentication.getPrincipal();

		SampleType existing = sampleTypeService.getSampleTypeById(id);

		existing.setUpdatedBy(user.getId());

		SampleType updated = sampleTypeService.updateSampleTypeStatus(id, activeFlag);

		return ResponseEntity.ok(toDTO(updated));
	}

	private SampleTypeDTO toDTO(SampleType sampleType) {

		SampleTypeDTO dto = new SampleTypeDTO();

		dto.setId(sampleType.getId());
		dto.setSampleTypeName(sampleType.getSampleTypeName());
		dto.setDescription(sampleType.getDescription());
		dto.setActiveFlag(sampleType.getActiveFlag());

		return dto;
	}
}
