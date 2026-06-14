// src/main/java/com/example/demo/controller/BaseController.java
package com.example.demo.controller;

import jakarta.servlet.http.HttpSession;

public abstract class BaseController {
    
    protected boolean isOwner(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return role != null && role.equalsIgnoreCase("owner");
    }

    protected boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("role") != null;
    }

    protected String getRole(HttpSession session) {
        return (String) session.getAttribute("role");
    }
}