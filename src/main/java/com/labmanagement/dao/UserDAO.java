package com.labmanagement.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.labmanagement.entity.User;

public interface UserDAO extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    
    Optional<User> findByEmailIgnoreCase(String email);
}