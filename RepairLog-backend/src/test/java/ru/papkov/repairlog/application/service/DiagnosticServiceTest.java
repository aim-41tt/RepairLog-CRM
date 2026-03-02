package ru.papkov.repairlog.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.papkov.repairlog.application.dto.diagnostic.CreateDiagnosticRequest;
import ru.papkov.repairlog.application.dto.diagnostic.DiagnosticResponse;
import ru.papkov.repairlog.domain.exception.BusinessLogicException;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.*;
import ru.papkov.repairlog.domain.repository.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosticServiceTest {

    @Mock private DiagnosticRepository diagnosticRepository;
    @Mock private RepairOrderRepository repairOrderRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private RepairStatusRepository repairStatusRepository;
    @Mock private StatusHistoryRepository statusHistoryRepository;

    @InjectMocks
    private DiagnosticService diagnosticService;

    private RepairOrder testOrder;
    private Employee testTechnician;
    private Diagnostic testDiagnostic;

    @BeforeEach
    void setUp() {
        Client client = new Client();
        client.setId(1L);
        client.setName("Иван");
        client.setSurname("Петров");
        client.setPatronymic("С");

        testOrder = new RepairOrder();
        testOrder.setId(1L);
        testOrder.setOrderNumber("RO-20260228-0001");
        testOrder.setClient(client);

        testTechnician = new Employee();
        testTechnician.setId(1L);
        testTechnician.setName("Мастер");
        testTechnician.setSurname("Техников");
        testTechnician.setLogin("tech1");

        testDiagnostic = new Diagnostic();
        testDiagnostic.setId(1L);
        testDiagnostic.setRepairOrder(testOrder);
        testDiagnostic.setDescription("Неисправен экран");
        testDiagnostic.setSolution("Замена экрана");
        testDiagnostic.setPerformedBy(testTechnician);
    }

    @Test
    @DisplayName("getByOrderId - возвращает диагностику по ID заказа")
    void getByOrderId_returnsDiagnostic() {
        when(diagnosticRepository.findByRepairOrderId(1L)).thenReturn(Optional.of(testDiagnostic));

        DiagnosticResponse result = diagnosticService.getByOrderId(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Неисправен экран");
        assertThat(result.getSolution()).isEqualTo("Замена экрана");
        assertThat(result.getRepairOrderId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getByOrderId - ошибка если диагностика не найдена")
    void getByOrderId_throwsWhenNotFound() {
        when(diagnosticRepository.findByRepairOrderId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diagnosticService.getByOrderId(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("create - успешное создание диагностики")
    void create_success() {
        CreateDiagnosticRequest request = new CreateDiagnosticRequest();
        request.setRepairOrderId(1L);
        request.setDescription("Неисправен экран");
        request.setSolution("Замена экрана");

        when(repairOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(employeeRepository.findByLogin("tech1")).thenReturn(Optional.of(testTechnician));
        when(diagnosticRepository.findByRepairOrderId(1L)).thenReturn(Optional.empty());
        when(diagnosticRepository.save(any(Diagnostic.class))).thenAnswer(inv -> {
            Diagnostic d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });

        RepairStatus diagStatus = new RepairStatus();
        diagStatus.setId(3L);
        diagStatus.setName("Диагностика");
        when(repairStatusRepository.findByName("Диагностика")).thenReturn(Optional.of(diagStatus));
        when(repairOrderRepository.save(any(RepairOrder.class))).thenReturn(testOrder);

        DiagnosticResponse result = diagnosticService.create(request, "tech1");

        assertThat(result.getDescription()).isEqualTo("Неисправен экран");
        verify(diagnosticRepository).save(any(Diagnostic.class));
        verify(statusHistoryRepository).save(any(StatusHistory.class));
    }

    @Test
    @DisplayName("create - ошибка если заказ не найден")
    void create_throwsWhenOrderNotFound() {
        CreateDiagnosticRequest request = new CreateDiagnosticRequest();
        request.setRepairOrderId(99L);

        when(repairOrderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diagnosticService.create(request, "tech1"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("create - ошибка если сотрудник не найден")
    void create_throwsWhenEmployeeNotFound() {
        CreateDiagnosticRequest request = new CreateDiagnosticRequest();
        request.setRepairOrderId(1L);

        when(repairOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(employeeRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> diagnosticService.create(request, "unknown"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("create - ошибка если диагностика уже существует")
    void create_throwsWhenAlreadyExists() {
        CreateDiagnosticRequest request = new CreateDiagnosticRequest();
        request.setRepairOrderId(1L);

        when(repairOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(employeeRepository.findByLogin("tech1")).thenReturn(Optional.of(testTechnician));
        when(diagnosticRepository.findByRepairOrderId(1L)).thenReturn(Optional.of(testDiagnostic));

        assertThatThrownBy(() -> diagnosticService.create(request, "tech1"))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("уже существует");
    }
}
