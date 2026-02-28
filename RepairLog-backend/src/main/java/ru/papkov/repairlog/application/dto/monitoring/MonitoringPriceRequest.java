package ru.papkov.repairlog.application.dto.monitoring;

import java.util.List;

/**
 * DTO исходящего запроса к Сервису Мониторинга на получение цен.
 */
public class MonitoringPriceRequest {

    private String supplierId;
    private List<String> partNumbers;

    public MonitoringPriceRequest() {}

    public MonitoringPriceRequest(String supplierId, List<String> partNumbers) {
        this.supplierId = supplierId;
        this.partNumbers = partNumbers;
    }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public List<String> getPartNumbers() { return partNumbers; }
    public void setPartNumbers(List<String> partNumbers) { this.partNumbers = partNumbers; }
}
