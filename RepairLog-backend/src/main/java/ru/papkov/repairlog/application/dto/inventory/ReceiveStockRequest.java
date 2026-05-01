package ru.papkov.repairlog.application.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Запрос на приёмку товара на склад.
 *
 * @author aim-41tt
 */
public class ReceiveStockRequest {

    @NotNull(message = "Количество обязательно")
    @Min(value = 1, message = "Количество должно быть больше 0")
    private Integer quantity;

    @Size(max = 500, message = "Комментарий не должен превышать 500 символов")
    private String comment;

    public ReceiveStockRequest() {}

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
