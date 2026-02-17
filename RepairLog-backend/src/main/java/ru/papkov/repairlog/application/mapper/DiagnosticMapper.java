package ru.papkov.repairlog.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.papkov.repairlog.application.dto.diagnostic.DiagnosticResponse;
import ru.papkov.repairlog.domain.model.Diagnostic;

import java.util.List;

/**
 * MapStruct маппер для сущности {@link Diagnostic}.
 *
 * @author aim-41tt
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DiagnosticMapper {

    /**
     * Конвертирует entity {@link Diagnostic} в {@link DiagnosticResponse}.
     */
    @Mapping(target = "repairOrderId", source = "repairOrder.id")
    @Mapping(target = "orderNumber", source = "repairOrder.orderNumber")
    @Mapping(target = "performedByName", expression = "java(diagnostic.getPerformedBy() != null ? diagnostic.getPerformedBy().getFullName() : null)")
    DiagnosticResponse toResponse(Diagnostic diagnostic);

    /**
     * Конвертирует список entity в список DTO.
     */
    List<DiagnosticResponse> toResponseList(List<Diagnostic> diagnostics);
}
