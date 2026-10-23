
package com.labmanagement.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import com.labmanagement.dao.TestDAO;
import com.labmanagement.dto.TestCategoryDTO;
import com.labmanagement.entity.TestCategory;
import com.labmanagement.entity.User;
import com.labmanagement.service.TestCategoryService;

@RestController
@RequestMapping("/api/test/categories")
public class TestCategoryController {

	private final TestCategoryService testCategoryService;
	private final TestDAO testDAO;

	public TestCategoryController(TestCategoryService testCategoryService, TestDAO testDAO) {
		this.testCategoryService = testCategoryService;
		this.testDAO = testDAO;
	}

	// Create category
	@PostMapping
	@PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'TESTS', 'Manage Test Categories')")
	public ResponseEntity<TestCategoryDTO> createCategory(@RequestBody TestCategoryDTO request,
			Authentication authentication) {

		User user = (User) authentication.getPrincipal();

		TestCategory category = new TestCategory();
		category.setCategoryName(request.getCategoryName());
		category.setDescription(request.getDescription());
		category.setUpdatedBy(user.getId());

		TestCategory saved = testCategoryService.createCategory(category);

		return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
	}

	// Get all categories
	@GetMapping
	@PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'TESTS', 'View Test Categories')")
	public ResponseEntity<List<TestCategoryDTO>> getAllCategories() {

		List<TestCategoryDTO> response = testCategoryService.getAllCategories().stream().map(category -> {
			TestCategoryDTO dto = toDTO(category);

			dto.setTestCount(testDAO.countByCategory_IdAndActiveFlag(category.getId(), 1));

			return dto;
		}).toList();

		return ResponseEntity.ok(response);
	}

	// Get category by ID
	@GetMapping("/{id}")
	@PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'TESTS', 'View Test Categories')")
	public ResponseEntity<TestCategoryDTO> getCategoryById(@PathVariable Integer id) {

		TestCategory category = testCategoryService.getCategoryById(id);

		return ResponseEntity.ok(toDTO(category));
	}

	// Update category
	@PutMapping("/{id}")
	@PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'TESTS', 'Manage Test Categories')")
	public ResponseEntity<TestCategoryDTO> updateCategory(@PathVariable Integer id,
			@RequestBody TestCategoryDTO request, Authentication authentication) {

		User user = (User) authentication.getPrincipal();

		TestCategory category = new TestCategory();
		category.setCategoryName(request.getCategoryName());
		category.setDescription(request.getDescription());
		category.setUpdatedBy(user.getId());

		TestCategory updated = testCategoryService.updateCategory(id, category);

		return ResponseEntity.ok(toDTO(updated));
	}

	// Update category status
	@PatchMapping("/{id}/status")
	@PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'TESTS', 'Manage Test Categories')")
	public ResponseEntity<TestCategoryDTO> updateCategoryStatus(@PathVariable Integer id,
			@RequestParam Integer activeFlag) {

		TestCategory updated = testCategoryService.updateCategoryStatus(id, activeFlag);

		return ResponseEntity.ok(toDTO(updated));
	}

	private TestCategoryDTO toDTO(TestCategory category) {
		return new TestCategoryDTO(category.getId(), category.getCategoryName(), category.getDescription(),
				category.getActiveFlag());
	}
}
