package com.labmanagement.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.labmanagement.dto.ForgotPasswordDTO;
import com.labmanagement.dto.ResetPasswordDTO;
import com.labmanagement.dto.ResponseDTO;
import com.labmanagement.dto.SigninDTO;
import com.labmanagement.dto.SignupDTO;
import com.labmanagement.service.AuthService;
import com.labmanagement.service.PasswordResetService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	private final PasswordResetService passwordResetService;

	public AuthController(AuthService authService, PasswordResetService passwordResetService) {
		this.authService = authService;
		this.passwordResetService = passwordResetService;
	}

	@PostMapping("/signup")
	public ResponseEntity<ResponseDTO<Void>> signUp(@Valid @RequestBody SignupDTO request) {

		authService.signUp(request);

		return ResponseEntity.status(HttpStatus.CREATED).body(ResponseDTO.success());
	}

	@PostMapping("/signin")
	public ResponseEntity<ResponseDTO<String>> signIn(@Valid @RequestBody SigninDTO request) {

		String token = authService.signIn(request);

		return ResponseEntity.ok(ResponseDTO.success(token));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ResponseDTO<Void>> handleBadRequest(IllegalArgumentException exception) {

		return ResponseEntity.badRequest().body(ResponseDTO.failure(exception.getMessage()));
	}

	@PostMapping("/forgot/password")
	public ResponseEntity<ResponseDTO<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordDTO request) {

		passwordResetService.createResetToken(request.getEmail());

		return ResponseEntity.ok(ResponseDTO.success());
	}

	@PostMapping("/reset/password")
	public ResponseEntity<ResponseDTO<Void>> resetPassword(@Valid @RequestBody ResetPasswordDTO request) {

		passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

		return ResponseEntity.ok(ResponseDTO.success());
	}

}