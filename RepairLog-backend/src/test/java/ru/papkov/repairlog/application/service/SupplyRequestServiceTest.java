package ru.papkov.repairlog.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.papkov.repairlog.application.dto.supply.SupplyRequestResponse;
import ru.papkov.repairlog.domain.exception.BusinessLogicException;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.*;
import ru.papkov.repairlog.domain.model.enums.SupplyRequestSource;
import ru.papkov.repairlog.domain.repository.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplyRequestServiceTest {

    @Mock private SupplyRequestRepository supplyRequestRepository;
    @Mock private SupplyRequestItemRepository supplyRequestItemRepository;
    @Mock private SupplyRequestStatusRepository statusRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private InventoryItemRepository inventoryItemRepository;
    @Mock private RepairOrderRepository repairOrderRepository;

    @InjectMocks
    private SupplyRequestService supplyRequestService;

    private SupplyRequest testRequest;
    private SupplyRequestStatus statusNew;
    private SupplyRequestStatus statusApproved;
    private SupplyRequestStatus statusOrdered;
    private SupplyRequestStatus statusInTransit;
    private SupplyRequestStatus statusDelivered;
    private SupplyRequestStatus statusCancelled;
    private Employee testAdmin;
    private Supplier testSupplier;

    @BeforeEach
    void setUp() {
        statusNew = new SupplyRequestStatus();
        statusNew.setId(1L);
        statusNew.setName("NEW");

        statusApproved = new SupplyRequestStatus();
        statusApproved.setId(2L);
        statusApproved.setName("APPROVED");

        statusOrdered = new SupplyRequestStatus();
        statusOrdered.setId(3L);
        statusOrdered.setName("ORDERED");

        statusInTransit = new SupplyRequestStatus();
        statusInTransit.setId(4L);
        statusInTransit.setName("IN_TRANSIT");

        statusDelivered = new SupplyRequestStatus();
        statusDelivered.setId(5L);
        statusDelivered.setName("DELIVERED");

        statusCancelled = new SupplyRequestStatus();
        statusCancelled.setId(6L);
        statusCancelled.setName("CANCELLED");

        testAdmin = new Employee();
        testAdmin.setId(1L);
        testAdmin.setName("Админ");
        testAdmin.setSurname("Админов");
        testAdmin.setLogin("admin");

        testSupplier = new Supplier();
        testSupplier.setId(1L);
        testSupplier.setName("ООО Запчасти");

        testRequest = new SupplyRequest();
        testRequest.setId(1L);
        testRequest.setRequestNumber("SR-20260228-0001");
        testRequest.setStatus(statusNew);
        testRequest.setRequestedBy(testAdmin);
        testRequest.setSource(SupplyRequestSource.MANUAL);
    }

    @Test
    @DisplayName("getById - возвращает заявку по ID")
    void getById_returnsRequest() {
        when(supplyRequestRepository.findById(1L)).thenReturn(Optional.of(testRequest));

        SupplyRequestResponse result = supplyRequestService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getRequestNumber()).isEqualTo("SR-20260228-0001");
        assertThat(result.getStatusName()).isEqualTo("NEW");
    }

    @Test
    @DisplayName("getById - ошибка если заявка не найдена")
    void getById_throwsWhenNotFound() {
        when(supplyRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplyRequestService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("approve - успешное подтверждение заявки из NEW")
    void approve_fromNew_success() {
        when(supplyRequestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(employeeRepository.findByLogin("admin")).thenReturn(Optional.of(testAdmin));
        when(statusRepository.findByName("APPROVED")).thenReturn(Optional.of(statusApproved));
        when(supplyRequestRepository.save(any())).thenReturn(testRequest);

        SupplyRequestResponse result = supplyRequestService.approve(1L, "admin");

        assertThat(testRequest.getStatus()).isEqualTo(statusApproved);
        assertThat(testRequest.getApprovedBy()).isEqualTo(testAdmin);
    }

    @Test
    @DisplayName("approve - ошибка если заявка в неверном статусе")
    void approve_throwsWhenWrongStatus() {
        testRequest.setStatus(statusOrdered);
        when(supplyRequestRepository.findById(1L)).thenReturn(Optional.of(testRequest));

        assertThatThrownBy(() -> supplyRequestService.approve(1L, "admin"))
                .isInstanceOf(BusinessLogicException.class);
    }

    @Test
    @DisplayName("markOrdered - успешный перевод в ORDERED")
    void markOrdered_success() {
        testRequest.setStatus(statusApproved);
        when(supplyRequestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(statusRepository.findByName("ORDERED")).thenReturn(Optional.of(statusOrdered));
        when(supplyRequestRepository.save(any())).thenReturn(testRequest);

        supplyRequestService.markOrdered(1L);

        assertThat(testRequest.getStatus()).isEqualTo(statusOrdered);
    }

    @Test
    @DisplayName("markOrdered - ошибка из неверного статуса")
    void markOrdered_throwsFromWrongStatus() {
        testRequest.setStatus(statusNew); // должен быть APPROVED
        when(supplyRequestRepository.findById(1L)).thenReturn(Optional.of(testRequest));

        assertThatThrownBy(() -> supplyRequestService.markOrdered(1L))
                .isInstanceOf(BusinessLogicException.class);
    }

    @Test
    @DisplayName("markInTransit - успешный перевод в IN_TRANSIT")
    void markInTransit_success() {
        testRequest.setStatus(statusOrdered);
        when(supplyRequestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(statusRepository.findByName("IN_TRANSIT")).thenReturn(Optional.of(statusInTransit));
        when(supplyRequestRepository.save(any())).thenReturn(testRequest);

        supplyRequestService.markInTransit(1L);

        assertThat(testRequest.getStatus()).isEqualTo(statusInTransit);
    }

    @Test
    @DisplayName("markDelivered - успешная доставка из ORDERED")
    void markDelivered_fromOrdered() {
        testRequest.setStatus(statusOrdered);
        when(supplyRequestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(statusRepository.findByName("DELIVERED")).thenReturn(Optional.of(statusDelivered));
        when(supplyRequestRepository.save(any())).thenReturn(testRequest);

        supplyRequestService.markDelivered(1L);

        assertThat(testRequest.getStatus()).isEqualTo(statusDelivered);
    }

    @Test
    @DisplayName("markDelivered - ошибка из неверного статуса")
    void markDelivered_throwsFromWrongStatus() {
        testRequest.setStatus(statusNew);
        when(supplyRequestRepository.findById(1L)).thenReturn(Optional.of(testRequest));

        assertThatThrownBy(() -> supplyRequestService.markDelivered(1L))
                .isInstanceOf(BusinessLogicException.class);
    }

    @Test
    @DisplayName("cancel - успешная отмена заявки")
    void cancel_success() {
        when(supplyRequestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(statusRepository.findByName("CANCELLED")).thenReturn(Optional.of(statusCancelled));
        when(supplyRequestRepository.save(any())).thenReturn(testRequest);

        supplyRequestService.cancel(1L);

        assertThat(testRequest.getStatus()).isEqualTo(statusCancelled);
    }

    @Test
    @DisplayName("cancel - ошибка если заявка уже доставлена")
    void cancel_throwsWhenDelivered() {
        testRequest.setStatus(statusDelivered);
        when(supplyRequestRepository.findById(1L)).thenReturn(Optional.of(testRequest));

        assertThatThrownBy(() -> supplyRequestService.cancel(1L))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("доставленную");
    }

    @Test
    @DisplayName("assignSupplier - привязка поставщика к заявке")
    void assignSupplier_success() {
        when(supplyRequestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(supplyRequestRepository.save(any())).thenReturn(testRequest);

        supplyRequestService.assignSupplier(1L, 1L);

        assertThat(testRequest.getSupplier()).isEqualTo(testSupplier);
    }

    @Test
    @DisplayName("assignSupplier - ошибка если поставщик не найден")
    void assignSupplier_throwsWhenSupplierNotFound() {
        when(supplyRequestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplyRequestService.assignSupplier(1L, 99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("updateExternalOrderInfo - успешное обновление статуса внешнего заказа")
    void updateExternalOrderInfo_success() {
        testRequest.setExternalOrderId("EXT-001");
        when(supplyRequestRepository.findByExternalOrderId("EXT-001"))
                .thenReturn(Optional.of(testRequest));
        when(supplyRequestRepository.save(any())).thenReturn(testRequest);

        supplyRequestService.updateExternalOrderInfo("EXT-001", "SHIPPED");

        assertThat(testRequest.getExternalOrderStatus()).isEqualTo("SHIPPED");
        verify(supplyRequestRepository).findByExternalOrderId("EXT-001");
        verify(supplyRequestRepository, never()).findAll();
    }

    @Test
    @DisplayName("updateExternalOrderInfo - ошибка если заявка не найдена")
    void updateExternalOrderInfo_throwsWhenNotFound() {
        when(supplyRequestRepository.findByExternalOrderId("EXT-999"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplyRequestService.updateExternalOrderInfo("EXT-999", "SHIPPED"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("EXT-999");
    }
}
