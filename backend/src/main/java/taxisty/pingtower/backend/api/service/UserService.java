package taxisty.pingtower.backend.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taxisty.pingtower.backend.api.dto.AuthResponse;
import taxisty.pingtower.backend.api.dto.LoginRequest;
import taxisty.pingtower.backend.api.dto.RegisterRequest;
import taxisty.pingtower.backend.api.dto.UserProfile;
import taxisty.pingtower.backend.config.JwtUtil;
import taxisty.pingtower.backend.monitoring.repository.UserRepository;
import taxisty.pingtower.backend.storage.model.User;

import java.time.LocalDateTime;

/**
 * Service for user authentication and management
 */
@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TelegramOnboardingService telegramOnboardingService;
    
    /**
     * Register a new user
     */
    public AuthResponse registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email is already in use!");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setIsActive(true);
        
        user = userRepository.save(user);

        telegramOnboardingService.prepareLink(user.getId(), user.getUsername());
        
        // Authenticate the user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateToken(authentication);
        
        return new AuthResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), user.getRoles());
    }
    
    /**
     * Authenticate user and return JWT token
     */
    public AuthResponse authenticateUser(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtil.generateToken(authentication);
        
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        return new AuthResponse(jwt, user.getId(), user.getUsername(), user.getEmail(), user.getRoles());
    }
    
    /**
     * Get current user profile
     */
    public UserProfile getCurrentUserProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return new UserProfile(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isActive()
        );
    }
    
    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}