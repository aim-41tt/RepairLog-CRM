package ru.papkov.repairlog.application.dto.receipt;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Запрос на добавление ремонтной работы.
 *
 * @author aim-41tt
 */
public class AddRepairWorkRequest {

    @NotNull(message = "ID чека обязателен")
    private Long receiptId;

    @NotBlank(message = "Описание работы обязательно")
    private String description;

    @NotNull(message = "Стоимость обязательна")
    @DecimalMin(value = "0.00", message = "Стоимость не может быть отрицательной")
    private BigDecimal price;

    public AddRepairWorkRequest() {}

    public Long getReceiptId() { return receiptId; }
    public void setReceiptId(Long receiptId) { this.receiptId = receiptId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
