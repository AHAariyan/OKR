package com.onnorokom.okr.controller;

import com.onnorokom.okr.dto.KeyResultDto;
import com.onnorokom.okr.dto.UpdateKeyResultRequest;
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
@RequestMapping("/api/key-results")
public class KeyResultController {

    @Autowired
    private OkrService okrService;

    @Autowired
    private UserRepository userRepository;

    @PatchMapping("/{id}")
    public KeyResultDto updateKeyResult(@PathVariable UUID id, @RequestBody UpdateKeyResultRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User actor = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        return okrService.updateKeyResult(id, request, actor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKeyResult(@PathVariable UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User actor = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        okrService.deleteKeyResult(id, actor);
        return ResponseEntity.noContent().build();
    }
}
