package ru.papkov.repairlog.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.papkov.repairlog.application.dto.auth.ChangePasswordRequest;
import ru.papkov.repairlog.application.dto.auth.LoginRequest;
import ru.papkov.repairlog.application.dto.auth.LoginResponse;
import ru.papkov.repairlog.application.service.AuthenticationService;

import java.util.Map;

/**
 * Контроллер аутентификации.
 * Публичные endpoints для входа и управления сессией.
 *
 * @author aim-41tt
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Аутентификация", description = "Вход, выход, управление паролем")
public class AuthController {

    private final AuthenticationService authService;

    public AuthController(AuthenticationService authService) {
        this.authService = authService;
    }

    /**
     * Вход в систему по логину и паролю.
     *
     * @param request     запрос с учётными данными
     * @param httpRequest HTTP-запрос для извлечения IP и User-Agent
     * @return JWT-токен и данные пользователя
     */
    @PostMapping("/login")
    @Operation(summary = "Вход в систему")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                                HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        String ua = httpRequest.getHeader("User-Agent");
        return ResponseEntity.ok(authService.login(request, ip, ua));
    }

    /**
     * Выход из системы (инвалидация токена).
     *
     * @param user текущий аутентифицированный пользователь
     * @return подтверждение выхода
     */
    @PostMapping("/logout")
    @Operation(summary = "Выход из системы")
    public ResponseEntity<Map<String, String>> logout(@AuthenticationPrincipal UserDetails user) {
        authService.logout(user.getUsername());
        return ResponseEntity.ok(Map.of("message", "Выход выполнен"));
    }

    /**
     * Смена пароля текущего пользователя.
     *
     * @param user    текущий аутентифицированный пользователь
     * @param request запрос с текущим и новым паролем
     * @return подтверждение смены пароля
     */
    @PostMapping("/change-password")
    @Operation(summary = "Смена пароля")
    public ResponseEntity<Map<String, String>> changePassword(@AuthenticationPrincipal UserDetails user,
                                                               @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(user.getUsername(), request);
        return ResponseEntity.ok(Map.of("message", "Пароль успешно изменён"));
    }
}
