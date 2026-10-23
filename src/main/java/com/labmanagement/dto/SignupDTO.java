
package com.labmanagement.dto;

import com.labmanagement.entity.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SignupDTO {

	@NotBlank(message = "Laboratory name is required")
	@Size(max = 150, message = "Laboratory name cannot exceed 150 characters")
	private String laboratoryName;

	@NotBlank(message = "Laboratory address is required")
	@Size(max = 500, message = "Laboratory address cannot exceed 500 characters")
	private String laboratoryAddress;

	@NotBlank(message = "Administrator name is required")
	@Size(max = 150, message = "Administrator name cannot exceed 150 characters")
	private String administratorName;

	@NotBlank(message = "Phone number is required")
	@Pattern(regexp = "^[0-9]{10,12}$", message = "Phone number must contain 10 to 12 digits")
	private String phoneNumber;

	@NotBlank(message = "Email is required")
	@Email(message = "Enter a valid email address")
	@Size(max = 150, message = "Email cannot exceed 150 characters")
	private String email;

	@NotBlank(message = "Password is required")
	@Size(min = 8, max = 72, message = "Password must contain 8 to 72 characters")
	@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S+$", message = "Password must contain uppercase, lowercase, digit, and special character, with no spaces")
	private String password;

	@NotBlank(message = "Confirm password is required")
	private String confirmPassword;
	
	private UserRole role;

	// Getters and setters

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public String getLaboratoryName() {
		return laboratoryName;
	}

	public void setLaboratoryName(String laboratoryName) {
		this.laboratoryName = laboratoryName;
	}

	public String getLaboratoryAddress() {
		return laboratoryAddress;
	}

	public void setLaboratoryAddress(String laboratoryAddress) {
		this.laboratoryAddress = laboratoryAddress;
	}

	public String getAdministratorName() {
		return administratorName;
	}

	public void setAdministratorName(String administratorName) {
		this.administratorName = administratorName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email == null ? null : email.trim();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}
}
