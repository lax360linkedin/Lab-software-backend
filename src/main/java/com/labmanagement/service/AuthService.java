
package com.labmanagement.service;

import java.util.Locale;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.labmanagement.dao.LaboratoryDAO;
import com.labmanagement.dao.UserDAO;
import com.labmanagement.dto.SigninDTO;
import com.labmanagement.dto.SignupDTO;
import com.labmanagement.entity.Laboratory;
import com.labmanagement.entity.User;
import com.labmanagement.security.JwtService;

@Service
public class AuthService {

	private final LaboratoryDAO laboratoryRepository;
	private final UserDAO userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(LaboratoryDAO laboratoryDAO, UserDAO userDAO, PasswordEncoder passwordEncoder,
			JwtService jwtService) {

		this.laboratoryRepository = laboratoryDAO;
		this.userRepository = userDAO;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Transactional
	public void signUp(SignupDTO request) {

		// 1. Validate password confirmation
		if (request.getPassword() == null || !request.getPassword().equals(request.getConfirmPassword())) {

			throw new IllegalArgumentException("Password and confirm password do not match");
		}

		// 2. Validate and normalize email
		if (request.getEmail() == null || request.getEmail().isBlank()) {

			throw new IllegalArgumentException("Email cannot be null or blank");
		}

		String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

		// 3. Prevent duplicate email registration
		if (userRepository.existsByEmail(email)) {
			throw new IllegalArgumentException("An account with this email already exists");
		}

		// 4. Find an existing laboratory by name
		String laboratoryName = request.getLaboratoryName().trim();

		Optional<Laboratory> existingLaboratory = laboratoryRepository.findByLaboratoryNameIgnoreCase(laboratoryName);

		Laboratory savedLaboratory;

		if (existingLaboratory.isPresent()) {

			// Reuse the existing laboratory
			savedLaboratory = existingLaboratory.get();

		} else {

			// Create a new laboratory if the name is not found
			Laboratory laboratory = new Laboratory();

			laboratory.setLaboratoryName(laboratoryName);
			laboratory.setLaboratoryAddress(request.getLaboratoryAddress().trim());
			laboratory.setActiveFlag(1);

			savedLaboratory = laboratoryRepository.save(laboratory);
		}

		// 5. Create the user
		User user = new User();

		user.setLaboratory(savedLaboratory);
		user.setAdministratorName(request.getAdministratorName().trim());
		user.setPhoneNumber(request.getPhoneNumber().trim());
		user.setEmail(email);

		// Current implementation assigns ADMIN to every signup
		user.setRole(request.getRole());

		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		user.setActiveFlag(1);

		userRepository.save(user);
	}

	@Transactional(readOnly = true)
	public String signIn(SigninDTO request) {

		if (request.getEmail() == null || request.getEmail().isBlank() || request.getPassword() == null
				|| request.getPassword().isBlank()) {

			throw new IllegalArgumentException("Email and password are required");
		}

		String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

		User user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {

			throw new IllegalArgumentException("Invalid email or password");
		}

		if (user.getActiveFlag() != 1) {
			throw new IllegalArgumentException("Account is inactive");
		}

		return jwtService.generateToken(user.getEmail());
	}
}
