package com.onnorokom.okr.service.impl;

import com.onnorokom.okr.model.*;
import com.onnorokom.okr.repository.RoleAssignmentRepository;
import com.onnorokom.okr.repository.UserRepository;
import com.onnorokom.okr.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private RoleAssignmentRepository roleAssignmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean canEditSheet(User actor, OkrSheet sheet) {
        // 1. Super Admins can edit anything
        List<RoleAssignment> roles = roleAssignmentRepository.findByUserId(actor.getId());
        if (roles.stream().anyMatch(r -> "SUPER_ADMIN".equals(r.getRole()))) {
            return true;
        }

        // 2. ORG_LEADERSHIP is view-only per spec - they cannot edit anything
        if (roles.stream().anyMatch(r -> "ORG_LEADERSHIP".equals(r.getRole()))) {
            return false;
        }

        switch (sheet.getScopeType()) {
            case "COMPANY":
                // Only Super Admin can edit company sheets (already handled above)
                return false;

            case "TEAM":
                // Team Admin can edit their team's OKR
                return roles.stream()
                        .anyMatch(r -> "TEAM_ADMIN".equals(r.getRole())
                                && "TEAM".equals(r.getScopeType())
                                && Objects.equals(r.getScopeId(), sheet.getScopeId()));

            case "DEPARTMENT":
                // Only Department Admin can edit their department's OKR
                // Regular members can only READ department OKR, not edit
                return roles.stream().anyMatch(r -> "DEPARTMENT_ADMIN".equals(r.getRole())
                        && "DEPARTMENT".equals(r.getScopeType())
                        && Objects.equals(r.getScopeId(), sheet.getScopeId()));

            case "PERSONAL":
                // Owner can edit their own personal OKR
                if (Objects.equals(actor.getId(), sheet.getScopeId())) {
                    return true;
                }

                // DEPARTMENT_ADMIN can edit personal OKRs of members in their department
                UUID ownerUserId = sheet.getScopeId();
                var ownerOpt = userRepository.findById(ownerUserId);
                if (ownerOpt.isPresent()) {
                    User owner = ownerOpt.get();

                    // Check if actor is DEPARTMENT_ADMIN of the owner's department
                    if (owner.getDepartment() != null) {
                        UUID ownerDeptId = owner.getDepartment().getId();
                        if (roles.stream().anyMatch(r -> "DEPARTMENT_ADMIN".equals(r.getRole())
                                && "DEPARTMENT".equals(r.getScopeType())
                                && Objects.equals(r.getScopeId(), ownerDeptId))) {
                            return true;
                        }
                    }

                    // Check if actor is TEAM_ADMIN of the owner's team
                    if (owner.getTeam() != null) {
                        UUID ownerTeamId = owner.getTeam().getId();
                        if (roles.stream().anyMatch(r -> "TEAM_ADMIN".equals(r.getRole())
                                && "TEAM".equals(r.getScopeType())
                                && Objects.equals(r.getScopeId(), ownerTeamId))) {
                            return true;
                        }
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
}
