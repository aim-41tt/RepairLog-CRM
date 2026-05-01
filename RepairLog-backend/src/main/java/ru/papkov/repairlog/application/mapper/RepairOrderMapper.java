package ru.papkov.repairlog.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.papkov.repairlog.application.dto.order.RepairOrderResponse;
import ru.papkov.repairlog.application.dto.order.StatusHistoryResponse;
import ru.papkov.repairlog.domain.model.RepairOrder;
import ru.papkov.repairlog.domain.model.StatusHistory;

import java.util.List;

/**
 * MapStruct маппер для сущности {@link RepairOrder}.
 * <p>
 * Маппит вложенные объекты: client, device, status, priority, сотрудники.
 * Поля totalAmount/paymentStatus игнорируются — они заполняются отдельно
 * из связанного {@link ru.papkov.repairlog.domain.model.Receipt} (если есть).
 * </p>
 *
 * @author aim-41tt
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RepairOrderMapper {

    /**
     * Конвертирует entity {@link RepairOrder} в {@link RepairOrderResponse}.
     */
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientFullName", expression = "java(order.getClient() != null ? order.getClient().getFullName() : null)")
    @Mapping(target = "clientPhone", source = "client.phone")
    @Mapping(target = "deviceId", source = "device.id")
    @Mapping(target = "deviceDescription", expression = "java(order.getDevice() != null ? order.getDevice().getDescription() : null)")
    @Mapping(target = "currentStatusName", source = "currentStatus.name")
    @Mapping(target = "currentStatusId", source = "currentStatus.id")
    @Mapping(target = "currentStatusCode", source = "currentStatus.code")
    @Mapping(target = "priorityName", source = "priority.name")
    @Mapping(target = "acceptedByName", expression = "java(order.getAcceptedBy() != null ? order.getAcceptedBy().getFullName() : null)")
    @Mapping(target = "assignedMasterName", expression = "java(order.getAssignedMaster() != null ? order.getAssignedMaster().getFullName() : null)")
    @Mapping(target = "assignedMasterId", source = "assignedMaster.id")
    @Mapping(target = "warrantyRepair", source = "warrantyRepair")
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    RepairOrderResponse toResponse(RepairOrder order);

    /**
     * Конвертирует список entity в список DTO.
     */
    List<RepairOrderResponse> toResponseList(List<RepairOrder> orders);

    /**
     * Конвертирует entity {@link StatusHistory} в {@link StatusHistoryResponse}.
     */
    @Mapping(target = "statusName", source = "status.name")
    @Mapping(target = "changedByName", expression = "java(history.getChangedBy() != null ? history.getChangedBy().getFullName() : null)")
    StatusHistoryResponse toStatusHistoryResponse(StatusHistory history);

    /**
     * Конвертирует список истории статусов в список DTO.
     */
    List<StatusHistoryResponse> toStatusHistoryList(List<StatusHistory> history);
}
