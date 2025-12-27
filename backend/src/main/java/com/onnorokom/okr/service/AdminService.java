package com.onnorokom.okr.service;

import com.onnorokom.okr.dto.*;
import java.util.List;
import java.util.UUID;

public interface AdminService {
    List<UserDto> getAllUsers();

    UserDto createUser(CreateUserRequest request);

    UserDto blockUser(UUID userId, boolean blocked);

    List<TeamDto> getAllTeams();

    TeamDto createTeam(CreateTeamRequest request);

    List<DepartmentDto> getAllDepartments();

    DepartmentDto createDepartment(CreateDepartmentRequest request);
}
