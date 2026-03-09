package ru.papkov.repairlog.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.receipt.*;
import ru.papkov.repairlog.domain.exception.BusinessLogicException;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.*;
import ru.papkov.repairlog.domain.repository.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис управления чеками и платежами.
 *
 * @author aim-41tt
 */
@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final RepairOrderRepository repairOrderRepository;
    private final RepairWorkRepository repairWorkRepository;
    private final ReceiptPaymentRepository receiptPaymentRepository;
    private final EmployeeRepository employeeRepository;

    public ReceiptService(ReceiptRepository receiptRepository,
                          RepairOrderRepository repairOrderRepository,
                          RepairWorkRepository repairWorkRepository,
                          ReceiptPaymentRepository receiptPaymentRepository,
                          EmployeeRepository employeeRepository) {
        this.receiptRepository = receiptRepository;
        this.repairOrderRepository = repairOrderRepository;
        this.repairWorkRepository = repairWorkRepository;
        this.receiptPaymentRepository = receiptPaymentRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Получить чек по заказу на ремонт.
     */
    @Transactional(readOnly = true)
    public ReceiptResponse getByOrderId(Long orderId) {
        RepairOrder order = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));
        Receipt receipt = receiptRepository.findByRepairOrder(order)
                .orElseThrow(() -> new EntityNotFoundException("Чек не найден для заказа: " + orderId));
        return toResponse(receipt);
    }

    /**
     * Добавить работу к чеку (TECHNICIAN).
     */
    @Transactional
    public void addWork(AddRepairWorkRequest request, String employeeLogin) {
        Receipt receipt = receiptRepository.findById(request.getReceiptId())
                .orElseThrow(() -> new EntityNotFoundException("Чек не найден"));

        // Проверяем, что чек принадлежит заказу, назначенному на текущего техника
        RepairOrder order = receipt.getRepairOrder();
        if (order.getAssignedMaster() == null ||
                !order.getAssignedMaster().getLogin().equals(employeeLogin)) {
            throw new BusinessLogicException("Вы не являетесь мастером этого заказа");
        }

        if (!receipt.isEditable()) {
            throw new BusinessLogicException("Чек заблокирован для редактирования");
        }

        Employee employee = employeeRepository.findByLogin(employeeLogin)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден"));

        RepairWork work = new RepairWork();
        work.setReceipt(receipt);
        work.setEmployee(employee);
        work.setDescription(request.getDescription());
        work.setPrice(request.getPrice());
        repairWorkRepository.save(work);
        // пересчёт суммы чека идёт через триггер в БД
    }

    /**
     * Провести оплату (RECEPTIONIST).
     * Используется PESSIMISTIC_WRITE для предотвращения race condition при параллельных платежах.
     */
    @Transactional
    public void processPayment(CreatePaymentRequest request, String acceptedByLogin) {
        Receipt receipt = receiptRepository.findByIdForUpdate(request.getReceiptId())
                .orElseThrow(() -> new EntityNotFoundException("Чек не найден"));

        Employee acceptedBy = employeeRepository.findByLogin(acceptedByLogin)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден"));

        // блокируем чек при первом платеже
        if (!receipt.getLocked()) {
            receipt.lock(acceptedBy);
            receiptRepository.save(receipt);
        }

        ReceiptPayment payment = new ReceiptPayment();
        payment.setReceipt(receipt);
        payment.setPaidAmount(request.getPaidAmount());
        payment.setPaymentMethod(ReceiptPayment.PaymentMethod.valueOf(request.getPaymentMethod()));
        payment.setAcceptedBy(acceptedBy);
        payment.setTransactionId(request.getTransactionId());
        receiptPaymentRepository.save(payment);
        // обновление payment_status идёт через триггер в БД
    }

    // ========== Helpers ==========

    private ReceiptResponse toResponse(Receipt receipt) {
        ReceiptResponse r = new ReceiptResponse();
        r.setId(receipt.getId());
        r.setRepairOrderId(receipt.getRepairOrder().getId());
        r.setOrderNumber(receipt.getRepairOrder().getOrderNumber());
        r.setSubtotal(receipt.getSubtotal());
        r.setDiscountAmount(receipt.getDiscountAmount());
        r.setTaxAmount(receipt.getTaxAmount());
        r.setTotalAmount(receipt.getTotalAmount());
        r.setPaymentStatus(receipt.getPaymentStatus().name());
        r.setLocked(receipt.getLocked());
        r.setCreatedAt(receipt.getCreatedAt());

        // работы
        List<ReceiptResponse.RepairWorkResponse> works = repairWorkRepository.findByReceipt(receipt).stream()
                .map(w -> {
                    ReceiptResponse.RepairWorkResponse wr = new ReceiptResponse.RepairWorkResponse();
                    wr.setId(w.getId());
                    wr.setDescription(w.getDescription());
                    wr.setPrice(w.getPrice());
                    wr.setEmployeeName(w.getEmployee().getFullName());
                    wr.setCompletedAt(w.getCompletedAt());
                    return wr;
                }).collect(Collectors.toList());
        r.setWorks(works);

        // платежи
        List<ReceiptResponse.PaymentResponse> payments = receiptPaymentRepository.findByReceipt(receipt).stream()
                .map(p -> {
                    ReceiptResponse.PaymentResponse pr = new ReceiptResponse.PaymentResponse();
                    pr.setId(p.getId());
                    pr.setPaidAmount(p.getPaidAmount());
                    pr.setPaymentMethod(p.getPaymentMethod().name());
                    pr.setPaidAt(p.getPaidAt());
                    pr.setAcceptedByName(p.getAcceptedBy().getFullName());
                    return pr;
                }).collect(Collectors.toList());
        r.setPayments(payments);

        return r;
    }
}
