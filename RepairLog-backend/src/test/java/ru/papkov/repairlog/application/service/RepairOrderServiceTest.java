package ru.papkov.repairlog.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.papkov.repairlog.application.dto.order.ChangeStatusRequest;
import ru.papkov.repairlog.application.dto.order.CreateRepairOrderRequest;
import ru.papkov.repairlog.application.dto.order.RepairOrderResponse;
import ru.papkov.repairlog.domain.exception.BusinessLogicException;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.*;
import ru.papkov.repairlog.domain.repository.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairOrderServiceTest {

    @Mock private RepairOrderRepository repairOrderRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private DeviceRepository deviceRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private RepairStatusRepository repairStatusRepository;
    @Mock private RepairPriorityRepository repairPriorityRepository;
    @Mock private StatusHistoryRepository statusHistoryRepository;
    @Mock private ReceiptRepository receiptRepository;

    @InjectMocks
    private RepairOrderService repairOrderService;

    private Client testClient;
    private Device testDevice;
    private Employee testReceptionist;
    private Employee testTechnician;
    private RepairStatus statusNew;
    private RepairStatus statusAccepted;
    private RepairOrder testOrder;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setId(1L);
        testClient.setName("Иван");
        testClient.setSurname("Петров");
        testClient.setPatronymic("С");
        testClient.setPhone("+79001234567");

        testDevice = new Device();
        testDevice.setId(1L);

        testReceptionist = new Employee();
        testReceptionist.setId(1L);
        testReceptionist.setName("Анна");
        testReceptionist.setSurname("Рецепционист");
        testReceptionist.setLogin("receptionist1");

        Role techRole = new Role();
        techRole.setId(1L);
        techRole.setName("TECHNICIAN");

        testTechnician = new Employee();
        testTechnician.setId(2L);
        testTechnician.setName("Мастер");
        testTechnician.setSurname("Техников");
        testTechnician.setLogin("tech1");
        testTechnician.setRoles(new HashSet<>(Set.of(techRole)));

        statusNew = new RepairStatus();
        statusNew.setId(1L);
        statusNew.setName("Новая");

        statusAccepted = new RepairStatus();
        statusAccepted.setId(2L);
        statusAccepted.setName("Принята");

        testOrder = new RepairOrder();
        testOrder.setId(1L);
        testOrder.setOrderNumber("RO-20260228-0001");
        testOrder.setClient(testClient);
        testOrder.setDevice(testDevice);
        testOrder.setAcceptedBy(testReceptionist);
        testOrder.setCurrentStatus(statusNew);
        testOrder.setClientComplaint("Не включается");
        testOrder.setWarrantyRepair(false);
    }

    @Test
    @DisplayName("getAllActive - возвращает активные заказы")
    void getAllActive_returnsList() {
        when(repairOrderRepository.findAllActiveOrders()).thenReturn(List.of(testOrder));
        when(receiptRepository.findByRepairOrder(any())).thenReturn(Optional.empty());

        List<RepairOrderResponse> result = repairOrderService.getAllActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOrderNumber()).isEqualTo("RO-20260228-0001");
    }

    @Test
    @DisplayName("getUnassigned - возвращает заказы без мастера")
    void getUnassigned_returnsList() {
        when(repairOrderRepository.findUnassignedOrders()).thenReturn(List.of(testOrder));
        when(receiptRepository.findByRepairOrder(any())).thenReturn(Optional.empty());

        List<RepairOrderResponse> result = repairOrderService.getUnassigned();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAssignedMasterName()).isNull();
    }

    @Test
    @DisplayName("getByMaster - возвращает заказы мастера")
    void getByMaster_returnsList() {
        testOrder.setAssignedMaster(testTechnician);
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(testTechnician));
        when(repairOrderRepository.findActiveOrdersByMaster(testTechnician)).thenReturn(List.of(testOrder));
        when(receiptRepository.findByRepairOrder(any())).thenReturn(Optional.empty());

        List<RepairOrderResponse> result = repairOrderService.getByMaster(2L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getByMaster - ошибка если мастер не найден")
    void getByMaster_throwsWhenNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repairOrderService.getByMaster(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("getById - возвращает заказ по ID")
    void getById_returnsOrder() {
        when(repairOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(receiptRepository.findByRepairOrder(testOrder)).thenReturn(Optional.empty());

        RepairOrderResponse result = repairOrderService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCurrentStatusName()).isEqualTo("Новая");
    }

    @Test
    @DisplayName("getById - ошибка если заказ не найден")
    void getById_throwsWhenNotFound() {
        when(repairOrderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repairOrderService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("getByOrderNumber - возвращает заказ по номеру")
    void getByOrderNumber_returnsOrder() {
        when(repairOrderRepository.findByOrderNumber("RO-20260228-0001")).thenReturn(Optional.of(testOrder));
        when(receiptRepository.findByRepairOrder(testOrder)).thenReturn(Optional.empty());

        RepairOrderResponse result = repairOrderService.getByOrderNumber("RO-20260228-0001");

        assertThat(result.getOrderNumber()).isEqualTo("RO-20260228-0001");
    }

    @Test
    @DisplayName("getByClient - возвращает заказы клиента")
    void getByClient_returnsList() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(repairOrderRepository.findByClient(testClient)).thenReturn(List.of(testOrder));
        when(receiptRepository.findByRepairOrder(any())).thenReturn(Optional.empty());

        List<RepairOrderResponse> result = repairOrderService.getByClient(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("create - успешное создание заказа")
    void create_success() {
        CreateRepairOrderRequest request = new CreateRepairOrderRequest();
        request.setClientId(1L);
        request.setDeviceId(1L);
        request.setClientComplaint("Не включается");
        request.setWarrantyRepair(false);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));
        when(employeeRepository.findByLogin("receptionist1")).thenReturn(Optional.of(testReceptionist));
        when(repairStatusRepository.findByName("Новая")).thenReturn(Optional.of(statusNew));
        when(repairOrderRepository.save(any(RepairOrder.class))).thenAnswer(inv -> {
            RepairOrder o = inv.getArgument(0);
            o.setId(1L);
            o.setOrderNumber("RO-20260228-0001");
            return o;
        });
        when(receiptRepository.findByRepairOrder(any())).thenReturn(Optional.empty());

        RepairOrderResponse result = repairOrderService.create(request, "receptionist1");

        assertThat(result.getClientComplaint()).isEqualTo("Не включается");
        verify(statusHistoryRepository).save(any(StatusHistory.class));
    }

    @Test
    @DisplayName("create - ошибка если клиент не найден")
    void create_throwsWhenClientNotFound() {
        CreateRepairOrderRequest request = new CreateRepairOrderRequest();
        request.setClientId(99L);

        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repairOrderService.create(request, "admin"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("create - создание заказа с приоритетом")
    void create_withPriority() {
        RepairPriority priority = new RepairPriority();
        priority.setId(1L);
        priority.setName("Срочный");

        CreateRepairOrderRequest request = new CreateRepairOrderRequest();
        request.setClientId(1L);
        request.setDeviceId(1L);
        request.setPriorityId(1L);

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(deviceRepository.findById(1L)).thenReturn(Optional.of(testDevice));
        when(employeeRepository.findByLogin("receptionist1")).thenReturn(Optional.of(testReceptionist));
        when(repairStatusRepository.findByName("Новая")).thenReturn(Optional.of(statusNew));
        when(repairPriorityRepository.findById(1L)).thenReturn(Optional.of(priority));
        when(repairOrderRepository.save(any(RepairOrder.class))).thenAnswer(inv -> {
            RepairOrder o = inv.getArgument(0);
            o.setId(1L);
            o.setOrderNumber("RO-20260228-0002");
            return o;
        });
        when(receiptRepository.findByRepairOrder(any())).thenReturn(Optional.empty());

        RepairOrderResponse result = repairOrderService.create(request, "receptionist1");

        assertThat(result.getPriorityName()).isEqualTo("Срочный");
    }

    @Test
    @DisplayName("assignMaster - успешное назначение мастера")
    void assignMaster_success() {
        when(repairOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(testTechnician));
        when(repairStatusRepository.findByName("Принята")).thenReturn(Optional.of(statusAccepted));
        when(repairOrderRepository.save(any(RepairOrder.class))).thenReturn(testOrder);
        when(receiptRepository.findByRepairOrder(any())).thenReturn(Optional.empty());

        RepairOrderResponse result = repairOrderService.assignMaster(1L, 2L);

        assertThat(testOrder.getAssignedMaster()).isEqualTo(testTechnician);
        verify(statusHistoryRepository).save(any(StatusHistory.class));
    }

    @Test
    @DisplayName("assignMaster - ошибка если сотрудник не TECHNICIAN")
    void assignMaster_throwsWhenNotTechnician() {
        Employee notTech = new Employee();
        notTech.setId(3L);
        notTech.setName("Не");
        notTech.setSurname("Техник");
        notTech.setRoles(new HashSet<>());

        when(repairOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(employeeRepository.findById(3L)).thenReturn(Optional.of(notTech));

        assertThatThrownBy(() -> repairOrderService.assignMaster(1L, 3L))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("не является техником");
    }

    @Test
    @DisplayName("changeStatus - успешная смена статуса")
    void changeStatus_success() {
        RepairStatus inRepairStatus = new RepairStatus();
        inRepairStatus.setId(3L);
        inRepairStatus.setName("В ремонте");

        ChangeStatusRequest request = new ChangeStatusRequest();
        request.setStatusId(3L);
        request.setComment("Начат ремонт");

        when(repairOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(repairStatusRepository.findById(3L)).thenReturn(Optional.of(inRepairStatus));
        when(employeeRepository.findByLogin("tech1")).thenReturn(Optional.of(testTechnician));
        when(repairOrderRepository.save(any(RepairOrder.class))).thenReturn(testOrder);
        when(receiptRepository.findByRepairOrder(any())).thenReturn(Optional.empty());

        RepairOrderResponse result = repairOrderService.changeStatus(1L, request, "tech1");

        assertThat(testOrder.getCurrentStatus()).isEqualTo(inRepairStatus);
        verify(statusHistoryRepository).save(any(StatusHistory.class));
    }

    @Test
    @DisplayName("changeStatus - статус 'Выдан' автоматически завершает заказ")
    void changeStatus_completesOnIssued() {
        RepairStatus issuedStatus = new RepairStatus();
        issuedStatus.setId(5L);
        issuedStatus.setName("Выдан");

        ChangeStatusRequest request = new ChangeStatusRequest();
        request.setStatusId(5L);

        when(repairOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(repairStatusRepository.findById(5L)).thenReturn(Optional.of(issuedStatus));
        when(employeeRepository.findByLogin("receptionist1")).thenReturn(Optional.of(testReceptionist));
        when(repairOrderRepository.save(any(RepairOrder.class))).thenReturn(testOrder);
        when(receiptRepository.findByRepairOrder(any())).thenReturn(Optional.empty());

        repairOrderService.changeStatus(1L, request, "receptionist1");

        assertThat(testOrder.getActualCompletionDate()).isNotNull();
    }

    @Test
    @DisplayName("changeStatus - статус 'Ремонт завершен' автоматически завершает заказ")
    void changeStatus_completesOnRepairDone() {
        RepairStatus doneStatus = new RepairStatus();
        doneStatus.setId(6L);
        doneStatus.setName("Ремонт завершен");

        ChangeStatusRequest request = new ChangeStatusRequest();
        request.setStatusId(6L);

        when(repairOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(repairStatusRepository.findById(6L)).thenReturn(Optional.of(doneStatus));
        when(employeeRepository.findByLogin("tech1")).thenReturn(Optional.of(testTechnician));
        when(repairOrderRepository.save(any(RepairOrder.class))).thenReturn(testOrder);
        when(receiptRepository.findByRepairOrder(any())).thenReturn(Optional.empty());

        repairOrderService.changeStatus(1L, request, "tech1");

        assertThat(testOrder.getActualCompletionDate()).isNotNull();
    }
}
