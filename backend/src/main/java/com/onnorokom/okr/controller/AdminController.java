package com.onnorokom.okr.controller;

import com.onnorokom.okr.dto.*;
import com.onnorokom.okr.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
// @PreAuthorize("hasRole('SUPER_ADMIN')") // In real security config we need to
// map roles correctly.
// For now, we'll verify permissions manually or via config.
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/users")
    public List<UserDto> getAllUsers() {
        return adminService.getAllUsers();
    }

    @PostMapping("/users")
    public UserDto createUser(@RequestBody CreateUserRequest request) {
        return adminService.createUser(request);
    }

    @PatchMapping("/users/{id}/block")
    public UserDto blockUser(@PathVariable UUID id, @RequestParam boolean blocked) {
        return adminService.blockUser(id, blocked);
    }

    @GetMapping("/teams")
    public List<TeamDto> getAllTeams() {
        return adminService.getAllTeams();
    }

    @PostMapping("/teams")
    public TeamDto createTeam(@RequestBody CreateTeamRequest request) {
        return adminService.createTeam(request);
    }

    @GetMapping("/departments")
    public List<DepartmentDto> getAllDepartments() {
        return adminService.getAllDepartments();
    }

    @PostMapping("/departments")
    public DepartmentDto createDepartment(@RequestBody CreateDepartmentRequest request) {
        return adminService.createDepartment(request);
    }

    @Autowired
    private com.onnorokom.okr.service.CycleService cycleService;

    @GetMapping("/cycles")
    public List<CycleDto> getAllCycles() {
        return cycleService.getAllCycles();
    }

    @PostMapping("/cycles")
    public CycleDto createCycle(@RequestBody CreateCycleRequest request) {
        return cycleService.createCycle(request);
    }
}
