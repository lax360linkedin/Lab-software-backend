
package com.labmanagement.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.labmanagement.dao.TestDAO;
import com.labmanagement.dao.TestParameterDAO;
import com.labmanagement.entity.Test;
import com.labmanagement.entity.TestParameter;

@Service
public class TestParameterService {

	private final TestParameterDAO testParameterDAO;
	private final TestDAO testDAO;

	public TestParameterService(TestParameterDAO testParameterDAO, TestDAO testDAO) {
		this.testParameterDAO = testParameterDAO;
		this.testDAO = testDAO;
	}

	@Transactional
	public TestParameter createParameter(Integer testId, TestParameter parameter) {

		validateParameter(parameter);

		Test test = testDAO.findById(testId).orElseThrow(() -> new IllegalArgumentException("Test not found"));

		if (test.getActiveFlag() != 1) {
			throw new IllegalArgumentException("Cannot add parameters to an inactive test");
		}

		parameter.setTest(test);
		parameter.setParameterName(parameter.getParameterName().trim());

		return testParameterDAO.save(parameter);
	}

	@Transactional(readOnly = true)
	public List<TestParameter> getAllParameters() {
		return testParameterDAO.findAll();
	}

	@Transactional(readOnly = true)
	public TestParameter getParameterById(Integer id) {

		if (id == null || id <= 0) {
			throw new IllegalArgumentException("Valid parameter ID is required");
		}

		return testParameterDAO.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Test parameter not found"));
	}

	private void validateParameter(TestParameter parameter) {

		if (parameter == null) {
			throw new IllegalArgumentException("Parameter details are required");
		}

		if (parameter.getTest() == null || parameter.getTest().getId() == null || parameter.getTest().getId() <= 0) {
			throw new IllegalArgumentException("Valid test ID is required");
		}

		if (parameter.getParameterName() == null || parameter.getParameterName().isBlank()) {
			throw new IllegalArgumentException("Parameter name is required");
		}

		if (parameter.getParameterName().trim().length() > 150) {
			throw new IllegalArgumentException("Parameter name cannot exceed 150 characters");
		}

		if (parameter.getUnit() != null && parameter.getUnit().length() > 50) {
			throw new IllegalArgumentException("Unit cannot exceed 50 characters");
		}

		if (parameter.getReferenceRange() != null && parameter.getReferenceRange().length() > 100) {
			throw new IllegalArgumentException("Reference range cannot exceed 100 characters");
		}

		if (parameter.getUpdatedBy() == null || parameter.getUpdatedBy() <= 0) {
			throw new IllegalArgumentException("Updated by user ID is required");
		}
	}

	@Transactional(readOnly = true)
	public List<TestParameter> getParametersByTestId(Integer testId) {

		if (testId == null || testId <= 0) {
			throw new IllegalArgumentException("Valid test ID is required");
		}

		if (!testDAO.existsById(testId)) {
			throw new IllegalArgumentException("Test not found");
		}

		return testParameterDAO.findByTest_Id(testId);
	}

}
