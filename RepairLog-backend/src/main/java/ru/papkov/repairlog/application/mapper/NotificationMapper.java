package ru.papkov.repairlog.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.papkov.repairlog.application.dto.notification.NotificationResponse;
import ru.papkov.repairlog.domain.model.Notification;

import java.util.List;

/**
 * MapStruct маппер для сущности {@link Notification}.
 *
 * @author aim-41tt
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationMapper {

    /**
     * Конвертирует entity {@link Notification} в {@link NotificationResponse}.
     */
    @Mapping(target = "type", expression = "java(notification.getNotificationType() != null ? notification.getNotificationType().name() : null)")
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", expression = "java(notification.getClient() != null ? (notification.getClient().getSurname() + \" \" + notification.getClient().getName()) : null)")
    @Mapping(target = "repairOrderId", source = "repairOrder.id")
    @Mapping(target = "orderNumber", source = "repairOrder.orderNumber")
    @Mapping(target = "status", expression = "java(notification.getStatus() != null ? notification.getStatus().name() : null)")
    @Mapping(target = "messageBody", source = "message")
    @Mapping(target = "channel", ignore = true)
    @Mapping(target = "subject", ignore = true)
    NotificationResponse toResponse(Notification notification);

    /**
     * Конвертирует список entity в список DTO.
     */
    List<NotificationResponse> toResponseList(List<Notification> notifications);
}
