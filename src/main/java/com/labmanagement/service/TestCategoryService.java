
package com.labmanagement.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.labmanagement.dao.TestCategoryDAO;
import com.labmanagement.entity.TestCategory;

@Service
public class TestCategoryService {

	private final TestCategoryDAO testCategoryDAO;

	public TestCategoryService(TestCategoryDAO testCategoryDAO) {
		this.testCategoryDAO = testCategoryDAO;
	}

	@Transactional
	public TestCategory createCategory(TestCategory category) {

		validateCategory(category);

		String categoryName = category.getCategoryName().trim();

		if (testCategoryDAO.existsByCategoryName(categoryName)) {
			throw new IllegalArgumentException("Category name already exists");
		}

		category.setCategoryName(categoryName);
		category.setActiveFlag(1);

		return testCategoryDAO.save(category);
	}

	@Transactional(readOnly = true)
	public List<TestCategory> getAllCategories() {
		return testCategoryDAO.findAll();
	}

	@Transactional(readOnly = true)
	public TestCategory getCategoryById(Integer id) {

		if (id == null || id <= 0) {
			throw new IllegalArgumentException("Valid category ID is required");
		}

		return testCategoryDAO.findById(id).orElseThrow(() -> new IllegalArgumentException("Test category not found"));
	}

	@Transactional
	public TestCategory updateCategory(Integer id, TestCategory request) {

		validateCategory(request);

		TestCategory existing = getCategoryById(id);

		String categoryName = request.getCategoryName().trim();

		if (!existing.getCategoryName().equalsIgnoreCase(categoryName)
				&& testCategoryDAO.existsByCategoryName(categoryName)) {
			throw new IllegalArgumentException("Category name already exists");
		}

		existing.setCategoryName(categoryName);
		existing.setDescription(request.getDescription());

		return testCategoryDAO.save(existing);
	}

	@Transactional
	public TestCategory updateCategoryStatus(Integer id, Integer activeFlag) {

		if (activeFlag == null || (activeFlag != 0 && activeFlag != 1)) {
			throw new IllegalArgumentException("Active flag must be 1 or 0");
		}

		TestCategory existing = getCategoryById(id);
		existing.setActiveFlag(activeFlag);

		return testCategoryDAO.save(existing);
	}

	private void validateCategory(TestCategory category) {

		if (category == null) {
			throw new IllegalArgumentException("Category details are required");
		}

		if (category.getCategoryName() == null || category.getCategoryName().isBlank()) {
			throw new IllegalArgumentException("Category name is required");
		}

		if (category.getCategoryName().trim().length() > 100) {
			throw new IllegalArgumentException("Category name cannot exceed 100 characters");
		}

		if (category.getDescription() != null && category.getDescription().length() > 500) {
			throw new IllegalArgumentException("Description cannot exceed 500 characters");
		}

		if (category.getUpdatedBy() == null || category.getUpdatedBy() <= 0) {
			throw new IllegalArgumentException("Updated by user ID is required");
		}
	}
}
