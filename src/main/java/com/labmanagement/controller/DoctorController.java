package com.labmanagement.controller;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.labmanagement.dto.DoctorDTO;
import com.labmanagement.dto.ResponseDTO;
import com.labmanagement.entity.Doctor;
import com.labmanagement.entity.User;
import com.labmanagement.entity.UserRole;
import com.labmanagement.service.DoctorService;
import com.labmanagement.service.RolePermissionService;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

	private static final long MAX_PHOTO_SIZE = 10L * 1024 * 1024;

	private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

	private final DoctorService doctorService;
	private final RolePermissionService rolePermissionService;

	public DoctorController(DoctorService doctorService, RolePermissionService rolePermissionService) {

		this.doctorService = doctorService;
		this.rolePermissionService = rolePermissionService;
	}

	// Create doctor
	@PostMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<DoctorDTO> createDoctor(@RequestBody DoctorDTO request, Authentication authentication) {

		checkPermission(authentication, "Create Doctor");

		User user = getAuthenticatedUser(authentication);

		Doctor saved = doctorService.createDoctor(request, user.getId());

		return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(saved));
	}

	// Get all active doctors
	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<DoctorDTO>> getAllDoctors(Authentication authentication) {

		checkPermission(authentication, "View Doctors");

		List<DoctorDTO> response = doctorService.getAllActiveDoctors().stream().map(this::toDTO).toList();

		return ResponseEntity.ok(response);
	}

	// Get doctor by code
	@GetMapping("/code/{doctorCode}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<DoctorDTO> getDoctorByCode(@PathVariable String doctorCode, Authentication authentication) {

		checkPermission(authentication, "View Doctors");

		return ResponseEntity.ok(toDTO(doctorService.getDoctorByCode(doctorCode)));
	}

	// Update doctor details
	@PutMapping("/code/{doctorCode}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<DoctorDTO> updateDoctor(@PathVariable String doctorCode, @RequestBody DoctorDTO request,
			Authentication authentication) {

		checkPermission(authentication, "Update Doctor");

		User user = getAuthenticatedUser(authentication);

		Doctor updated = doctorService.updateDoctorByCode(doctorCode, request, user.getId());

		return ResponseEntity.ok(toDTO(updated));
	}

	// Activate or deactivate doctor
	@PatchMapping("/code/{doctorCode}/status")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ResponseDTO<Void>> updateDoctorStatus(@PathVariable String doctorCode,
			@RequestParam Integer activeFlag, Authentication authentication) {

		checkPermission(authentication, "Manage Doctors");

		User user = getAuthenticatedUser(authentication);

		if (activeFlag == null || (activeFlag != 0 && activeFlag != 1)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "activeFlag must be 0 or 1");
		}

		doctorService.updateDoctorStatusByCode(doctorCode, activeFlag, user.getId());

		return ResponseEntity.ok(ResponseDTO.success());
	}

	// Upload doctor photo
	@PutMapping(value = "/code/{doctorCode}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ResponseDTO<Void>> uploadDoctorPhoto(@PathVariable String doctorCode,
			@RequestPart("photo") MultipartFile photo, Authentication authentication) {

		checkPermission(authentication, "Manage Doctors");

		User user = getAuthenticatedUser(authentication);

		validateDoctorPhoto(photo);

		try {
			byte[] photoBytes = photo.getBytes();

			// Validate actual file signature, not just the MIME type.
			detectImageType(photoBytes);

			doctorService.updateDoctorPhoto(doctorCode, photoBytes, user.getId());

			return ResponseEntity.ok(ResponseDTO.success());

		} catch (IOException exception) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read uploaded doctor photo",
					exception);
		}
	}

	// Retrieve doctor photo
	@GetMapping("/code/{doctorCode}/photo")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<byte[]> getDoctorPhoto(@PathVariable String doctorCode, Authentication authentication) {

		checkPermission(authentication, "View Doctors");

		Doctor doctor = doctorService.getDoctorByCode(doctorCode);
		byte[] photo = doctor.getDoctorPhoto();

		if (photo == null || photo.length == 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor photo not found");
		}

		MediaType contentType = detectImageType(photo);

		return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-cache").contentType(contentType).body(photo);
	}

	// Validate uploaded photo
	private void validateDoctorPhoto(MultipartFile photo) {

		if (photo == null || photo.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor photo is required");
		}

		if (photo.getSize() > MAX_PHOTO_SIZE) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum doctor photo size is 10 MB");
		}

		String contentType = photo.getContentType();

		if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {

			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG, PNG, and WEBP images are allowed");
		}
	}

	// Detect image format from its file signature
	private MediaType detectImageType(byte[] photo) {

		if (photo == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Photo is required");
		}

		// JPEG / JPG
		if (photo.length >= 3 && (photo[0] & 0xFF) == 0xFF && (photo[1] & 0xFF) == 0xD8 && (photo[2] & 0xFF) == 0xFF) {

			return MediaType.IMAGE_JPEG;
		}

		// PNG
		if (photo.length >= 8 && (photo[0] & 0xFF) == 0x89 && photo[1] == 0x50 && photo[2] == 0x4E && photo[3] == 0x47
				&& photo[4] == 0x0D && photo[5] == 0x0A && photo[6] == 0x1A && photo[7] == 0x0A) {

			return MediaType.IMAGE_PNG;
		}

		// WEBP: RIFF....WEBP
		if (photo.length >= 12 && photo[0] == 'R' && photo[1] == 'I' && photo[2] == 'F' && photo[3] == 'F'
				&& photo[8] == 'W' && photo[9] == 'E' && photo[10] == 'B' && photo[11] == 'P') {

			return MediaType.parseMediaType("image/webp");
		}

		throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
				"Invalid image content. Upload a valid JPG, PNG, or WEBP file");
	}

	// Get authenticated user
	private User getAuthenticatedUser(Authentication authentication) {

		if (authentication == null || !authentication.isAuthenticated()
				|| !(authentication.getPrincipal() instanceof User)) {

			throw new AccessDeniedException("Authenticated user required");
		}

		return (User) authentication.getPrincipal();
	}

	// Check role permission
	private void checkPermission(Authentication authentication, String eventName) {

		User user = getAuthenticatedUser(authentication);

		if (user.getRole() == UserRole.ADMIN) {
			return;
		}

		boolean allowed = rolePermissionService.hasEventAccess(authentication, "DOCTORS", eventName);

		if (!allowed) {
			throw new AccessDeniedException("You do not have permission to perform this action");
		}
	}

	// Convert entity to DTO
	private DoctorDTO toDTO(Doctor doctor) {

		DoctorDTO dto = new DoctorDTO();

		dto.setId(doctor.getId());
		dto.setDoctorName(doctor.getDoctorName());
		dto.setDoctorCode(doctor.getDoctorCode());

		// Photo is returned through the separate photo API.
		// Do not include the full image bytes in doctor JSON.

		dto.setSpecialization(doctor.getSpecialization());
		dto.setHospitalClinic(doctor.getHospitalClinic());
		dto.setPhone(doctor.getPhone());
		dto.setEmail(doctor.getEmail());
		dto.setAddress(doctor.getAddress());
		dto.setReferralType(doctor.getReferralType());
		dto.setCommissionApplicable(doctor.getCommissionApplicable());
		dto.setReferralCommission(doctor.getReferralCommission());
		dto.setNotes(doctor.getNotes());
		dto.setActiveFlag(doctor.getActiveFlag());
		dto.setUpdatedAt(doctor.getUpdatedAt());

		return dto;
	}

	@GetMapping("/referral/summary")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<DoctorDTO>> getDoctorReferralSummary(Authentication authentication) {

		checkPermission(authentication, "View Doctors");

		return ResponseEntity.ok(doctorService.getDoctorReferralSummary());
	}

}