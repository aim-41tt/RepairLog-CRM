package ru.papkov.repairlog.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.papkov.repairlog.application.dto.supply.SupplySettingResponse;
import ru.papkov.repairlog.application.dto.supply.UpdateSupplySettingRequest;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.Employee;
import ru.papkov.repairlog.domain.model.SupplySetting;
import ru.papkov.repairlog.domain.repository.EmployeeRepository;
import ru.papkov.repairlog.domain.repository.SupplySettingRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplySettingServiceTest {

    @Mock private SupplySettingRepository settingRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private SupplySettingService supplySettingService;

    private SupplySetting testSetting;
    private Employee testAdmin;

    @BeforeEach
    void setUp() {
        testSetting = new SupplySetting();
        testSetting.setId(1L);
        testSetting.setSettingKey("auto_reorder_enabled");
        testSetting.setSettingValue("true");
        testSetting.setDescription("Автозаказ при низком остатке");

        testAdmin = new Employee();
        testAdmin.setId(1L);
        testAdmin.setName("Админ");
        testAdmin.setSurname("Админов");
        testAdmin.setLogin("admin");
    }

    @Test
    @DisplayName("getAll - возвращает все настройки")
    void getAll_returnsList() {
        when(settingRepository.findAll()).thenReturn(List.of(testSetting));

        List<SupplySettingResponse> result = supplySettingService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSettingKey()).isEqualTo("auto_reorder_enabled");
    }

    @Test
    @DisplayName("getByKey - возвращает настройку по ключу")
    void getByKey_returnsSetting() {
        when(settingRepository.findBySettingKey("auto_reorder_enabled"))
                .thenReturn(Optional.of(testSetting));

        SupplySettingResponse result = supplySettingService.getByKey("auto_reorder_enabled");

        assertThat(result.getSettingValue()).isEqualTo("true");
    }

    @Test
    @DisplayName("getByKey - ошибка если не найдена")
    void getByKey_throwsWhenNotFound() {
        when(settingRepository.findBySettingKey("unknown_key")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplySettingService.getByKey("unknown_key"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("getValue - возвращает значение настройки")
    void getValue_returnsValue() {
        when(settingRepository.findBySettingKey("auto_reorder_enabled"))
                .thenReturn(Optional.of(testSetting));

        String result = supplySettingService.getValue("auto_reorder_enabled");

        assertThat(result).isEqualTo("true");
    }

    @Test
    @DisplayName("getValue - с defaultValue возвращает значение по умолчанию")
    void getValue_returnsDefault() {
        when(settingRepository.findBySettingKey("missing")).thenReturn(Optional.empty());

        String result = supplySettingService.getValue("missing", "default_value");

        assertThat(result).isEqualTo("default_value");
    }

    @Test
    @DisplayName("getIntValue - парсит целочисленное значение")
    void getIntValue_parsesInt() {
        SupplySetting intSetting = new SupplySetting();
        intSetting.setSettingKey("max_retries");
        intSetting.setSettingValue("5");
        when(settingRepository.findBySettingKey("max_retries"))
                .thenReturn(Optional.of(intSetting));

        int result = supplySettingService.getIntValue("max_retries", 3);

        assertThat(result).isEqualTo(5);
    }

    @Test
    @DisplayName("getIntValue - возвращает default при ошибке парсинга")
    void getIntValue_returnsDefaultOnParseError() {
        SupplySetting badSetting = new SupplySetting();
        badSetting.setSettingKey("bad_key");
        badSetting.setSettingValue("not_a_number");
        when(settingRepository.findBySettingKey("bad_key"))
                .thenReturn(Optional.of(badSetting));

        int result = supplySettingService.getIntValue("bad_key", 42);

        assertThat(result).isEqualTo(42);
    }

    @Test
    @DisplayName("getBooleanValue - парсит булево значение")
    void getBooleanValue_parsesBoolean() {
        when(settingRepository.findBySettingKey("auto_reorder_enabled"))
                .thenReturn(Optional.of(testSetting));

        boolean result = supplySettingService.getBooleanValue("auto_reorder_enabled", false);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("update - успешное обновление настройки")
    void update_success() {
        UpdateSupplySettingRequest request = new UpdateSupplySettingRequest();
        request.setSettingValue("false");

        when(settingRepository.findBySettingKey("auto_reorder_enabled"))
                .thenReturn(Optional.of(testSetting));
        when(employeeRepository.findByLogin("admin")).thenReturn(Optional.of(testAdmin));
        when(settingRepository.save(any(SupplySetting.class))).thenReturn(testSetting);

        SupplySettingResponse result = supplySettingService.update("auto_reorder_enabled", request, "admin");

        assertThat(testSetting.getSettingValue()).isEqualTo("false");
        assertThat(testSetting.getModifiedBy()).isEqualTo(testAdmin);
        assertThat(testSetting.getLastModifiedAt()).isNotNull();
        verify(settingRepository).save(testSetting);
    }

    @Test
    @DisplayName("update - ошибка если сотрудник не найден")
    void update_throwsWhenEmployeeNotFound() {
        UpdateSupplySettingRequest request = new UpdateSupplySettingRequest();
        request.setSettingValue("false");

        when(settingRepository.findBySettingKey("auto_reorder_enabled"))
                .thenReturn(Optional.of(testSetting));
        when(employeeRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplySettingService.update("auto_reorder_enabled", request, "unknown"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
