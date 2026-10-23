
package com.labmanagement.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.labmanagement.entity.Menu;

public interface MenuDAO extends JpaRepository<Menu, Integer> {

	Optional<Menu> findByMenuCode(String menuCode);

}
