package ru.papkov.repairlog.application.dto.monitoring;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO ответа от Сервиса Мониторинга с ценами.
 */
public class MonitoringPriceResponse {

    private String supplierId;
    private List<PriceItem> items;

    public MonitoringPriceResponse() {}

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public List<PriceItem> getItems() { return items; }
    public void setItems(List<PriceItem> items) { this.items = items; }

    public static class PriceItem {
        private String partNumber;
        private String itemName;
        private BigDecimal price;
        private Boolean available;

        public PriceItem() {}

        public String getPartNumber() { return partNumber; }
        public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public Boolean getAvailable() { return available; }
        public void setAvailable(Boolean available) { this.available = available; }
    }
}
