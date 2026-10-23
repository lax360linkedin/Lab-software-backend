
package com.labmanagement.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.labmanagement.dao.TestParameterDAO;
import com.labmanagement.dto.ResponseDTO;
import com.labmanagement.dto.TestDTO;
import com.labmanagement.dto.TestParameterDTO;
import com.labmanagement.entity.SampleType;
import com.labmanagement.entity.Test;
import com.labmanagement.entity.TestCategory;
import com.labmanagement.entity.TestParameter;
import com.labmanagement.entity.User;
import com.labmanagement.service.TestService;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final TestService testService;
    private final TestParameterDAO testParameterDAO;

    public TestController(
            TestService testService,
            TestParameterDAO testParameterDAO) {
        this.testService = testService;
        this.testParameterDAO = testParameterDAO;
    }

    // Create test with parameters
    @PostMapping
    @PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'TESTS', 'Manage Tests')")
    public ResponseEntity<TestDTO> createTest(
            @RequestBody TestDTO request,
            Authentication authentication) {

        User loggedInUser = (User) authentication.getPrincipal();

        Test test = new Test();
        test.setTestName(request.getTestName());
        test.setTestCode(request.getTestCode());
        test.setMethod(request.getMethod());
        test.setPrice(request.getPrice());
        test.setTurnaroundTime(request.getTurnaroundTime());
        test.setUpdatedBy(loggedInUser.getId());

        TestCategory category = new TestCategory();
        category.setId(request.getCategoryId());
        test.setCategory(category);

        SampleType sampleType = new SampleType();
        sampleType.setId(request.getSampleTypeId());
        test.setSampleType(sampleType);

        Test savedTest = testService.createTest(test, request.getParameters());

        TestDTO response = new TestDTO();
        response.setTestCode(savedTest.getTestCode());
        response.setTestName(savedTest.getTestName());
        response.setActiveFlag(savedTest.getActiveFlag());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Update test and parameters
    @PutMapping("/code/{testCode}")
    @PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'TESTS', 'Manage Tests')")
    public ResponseEntity<ResponseDTO<Void>> updateTestByCode(
            @PathVariable String testCode,
            @RequestBody TestDTO request,
            Authentication authentication) {

        User loggedInUser = (User) authentication.getPrincipal();

        testService.updateTestByCode(testCode, request, loggedInUser.getId());

		return ResponseEntity.ok(ResponseDTO.success());
    }

    // Activate test
    @PatchMapping("/code/{testCode}/activate")
    @PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'TESTS', 'Manage Tests')")
    public ResponseEntity<ResponseDTO<Void>> activateTestByCode(
            @PathVariable String testCode) {

        testService.activateTestByCode(testCode);

        return ResponseEntity.ok(ResponseDTO.success());
    }

    // Deactivate test
    @PatchMapping("/code/{testCode}/deactivate")
    @PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'TESTS', 'Manage Tests')")
    public ResponseEntity<ResponseDTO<Void>> deactivateTestByCode(
            @PathVariable String testCode) {

        testService.deactivateTestByCode(testCode);

        return ResponseEntity.ok(ResponseDTO.success());
    }

    // Get test by code
    @GetMapping("/code/{testCode}")
    @PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'TESTS', 'View Tests')")
    public ResponseEntity<TestDTO> getTestByCode(
            @PathVariable String testCode) {

        Test test = testService.getTestByCode(testCode);

        return ResponseEntity.ok(convertToDTO(test));
    }

    // Get all tests
    @GetMapping
    @PreAuthorize("@rolePermissionService.hasEventAccess(authentication, 'TESTS', 'View Tests')")
    public ResponseEntity<List<TestDTO>> getAllTests() {

        List<TestDTO> tests = testService.getAllTests()
                .stream()
                .map(this::convertToDTO)
                .toList();

        return ResponseEntity.ok(tests);
    }

    private TestDTO convertToDTO(Test test) {

        TestDTO dto = new TestDTO();

        dto.setId(test.getId());
        dto.setTestName(test.getTestName());
        dto.setTestCode(test.getTestCode());

        dto.setCategoryId(test.getCategory().getId());
        dto.setCategoryName(test.getCategory().getCategoryName());

        dto.setSampleTypeId(test.getSampleType().getId());
        dto.setSampleTypeName(test.getSampleType().getSampleTypeName());

        dto.setMethod(test.getMethod());
        dto.setPrice(test.getPrice());
        dto.setTurnaroundTime(test.getTurnaroundTime());
        dto.setActiveFlag(test.getActiveFlag());

        List<TestParameterDTO> parameters = new ArrayList<>();

        List<TestParameter> testParameters =
                testParameterDAO.findByTest_Id(test.getId());

        for (TestParameter parameter : testParameters) {

            TestParameterDTO paramDTO = new TestParameterDTO();

            paramDTO.setId(parameter.getId());
            paramDTO.setTestId(test.getId());
            paramDTO.setParameterName(parameter.getParameterName());
            paramDTO.setUnit(parameter.getUnit());
            paramDTO.setReferenceRange(parameter.getReferenceRange());
            paramDTO.setActiveFlag(parameter.getActiveFlag());

            parameters.add(paramDTO);
        }

        dto.setParameters(parameters);

        return dto;
    }
}
