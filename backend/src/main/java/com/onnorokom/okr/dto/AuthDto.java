package com.onnorokom.okr.dto;

import lombok.Data;

public class AuthDto {
    @Data
    public static class LoginRequest {
        private String email;
        private String pin;
        private String password;
    }

    @Data
    public static class JwtAuthenticationResponse {
        private String accessToken;
        private String tokenType = "Bearer";

        public JwtAuthenticationResponse(String accessToken) {
            this.accessToken = accessToken;
        }
    }

    @Data
    public static class ForgotPasswordRequest {
        private String email;
    }

    @Data
    public static class ResetPasswordRequest {
        private String token;
        private String newPassword;
    }

    @Data
    public static class MessageResponse {
        private String message;

        public MessageResponse(String message) {
            this.message = message;
        }
    }
}
