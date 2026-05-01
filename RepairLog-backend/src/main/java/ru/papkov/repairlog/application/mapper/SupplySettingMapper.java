package ru.papkov.repairlog.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.papkov.repairlog.application.dto.supply.SupplySettingResponse;
import ru.papkov.repairlog.domain.model.SupplySetting;

import java.util.List;

/**
 * MapStruct маппер для сущности {@link SupplySetting}.
 *
 * @author aim-41tt
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SupplySettingMapper {

    /**
     * Конвертирует entity {@link SupplySetting} в {@link SupplySettingResponse}.
     */
    @Mapping(target = "modifiedByName", expression = "java(setting.getModifiedBy() != null ? setting.getModifiedBy().getFullName() : null)")
    SupplySettingResponse toResponse(SupplySetting setting);

    /**
     * Конвертирует список entity в список DTO.
     */
    List<SupplySettingResponse> toResponseList(List<SupplySetting> settings);
}
