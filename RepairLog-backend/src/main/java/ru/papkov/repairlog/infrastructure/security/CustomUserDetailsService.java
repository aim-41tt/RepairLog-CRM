package ru.papkov.repairlog.infrastructure.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.domain.model.Employee;
import ru.papkov.repairlog.domain.repository.EmployeeRepository;

import java.util.stream.Collectors;

/**
 * Сервис загрузки пользователей для Spring Security.
 * 
 * @author aim-41tt
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    public CustomUserDetailsService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee employee = employeeRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (employee.getBlocked()) {
            throw new UsernameNotFoundException("User is blocked: " + username);
        }

        if (employee.isAccountLocked()) {
            throw new UsernameNotFoundException("Account is locked: " + username);
        }

        return User.builder()
                .username(employee.getLogin())
                .password(employee.getPassword())
                .authorities(employee.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                        .collect(Collectors.toList()))
                .accountExpired(false)
                .accountLocked(employee.isAccountLocked())
                .credentialsExpired(false)
                .disabled(employee.getBlocked())
                .build();
    }
}
