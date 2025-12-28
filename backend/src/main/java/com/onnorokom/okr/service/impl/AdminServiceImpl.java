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
    @Autowired
    private ObjectiveRepository objectiveRepository;
    @Autowired
    private KeyResultRepository keyResultRepository;
    @Autowired
    private AuditLogRepository auditLogRepository;

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

        // Auto-create Team OKR sheet for the active cycle
        Optional<Cycle> activeCycle = cycleService.getActiveCycle();
        if (activeCycle.isPresent()) {
            OkrSheet teamSheet = new OkrSheet();
            teamSheet.setCycle(activeCycle.get());
            teamSheet.setScopeType("TEAM");
            teamSheet.setScopeId(team.getId());
            teamSheet.setComputedOverallProgress(0.0);
            okrSheetRepository.save(teamSheet);
        }

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
    @Transactional
    public void deleteUser(java.util.UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Nullify owner references in key results
        List<KeyResult> userKeyResults = keyResultRepository.findByOwnerId(userId);
        for (KeyResult kr : userKeyResults) {
            kr.setOwner(null);
            keyResultRepository.save(kr);
        }

        // Nullify owner references in objectives
        List<Objective> userObjectives = objectiveRepository.findByOwnerId(userId);
        for (Objective obj : userObjectives) {
            obj.setOwner(null);
            objectiveRepository.save(obj);
        }

        // Delete role assignments
        roleAssignmentRepository.deleteByUserId(userId);

        // Delete audit logs for this user
        auditLogRepository.deleteByActorId(userId);

        // Delete personal OKR sheets (with their objectives and key results)
        deleteOkrSheetsByScope("PERSONAL", userId);

        // Delete the user
        userRepository.delete(user);
    }

    /**
     * Helper method to delete OKR sheets and all their objectives/key results
     */
    private void deleteOkrSheetsByScope(String scopeType, java.util.UUID scopeId) {
        List<OkrSheet> sheets = okrSheetRepository.findAll().stream()
                .filter(s -> scopeType.equals(s.getScopeType()) && scopeId.equals(s.getScopeId()))
                .collect(Collectors.toList());

        for (OkrSheet sheet : sheets) {
            deleteOkrSheetContents(sheet.getId());
        }
        okrSheetRepository.deleteAll(sheets);
    }

    /**
     * Helper method to delete objectives and key results for a sheet
     */
    private void deleteOkrSheetContents(java.util.UUID sheetId) {
        List<Objective> objectives = objectiveRepository.findBySheetId(sheetId);
        for (Objective obj : objectives) {
            keyResultRepository.deleteByObjectiveId(obj.getId());
        }
        objectiveRepository.deleteBySheetId(sheetId);
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

        // Auto-create Department OKR sheet for the active cycle
        Optional<Cycle> activeCycle = cycleService.getActiveCycle();
        if (activeCycle.isPresent()) {
            OkrSheet deptSheet = new OkrSheet();
            deptSheet.setCycle(activeCycle.get());
            deptSheet.setScopeType("DEPARTMENT");
            deptSheet.setScopeId(dept.getId());
            deptSheet.setComputedOverallProgress(0.0);
            okrSheetRepository.save(deptSheet);
        }

        DepartmentDto dto = new DepartmentDto();
        dto.setId(dept.getId());
        dto.setName(dept.getName());
        dto.setTeamId(team.getId());
        dto.setTeamName(team.getName());
        return dto;
    }

    @Override
    @Transactional
    public void deleteTeam(java.util.UUID teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        // Unassign users from this team first (before deleting departments)
        List<User> teamUsers = userRepository.findByTeamId(teamId);
        for (User user : teamUsers) {
            user.setTeam(null);
            user.setDepartment(null); // Also clear department since it belongs to this team
            userRepository.save(user);
        }

        // Get all departments in this team and delete them with their OKR sheets
        List<Department> departments = departmentRepository.findByTeamId(teamId);
        for (Department dept : departments) {
            // Delete department OKR sheets with objectives and key results
            deleteOkrSheetsByScope("DEPARTMENT", dept.getId());
            // Delete role assignments for this department scope
            roleAssignmentRepository.deleteByScopeTypeAndScopeId("DEPARTMENT", dept.getId());
        }
        // Delete all departments in this team
        departmentRepository.deleteAll(departments);

        // Delete team OKR sheets with objectives and key results
        deleteOkrSheetsByScope("TEAM", teamId);

        // Delete role assignments for this team scope
        roleAssignmentRepository.deleteByScopeTypeAndScopeId("TEAM", teamId);

        // Delete the team
        teamRepository.delete(team);
    }

    @Override
    @Transactional
    public void deleteDepartment(java.util.UUID departmentId) {
        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found"));

        // Unassign users from this department
        List<User> deptUsers = userRepository.findByDepartmentId(departmentId);
        for (User user : deptUsers) {
            user.setDepartment(null);
            userRepository.save(user);
        }

        // Delete department OKR sheets with objectives and key results
        deleteOkrSheetsByScope("DEPARTMENT", departmentId);

        // Delete role assignments for this department scope
        roleAssignmentRepository.deleteByScopeTypeAndScopeId("DEPARTMENT", departmentId);

        // Delete the department
        departmentRepository.delete(dept);
    }
}
