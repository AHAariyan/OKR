package com.onnorokom.okr.service;

import com.onnorokom.okr.dto.*;
import java.util.List;
import java.util.UUID;

public interface AdminService {
    List<UserDto> getAllUsers();

    UserDto createUser(CreateUserRequest request);

    UserDto blockUser(UUID userId, boolean blocked);

    void deleteUser(UUID userId);

    List<TeamDto> getAllTeams();

    TeamDto createTeam(CreateTeamRequest request);

    void deleteTeam(UUID teamId);

    List<DepartmentDto> getAllDepartments();

    DepartmentDto createDepartment(CreateDepartmentRequest request);

    void deleteDepartment(UUID departmentId);
}
