package ru.papkov.repairlog.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.papkov.repairlog.application.dto.supplier.CreateSupplierRequest;
import ru.papkov.repairlog.application.dto.supplier.SupplierResponse;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.Supplier;
import ru.papkov.repairlog.domain.model.enums.IntegrationType;
import ru.papkov.repairlog.domain.repository.SupplierRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier testSupplier;

    @BeforeEach
    void setUp() {
        testSupplier = new Supplier();
        testSupplier.setId(1L);
        testSupplier.setName("ООО Запчасти");
        testSupplier.setContactPerson("Иванов Иван");
        testSupplier.setPhone("+79001234567");
        testSupplier.setEmail("parts@example.com");
        testSupplier.setActive(true);
        testSupplier.setIntegrationType(IntegrationType.MANUAL);
    }

    @Test
    @DisplayName("getAll - возвращает всех поставщиков")
    void getAll_returnsList() {
        when(supplierRepository.findAll()).thenReturn(List.of(testSupplier));

        List<SupplierResponse> result = supplierService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("ООО Запчасти");
    }

    @Test
    @DisplayName("getById - возвращает поставщика по ID")
    void getById_returnsSupplier() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));

        SupplierResponse result = supplierService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("ООО Запчасти");
        assertThat(result.getIntegrationType()).isEqualTo("MANUAL");
    }

    @Test
    @DisplayName("getById - ошибка если поставщик не найден")
    void getById_throwsWhenNotFound() {
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("getActive - возвращает только активных")
    void getActive_returnsActiveOnly() {
        when(supplierRepository.findByActiveTrue()).thenReturn(List.of(testSupplier));

        List<SupplierResponse> result = supplierService.getActive();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("create - успешное создание поставщика")
    void create_success() {
        CreateSupplierRequest request = new CreateSupplierRequest();
        request.setName("Новый поставщик");
        request.setPhone("+79009876543");
        request.setEmail("new@example.com");

        when(supplierRepository.save(any(Supplier.class))).thenAnswer(inv -> {
            Supplier s = inv.getArgument(0);
            s.setId(2L);
            return s;
        });

        SupplierResponse result = supplierService.create(request);

        assertThat(result.getName()).isEqualTo("Новый поставщик");
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    @DisplayName("update - успешное обновление поставщика")
    void update_success() {
        CreateSupplierRequest request = new CreateSupplierRequest();
        request.setName("Обновлённое имя");
        request.setPhone("+79001111111");

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(testSupplier);

        SupplierResponse result = supplierService.update(1L, request);

        assertThat(testSupplier.getName()).isEqualTo("Обновлённое имя");
        verify(supplierRepository).save(testSupplier);
    }

    @Test
    @DisplayName("toggleActive - деактивация поставщика")
    void toggleActive_deactivate() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));

        supplierService.toggleActive(1L, false);

        assertThat(testSupplier.getActive()).isFalse();
        verify(supplierRepository).save(testSupplier);
    }

    @Test
    @DisplayName("toggleActive - активация поставщика")
    void toggleActive_activate() {
        testSupplier.setActive(false);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));

        supplierService.toggleActive(1L, true);

        assertThat(testSupplier.getActive()).isTrue();
    }

    @Test
    @DisplayName("getByIntegrationType - фильтр по типу интеграции")
    void getByIntegrationType_returnsList() {
        when(supplierRepository.findByIntegrationType(IntegrationType.MANUAL))
                .thenReturn(List.of(testSupplier));

        List<SupplierResponse> result = supplierService.getByIntegrationType("MANUAL");

        assertThat(result).hasSize(1);
    }
}
