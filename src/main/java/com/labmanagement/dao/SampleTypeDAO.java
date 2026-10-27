package com.labmanagement.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.labmanagement.entity.SampleType;

public interface SampleTypeDAO extends JpaRepository<SampleType, Integer> {

	boolean existsBySampleTypeName(String sampleTypeName);

	List<SampleType> findByActiveFlag(Integer activeFlag);
}