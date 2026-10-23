
package com.labmanagement.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.labmanagement.entity.TestParameter;

public interface TestParameterDAO extends JpaRepository<TestParameter, Integer> {

	List<TestParameter> findByTest_Id(Integer testId);

}
