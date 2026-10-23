
package com.labmanagement.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.labmanagement.entity.MenuEvent;

public interface MenuEventDAO extends JpaRepository<MenuEvent, Integer> {

	List<MenuEvent> findByMenuIdAndActiveFlag(Integer menuId, Integer activeFlag);

	Optional<MenuEvent> findByIdAndMenuMenuCode(Integer id, String menuCode);
}
