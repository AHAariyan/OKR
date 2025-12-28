package com.onnorokom.okr.service.impl;

import com.onnorokom.okr.model.*;
import com.onnorokom.okr.repository.DepartmentRepository;
import com.onnorokom.okr.repository.RoleAssignmentRepository;
import com.onnorokom.okr.repository.UserRepository;
import com.onnorokom.okr.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Permission Service Implementation
 *
 * Role Hierarchy (from spec.md):
 * - SUPER_ADMIN: can do anything/everything across the company
 * - ORG_LEADERSHIP: company-wide view-only (Chairman/CEO/MD/CTO)
 * - TEAM_ADMIN: manages own team OKR + department OKRs under that team + personal OKRs of team members
 * - DEPARTMENT_ADMIN: manages own department OKR + personal OKR; can view company+team read-only
 * - MEMBER: manages personal OKR + department OKR; can view company+team read-only
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private RoleAssignmentRepository roleAssignmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public boolean canEditSheet(User actor, OkrSheet sheet) {
        List<RoleAssignment> roles = roleAssignmentRepository.findByUserId(actor.getId());

        // 1. SUPER_ADMIN can edit anything
        if (hasRole(roles, "SUPER_ADMIN")) {
            return true;
        }

        // 2. ORG_LEADERSHIP is view-only - cannot edit anything
        if (hasRole(roles, "ORG_LEADERSHIP")) {
            return false;
        }

        switch (sheet.getScopeType()) {
            case "COMPANY":
                // Only Super Admin can edit company sheets (already handled above)
                return false;

            case "TEAM":
                // TEAM_ADMIN can edit their own team's OKR
                return roles.stream()
                        .anyMatch(r -> "TEAM_ADMIN".equals(r.getRole())
                                && "TEAM".equals(r.getScopeType())
                                && Objects.equals(r.getScopeId(), sheet.getScopeId()));

            case "DEPARTMENT":
                UUID deptId = sheet.getScopeId();

                // DEPARTMENT_ADMIN can edit their department's OKR
                if (roles.stream().anyMatch(r -> "DEPARTMENT_ADMIN".equals(r.getRole())
                        && "DEPARTMENT".equals(r.getScopeType())
                        && Objects.equals(r.getScopeId(), deptId))) {
                    return true;
                }

                // TEAM_ADMIN can edit department OKRs if the department is under their team
                Department dept = departmentRepository.findById(deptId).orElse(null);
                if (dept != null && dept.getTeam() != null) {
                    UUID deptTeamId = dept.getTeam().getId();
                    if (roles.stream().anyMatch(r -> "TEAM_ADMIN".equals(r.getRole())
                            && "TEAM".equals(r.getScopeType())
                            && Objects.equals(r.getScopeId(), deptTeamId))) {
                        return true;
                    }
                }

                // MEMBER can edit their own department's OKR (per spec: "manages personal OKR + department OKR")
                if (actor.getDepartment() != null && Objects.equals(actor.getDepartment().getId(), deptId)) {
                    // Check if actor is a MEMBER of this department
                    if (roles.stream().anyMatch(r -> "MEMBER".equals(r.getRole()))) {
                        return true;
                    }
                }

                return false;

            case "PERSONAL":
                UUID ownerUserId = sheet.getScopeId();

                // Owner can always edit their own personal OKR
                if (Objects.equals(actor.getId(), ownerUserId)) {
                    return true;
                }

                // Get the owner to check their team/department
                var ownerOpt = userRepository.findById(ownerUserId);
                if (ownerOpt.isEmpty()) {
                    return false;
                }
                User owner = ownerOpt.get();

                // TEAM_ADMIN can edit personal OKRs of members in their team
                if (owner.getTeam() != null) {
                    UUID ownerTeamId = owner.getTeam().getId();
                    if (roles.stream().anyMatch(r -> "TEAM_ADMIN".equals(r.getRole())
                            && "TEAM".equals(r.getScopeType())
                            && Objects.equals(r.getScopeId(), ownerTeamId))) {
                        return true;
                    }
                }

                // DEPARTMENT_ADMIN can edit personal OKRs of members in their department
                if (owner.getDepartment() != null) {
                    UUID ownerDeptId = owner.getDepartment().getId();
                    if (roles.stream().anyMatch(r -> "DEPARTMENT_ADMIN".equals(r.getRole())
                            && "DEPARTMENT".equals(r.getScopeType())
                            && Objects.equals(r.getScopeId(), ownerDeptId))) {
                        return true;
                    }
                }

                return false;

            default:
                return false;
        }
    }

    @Override
    public boolean canEditKeyResult(User actor, KeyResult kr) {
        // 1. Owner of KR can always edit
        if (kr.getOwner() != null && Objects.equals(kr.getOwner().getId(), actor.getId())) {
            return true;
        }

        // 2. Fallback to Sheet permissions
        return canEditSheet(actor, kr.getObjective().getSheet());
    }

    @Override
    public boolean isSuperAdmin(User actor) {
        List<RoleAssignment> roles = roleAssignmentRepository.findByUserId(actor.getId());
        return hasRole(roles, "SUPER_ADMIN");
    }

    @Override
    public boolean isOrgLeadership(User actor) {
        List<RoleAssignment> roles = roleAssignmentRepository.findByUserId(actor.getId());
        return hasRole(roles, "ORG_LEADERSHIP");
    }

    private boolean hasRole(List<RoleAssignment> roles, String roleName) {
        return roles.stream().anyMatch(r -> roleName.equals(r.getRole()));
    }
}
