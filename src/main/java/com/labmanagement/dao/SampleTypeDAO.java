package com.labmanagement.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.labmanagement.entity.SampleType;

@Repository
public interface SampleTypeDAO extends JpaRepository<SampleType, Integer> {

	boolean existsBySampleTypeName(String sampleTypeName);

	List<SampleType> findByActiveFlag(Integer activeFlag);
}