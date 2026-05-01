package ru.papkov.repairlog.application.dto.inventory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Запрос на создание складской позиции.
 *
 * @author aim-41tt
 */
public class CreateInventoryItemRequest {

    @NotBlank(message = "Наименование обязательно")
    @Size(max = 200, message = "Наименование не должно превышать 200 символов")
    private String name;

    @Size(max = 100, message = "Артикул не должен превышать 100 символов")
    private String partNumber;

    private String description;

    @NotNull(message = "Количество обязательно")
    @Min(value = 0, message = "Количество не может быть отрицательным")
    private Integer quantity;

    @Min(value = 0, message = "Минимальный остаток не может быть отрицательным")
    private Integer minQuantity;

    // purchasePrice can be 0 (own production / donated), but must not be negative
    @DecimalMin(value = "0.00", message = "Закупочная цена не может быть отрицательной")
    private BigDecimal purchasePrice;

    // B-14: selling price must be > 0 — a zero price leads to silent billing errors
    @NotNull(message = "Цена продажи обязательна")
    @DecimalMin(value = "0.01", message = "Цена продажи должна быть больше 0")
    private BigDecimal sellingPrice;

    public CreateInventoryItemRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPartNumber() { return partNumber; }
    public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getMinQuantity() { return minQuantity; }
    public void setMinQuantity(Integer minQuantity) { this.minQuantity = minQuantity; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
}
