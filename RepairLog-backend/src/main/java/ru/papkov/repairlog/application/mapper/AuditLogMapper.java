package ru.papkov.repairlog.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.papkov.repairlog.application.dto.audit.AuditLogResponse;
import ru.papkov.repairlog.domain.model.SecurityAuditLog;

import java.util.List;

/**
 * MapStruct маппер для сущности {@link SecurityAuditLog}.
 *
 * @author aim-41tt
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AuditLogMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", expression = "java(log.getEmployee() != null ? log.getEmployee().getFullName() : null)")
    @Mapping(target = "eventType", expression = "java(log.getEventType() != null ? log.getEventType().name() : null)")
    @Mapping(target = "result", expression = "java(log.getResult() != null ? log.getResult().name() : null)")
    AuditLogResponse toResponse(SecurityAuditLog log);

    List<AuditLogResponse> toResponseList(List<SecurityAuditLog> logs);
}
