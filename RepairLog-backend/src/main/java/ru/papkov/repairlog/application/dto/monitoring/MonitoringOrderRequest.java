package ru.papkov.repairlog.application.dto.monitoring;

import java.util.List;

/**
 * DTO исходящего запроса к Сервису Мониторинга на создание заказа.
 */
public class MonitoringOrderRequest {

    private String supplierId;
    private String callbackUrl;
    private List<OrderItem> items;

    public MonitoringOrderRequest() {}

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getCallbackUrl() { return callbackUrl; }
    public void setCallbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public static class OrderItem {
        private String partNumber;
        private String itemName;
        private Integer quantity;

        public OrderItem() {}

        public OrderItem(String partNumber, String itemName, Integer quantity) {
            this.partNumber = partNumber;
            this.itemName = itemName;
            this.quantity = quantity;
        }

        public String getPartNumber() { return partNumber; }
        public void setPartNumber(String partNumber) { this.partNumber = partNumber; }
        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}
