package com.onnorokom.okr.controller;

import com.onnorokom.okr.dto.DashboardDto;
import com.onnorokom.okr.security.CustomUserDetailsService;
import com.onnorokom.okr.service.OkrService;
import com.onnorokom.okr.model.User;
import com.onnorokom.okr.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private OkrService okrService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public DashboardDto getDashboard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        return okrService.getDashboard(user);
    }
}
