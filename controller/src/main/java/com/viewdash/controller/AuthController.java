package com.viewdash.controller;

import com.viewdash.document.DTO.LoginUserRequestDTO;
import com.viewdash.document.DTO.RegisterUserRequestDTO;
import com.viewdash.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
