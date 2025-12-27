package com.onnorokom.okr.service.impl;

import com.onnorokom.okr.dto.*;
import com.onnorokom.okr.model.*;
import com.onnorokom.okr.repository.*;
import com.onnorokom.okr.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OkrServiceImpl implements OkrService {

    @Autowired
    private OkrSheetRepository sheetRepository;
    @Autowired
    private ObjectiveRepository objectiveRepository;
    @Autowired
    private KeyResultRepository krRepository;
    @Autowired
    private CycleService cycleService;
    @Autowired
    private CalculationService calculationService;
    @Autowired
    private AuditService auditService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
    @Autowired
    private PermissionService permissionService;

    @Override
    public DashboardDto getDashboard(User user) {
        Optional<Cycle> cycleOpt = cycleService.getActiveCycle();
        if (cycleOpt.isEmpty()) {
            return new DashboardDto(); // Or empty list
        }
        Cycle cycle = cycleOpt.get();
        UUID cycleId = cycle.getId();

        // Spec: Dashboard card order: Company -> Team(s) -> Department -> Personal

        List<SheetSummaryDto> summaries = new ArrayList<>();

        // 1. Company
        List<OkrSheet> companySheets = sheetRepository.findByCycleIdAndScopeType(cycleId, "COMPANY");
        companySheets.forEach(s -> summaries.add(mapToSummary(s, "Company", cycle)));

        // 2. Teams (Permitted)
        // For MVP, showing User's Team. RBAC later for "permitted".
        // If user has a team, show it.
        if (user.getTeam() != null) {
            List<OkrSheet> teamSheets = sheetRepository.findByCycleIdAndScopeTypeAndScopeId(cycleId, "TEAM",
                    user.getTeam().getId());
            teamSheets.forEach(s -> summaries.add(mapToSummary(s, user.getTeam().getName(), cycle)));
        }

        // 3. Department
        if (user.getDepartment() != null) {
            List<OkrSheet> deptSheets = sheetRepository.findByCycleIdAndScopeTypeAndScopeId(cycleId, "DEPARTMENT",
                    user.getDepartment().getId());
            deptSheets.forEach(s -> summaries.add(mapToSummary(s, user.getDepartment().getName(), cycle)));
        }

        // 4. Personal
        List<OkrSheet> personalSheets = sheetRepository.findByCycleIdAndScopeTypeAndScopeId(cycleId, "PERSONAL",
                user.getId());
        personalSheets.forEach(s -> summaries.add(mapToSummary(s, "My OKRs", cycle)));

        DashboardDto dashboard = new DashboardDto();
        dashboard.setSheets(summaries);
        return dashboard;
    }

    private SheetSummaryDto mapToSummary(OkrSheet sheet, String titleFallback, Cycle cycle) {
        SheetSummaryDto dto = new SheetSummaryDto();
        dto.setId(sheet.getId());
        dto.setScopeType(sheet.getScopeType());
        dto.setScopeId(sheet.getScopeId());

        // Resolve title dynamically if needed, but fallback is good for now
        // Ideally we fetch name from DB if not passed, but for optimization we passed
        // it.
        dto.setTitle(titleFallback);

        dto.setComputedOverallProgress(sheet.getComputedOverallProgress());
        dto.setComputedTimeProgress(cycleService.calculateTimeProgress(cycle));
        dto.setDaysRemaining(cycleService.calculateDaysRemaining(cycle));
        dto.setTotalDays(cycleService.calculateTotalDays(cycle));

        // Get Top Objectives for preview (e.g. 3)
        List<Objective> objs = objectiveRepository.findBySheetIdOrderBySortOrderAsc(sheet.getId());
        List<SheetSummaryDto.ObjectiveSummaryDto> objSummaries = objs.stream().limit(3).map(o -> {
            SheetSummaryDto.ObjectiveSummaryDto os = new SheetSummaryDto.ObjectiveSummaryDto();
            os.setTitle(o.getTitle());
            os.setProgress(o.getComputedProgress());
            return os;
        }).collect(Collectors.toList());
        dto.setObjectives(objSummaries);

        return dto;
    }

    @Override
    public SheetDetailDto getSheetDetails(UUID sheetId) {
        OkrSheet sheet = sheetRepository.findById(sheetId).orElseThrow(() -> new RuntimeException("Sheet not found"));
        Cycle cycle = sheet.getCycle();

        // Determine current user from security context to check permissions
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        SheetDetailDto dto = new SheetDetailDto();
        dto.setId(sheet.getId());
        dto.setCycleName(cycle.getName());
        dto.setStartDate(cycle.getStartDate());
        dto.setEndDate(cycle.getEndDate());
        dto.setComputedOverallProgress(sheet.getComputedOverallProgress());
        double timeProg = cycleService.calculateTimeProgress(cycle);
        dto.setComputedTimeProgress(timeProg);
        dto.setDaysRemaining(cycleService.calculateDaysRemaining(cycle));

        // Resolving Title
        String title = "OKR Sheet";
        if ("COMPANY".equals(sheet.getScopeType()))
            title = "Company"; // Name fetched via ID ideally
        else if ("TEAM".equals(sheet.getScopeType()))
            title = teamRepository.findById(sheet.getScopeId()).map(Team::getName).orElse("Team");
        else if ("DEPARTMENT".equals(sheet.getScopeType()))
            title = departmentRepository.findById(sheet.getScopeId()).map(Department::getName).orElse("Department");
        else if ("PERSONAL".equals(sheet.getScopeType()))
            title = userRepository.findById(sheet.getScopeId())
                    .map(u -> u.getName() != null ? u.getName() : u.getEmail())
                    .orElse("Personal");
        dto.setTitle(title);
        dto.setScopeType(sheet.getScopeType());
        dto.setScopeId(sheet.getScopeId());

        // Objectives
        List<Objective> objectives = objectiveRepository.findBySheetIdOrderBySortOrderAsc(sheetId);
        dto.setObjectives(objectives.stream().map(obj -> mapObjective(obj, currentUser)).collect(Collectors.toList()));

        return dto;
    }

    private ObjectiveDto mapObjective(Objective obj, User currentUser) {
        ObjectiveDto dto = new ObjectiveDto();
        dto.setId(obj.getId());
        dto.setTitle(obj.getTitle());
        dto.setOwnerName(obj.getOwner() != null ? obj.getOwner().getEmail() : null);
        dto.setComputedProgress(obj.getComputedProgress());
        dto.setSortOrder(obj.getSortOrder());

        List<KeyResult> krs = krRepository.findByObjectiveIdOrderBySortOrderAsc(obj.getId());
        dto.setKeyResults(krs.stream().map(kr -> mapKeyResult(kr, currentUser)).collect(Collectors.toList()));
        return dto;
    }

    private KeyResultDto mapKeyResult(KeyResult kr, User currentUser) {
        KeyResultDto dto = new KeyResultDto();
        dto.setId(kr.getId());
        dto.setTitle(kr.getTitle());
        dto.setStartValue(kr.getStartValue());
        dto.setTargetValue(kr.getTargetValue());
        dto.setCurrentValue(kr.getCurrentValue());
        dto.setComputedProgress(kr.getComputedProgress());
        dto.setOwnerName(kr.getOwner() != null ? kr.getOwner().getEmail() : null);
        dto.setConfidenceLevel(kr.getConfidenceLevel());
        dto.setDeadline(kr.getDeadline());
        dto.setAlignedProjects(kr.getAlignedProjects());
        dto.setComments(kr.getComments());
        dto.setSortOrder(kr.getSortOrder());

        dto.setCanEdit(permissionService.canEditKeyResult(currentUser, kr));

        return dto;
    }

    @Override
    @Transactional
    public KeyResultDto updateKeyResult(UUID krId, UpdateKeyResultRequest request, User actor) {
        KeyResult kr = krRepository.findById(krId).orElseThrow(() -> new RuntimeException("KR not found"));

        if (!permissionService.canEditKeyResult(actor, kr)) {
            throw new RuntimeException("Access Denied: You cannot edit this Key Result.");
        }

        // Check for changes and Audit
        checkAndAudit(actor, "KEY_RESULT", kr.getId(), "current_value", String.valueOf(kr.getCurrentValue()),
                String.valueOf(request.getCurrentValue()));
        checkAndAudit(actor, "KEY_RESULT", kr.getId(), "confidence_level", kr.getConfidenceLevel(),
                request.getConfidenceLevel());
        checkAndAudit(actor, "KEY_RESULT", kr.getId(), "comments", kr.getComments(), request.getComments());
        // ... more fields

        if (request.getCurrentValue() != null)
            kr.setCurrentValue(request.getCurrentValue());
        if (request.getConfidenceLevel() != null)
            kr.setConfidenceLevel(request.getConfidenceLevel());
        if (request.getComments() != null)
            kr.setComments(request.getComments());
        if (request.getTitle() != null)
            kr.setTitle(request.getTitle());

        // Recalculate KR Progress
        double newProgress = calculationService.calculateKrProgress(kr.getStartValue(), kr.getTargetValue(),
                kr.getCurrentValue());
        kr.setComputedProgress(newProgress);
        krRepository.save(kr);

        // Rollup
        rollupObjective(kr.getObjective());

        return mapKeyResult(kr, actor);
    }

    private void rollupObjective(Objective obj) {
        List<KeyResult> krs = krRepository.findByObjectiveIdOrderBySortOrderAsc(obj.getId());
        double objProgress = calculationService.calculateObjectiveProgress(krs);
        obj.setComputedProgress(objProgress);
        objectiveRepository.save(obj);

        rollupSheet(obj.getSheet());
    }

    private void rollupSheet(OkrSheet sheet) {
        List<Objective> objs = objectiveRepository.findBySheetIdOrderBySortOrderAsc(sheet.getId());
        double sheetProgress = calculationService.calculateSheetProgress(objs);
        sheet.setComputedOverallProgress(sheetProgress);
        sheetRepository.save(sheet);
    }

    private void checkAndAudit(User actor, String type, UUID id, String field, String oldVal, String newVal) {
        // Simple string comparison. Handling nulls safely.
        String o = oldVal == null ? "" : oldVal;
        String n = newVal == null ? "" : newVal;
        if (!o.equals(n)) {
            auditService.logChange(actor, type, id, field, oldVal, newVal);
        }
    }

    @Override
    @Transactional
    public ObjectiveDto updateObjective(UUID objectiveId, UpdateObjectiveRequest request, User actor) {
        Objective obj = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new RuntimeException("Objective not found"));

        // Check permissions via the sheet
        if (!permissionService.canEditSheet(actor, obj.getSheet())) {
            throw new RuntimeException("Access Denied: You cannot edit this Objective.");
        }

        // Audit and update title
        if (request.getTitle() != null) {
            checkAndAudit(actor, "OBJECTIVE", obj.getId(), "title", obj.getTitle(), request.getTitle());
            obj.setTitle(request.getTitle());
        }

        // Audit and update owner
        if (request.getOwnerUserId() != null) {
            String oldOwner = obj.getOwner() != null ? obj.getOwner().getId().toString() : null;
            checkAndAudit(actor, "OBJECTIVE", obj.getId(), "owner_user_id", oldOwner, request.getOwnerUserId().toString());

            User newOwner = userRepository.findById(request.getOwnerUserId())
                    .orElseThrow(() -> new RuntimeException("Owner user not found"));
            obj.setOwner(newOwner);
        }

        objectiveRepository.save(obj);

        return mapObjective(obj, actor);
    }

    @Override
    @Transactional
    public ObjectiveDto createObjective(UUID sheetId, CreateObjectiveRequest request, User actor) {
        OkrSheet sheet = sheetRepository.findById(sheetId)
                .orElseThrow(() -> new RuntimeException("Sheet not found"));

        if (!permissionService.canEditSheet(actor, sheet)) {
            throw new RuntimeException("Access Denied: You cannot add objectives to this sheet.");
        }

        // Get max sort order
        List<Objective> existingObjs = objectiveRepository.findBySheetIdOrderBySortOrderAsc(sheetId);
        int nextSortOrder = existingObjs.isEmpty() ? 1 : existingObjs.get(existingObjs.size() - 1).getSortOrder() + 1;

        Objective obj = new Objective();
        obj.setSheet(sheet);
        obj.setTitle(request.getTitle());
        obj.setOwner(actor);
        obj.setSortOrder(nextSortOrder);
        obj.setComputedProgress(0.0);

        obj = objectiveRepository.save(obj);

        auditService.logChange(actor, "OBJECTIVE", obj.getId(), "created", null, request.getTitle());

        return mapObjective(obj, actor);
    }

    @Override
    @Transactional
    public KeyResultDto createKeyResult(UUID objectiveId, CreateKeyResultRequest request, User actor) {
        Objective obj = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new RuntimeException("Objective not found"));

        if (!permissionService.canEditSheet(actor, obj.getSheet())) {
            throw new RuntimeException("Access Denied: You cannot add key results to this objective.");
        }

        // Get max sort order
        List<KeyResult> existingKrs = krRepository.findByObjectiveIdOrderBySortOrderAsc(objectiveId);
        int nextSortOrder = existingKrs.isEmpty() ? 1 : existingKrs.get(existingKrs.size() - 1).getSortOrder() + 1;

        KeyResult kr = new KeyResult();
        kr.setObjective(obj);
        kr.setTitle(request.getTitle());
        kr.setStartValue(request.getStartValue() != null ? request.getStartValue() : 0.0);
        kr.setTargetValue(request.getTargetValue() != null ? request.getTargetValue() : 100.0);
        kr.setCurrentValue(kr.getStartValue());
        kr.setComputedProgress(0.0);
        kr.setOwner(actor);
        kr.setConfidenceLevel("ON_TRACK");
        kr.setDeadline(request.getDeadline());
        kr.setSortOrder(nextSortOrder);

        kr = krRepository.save(kr);

        auditService.logChange(actor, "KEY_RESULT", kr.getId(), "created", null, request.getTitle());

        return mapKeyResult(kr, actor);
    }

    @Override
    @Transactional
    public void deleteObjective(UUID objectiveId, User actor) {
        Objective obj = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new RuntimeException("Objective not found"));

        if (!permissionService.canEditSheet(actor, obj.getSheet())) {
            throw new RuntimeException("Access Denied: You cannot delete this objective.");
        }

        OkrSheet sheet = obj.getSheet();

        // Delete all key results first
        List<KeyResult> krs = krRepository.findByObjectiveIdOrderBySortOrderAsc(objectiveId);
        krRepository.deleteAll(krs);

        auditService.logChange(actor, "OBJECTIVE", obj.getId(), "deleted", obj.getTitle(), null);

        objectiveRepository.delete(obj);

        // Rollup sheet progress
        rollupSheet(sheet);
    }

    @Override
    @Transactional
    public void deleteKeyResult(UUID krId, User actor) {
        KeyResult kr = krRepository.findById(krId)
                .orElseThrow(() -> new RuntimeException("Key Result not found"));

        if (!permissionService.canEditKeyResult(actor, kr)) {
            throw new RuntimeException("Access Denied: You cannot delete this key result.");
        }

        Objective obj = kr.getObjective();

        auditService.logChange(actor, "KEY_RESULT", kr.getId(), "deleted", kr.getTitle(), null);

        krRepository.delete(kr);

        // Rollup objective and sheet
        rollupObjective(obj);
    }
}
