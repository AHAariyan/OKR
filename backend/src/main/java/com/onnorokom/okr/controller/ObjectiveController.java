package com.onnorokom.okr.controller;

import com.onnorokom.okr.dto.CreateKeyResultRequest;
import com.onnorokom.okr.dto.KeyResultDto;
import com.onnorokom.okr.dto.ObjectiveDto;
import com.onnorokom.okr.dto.UpdateObjectiveRequest;
import com.onnorokom.okr.model.User;
import com.onnorokom.okr.repository.UserRepository;
import com.onnorokom.okr.service.OkrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/objectives")
public class ObjectiveController {

    @Autowired
    private OkrService okrService;

    @Autowired
    private UserRepository userRepository;

    @PatchMapping("/{id}")
    public ObjectiveDto updateObjective(@PathVariable UUID id, @RequestBody UpdateObjectiveRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User actor = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        return okrService.updateObjective(id, request, actor);
    }

    @PostMapping("/{id}/key-results")
    public KeyResultDto createKeyResult(@PathVariable UUID id, @RequestBody CreateKeyResultRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User actor = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        return okrService.createKeyResult(id, request, actor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteObjective(@PathVariable UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User actor = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        okrService.deleteObjective(id, actor);
        return ResponseEntity.noContent().build();
    }
}
