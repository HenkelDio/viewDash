package com.viewdash.document.DTO;

public record RegisterUserRequestDTO (
        String name, String email, String password, String document) {
}
