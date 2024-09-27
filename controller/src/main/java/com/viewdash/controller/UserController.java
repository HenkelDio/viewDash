package com.viewdash.controller;

import com.viewdash.document.User;
import com.viewdash.security.CustomUserDetails;
import com.viewdash.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("users")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping
    public ResponseEntity<User> getUser(@AuthenticationPrincipal User principal, @RequestParam String document) {
        return userService.findUserByDocument(document, principal.getDocument());
    }

    @GetMapping("find-all")
    public ResponseEntity<List<User>> findAll(@AuthenticationPrincipal User principal) {
        return userService.findAllUsers(principal.getDocument());
    }

    @PostMapping("create-user")
    public ResponseEntity<User> createUser(@AuthenticationPrincipal User principal, @RequestBody User user) {
        return userService.createUser(user, principal.getDocument());
    }

    @PutMapping("update-user")
    public ResponseEntity<User> updateUser(@AuthenticationPrincipal User principal, @RequestBody User user) {
        return userService.updateUser(user, principal.getDocument());
    }
}
