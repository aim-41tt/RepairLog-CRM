package ru.papkov.repairlog.application.dto.order;

import jakarta.validation.constraints.NotNull;

/**
 * Запрос на изменение статуса заказа.
 *
 * @author aim-41tt
 */
public class ChangeStatusRequest {

    @NotNull(message = "ID нового статуса обязателен")
    private Long statusId;

    private String comment;

    public ChangeStatusRequest() {}

    public Long getStatusId() { return statusId; }
    public void setStatusId(Long statusId) { this.statusId = statusId; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
