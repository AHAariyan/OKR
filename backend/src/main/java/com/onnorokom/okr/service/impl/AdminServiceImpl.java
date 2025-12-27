package com.onnorokom.okr.service.impl;

import com.onnorokom.okr.dto.*;
import com.onnorokom.okr.model.*;
import com.onnorokom.okr.repository.*;
import com.onnorokom.okr.service.AdminService;
import com.onnorokom.okr.service.CycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private RoleAssignmentRepository roleAssignmentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private OkrSheetRepository okrSheetRepository;
    @Autowired
    private CycleService cycleService;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapUser).collect(Collectors.toList());
    }

    private UserDto mapUser(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setTeamName(user.getTeam() != null ? user.getTeam().getName() : "-");
        dto.setDepartmentName(user.getDepartment() != null ? user.getDepartment().getName() : "-");
        dto.setBlocked(user.getIsBlocked() != null && user.getIsBlocked());
        return dto;
    }

    private Company getOrCreateDefaultCompany() {
        List<Company> companies = companyRepository.findAll();
        if (!companies.isEmpty()) {
            return companies.get(0);
        }
        Company c = new Company();
        c.setName("Onnorokom");
        return companyRepository.save(c);
    }

    @Override
    @Transactional
    public UserDto createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setPin(request.getPin());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // Assign default company to user as well if needed? User entity usually has
        // company link?
        // Let's check User model later if 500 persists. But usually User -> Team ->
        // Company is likely path, or User -> Company directly.
        // Assuming User table might need company too.

        if (request.getTeamId() != null) {
            Team team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new RuntimeException("Team not found"));
            user.setTeam(team);
        }

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Dept not found"));
            user.setDepartment(dept);
        }

        user = userRepository.save(user);

        // Assign Role
        RoleAssignment ra = new RoleAssignment();
        ra.setUser(user);
        ra.setRole(request.getRole()); // e.g. MEMBER, SUPER_ADMIN

        // Set scope based on role type
        String role = request.getRole();
        if ("DEPARTMENT_ADMIN".equals(role) && user.getDepartment() != null) {
            ra.setScopeType("DEPARTMENT");
            ra.setScopeId(user.getDepartment().getId());
        } else if ("TEAM_ADMIN".equals(role) && user.getTeam() != null) {
            ra.setScopeType("TEAM");
            ra.setScopeId(user.getTeam().getId());
        } else if ("SUPER_ADMIN".equals(role) || "ORG_LEADERSHIP".equals(role)) {
            // Get or create default company
            Company company = getOrCreateDefaultCompany();
            ra.setScopeType("COMPANY");
            ra.setScopeId(company.getId());
        } else {
            // Default: PERSONAL scope for MEMBER
            ra.setScopeType("PERSONAL");
            ra.setScopeId(user.getId());
        }
        roleAssignmentRepository.save(ra);

        // Auto-create personal OKR sheet for the active cycle
        Optional<Cycle> activeCycle = cycleService.getActiveCycle();
        if (activeCycle.isPresent()) {
            OkrSheet personalSheet = new OkrSheet();
            personalSheet.setCycle(activeCycle.get());
            personalSheet.setScopeType("PERSONAL");
            personalSheet.setScopeId(user.getId());
            personalSheet.setComputedOverallProgress(0.0);
            okrSheetRepository.save(personalSheet);
        }

        return mapUser(user);
    }

    @Override
    public List<TeamDto> getAllTeams() {
        return teamRepository.findAll().stream().map(t -> {
            TeamDto dto = new TeamDto();
            dto.setId(t.getId());
            dto.setName(t.getName());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TeamDto createTeam(CreateTeamRequest request) {
        Team team = new Team();
        team.setName(request.getName());
        team.setCompany(getOrCreateDefaultCompany());
        team = teamRepository.save(team);

        TeamDto dto = new TeamDto();
        dto.setId(team.getId());
        dto.setName(team.getName());
        return dto;
    }

    @Override
    @Transactional
    public UserDto blockUser(java.util.UUID userId, boolean blocked) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsBlocked(blocked);
        userRepository.save(user);
        return mapUser(user);
    }

    @Override
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream().map(d -> {
            DepartmentDto dto = new DepartmentDto();
            dto.setId(d.getId());
            dto.setName(d.getName());
            dto.setTeamId(d.getTeam().getId());
            dto.setTeamName(d.getTeam().getName());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DepartmentDto createDepartment(CreateDepartmentRequest request) {
        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found"));

        Department dept = new Department();
        dept.setName(request.getName());
        dept.setTeam(team);
        dept = departmentRepository.save(dept);

        DepartmentDto dto = new DepartmentDto();
        dto.setId(dept.getId());
        dto.setName(dept.getName());
        dto.setTeamId(team.getId());
        dto.setTeamName(team.getName());
        return dto;
    }
}
