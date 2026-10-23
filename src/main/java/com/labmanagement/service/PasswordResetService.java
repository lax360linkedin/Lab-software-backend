package com.labmanagement.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.labmanagement.dao.PasswordResetTokenDAO;
import com.labmanagement.dao.UserDAO;
import com.labmanagement.email.EmailService;
import com.labmanagement.entity.PasswordResetToken;
import com.labmanagement.entity.User;

@Service
public class PasswordResetService {

	private final UserDAO userDAO;
	private final PasswordResetTokenDAO tokenDAO;
	private final EmailService emailService;
	private final PasswordEncoder passwordEncoder;

	public PasswordResetService(UserDAO userDAO, PasswordResetTokenDAO tokenDAO, EmailService emailService,
			PasswordEncoder passwordEncoder) {

		this.userDAO = userDAO;
		this.tokenDAO = tokenDAO;
		this.emailService = emailService;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public void createResetToken(String email) {

		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email is required");
		}

		String normalizedEmail = email.trim().toLowerCase();

		User user = userDAO.findByEmail(normalizedEmail).orElse(null);

		// Do not reveal whether an email is registered.
		if (user == null || user.getActiveFlag() != 1) {
			return;
		}

		byte[] randomBytes = new byte[32];
		new SecureRandom().nextBytes(randomBytes);

		String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

		PasswordResetToken resetToken = new PasswordResetToken();
		resetToken.setUser(user);
		resetToken.setTokenHash(hashToken(rawToken));
		resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
		resetToken.setUsedFlag(0);
		resetToken.setActiveFlag(1);

		tokenDAO.save(resetToken);

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				emailService.sendPasswordResetEmail(normalizedEmail, rawToken);
			}
		});
	}

	private String hashToken(String token) {

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

			return HexFormat.of().formatHex(hash);

		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("Unable to generate token hash", exception);
		}
	}

	@Transactional
	public void resetPassword(String rawToken, String newPassword) {

		if (rawToken == null || rawToken.isBlank()) {
			throw new IllegalArgumentException("Reset token is required");
		}

		if (newPassword == null || newPassword.isBlank() || newPassword.length() < 8) {
			throw new IllegalArgumentException("Password must contain at least 8 characters");
		}

		String tokenHash = hashToken(rawToken);

		PasswordResetToken resetToken = tokenDAO.findByTokenHashAndActiveFlagAndUsedFlag(tokenHash, 1, 0)
				.orElseThrow(() -> new IllegalArgumentException("Invalid or already used reset token"));

		if (!resetToken.getExpiresAt().isAfter(LocalDateTime.now())) {
			resetToken.setActiveFlag(0);
			tokenDAO.save(resetToken);

			throw new IllegalArgumentException("Reset token has expired. Request a new link");
		}

		User user = resetToken.getUser();

		user.setPasswordHash(passwordEncoder.encode(newPassword));
		userDAO.save(user);

		resetToken.setUsedFlag(1);
		resetToken.setActiveFlag(0);
		tokenDAO.save(resetToken);
	}

}