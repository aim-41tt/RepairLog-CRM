package ru.papkov.repairlog.application.dto.diagnostic;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Запрос на создание диагностики.
 *
 * @author aim-41tt
 */
public class CreateDiagnosticRequest {

    @NotNull(message = "ID заказа обязателен")
    private Long repairOrderId;

    @NotBlank(message = "Описание обязательно")
    private String description;

    private String solution;

    public CreateDiagnosticRequest() {}

    public Long getRepairOrderId() { return repairOrderId; }
    public void setRepairOrderId(Long repairOrderId) { this.repairOrderId = repairOrderId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSolution() { return solution; }
    public void setSolution(String solution) { this.solution = solution; }
}
