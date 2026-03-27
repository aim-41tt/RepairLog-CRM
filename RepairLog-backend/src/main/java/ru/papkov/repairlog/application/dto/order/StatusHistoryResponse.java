package ru.papkov.repairlog.application.dto.order;

import java.time.LocalDateTime;

/**
 * DTO ответа с данными записи истории статусов.
 *
 * @author aim-41tt
 */
public class StatusHistoryResponse {
    private Long id;
    private String statusName;
    private String changedByName;
    private LocalDateTime changedAt;
    private String comment;

    public StatusHistoryResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public void setChangedByName(String changedByName) {
        this.changedByName = changedByName;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
