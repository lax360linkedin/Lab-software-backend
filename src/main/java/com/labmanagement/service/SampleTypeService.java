
package com.labmanagement.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.labmanagement.dao.SampleTypeDAO;
import com.labmanagement.dao.TestDAO;
import com.labmanagement.entity.SampleType;

@Service
public class SampleTypeService {

	private final SampleTypeDAO sampleTypeDAO;
	private final TestDAO testDAO;

	public SampleTypeService(SampleTypeDAO sampleTypeDAO, TestDAO testDAO) {
		this.sampleTypeDAO = sampleTypeDAO;
		this.testDAO = testDAO;
	}

	@Transactional
	public SampleType createSampleType(SampleType sampleType) {

		validateSampleType(sampleType);

		String name = sampleType.getSampleTypeName().trim();

		if (sampleTypeDAO.existsBySampleTypeName(name)) {
			throw new IllegalArgumentException("Sample type name already exists");
		}

		sampleType.setSampleTypeName(name);
		sampleType.setActiveFlag(1);

		return sampleTypeDAO.save(sampleType);
	}

	@Transactional(readOnly = true)
	public List<SampleType> getAllSampleTypes() {
		return sampleTypeDAO.findByActiveFlag(1);
	}

	@Transactional(readOnly = true)
	public SampleType getSampleTypeById(Integer id) {

		if (id == null || id <= 0) {
			throw new IllegalArgumentException("Valid sample type ID is required");
		}

		return sampleTypeDAO.findById(id).orElseThrow(() -> new IllegalArgumentException("Sample type not found"));
	}

	@Transactional
	public SampleType updateSampleType(Integer id, SampleType request) {

		validateSampleType(request);

		SampleType existing = getSampleTypeById(id);
		String name = request.getSampleTypeName().trim();

		if (!existing.getSampleTypeName().equalsIgnoreCase(name) && sampleTypeDAO.existsBySampleTypeName(name)) {
			throw new IllegalArgumentException("Sample type name already exists");
		}

		existing.setSampleTypeName(name);
		existing.setDescription(request.getDescription());

		return sampleTypeDAO.save(existing);
	}

	@Transactional
	public SampleType updateSampleTypeStatus(Integer id, Integer activeFlag) {

		if (activeFlag == null || (activeFlag != 0 && activeFlag != 1)) {
			throw new IllegalArgumentException("Active flag must be 1 or 0");
		}

		SampleType existing = getSampleTypeById(id);
		existing.setActiveFlag(activeFlag);

		return sampleTypeDAO.save(existing);
	}

	private void validateSampleType(SampleType sampleType) {

		if (sampleType == null) {
			throw new IllegalArgumentException("Sample type details are required");
		}

		if (sampleType.getSampleTypeName() == null || sampleType.getSampleTypeName().isBlank()) {
			throw new IllegalArgumentException("Sample type name is required");
		}

		if (sampleType.getSampleTypeName().trim().length() > 100) {
			throw new IllegalArgumentException("Sample type name cannot exceed 100 characters");
		}

		if (sampleType.getDescription() != null && sampleType.getDescription().length() > 500) {
			throw new IllegalArgumentException("Description cannot exceed 500 characters");
		}

		if (sampleType.getUpdatedBy() == null || sampleType.getUpdatedBy() <= 0) {
			throw new IllegalArgumentException("Updated by user ID is required");
		}
	}

	@Transactional(readOnly = true)
	public long getActiveTestCount(Integer sampleTypeId) {
		return testDAO.countBySampleType_IdAndActiveFlag(sampleTypeId, 1);
	}
}
