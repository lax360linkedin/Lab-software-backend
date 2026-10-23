
package com.labmanagement.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.labmanagement.entity.TestCategory;

@Repository
public interface TestCategoryDAO extends JpaRepository<TestCategory, Integer> {

	boolean existsByCategoryName(String categoryName);
}
