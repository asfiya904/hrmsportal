package com.hireconnect.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hireconnect.dto.request.LoginRequest;
import com.hireconnect.dto.response.ApiResponse;
import com.hireconnect.dto.response.AuthResponse;
import com.hireconnect.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(
	    origins = {"http://localhost:5173", "http://localhost:3000"},
	    allowCredentials = "true"
	)

@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response) {

        AuthResponse auth = authService.authenticate(request);

        ResponseCookie cookie = ResponseCookie.from("access_token", auth.getToken())
                .httpOnly(true)
                .secure(true) 
                .path("/")
                .maxAge(24 * 60 * 60)
                .sameSite("None")  
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

 
        auth.setToken(null);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", auth)
        );
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {

        ResponseCookie deleteCookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)          // MUST match login
                .path("/")              // MUST match login
                .sameSite("None")        // MUST match login
                .maxAge(0)              // 🔥 deletes cookie on logout
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getCurrentUser() {
        try {
            var user = authService.getCurrentUser();
            return ResponseEntity.ok(
                    ApiResponse.success("User fetched", user)
            );
        } catch (Exception e) {
            return ResponseEntity.status(401).body(
                    ApiResponse.error("Unauthorized")
            );
        }
    }

}
