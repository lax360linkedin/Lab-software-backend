
package com.labmanagement.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.labmanagement.entity.PatientTest;


public interface PatientTestDAO extends JpaRepository<PatientTest, Integer> {

	List<PatientTest> findByPatientId(Integer patientId);

	List<PatientTest> findByPatientIdAndActiveFlag(Integer patientId, Integer activeFlag);

	boolean existsByPatientIdAndTestId(Integer patientId, Integer testId);

	List<PatientTest> findByVisitIdAndActiveFlag(Integer visitId, Integer activeFlag);

	List<PatientTest> findByVisitIdAndPatientIdAndActiveFlag(Integer visitId, Integer patientId, Integer activeFlag);
}
