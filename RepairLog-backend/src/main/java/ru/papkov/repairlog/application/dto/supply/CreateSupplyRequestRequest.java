package ru.papkov.repairlog.application.dto.supply;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Запрос на создание заявки на поставку.
 *
 * @author aim-41tt
 */
public class CreateSupplyRequestRequest {

    @NotNull(message = "ID поставщика обязателен")
    private Long supplierId;

    private String comment;

    @NotEmpty(message = "Список позиций не может быть пустым")
    @Valid
    private List<CreateSupplyRequestItemRequest> items;

    public CreateSupplyRequestRequest() {}

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public List<CreateSupplyRequestItemRequest> getItems() { return items; }
    public void setItems(List<CreateSupplyRequestItemRequest> items) { this.items = items; }
}
