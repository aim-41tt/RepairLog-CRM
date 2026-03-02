package ru.papkov.repairlog.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.papkov.repairlog.application.dto.receipt.AddRepairWorkRequest;
import ru.papkov.repairlog.application.dto.receipt.CreatePaymentRequest;
import ru.papkov.repairlog.application.dto.receipt.ReceiptResponse;
import ru.papkov.repairlog.domain.exception.BusinessLogicException;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.*;
import ru.papkov.repairlog.domain.repository.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {

    @Mock private ReceiptRepository receiptRepository;
    @Mock private RepairOrderRepository repairOrderRepository;
    @Mock private RepairWorkRepository repairWorkRepository;
    @Mock private ReceiptPaymentRepository receiptPaymentRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private ReceiptService receiptService;

    private RepairOrder testOrder;
    private Receipt testReceipt;
    private Employee testEmployee;

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

        testReceipt = new Receipt();
        testReceipt.setId(1L);
        testReceipt.setRepairOrder(testOrder);
        testReceipt.setSubtotal(new BigDecimal("5000.00"));
        testReceipt.setTotalAmount(new BigDecimal("5500.00"));
        testReceipt.setTaxAmount(new BigDecimal("500.00"));

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("Мастер");
        testEmployee.setSurname("Техников");
        testEmployee.setLogin("tech1");
    }

    @Test
    @DisplayName("getByOrderId - возвращает чек по заказу")
    void getByOrderId_returnsReceipt() {
        when(repairOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(receiptRepository.findByRepairOrder(testOrder)).thenReturn(Optional.of(testReceipt));
        when(repairWorkRepository.findByReceipt(testReceipt)).thenReturn(List.of());
        when(receiptPaymentRepository.findByReceipt(testReceipt)).thenReturn(List.of());

        ReceiptResponse result = receiptService.getByOrderId(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getOrderNumber()).isEqualTo("RO-20260228-0001");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("5500.00");
    }

    @Test
    @DisplayName("getByOrderId - ошибка если заказ не найден")
    void getByOrderId_throwsWhenOrderNotFound() {
        when(repairOrderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> receiptService.getByOrderId(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("getByOrderId - ошибка если чек не найден")
    void getByOrderId_throwsWhenReceiptNotFound() {
        when(repairOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(receiptRepository.findByRepairOrder(testOrder)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> receiptService.getByOrderId(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("addWork - успешное добавление работы")
    void addWork_success() {
        AddRepairWorkRequest request = new AddRepairWorkRequest();
        request.setReceiptId(1L);
        request.setDescription("Замена экрана");
        request.setPrice(new BigDecimal("3000.00"));

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(testReceipt));
        when(employeeRepository.findByLogin("tech1")).thenReturn(Optional.of(testEmployee));

        receiptService.addWork(request, "tech1");

        verify(repairWorkRepository).save(any(RepairWork.class));
    }

    @Test
    @DisplayName("addWork - ошибка если чек заблокирован")
    void addWork_throwsWhenLocked() {
        testReceipt.lock(testEmployee);

        AddRepairWorkRequest request = new AddRepairWorkRequest();
        request.setReceiptId(1L);

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(testReceipt));

        assertThatThrownBy(() -> receiptService.addWork(request, "tech1"))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessageContaining("заблокирован");
    }

    @Test
    @DisplayName("processPayment - успешная оплата")
    void processPayment_success() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setReceiptId(1L);
        request.setPaidAmount(new BigDecimal("5500.00"));
        request.setPaymentMethod("CASH");

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(testReceipt));
        when(employeeRepository.findByLogin("tech1")).thenReturn(Optional.of(testEmployee));

        receiptService.processPayment(request, "tech1");

        verify(receiptPaymentRepository).save(any(ReceiptPayment.class));
        verify(receiptRepository).save(testReceipt); // чек блокируется при первом платеже
        assertThat(testReceipt.getLocked()).isTrue();
    }

    @Test
    @DisplayName("processPayment - не блокирует повторно при втором платеже")
    void processPayment_doesNotRelockOnSecondPayment() {
        testReceipt.lock(testEmployee); // уже заблокирован

        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setReceiptId(1L);
        request.setPaidAmount(new BigDecimal("1000.00"));
        request.setPaymentMethod("CARD");

        when(receiptRepository.findById(1L)).thenReturn(Optional.of(testReceipt));
        when(employeeRepository.findByLogin("tech1")).thenReturn(Optional.of(testEmployee));

        receiptService.processPayment(request, "tech1");

        verify(receiptPaymentRepository).save(any(ReceiptPayment.class));
        verify(receiptRepository, never()).save(testReceipt); // не сохраняет повторно
    }
}
