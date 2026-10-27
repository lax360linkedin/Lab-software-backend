package com.labmanagement.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.labmanagement.entity.Doctor;

public interface DoctorDAO extends JpaRepository<Doctor, Integer> {

	Optional<Doctor> findByDoctorCode(String doctorCode);

	boolean existsByDoctorCode(String doctorCode);

	List<Doctor> findByActiveFlag(Integer activeFlag);

	List<Doctor> findBySpecializationAndActiveFlag(String specialization, Integer activeFlag);

	List<Doctor> findByDoctorNameContainingIgnoreCase(String doctorName);

	Optional<Doctor> findByDoctorCodeAndActiveFlag(String doctorCode, Integer activeFlag);
}