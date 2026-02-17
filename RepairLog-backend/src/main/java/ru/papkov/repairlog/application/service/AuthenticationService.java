package ru.papkov.repairlog.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.auth.ChangePasswordRequest;
import ru.papkov.repairlog.application.dto.auth.LoginRequest;
import ru.papkov.repairlog.application.dto.auth.LoginResponse;
import ru.papkov.repairlog.domain.exception.BusinessLogicException;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.Employee;
import ru.papkov.repairlog.domain.model.SecurityAuditLog;
import ru.papkov.repairlog.domain.repository.EmployeeRepository;
import ru.papkov.repairlog.infrastructure.security.audit.AuditLogger;
import ru.papkov.repairlog.infrastructure.security.jwt.JwtTokenProvider;
import ru.papkov.repairlog.infrastructure.security.jwt.RedisTokenStore;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис аутентификации и управления сессиями.
 * Поддерживает JWT + Redis для хранения токенов.
 *
 * @author aim-41tt
 */
@Service
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final RedisTokenStore tokenStore;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogger auditLogger;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${app.security.max-login-attempts}")
    private int maxLoginAttempts;

    @Value("${app.security.account-lock-duration-minutes}")
    private int accountLockDurationMinutes;

    public AuthenticationService(AuthenticationManager authenticationManager,
                                 JwtTokenProvider tokenProvider,
                                 RedisTokenStore tokenStore,
                                 EmployeeRepository employeeRepository,
                                 PasswordEncoder passwordEncoder,
                                 AuditLogger auditLogger) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.tokenStore = tokenStore;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogger = auditLogger;
    }

    /**
     * Аутентификация пользователя по логину и паролю.
     * При успешной проверке выдаёт JWT-токен и сохраняет его в Redis.
     *
     * @param request   запрос с логином и паролем
     * @param ipAddress IP-адрес клиента
     * @param userAgent User-Agent браузера
     * @return ответ с JWT-токеном и данными пользователя
     * @throws BadCredentialsException если логин или пароль неверны
     * @throws BusinessLogicException  если аккаунт заблокирован
     */
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        Employee employee = employeeRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new BadCredentialsException("Неверный логин или пароль"));

        // проверка блокировки аккаунта
        if (employee.isAccountLocked()) {
            throw new BusinessLogicException("Аккаунт заблокирован до " + employee.getAccountLockedUntil());
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword()));

            // сброс счётчика неудачных попыток
            employee.setFailedLoginAttempts(0);
            employee.setAccountLockedUntil(null);

            // выдаём JWT-токен
            return issueToken(authentication, employee, ipAddress, userAgent);

        } catch (BadCredentialsException ex) {
            handleFailedLogin(employee, ipAddress, userAgent);
            throw new BadCredentialsException("Неверный логин или пароль");
        }
    }

    /**
     * Выход из системы — удаление токена из Redis.
     *
     * @param username логин пользователя
     */
    @Transactional
    public void logout(String username) {
        tokenStore.delete(username);
        log.info("Пользователь {} вышел из системы", username);
    }

    /**
     * Смена пароля текущим пользователем.
     *
     * @param username логин текущего пользователя
     * @param request  запрос с текущим и новым паролем
     * @throws EntityNotFoundException если сотрудник не найден
     * @throws BusinessLogicException  если текущий пароль неверен
     */
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        Employee employee = employeeRepository.findByLogin(username)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), employee.getPassword())) {
            throw new BusinessLogicException("Текущий пароль неверен");
        }

        employee.setPassword(passwordEncoder.encode(request.getNewPassword()));
        employee.setLastPasswordChange(LocalDateTime.now());
        employeeRepository.save(employee);

        // инвалидируем текущую сессию — пользователю придётся войти заново
        tokenStore.delete(username);

        auditLogger.log(SecurityAuditLog.EventType.PASSWORD_CHANGE, employee,
                SecurityAuditLog.Result.SUCCESS, null, null, null);
    }

    /**
     * Принудительное завершение сессии сотрудника (только ADMIN).
     *
     * @param employeeId ID сотрудника
     * @throws EntityNotFoundException если сотрудник не найден
     */
    @Transactional
    public void terminateSession(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден"));
        tokenStore.delete(employee.getLogin());
        log.info("Сессия сотрудника {} принудительно завершена", employee.getLogin());
    }

    // ========== Вспомогательные методы ==========

    /**
     * Генерация и сохранение JWT-токена после успешной аутентификации.
     */
    private LoginResponse issueToken(Authentication authentication, Employee employee,
                                     String ipAddress, String userAgent) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        // сохраняем токен в Redis
        tokenStore.store(employee.getLogin(), token, Duration.ofMillis(jwtExpirationMs));

        // обновляем last_login
        employee.setLastLogin(LocalDateTime.now());
        employeeRepository.save(employee);

        auditLogger.log(SecurityAuditLog.EventType.LOGIN, employee,
                SecurityAuditLog.Result.SUCCESS, ipAddress, userAgent, null);

        List<String> roles = employee.getRoles().stream()
                .map(r -> r.getName()).collect(Collectors.toList());

        return new LoginResponse(token, employee.getFullName(), roles);
    }

    /**
     * Обработка неудачной попытки входа — инкремент счётчика и блокировка при превышении.
     */
    private void handleFailedLogin(Employee employee, String ipAddress, String userAgent) {
        int attempts = employee.getFailedLoginAttempts() + 1;
        employee.setFailedLoginAttempts(attempts);

        if (attempts >= maxLoginAttempts) {
            employee.setAccountLockedUntil(LocalDateTime.now().plusMinutes(accountLockDurationMinutes));
            log.warn("Аккаунт {} заблокирован после {} неудачных попыток", employee.getLogin(), attempts);
        }

        employeeRepository.save(employee);
        auditLogger.log(SecurityAuditLog.EventType.LOGIN_FAILED, employee,
                SecurityAuditLog.Result.FAILURE, ipAddress, userAgent, null);
    }
}
