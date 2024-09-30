package com.viewdash.controller;

import com.viewdash.document.DTO.LoginUserRequestDTO;
import com.viewdash.document.DTO.RegisterUserRequestDTO;
import com.viewdash.document.User;
import com.viewdash.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> signIn(@RequestBody LoginUserRequestDTO loginUserRequestDTO) {
        return authService.login(loginUserRequestDTO.email(), loginUserRequestDTO.password());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUserRequestDTO registerUserRequestDTO) {
        return authService.register(registerUserRequestDTO);
    }

    @PostMapping("/define-password")
    public ResponseEntity<?> definePassword(@RequestBody Map<String, String> json, @AuthenticationPrincipal User user) {
       return authService.definePassword(json.get("password"), user);
    }

}
