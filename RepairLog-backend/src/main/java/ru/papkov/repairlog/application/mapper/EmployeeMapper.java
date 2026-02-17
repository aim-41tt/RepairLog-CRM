package ru.papkov.repairlog.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.papkov.repairlog.application.dto.employee.CreateEmployeeRequest;
import ru.papkov.repairlog.application.dto.employee.EmployeeResponse;
import ru.papkov.repairlog.application.dto.employee.UpdateEmployeeRequest;
import ru.papkov.repairlog.domain.model.Employee;
import ru.papkov.repairlog.domain.model.Role;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MapStruct маппер для сущности {@link Employee}.
 * Преобразует Entity ↔ DTO.
 *
 * @author aim-41tt
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmployeeMapper {

    /**
     * Маппинг Employee → EmployeeResponse.
     * Роли преобразуются из Set&lt;Role&gt; в List&lt;String&gt;.
     */
    @Mapping(target = "fullName", expression = "java(employee.getFullName())")
    @Mapping(target = "roles", expression = "java(mapRoles(employee.getRoles()))")
    EmployeeResponse toResponse(Employee employee);

    /**
     * Маппинг списка Employee → список EmployeeResponse.
     */
    List<EmployeeResponse> toResponseList(List<Employee> employees);

    /**
     * Маппинг CreateEmployeeRequest → Employee.
     * Пароль и роли задаются отдельно в сервисе.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "blocked", constant = "false")
    @Mapping(target = "failedLoginAttempts", constant = "0")
    @Mapping(target = "lastPasswordChange", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "accountLockedUntil", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    Employee toEntity(CreateEmployeeRequest request);

    /**
     * Частичное обновление Employee из UpdateEmployeeRequest.
     * null-поля в request не перезаписывают значения в entity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "blocked", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "lastPasswordChange", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "accountLockedUntil", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    void updateEntity(UpdateEmployeeRequest request, @MappingTarget Employee employee);

    /**
     * Преобразование ролей из Set в List строк.
     */
    default List<String> mapRoles(Set<Role> roles) {
        if (roles == null) return List.of();
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }
}
