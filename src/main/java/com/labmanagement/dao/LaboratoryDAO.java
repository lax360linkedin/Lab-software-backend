
package com.labmanagement.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.labmanagement.entity.Laboratory;

public interface LaboratoryDAO extends JpaRepository<Laboratory, Integer> {

	Optional<Laboratory> findByLaboratoryNameIgnoreCase(String laboratoryName);
}
