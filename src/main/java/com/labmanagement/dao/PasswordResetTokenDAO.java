package com.labmanagement.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.labmanagement.entity.PasswordResetToken;

public interface PasswordResetTokenDAO
        extends JpaRepository<PasswordResetToken, Integer> {

    Optional<PasswordResetToken> findByTokenHashAndActiveFlagAndUsedFlag(
            String tokenHash, int activeFlag, int usedFlag);
}