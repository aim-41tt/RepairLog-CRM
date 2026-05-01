package ru.papkov.repairlog.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.papkov.repairlog.application.dto.receipt.ReceiptResponse;
import ru.papkov.repairlog.domain.model.Receipt;
import ru.papkov.repairlog.domain.model.ReceiptPayment;
import ru.papkov.repairlog.domain.model.RepairWork;

import java.util.List;

/**
 * MapStruct маппер для сущности {@link Receipt}.
 * <p>
 * Не маппит списки works/payments из entity (они хранятся в отдельных таблицах) —
 * контроллер/сервис должны заполнить их отдельными вызовами с использованием
 * {@link #toWorkResponse(RepairWork)} / {@link #toPaymentResponse(ReceiptPayment)}.
 * </p>
 *
 * @author aim-41tt
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ReceiptMapper {

    /**
     * Конвертирует entity {@link Receipt} в {@link ReceiptResponse}.
     * Поля works/payments устанавливаются контроллером отдельно.
     */
    @Mapping(target = "repairOrderId", source = "repairOrder.id")
    @Mapping(target = "orderNumber", source = "repairOrder.orderNumber")
    @Mapping(target = "paymentStatus", expression = "java(receipt.getPaymentStatus() != null ? receipt.getPaymentStatus().name() : null)")
    @Mapping(target = "locked", source = "locked")
    @Mapping(target = "works", ignore = true)
    @Mapping(target = "payments", ignore = true)
    ReceiptResponse toResponse(Receipt receipt);

    /**
     * Конвертирует {@link RepairWork} в вложенный DTO работы.
     */
    @Mapping(target = "employeeName", expression = "java(work.getEmployee() != null ? work.getEmployee().getFullName() : null)")
    ReceiptResponse.RepairWorkResponse toWorkResponse(RepairWork work);

    List<ReceiptResponse.RepairWorkResponse> toWorkResponseList(List<RepairWork> works);

    /**
     * Конвертирует {@link ReceiptPayment} в вложенный DTO платежа.
     */
    @Mapping(target = "paymentMethod", expression = "java(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null)")
    @Mapping(target = "acceptedByName", expression = "java(payment.getAcceptedBy() != null ? payment.getAcceptedBy().getFullName() : null)")
    ReceiptResponse.PaymentResponse toPaymentResponse(ReceiptPayment payment);

    List<ReceiptResponse.PaymentResponse> toPaymentResponseList(List<ReceiptPayment> payments);
}
