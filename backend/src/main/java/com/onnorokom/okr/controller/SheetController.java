package com.onnorokom.okr.controller;

import com.onnorokom.okr.dto.CreateObjectiveRequest;
import com.onnorokom.okr.dto.ObjectiveDto;
import com.onnorokom.okr.dto.SheetDetailDto;
import com.onnorokom.okr.model.User;
import com.onnorokom.okr.repository.UserRepository;
import com.onnorokom.okr.service.OkrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/sheets")
public class SheetController {

    @Autowired
    private OkrService okrService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{id}")
    public SheetDetailDto getSheet(@PathVariable UUID id) {
        return okrService.getSheetDetails(id);
    }

    @PostMapping("/{id}/objectives")
    public ObjectiveDto createObjective(@PathVariable UUID id, @RequestBody CreateObjectiveRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User actor = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        return okrService.createObjective(id, request, actor);
    }
}
