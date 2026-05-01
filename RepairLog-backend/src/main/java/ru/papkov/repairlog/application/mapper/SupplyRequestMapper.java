package ru.papkov.repairlog.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.papkov.repairlog.application.dto.supply.SupplierInvoiceResponse;
import ru.papkov.repairlog.application.dto.supply.SupplierPaymentResponse;
import ru.papkov.repairlog.application.dto.supply.SupplyRequestItemResponse;
import ru.papkov.repairlog.application.dto.supply.SupplyRequestResponse;
import ru.papkov.repairlog.domain.model.SupplierInvoice;
import ru.papkov.repairlog.domain.model.SupplierPayment;
import ru.papkov.repairlog.domain.model.SupplyRequest;
import ru.papkov.repairlog.domain.model.SupplyRequestItem;

import java.util.List;

/**
 * MapStruct маппер для сущностей, связанных с заявками на поставку.
 *
 * @author aim-41tt
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SupplyRequestMapper {

    /**
     * Конвертирует entity {@link SupplyRequest} в {@link SupplyRequestResponse}.
     */
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "statusName", source = "status.name")
    @Mapping(target = "requestedByName", expression = "java(request.getRequestedBy() != null ? request.getRequestedBy().getFullName() : null)")
    @Mapping(target = "approvedByName", expression = "java(request.getApprovedBy() != null ? request.getApprovedBy().getFullName() : null)")
    @Mapping(target = "source", expression = "java(request.getSource() != null ? request.getSource().name() : null)")
    @Mapping(target = "relatedRepairOrderId", source = "relatedRepairOrder.id")
    @Mapping(target = "relatedOrderNumber", source = "relatedRepairOrder.orderNumber")
    SupplyRequestResponse toResponse(SupplyRequest request);

    /**
     * Конвертирует список entity в список DTO.
     */
    List<SupplyRequestResponse> toResponseList(List<SupplyRequest> requests);

    /**
     * Конвертирует позицию заявки {@link SupplyRequestItem} в DTO.
     */
    @Mapping(target = "inventoryItemId", source = "inventoryItem.id")
    @Mapping(target = "inventoryItemName", source = "inventoryItem.name")
    SupplyRequestItemResponse toItemResponse(SupplyRequestItem item);

    /**
     * Конвертирует entity {@link SupplierPayment} в {@link SupplierPaymentResponse}.
     */
    @Mapping(target = "supplyRequestId", source = "supplyRequest.id")
    @Mapping(target = "requestNumber", source = "supplyRequest.requestNumber")
    @Mapping(target = "paymentMethod", expression = "java(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null)")
    @Mapping(target = "paidByName", expression = "java(payment.getPaidBy() != null ? payment.getPaidBy().getFullName() : null)")
    SupplierPaymentResponse toPaymentResponse(SupplierPayment payment);

    List<SupplierPaymentResponse> toPaymentResponseList(List<SupplierPayment> payments);

    /**
     * Конвертирует entity {@link SupplierInvoice} в {@link SupplierInvoiceResponse}.
     */
    @Mapping(target = "supplyRequestId", source = "supplyRequest.id")
    @Mapping(target = "requestNumber", source = "supplyRequest.requestNumber")
    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    @Mapping(target = "status", expression = "java(invoice.getStatus() != null ? invoice.getStatus().name() : null)")
    SupplierInvoiceResponse toInvoiceResponse(SupplierInvoice invoice);

    List<SupplierInvoiceResponse> toInvoiceResponseList(List<SupplierInvoice> invoices);
}
