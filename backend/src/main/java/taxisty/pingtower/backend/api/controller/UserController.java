package taxisty.pingtower.backend.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import taxisty.pingtower.backend.api.dto.AuthResponse;
import taxisty.pingtower.backend.api.dto.LoginRequest;
import taxisty.pingtower.backend.api.dto.RegisterRequest;
import taxisty.pingtower.backend.api.dto.UserProfile;
import taxisty.pingtower.backend.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

/**
 * REST API controller for user authentication and management
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Аутентификация", description = "API для аутентификации пользователей и управления профилем")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Register a new user
     */
    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя", description = "Создает новый аккаунт пользователя")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные регистрации")
    })
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.registerUser(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Authenticate user and return JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "Вход в систему", description = "Аутентифицирует пользователя и возвращает JWT токен")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Вход выполнен успешно"),
        @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.authenticateUser(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    @Operation(summary = "Получить профиль пользователя", description = "Возвращает профиль текущего аутентифицированного пользователя")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Профиль успешно получен"),
        @ApiResponse(responseCode = "401", description = "Не авторизован")
    })
    public ResponseEntity<UserProfile> getProfile() {
        UserProfile profile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }
}