package ru.papkov.repairlog.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.supply.SupplySettingResponse;
import ru.papkov.repairlog.application.dto.supply.UpdateSupplySettingRequest;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.Employee;
import ru.papkov.repairlog.domain.model.SupplySetting;
import ru.papkov.repairlog.domain.repository.EmployeeRepository;
import ru.papkov.repairlog.domain.repository.SupplySettingRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис управления настройками поставок.
 */
@Service
@Transactional(readOnly = true)
public class SupplySettingService {

    private final SupplySettingRepository settingRepository;
    private final EmployeeRepository employeeRepository;

    public SupplySettingService(SupplySettingRepository settingRepository,
                                EmployeeRepository employeeRepository) {
        this.settingRepository = settingRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<SupplySettingResponse> getAll() {
        return settingRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public SupplySettingResponse getByKey(String key) {
        return toResponse(findByKey(key));
    }

    public String getValue(String key) {
        return findByKey(key).getSettingValue();
    }

    public String getValue(String key, String defaultValue) {
        return settingRepository.findBySettingKey(key)
                .map(SupplySetting::getSettingValue)
                .orElse(defaultValue);
    }

    public int getIntValue(String key, int defaultValue) {
        try {
            return Integer.parseInt(getValue(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBooleanValue(String key, boolean defaultValue) {
        return Boolean.parseBoolean(getValue(key, String.valueOf(defaultValue)));
    }

    @Transactional
    public SupplySettingResponse update(String key, UpdateSupplySettingRequest request, String updatedByLogin) {
        SupplySetting setting = findByKey(key);

        Employee employee = employeeRepository.findByLogin(updatedByLogin)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден"));

        setting.setSettingValue(request.getSettingValue());
        setting.setLastModifiedAt(LocalDateTime.now());
        setting.setModifiedBy(employee);

        return toResponse(settingRepository.save(setting));
    }

    private SupplySetting findByKey(String key) {
        return settingRepository.findBySettingKey(key)
                .orElseThrow(() -> new EntityNotFoundException("Настройка не найдена: " + key));
    }

    private SupplySettingResponse toResponse(SupplySetting s) {
        SupplySettingResponse r = new SupplySettingResponse();
        r.setId(s.getId());
        r.setSettingKey(s.getSettingKey());
        r.setSettingValue(s.getSettingValue());
        r.setDescription(s.getDescription());
        r.setLastModifiedAt(s.getLastModifiedAt());
        if (s.getModifiedBy() != null) {
            r.setModifiedByName(s.getModifiedBy().getFullName());
        }
        return r;
    }
}
