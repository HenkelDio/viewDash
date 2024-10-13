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
import java.util.Map;

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
    public ResponseEntity<List<User>> findAll(@AuthenticationPrincipal User principal, @RequestParam("status") String status) {
        return userService.findAllUsers(principal.getDocument(),status);
    }

    @PostMapping("create-user")
    public ResponseEntity<User> createUser(@AuthenticationPrincipal User principal, @RequestBody User user) {
        return userService.createUser(user, principal.getDocument());
    }

    @PutMapping("update-user")
    public ResponseEntity<User> updateUser(@AuthenticationPrincipal User principal, @RequestBody User user) {
        return userService.updateUser(user, principal.getDocument());
    }

    @DeleteMapping("delete-user")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal User principal, @RequestParam("document") String document) {
        return userService.deleteUser(principal.getDocument(), document);
    }

    @DeleteMapping("disable-user")
    public ResponseEntity<?> disableUser(@AuthenticationPrincipal User principal, @RequestParam("document") String document) {
        return userService.disableUser(principal.getDocument(), document);
    }

    @PostMapping("reactivate-user")
    public ResponseEntity<?> reactivateUser(@AuthenticationPrincipal User principal, @RequestParam("document") String document) {
        return userService.reactivateUser(principal.getDocument(), document);
    }

    @PutMapping("update-user-name")
    public ResponseEntity<?> updateUserName(@AuthenticationPrincipal User principal, @RequestParam("document") String document, @RequestBody Map<String, String> json) {
        return userService.updateUserName(principal.getName(), document, json.get("name"));
    }

    @PutMapping("reset-password")
    public ResponseEntity<?> resetPassword(@AuthenticationPrincipal User principal, @RequestParam("document") String document) {
        return userService.resetPassword(principal.getDocument(), document);
    }

    @GetMapping("count-users")
    public ResponseEntity<?> countUsers() {
        return userService.countUsers();
    }

    @PutMapping("set-not-first-login")
    public ResponseEntity<?> setNotFirstLogin(@AuthenticationPrincipal User principal) {
        return userService.setNotFirstLogin(principal);
    }

}
