
package com.labmanagement.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.labmanagement.dao.MenuDAO;
import com.labmanagement.dao.MenuEventDAO;
import com.labmanagement.dao.RolePermissionDAO;
import com.labmanagement.entity.Menu;
import com.labmanagement.entity.MenuEvent;
import com.labmanagement.entity.RolePermission;
import com.labmanagement.entity.UserRole;

@Component
public class DataInitializer implements CommandLineRunner {

	private final MenuDAO menuDAO;
	private final MenuEventDAO menuEventDAO;
	private final RolePermissionDAO rolePermissionDAO;

	public DataInitializer(MenuDAO menuDAO, MenuEventDAO menuEventDAO, RolePermissionDAO rolePermissionDAO) {
		this.menuDAO = menuDAO;
		this.menuEventDAO = menuEventDAO;
		this.rolePermissionDAO = rolePermissionDAO;
	}

	@Override
	@Transactional
	public void run(String... args) {
		createMenu("DASHBOARD", "Dashboard", List.of("View Dashboard"));

		createMenu("TESTS", "Tests",
				List.of("View Tests", "Manage Tests", "View Test Categories", "Manage Test Categories",
						"View Sample Types", "Manage Sample Types", "View Test Parameters", "Manage Test Parameters",
						"View Test Pricing", "Manage Test Pricing"));
		createMenu("DOCTORS_REFERRALS", "Doctors/Referrals",
				List.of("View Doctors", "Manage Doctors", "View Referrals", "Manage Referrals"));

		createMenu("LAB_PROFILE", "Lab Profile", List.of("View Lab Profile", "Update Lab Profile"));

		createMenu("PATIENTS", "Patients", List.of("View Patients", "Register Patient", "Update Patient"));

		createMenu("BILLING", "Billing",
				List.of("View Bills", "Create Bill", "Update Bill", "View Payments", "Record Payment"));

		createMenu("ACCESSION", "Accession",
				List.of("View Samples", "Accept Sample", "Reject Sample", "Collect Sample", "Receive Sample"));

		createMenu("ANALYSIS", "Analysis", List.of("View Analysis", "Update Analysis"));

		createMenu("RESULTS", "Results", List.of("View Results", "Enter Results", "Update Results"));

		createMenu("QUALITY_CONTROL", "Quality Control",
				List.of("View Quality Control", "Record Quality Control", "Verify Results"));

		createMenu("REPORTS", "Reports", List.of("View Reports", "Generate Report", "Share Report", "Print Report"));

		createMenu("NOTIFICATIONS", "Notifications", List.of("View Notifications"));

		createMenu("FINANCIAL_ANALYSIS", "Financial Analysis", List.of("View Financial Analysis"));

		createMenu("EXPENSES", "Expenses", List.of("View Expenses", "Manage Expenses"));

		createMenu("STAFF_USERS", "Staff/Users", List.of("View Staff", "Manage Staff", "View Users", "Manage Users"));

		createMenu("SETTINGS", "Settings", List.of("View Settings", "Manage Settings"));
	}

	private void createMenu(String code, String name, List<String> eventNames) {

		Menu menu = menuDAO.findAll().stream().filter(m -> code.equals(m.getMenuCode())).findFirst().orElse(null);

		if (menu == null) {
			menu = new Menu();
			menu.setMenuCode(code);
			menu.setMenuName(name);
			menu.setActiveFlag(1);
			menu.setUpdatedBy(1);
			menu = menuDAO.saveAndFlush(menu);
		}

		for (String eventName : eventNames) {
			MenuEvent event = menuEventDAO.findByMenuIdAndActiveFlag(menu.getId(), 1).stream()
					.filter(e -> eventName.equals(e.getEventName())).findFirst().orElse(null);

			if (event == null) {
				event = new MenuEvent();
				event.setMenu(menu);
				event.setEventName(eventName);
				event.setEventType(MenuEvent.EventType.ACTION);
			}

			event.setActiveFlag(1);
			event.setUpdatedBy(1);
			event = menuEventDAO.saveAndFlush(event);

			for (UserRole role : UserRole.values()) {
				RolePermission.AccessLevel access = getAccessLevel(role, code, eventName);

				saveOrUpdatePermission(role, event, access);
			}
		}
	}

	private void saveOrUpdatePermission(UserRole role, MenuEvent event, RolePermission.AccessLevel access) {

		RolePermission permission = rolePermissionDAO.findAll().stream().filter(p -> p.getRoleName() == role
				&& p.getMenuEvent() != null && p.getMenuEvent().getId().equals(event.getId())).findFirst().orElse(null);

		if (permission == null) {
			permission = new RolePermission();
			permission.setRoleName(role);
			permission.setMenuEvent(event);
		}

		permission.setAccessLevel(access);
		permission.setActiveFlag(1);
		permission.setUpdatedBy(1);

		rolePermissionDAO.save(permission);
	}

	private RolePermission.AccessLevel getAccessLevel(UserRole role, String menuCode, String eventName) {

		if (role == UserRole.ADMIN) {
			return RolePermission.AccessLevel.FULL;
		}

		if (role == UserRole.RECEPTIONIST) {
			return switch (menuCode) {
			case "DASHBOARD", "TESTS", "LAB_PROFILE", "NOTIFICATIONS" -> RolePermission.AccessLevel.VIEW;

			case "DOCTORS_REFERRALS" -> RolePermission.AccessLevel.MANAGE_VIEW;

			case "PATIENTS", "BILLING" -> RolePermission.AccessLevel.FULL;

			case "REPORTS" -> RolePermission.AccessLevel.VIEW_SHARE_PRINT;

			default -> RolePermission.AccessLevel.NONE;
			};
		}

		if (role == UserRole.LAB_TECHNICIAN) {
			return switch (menuCode) {
			case "DASHBOARD", "TESTS", "DOCTORS_REFERRALS", "LAB_PROFILE", "NOTIFICATIONS" ->
				RolePermission.AccessLevel.VIEW;

			case "PATIENTS" -> eventName.equals("View Patients") ? RolePermission.AccessLevel.LIMITED_VIEW
					: RolePermission.AccessLevel.NONE;

			case "ACCESSION" -> RolePermission.AccessLevel.FULL;

			case "ANALYSIS", "RESULTS", "QUALITY_CONTROL" -> RolePermission.AccessLevel.TECHNICAL;

			case "REPORTS" -> RolePermission.AccessLevel.TECHNICAL;

			default -> RolePermission.AccessLevel.NONE;
			};
		}

		return RolePermission.AccessLevel.NONE;
	}
}
