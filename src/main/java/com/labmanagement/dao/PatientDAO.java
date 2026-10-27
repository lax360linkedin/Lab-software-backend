package com.labmanagement.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.labmanagement.entity.Patient;

public interface PatientDAO extends JpaRepository<Patient, Integer> {

	Optional<Patient> findByCode(String code);

	boolean existsByCode(String code);

	List<Patient> findByActiveFlag(Integer activeFlag);

	List<Patient> findByPatientNameContainingIgnoreCase(String patientName);

	Optional<Patient> findByCodeAndActiveFlag(String code, Integer activeFlag);
}