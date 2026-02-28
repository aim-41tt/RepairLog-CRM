package ru.papkov.repairlog.application.dto.supply;

import jakarta.validation.constraints.NotNull;

/**
 * DTO для привязки поставщика к заявке.
 */
public class AssignSupplierRequest {

    @NotNull(message = "ID поставщика обязателен")
    private Long supplierId;

    public AssignSupplierRequest() {}

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
}
