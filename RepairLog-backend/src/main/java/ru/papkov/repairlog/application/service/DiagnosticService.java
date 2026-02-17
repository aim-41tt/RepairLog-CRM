package ru.papkov.repairlog.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.diagnostic.*;
import ru.papkov.repairlog.domain.exception.BusinessLogicException;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.*;
import ru.papkov.repairlog.domain.repository.*;

/**
 * Сервис управления диагностикой устройств.
 *
 * @author aim-41tt
 */
@Service
public class DiagnosticService {

    private final DiagnosticRepository diagnosticRepository;
    private final RepairOrderRepository repairOrderRepository;
    private final EmployeeRepository employeeRepository;
    private final RepairStatusRepository repairStatusRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    public DiagnosticService(DiagnosticRepository diagnosticRepository,
                             RepairOrderRepository repairOrderRepository,
                             EmployeeRepository employeeRepository,
                             RepairStatusRepository repairStatusRepository,
                             StatusHistoryRepository statusHistoryRepository) {
        this.diagnosticRepository = diagnosticRepository;
        this.repairOrderRepository = repairOrderRepository;
        this.employeeRepository = employeeRepository;
        this.repairStatusRepository = repairStatusRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @Transactional(readOnly = true)
    public DiagnosticResponse getByOrderId(Long orderId) {
        Diagnostic diag = diagnosticRepository.findByRepairOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Диагностика не найдена для заказа: " + orderId));
        return toResponse(diag);
    }

    /**
     * Создание результата диагностики (TECHNICIAN).
     */
    @Transactional
    public DiagnosticResponse create(CreateDiagnosticRequest request, String performedByLogin) {
        RepairOrder order = repairOrderRepository.findById(request.getRepairOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));
        Employee performedBy = employeeRepository.findByLogin(performedByLogin)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден"));

        // один заказ — одна диагностика
        diagnosticRepository.findByRepairOrderId(order.getId()).ifPresent(d -> {
            throw new BusinessLogicException("Диагностика уже существует для этого заказа");
        });

        Diagnostic diag = new Diagnostic();
        diag.setRepairOrder(order);
        diag.setDescription(request.getDescription());
        diag.setSolution(request.getSolution());
        diag.setPerformedBy(performedBy);

        Diagnostic saved = diagnosticRepository.save(diag);

        // обновляем статус заказа на "Диагностика"
        repairStatusRepository.findByName("Диагностика").ifPresent(status -> {
            order.setCurrentStatus(status);
            repairOrderRepository.save(order);

            StatusHistory history = new StatusHistory();
            history.setRepairOrder(order);
            history.setStatus(status);
            history.setChangedBy(performedBy);
            history.setComment("Диагностика проведена");
            statusHistoryRepository.save(history);
        });

        return toResponse(saved);
    }

    private DiagnosticResponse toResponse(Diagnostic d) {
        DiagnosticResponse r = new DiagnosticResponse();
        r.setId(d.getId());
        r.setRepairOrderId(d.getRepairOrder().getId());
        r.setOrderNumber(d.getRepairOrder().getOrderNumber());
        r.setDescription(d.getDescription());
        r.setSolution(d.getSolution());
        r.setPerformedByName(d.getPerformedBy().getFullName());
        r.setCreatedAt(d.getCreatedAt());
        return r;
    }
}
