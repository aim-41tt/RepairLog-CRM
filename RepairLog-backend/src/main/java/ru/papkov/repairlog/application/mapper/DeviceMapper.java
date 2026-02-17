package ru.papkov.repairlog.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.papkov.repairlog.application.dto.device.DeviceResponse;
import ru.papkov.repairlog.domain.model.Device;

import java.util.List;

/**
 * MapStruct маппер для сущности {@link Device}.
 * <p>
 * Маппинг brandName идёт через цепочку device → model → brand → name.
 * clientFullName берётся из вычисляемого метода {@code Client.getFullName()}.
 * </p>
 *
 * @author aim-41tt
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DeviceMapper {

    /**
     * Конвертирует entity {@link Device} в {@link DeviceResponse}.
     */
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientFullName", source = "client.fullName")
    @Mapping(target = "deviceTypeName", source = "deviceType.name")
    @Mapping(target = "brandName", source = "model.brand.name")
    @Mapping(target = "modelName", source = "model.name")
    @Mapping(target = "clientOwned", source = "isClientOwned")
    DeviceResponse toResponse(Device device);

    /**
     * Конвертирует список entity в список DTO.
     */
    List<DeviceResponse> toResponseList(List<Device> devices);
}
