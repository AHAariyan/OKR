package com.onnorokom.okr.controller;

import com.onnorokom.okr.dto.*;
import com.onnorokom.okr.model.User;
import com.onnorokom.okr.repository.UserRepository;
import com.onnorokom.okr.service.AdminService;
import com.onnorokom.okr.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/**
 * Admin Controller - Only accessible by SUPER_ADMIN
 *
 * Provides endpoints for:
 * - User management (CRUD, block/unblock)
 * - Team management (CRUD)
 * - Department management (CRUD)
 * - Cycle management (CRUD)
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.onnorokom.okr.service.CycleService cycleService;

    /**
     * Verify that the current user is a SUPER_ADMIN.
     * Throws 403 Forbidden if not.
     */
    private void requireSuperAdmin() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User actor = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!permissionService.isSuperAdmin(actor)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: Super Admin access required");
        }
    }

    // ==================== USER MANAGEMENT ====================

    @GetMapping("/users")
    public List<UserDto> getAllUsers() {
        requireSuperAdmin();
        return adminService.getAllUsers();
    }

    @PostMapping("/users")
    public UserDto createUser(@RequestBody CreateUserRequest request) {
        requireSuperAdmin();
        return adminService.createUser(request);
    }

    @PatchMapping("/users/{id}/block")
    public UserDto blockUser(@PathVariable UUID id, @RequestParam boolean blocked) {
        requireSuperAdmin();
        return adminService.blockUser(id, blocked);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable UUID id) {
        requireSuperAdmin();
        adminService.deleteUser(id);
    }

    // ==================== TEAM MANAGEMENT ====================

    @GetMapping("/teams")
    public List<TeamDto> getAllTeams() {
        requireSuperAdmin();
        return adminService.getAllTeams();
    }

    @PostMapping("/teams")
    public TeamDto createTeam(@RequestBody CreateTeamRequest request) {
        requireSuperAdmin();
        return adminService.createTeam(request);
    }

    @DeleteMapping("/teams/{id}")
    public void deleteTeam(@PathVariable UUID id) {
        requireSuperAdmin();
        adminService.deleteTeam(id);
    }

    // ==================== DEPARTMENT MANAGEMENT ====================

    @GetMapping("/departments")
    public List<DepartmentDto> getAllDepartments() {
        requireSuperAdmin();
        return adminService.getAllDepartments();
    }

    @PostMapping("/departments")
    public DepartmentDto createDepartment(@RequestBody CreateDepartmentRequest request) {
        requireSuperAdmin();
        return adminService.createDepartment(request);
    }

    @DeleteMapping("/departments/{id}")
    public void deleteDepartment(@PathVariable UUID id) {
        requireSuperAdmin();
        adminService.deleteDepartment(id);
    }

    // ==================== CYCLE MANAGEMENT ====================

    @GetMapping("/cycles")
    public List<CycleDto> getAllCycles() {
        requireSuperAdmin();
        return cycleService.getAllCycles();
    }

    @PostMapping("/cycles")
    public CycleDto createCycle(@RequestBody CreateCycleRequest request) {
        requireSuperAdmin();
        return cycleService.createCycle(request);
    }

    @DeleteMapping("/cycles/{id}")
    public void deleteCycle(@PathVariable UUID id) {
        requireSuperAdmin();
        cycleService.deleteCycle(id);
    }
}
