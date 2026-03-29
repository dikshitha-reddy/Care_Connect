package com.example.demo.dto;

import com.example.demo.model.Role;
public class AuthResponse {
    private Long id;
    private String token;
    private String name;
    private String email;
    private Role role;

    public AuthResponse() {
    }

    public AuthResponse(Long id, String token, String name, String email, Role role) {
        this.id = id;
        this.token = token;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
