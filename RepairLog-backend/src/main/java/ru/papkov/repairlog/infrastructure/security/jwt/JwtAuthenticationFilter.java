package ru.papkov.repairlog.infrastructure.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.papkov.repairlog.domain.model.Employee;
import ru.papkov.repairlog.domain.repository.EmployeeRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Фильтр для проверки JWT токена в каждом запросе.
 * Дополнительно проверяет срок действия пароля сотрудника.
 *
 * @author aim-41tt
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    private final RedisTokenStore tokenStore;
    private final EmployeeRepository employeeRepository;
    private final long passwordExpirationDays;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   UserDetailsService userDetailsService,
                                   RedisTokenStore tokenStore,
                                   EmployeeRepository employeeRepository,
                                   @Value("${app.security.password-expiration-days:90}") long passwordExpirationDays) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.tokenStore = tokenStore;
        this.employeeRepository = employeeRepository;
        this.passwordExpirationDays = passwordExpirationDays;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromToken(jwt);
                if (tokenStore.exists(username, jwt)) {
                    // Проверка срока действия пароля
                    Employee employee = employeeRepository.findByLogin(username).orElse(null);
                    if (employee != null && employee.getLastPasswordChange() != null) {
                        long daysSinceChange = ChronoUnit.DAYS.between(
                                employee.getLastPasswordChange(), LocalDateTime.now());
                        if (daysSinceChange > passwordExpirationDays) {
                            log.warn("Пароль сотрудника {} истёк ({} дней назад)", username, daysSinceChange);
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(
                                    "{\"error\":\"PASSWORD_EXPIRED\",\"message\":\"Срок действия пароля истёк. Обратитесь к администратору.\"}");
                            return;
                        }
                    }

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
