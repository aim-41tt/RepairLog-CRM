package ru.papkov.repairlog.domain.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "supply_settings")
public class SupplySetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "setting_key", unique = true, nullable = false, length = 100)
    private String settingKey;

    @Column(name = "setting_value", nullable = false, length = 500)
    private String settingValue;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "last_modified_at")
    private LocalDateTime lastModifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by")
    private Employee modifiedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }
    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getLastModifiedAt() { return lastModifiedAt; }
    public void setLastModifiedAt(LocalDateTime lastModifiedAt) { this.lastModifiedAt = lastModifiedAt; }
    public Employee getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(Employee modifiedBy) { this.modifiedBy = modifiedBy; }
}
