package ru.papkov.repairlog.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.receipt.*;
import ru.papkov.repairlog.domain.exception.BusinessLogicException;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.*;
import ru.papkov.repairlog.domain.repository.*;

import java.util.List;

/**
 * Сервис управления чеками и платежами.
 * <p>
 * Возвращает entity — DTO-конверсия выполняется в контроллерах через маппер.
 * Для связанных коллекций (работы/платежи) используются специальные методы
 * {@link #getWorksByReceipt(Receipt)} и {@link #getPaymentsByReceipt(Receipt)}.
 * </p>
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
    public Receipt getByOrderId(Long orderId) {
        RepairOrder order = repairOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));
        return receiptRepository.findByRepairOrder(order)
                .orElseThrow(() -> new EntityNotFoundException("Чек не найден для заказа: " + orderId));
    }

    /**
     * Получить работы, относящиеся к чеку.
     */
    @Transactional(readOnly = true)
    public List<RepairWork> getWorksByReceipt(Receipt receipt) {
        return repairWorkRepository.findByReceipt(receipt);
    }

    /**
     * Получить платежи, относящиеся к чеку.
     */
    @Transactional(readOnly = true)
    public List<ReceiptPayment> getPaymentsByReceipt(Receipt receipt) {
        return receiptPaymentRepository.findByReceipt(receipt);
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
     *
     * <p>Проверки перед проведением платежа:</p>
     * <ul>
     *   <li>B-03: заказ должен иметь финальный или готовый статус (READY / ISSUED).</li>
     *   <li>B-04: transactionId уникален для данного чека — предотвращает дубли.</li>
     * </ul>
     */
    @Transactional
    public void processPayment(CreatePaymentRequest request, String acceptedByLogin) {
        Receipt receipt = receiptRepository.findByIdForUpdate(request.getReceiptId())
                .orElseThrow(() -> new EntityNotFoundException("Чек не найден"));

        // B-03: разрешаем оплату только когда заказ готов к выдаче или уже выдан
        String statusCode = receipt.getRepairOrder().getCurrentStatus().getCode();
        if (!java.util.Set.of(RepairOrderStatusCode.READY, RepairOrderStatusCode.ISSUED).contains(statusCode)) {
            throw new IllegalStateException(
                    "Оплата доступна только для заказов в статусе «Готов к выдаче» или «Выдан». " +
                    "Текущий статус: " + receipt.getRepairOrder().getCurrentStatus().getName());
        }

        // B-04: идемпотентность — отклоняем повторный transactionId
        String transactionId = request.getTransactionId();
        if (transactionId != null && !transactionId.isBlank() &&
                receiptPaymentRepository.existsByReceiptAndTransactionId(receipt, transactionId)) {
            throw new IllegalStateException("Платёж с таким идентификатором транзакции уже проведён");
        }

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
        payment.setTransactionId(transactionId);
        receiptPaymentRepository.save(payment);
        // обновление payment_status идёт через триггер в БД
    }
}
