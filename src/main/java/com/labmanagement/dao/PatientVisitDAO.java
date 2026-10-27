package com.labmanagement.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.labmanagement.entity.PatientVisit;

public interface PatientVisitDAO extends JpaRepository<PatientVisit, Integer> {

	List<PatientVisit> findByPatientIdOrderByVisitDateDesc(Integer patientId);

	List<PatientVisit> findByDoctor_DoctorCodeAndActiveFlagOrderByVisitDateDesc(String doctorCode, Integer activeFlag);

	long countDistinctPatientIdByDoctor_DoctorCodeAndActiveFlag(String doctorCode, Integer activeFlag);
}