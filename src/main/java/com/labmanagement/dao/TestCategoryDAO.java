
package com.labmanagement.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.labmanagement.entity.TestCategory;

public interface TestCategoryDAO extends JpaRepository<TestCategory, Integer> {

	boolean existsByCategoryName(String categoryName);
}
