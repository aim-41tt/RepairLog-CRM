package ru.papkov.repairlog.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.papkov.repairlog.application.dto.device.DeviceResponse;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.*;
import ru.papkov.repairlog.domain.repository.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock private DeviceRepository deviceRepository;
    @Mock private DeviceTypeRepository deviceTypeRepository;
    @Mock private ModelRepository modelRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private DeviceLocationRepository deviceLocationRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private BrandRepository brandRepository;

    @InjectMocks
    private DeviceService deviceService;

    private Device testDevice;
    private Client testClient;
    private DeviceType testDeviceType;
    private Brand testBrand;
    private Model testModel;

    @BeforeEach
    void setUp() {
        testBrand = new Brand();
        testBrand.setId(1L);
        testBrand.setName("Apple");

        testModel = new Model();
        testModel.setId(1L);
        testModel.setName("MacBook Pro");
        testModel.setBrand(testBrand);

        testDeviceType = new DeviceType();
        testDeviceType.setId(1L);
        testDeviceType.setName("Ноутбук");

        testClient = new Client();
        testClient.setId(1L);
        testClient.setName("Иван");
        testClient.setSurname("Петров");
        testClient.setPatronymic("С");
        testClient.setPhone("+79001234567");

        testDevice = new Device();
        testDevice.setId(1L);
        testDevice.setDeviceType(testDeviceType);
        testDevice.setModel(testModel);
        testDevice.setSerialNumber("SN123456");
        testDevice.setIsClientOwned(true);
        testDevice.setClient(testClient);
    }

    @Test
    @DisplayName("getById - возвращает устройство по ID")
    void getById_returnsDevice() {
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));

        DeviceResponse result = deviceService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDeviceTypeName()).isEqualTo("Ноутбук");
        assertThat(result.getBrandName()).isEqualTo("Apple");
        assertThat(result.getModelName()).isEqualTo("MacBook Pro");
        assertThat(result.getSerialNumber()).isEqualTo("SN123456");
        assertThat(result.getClientId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getById - ошибка если устройство не найдено")
    void getById_throwsWhenNotFound() {
        when(deviceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("getByClient - возвращает устройства клиента")
    void getByClient_returnsList() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(deviceRepository.findByClient(testClient)).thenReturn(List.of(testDevice));

        List<DeviceResponse> result = deviceService.getByClient(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClientId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getByClient - ошибка если клиент не найден")
    void getByClient_throwsWhenClientNotFound() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.getByClient(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("moveDevice - успешное перемещение устройства")
    void moveDevice_success() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setName("Тест");
        employee.setSurname("Тестов");
        employee.setLogin("test");

        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));
        when(employeeRepository.findByLogin("test")).thenReturn(Optional.of(employee));

        deviceService.moveDevice(1L, "Полка A3", "test", "Перемещено на полку");

        verify(deviceLocationRepository).save(any(DeviceLocation.class));
    }

    @Test
    @DisplayName("moveDevice - ошибка если устройство не найдено")
    void moveDevice_throwsWhenDeviceNotFound() {
        when(deviceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.moveDevice(99L, "Полка", "test", ""))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("moveDevice - ошибка если сотрудник не найден")
    void moveDevice_throwsWhenEmployeeNotFound() {
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));
        when(employeeRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.moveDevice(1L, "Полка", "unknown", ""))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
