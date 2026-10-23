
package com.labmanagement.service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.labmanagement.dao.SampleTypeDAO;
import com.labmanagement.dao.TestCategoryDAO;
import com.labmanagement.dao.TestDAO;
import com.labmanagement.dao.TestParameterDAO;
import com.labmanagement.dto.TestParameterDTO;
import com.labmanagement.entity.SampleType;
import com.labmanagement.entity.Test;
import com.labmanagement.entity.TestCategory;
import com.labmanagement.entity.TestParameter;

@Service
public class TestService {

	private final TestDAO testDAO;
	private final TestCategoryDAO testCategoryDAO;
	private final SampleTypeDAO sampleTypeDAO;
	private final TestParameterDAO testParameterDAO;

	public TestService(TestDAO testDAO, TestCategoryDAO testCategoryDAO, SampleTypeDAO sampleTypeDAO,
			TestParameterDAO testParameterDAO) {

		this.testDAO = testDAO;
		this.testCategoryDAO = testCategoryDAO;
		this.sampleTypeDAO = sampleTypeDAO;
		this.testParameterDAO = testParameterDAO;
	}

	// Create test and its parameters
	@Transactional
	public Test createTest(Test test, List<TestParameterDTO> parameters) {

		validateTest(test);

		String testCode = test.getTestCode().trim();

		if (testDAO.existsByTestCodeAndActiveFlag(testCode, 1)) {
			throw new IllegalArgumentException("An active test already exists with this test code");
		}

		TestCategory category = testCategoryDAO.findById(test.getCategory().getId())
				.orElseThrow(() -> new IllegalArgumentException("Test category not found"));

		SampleType sampleType = sampleTypeDAO.findById(test.getSampleType().getId())
				.orElseThrow(() -> new IllegalArgumentException("Sample type not found"));

		if (category.getActiveFlag() != 1) {
			throw new IllegalArgumentException("Test category is inactive");
		}

		if (sampleType.getActiveFlag() != 1) {
			throw new IllegalArgumentException("Sample type is inactive");
		}

		test.setTestName(test.getTestName().trim());
		test.setTestCode(testCode);
		test.setCategory(category);
		test.setSampleType(sampleType);
		test.setActiveFlag(1);

		// Validate duplicate parameter names before saving the test
		if (parameters != null) {

			Set<String> parameterNames = new HashSet<>();

			for (TestParameterDTO parameterDTO : parameters) {

				if (parameterDTO == null) {
					throw new IllegalArgumentException("Parameter details cannot be null");
				}

				String parameterName = parameterDTO.getParameterName();

				if (parameterName == null || parameterName.isBlank()) {
					throw new IllegalArgumentException("Invalid parameter name");
				}

				parameterName = parameterName.trim().toLowerCase();

				if (!parameterNames.add(parameterName)) {
					throw new IllegalArgumentException(
							"Duplicate parameter name: " + parameterDTO.getParameterName().trim());
				}
			}
		}

		// Save the test first
		Test savedTest = testDAO.save(test);

		// Save parameters linked to the saved test
		if (parameters != null) {

			for (TestParameterDTO parameterDTO : parameters) {

				TestParameter parameter = new TestParameter();

				parameter.setTest(savedTest);
				parameter.setParameterName(parameterDTO.getParameterName());
				parameter.setUnit(parameterDTO.getUnit());
				parameter.setReferenceRange(parameterDTO.getReferenceRange());
				parameter.setActiveFlag(1);
				parameter.setUpdatedBy(test.getUpdatedBy());

				validateParameter(parameter);

				parameter.setParameterName(parameter.getParameterName().trim());

				// Save parameter and get generated values
				TestParameter savedParameter = testParameterDAO.save(parameter);

				// Update DTO for the API response
				parameterDTO.setId(savedParameter.getId());
				parameterDTO.setTestId(savedTest.getId());
				parameterDTO.setActiveFlag(savedParameter.getActiveFlag());
			}
		}

		return savedTest;
	}

	// Get test by test code
	@Transactional(readOnly = true)
	public Test getTestByCode(String testCode) {

		if (testCode == null || testCode.isBlank()) {
			throw new IllegalArgumentException("Test code is required");
		}

		return testDAO.findByTestCode(testCode.trim())
				.orElseThrow(() -> new IllegalArgumentException("Test not found"));
	}

	// Validate test details
	private void validateTest(Test test) {

		if (test == null) {
			throw new IllegalArgumentException("Test details are required");
		}

		if (test.getTestName() == null || test.getTestName().isBlank() || test.getTestName().trim().length() > 150) {
			throw new IllegalArgumentException("Invalid test name");
		}

		if (test.getTestCode() == null || test.getTestCode().isBlank() || test.getTestCode().trim().length() > 50) {
			throw new IllegalArgumentException("Invalid test code");
		}

		if (test.getCategory() == null || test.getCategory().getId() == null || test.getCategory().getId() <= 0) {
			throw new IllegalArgumentException("Valid category ID is required");
		}

		if (test.getSampleType() == null || test.getSampleType().getId() == null || test.getSampleType().getId() <= 0) {
			throw new IllegalArgumentException("Valid sample type ID is required");
		}

		if (test.getMethod() == null || test.getMethod().isBlank() || test.getMethod().trim().length() > 150) {
			throw new IllegalArgumentException("Invalid test method");
		}

		BigDecimal price = test.getPrice();

		if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Price must be greater than zero");
		}

		if (test.getTurnaroundTime() <= 0) {
			throw new IllegalArgumentException("Turnaround time must be greater than zero");
		}

		if (test.getUpdatedBy() == null || test.getUpdatedBy() <= 0) {
			throw new IllegalArgumentException("Valid updatedBy is required");
		}
	}

	// Validate parameter details
	// Validate test parameter details
	private void validateParameter(TestParameter parameter) {

		if (parameter == null) {
			throw new IllegalArgumentException("Parameter details cannot be null");
		}

		if (parameter.getParameterName() == null || parameter.getParameterName().isBlank()) {
			throw new IllegalArgumentException("Invalid parameter name");
		}

		if (parameter.getUnit() == null || parameter.getUnit().isBlank()) {
			throw new IllegalArgumentException("Parameter unit is required");
		}

		if (parameter.getReferenceRange() == null || parameter.getReferenceRange().isBlank()) {
			throw new IllegalArgumentException("Reference range is required");
		}
	}

	@Transactional
	public Test activateTestByCode(String testCode) {

		if (testCode == null || testCode.isBlank()) {
			throw new IllegalArgumentException("Test code is required");
		}

		Test test = testDAO.findByTestCode(testCode.trim())
				.orElseThrow(() -> new IllegalArgumentException("Test not found"));

		if (test.getActiveFlag() == 1) {
			throw new IllegalArgumentException("Test is already active");
		}

		test.setActiveFlag(1);

		return testDAO.save(test);
	}

	@Transactional
	public Test deactivateTestByCode(String testCode) {

		if (testCode == null || testCode.isBlank()) {
			throw new IllegalArgumentException("Test code is required");
		}

		Test test = testDAO.findByTestCode(testCode.trim())
				.orElseThrow(() -> new IllegalArgumentException("Test not found"));

		if (test.getActiveFlag() == 0) {
			throw new IllegalArgumentException("Test is already inactive");
		}

		test.setActiveFlag(0);

		return testDAO.save(test);
	}

	@Transactional(readOnly = true)
	public List<Test> getAllTests() {
		return testDAO.findByActiveFlag(1);
	}

	@Transactional
	public Test updateTestByCode(String testCode, com.labmanagement.dto.TestDTO request, Integer updatedBy) {

		if (testCode == null || testCode.isBlank()) {
			throw new IllegalArgumentException("Test code is required");
		}

		if (request == null) {
			throw new IllegalArgumentException("Test details are required");
		}

		if (updatedBy == null || updatedBy <= 0) {
			throw new IllegalArgumentException("Invalid updatedBy");
		}

		Test test = testDAO.findByTestCode(testCode.trim())
				.orElseThrow(() -> new IllegalArgumentException("Test not found"));

		if (test.getActiveFlag() != 1) {
			throw new IllegalArgumentException("Cannot update an inactive test");
		}

		// Validate test name
		if (request.getTestName() == null || request.getTestName().isBlank()
				|| request.getTestName().trim().length() > 150) {
			throw new IllegalArgumentException("Invalid test name");
		}

		// Validate category
		if (request.getCategoryId() == null || request.getCategoryId() <= 0) {
			throw new IllegalArgumentException("Valid category ID is required");
		}

		TestCategory category = testCategoryDAO.findById(request.getCategoryId())
				.orElseThrow(() -> new IllegalArgumentException("Test category not found"));

		if (category.getActiveFlag() != 1) {
			throw new IllegalArgumentException("Test category is inactive");
		}

		// Validate sample type
		if (request.getSampleTypeId() == null || request.getSampleTypeId() <= 0) {
			throw new IllegalArgumentException("Valid sample type ID is required");
		}

		SampleType sampleType = sampleTypeDAO.findById(request.getSampleTypeId())
				.orElseThrow(() -> new IllegalArgumentException("Sample type not found"));

		if (sampleType.getActiveFlag() != 1) {
			throw new IllegalArgumentException("Sample type is inactive");
		}

		if (request.getMethod() == null || request.getMethod().isBlank() || request.getMethod().trim().length() > 150) {
			throw new IllegalArgumentException("Invalid test method");
		}

		if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Price must be greater than zero");
		}

		if (request.getTurnaroundTime() == null || request.getTurnaroundTime() <= 0) {
			throw new IllegalArgumentException("Turnaround time must be greater than zero");
		}

		// Update test details. Test code remains unchanged.
		test.setTestName(request.getTestName().trim());
		test.setCategory(category);
		test.setSampleType(sampleType);
		test.setMethod(request.getMethod().trim());
		test.setPrice(request.getPrice());
		test.setTurnaroundTime(request.getTurnaroundTime());
		test.setUpdatedBy(updatedBy);

		Test savedTest = testDAO.save(test);

		// Load existing parameters for this test
		List<TestParameter> existingParameters = testParameterDAO.findByTest_Id(savedTest.getId());

		// If parameters are supplied, update existing ones,
		// add new ones, and deactivate omitted ones.
		if (request.getParameters() != null) {

			java.util.Set<Integer> retainedIds = new java.util.HashSet<>();

			for (TestParameterDTO parameterDTO : request.getParameters()) {

				if (parameterDTO == null) {
					throw new IllegalArgumentException("Parameter details cannot be null");
				}

				TestParameter parameter;

				if (parameterDTO.getId() != null) {

					parameter = existingParameters.stream().filter(p -> p.getId().equals(parameterDTO.getId()))
							.findFirst()
							.orElseThrow(() -> new IllegalArgumentException("Parameter does not belong to this test"));

					if (parameter.getActiveFlag() != 1) {
						throw new IllegalArgumentException("Cannot update an inactive parameter");
					}

					retainedIds.add(parameter.getId());

				} else {
					parameter = new TestParameter();
					parameter.setTest(savedTest);
					parameter.setActiveFlag(1);
				}

				parameter.setParameterName(parameterDTO.getParameterName());
				parameter.setUnit(parameterDTO.getUnit());
				parameter.setReferenceRange(parameterDTO.getReferenceRange());
				parameter.setUpdatedBy(updatedBy);

				validateParameter(parameter);

				parameter.setParameterName(parameter.getParameterName().trim());

				TestParameter savedParameter = testParameterDAO.save(parameter);

				parameterDTO.setId(savedParameter.getId());
				parameterDTO.setTestId(savedTest.getId());
				parameterDTO.setActiveFlag(savedParameter.getActiveFlag());
			}

			// Deactivate existing parameters omitted from the request
			for (TestParameter existing : existingParameters) {

				if (existing.getActiveFlag() == 1 && !retainedIds.contains(existing.getId())
						&& request.getParameters().stream()
								.noneMatch(p -> p != null && p.getId() != null && p.getId().equals(existing.getId()))) {

					existing.setActiveFlag(0);
					existing.setUpdatedBy(updatedBy);
					testParameterDAO.save(existing);
				}
			}
		}

		return savedTest;
	}

}
