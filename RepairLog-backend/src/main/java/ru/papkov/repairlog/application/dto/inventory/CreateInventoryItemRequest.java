package ru.papkov.repairlog.application.dto.inventory;

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

    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private String location;

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
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
