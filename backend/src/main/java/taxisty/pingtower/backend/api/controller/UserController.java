package taxisty.pingtower.backend.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import taxisty.pingtower.backend.api.dto.AuthResponse;
import taxisty.pingtower.backend.api.dto.LoginRequest;
import taxisty.pingtower.backend.api.dto.RegisterRequest;
import taxisty.pingtower.backend.api.dto.UserProfile;
import taxisty.pingtower.backend.api.service.UserService;

import jakarta.validation.Valid;

/**
 * REST API controller for user authentication and management
 */
@RestController
@RequestMapping("/api/auth")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.registerUser(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Authenticate user and return JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.authenticateUser(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfile> getProfile() {
        UserProfile profile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }
}