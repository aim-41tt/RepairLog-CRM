package ru.papkov.repairlog.application.dto.diagnostic;

import jakarta.validation.constraints.NotBlank;

/**
 * Запрос на обновление диагностики (без смены заказа).
 *
 * @author aim-41tt
 */
public class UpdateDiagnosticRequest {

    @NotBlank(message = "Описание обязательно")
    private String description;

    private String solution;

    public UpdateDiagnosticRequest() {}

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSolution() { return solution; }
    public void setSolution(String solution) { this.solution = solution; }
}
