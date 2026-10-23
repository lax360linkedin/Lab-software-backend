
package com.labmanagement.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.labmanagement.entity.Test;

public interface TestDAO extends JpaRepository<Test, Integer> {

	boolean existsByTestCodeAndActiveFlag(String testCode, Integer activeFlag);

	Optional<Test> findByTestCode(String testCode);

	List<Test> findByActiveFlag(Integer activeFlag);

	long countBySampleType_IdAndActiveFlag(Integer sampleTypeId, Integer activeFlag);

	long countByCategory_IdAndActiveFlag(Integer categoryId, Integer activeFlag);

}
