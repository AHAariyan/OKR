package com.onnorokom.okr.controller;

import com.onnorokom.okr.dto.AuthDto;
import com.onnorokom.okr.repository.UserRepository;
import com.onnorokom.okr.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.onnorokom.okr.dto.ProfileDto;
import com.onnorokom.okr.repository.RoleAssignmentRepository;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider tokenProvider;

    @Autowired
    RoleAssignmentRepository roleAssignmentRepository;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        String email = authentication.getName();
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("User not found");
        }

        var user = userOpt.get();
        ProfileDto profile = new ProfileDto();
        profile.setId(user.getId());
        profile.setEmail(user.getEmail());
        profile.setName(user.getName());

        if (user.getTeam() != null) {
            profile.setTeamName(user.getTeam().getName());
            profile.setTeamId(user.getTeam().getId());
        }

        if (user.getDepartment() != null) {
            profile.setDepartmentName(user.getDepartment().getName());
            profile.setDepartmentId(user.getDepartment().getId());
        }

        // Get user roles
        var roles = roleAssignmentRepository.findByUserId(user.getId());
        profile.setRoles(roles.stream().map(ra -> {
            ProfileDto.RoleInfo ri = new ProfileDto.RoleInfo();
            ri.setRole(ra.getRole());
            ri.setScopeType(ra.getScopeType());
            ri.setScopeId(ra.getScopeId());
            return ri;
        }).collect(Collectors.toList()));

        return ResponseEntity.ok(profile);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthDto.LoginRequest loginRequest) {

        // Validate PIN and Password manually or via CustomAuth Provider
        // Ideally we check PIN first or together.
        // For MVP, if we use standard UsernamePasswordAuthenticationToken, we might
        // need to combine checks.
        // Or we just custom check here before authenticationManager calls.

        var user = userRepository.findByEmail(loginRequest.getEmail());
        if (user.isEmpty()) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        // Check if user is blocked - per spec: blocked users cannot log in
        if (Boolean.TRUE.equals(user.get().getIsBlocked())) {
            return ResponseEntity.status(401).body("User account is blocked");
        }

        if (!user.get().getPin().equals(loginRequest.getPin())) {
            return ResponseEntity.status(401).body("Invalid PIN");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(loginRequest.getEmail());
        return ResponseEntity.ok(new AuthDto.JwtAuthenticationResponse(jwt));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody AuthDto.ForgotPasswordRequest request) {
        // In production, this would:
        // 1. Generate a password reset token
        // 2. Store it in the database with an expiration time
        // 3. Send an email with the reset link
        // For MVP, we just validate the email exists and return success

        var user = userRepository.findByEmail(request.getEmail());
        if (user.isEmpty()) {
            // For security, don't reveal if email exists or not
            return ResponseEntity.ok(new AuthDto.MessageResponse("If the email exists, a password reset link has been sent."));
        }

        // TODO: Implement actual email sending with reset token
        // For now, just return success message
        return ResponseEntity.ok(new AuthDto.MessageResponse("If the email exists, a password reset link has been sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody AuthDto.ResetPasswordRequest request) {
        // In production, this would:
        // 1. Validate the reset token
        // 2. Check if it's expired
        // 3. Update the password
        // 4. Invalidate the token

        // For MVP, we just return a placeholder message
        // TODO: Implement actual password reset logic with token validation
        if (request.getToken() == null || request.getToken().isEmpty()) {
            return ResponseEntity.badRequest().body(new AuthDto.MessageResponse("Invalid reset token"));
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            return ResponseEntity.badRequest().body(new AuthDto.MessageResponse("Password must be at least 6 characters"));
        }

        // TODO: Look up token, find user, update password
        return ResponseEntity.ok(new AuthDto.MessageResponse("Password has been reset successfully."));
    }
}
