package ru.papkov.repairlog.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.supply.SupplyDashboardResponse;
import ru.papkov.repairlog.domain.model.SupplyRequest;
import ru.papkov.repairlog.domain.model.SupplyRequestStatus;
import ru.papkov.repairlog.domain.repository.InventoryItemRepository;
import ru.papkov.repairlog.domain.repository.SupplyRequestRepository;
import ru.papkov.repairlog.domain.repository.SupplyRequestStatusRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Сервис для дашборда поставок — агрегация метрик.
 */
@Service
@Transactional(readOnly = true)
public class SupplyDashboardService {

    private final SupplyRequestRepository supplyRequestRepository;
    private final SupplyRequestStatusRepository statusRepository;
    private final InventoryItemRepository inventoryItemRepository;

    public SupplyDashboardService(SupplyRequestRepository supplyRequestRepository,
                                   SupplyRequestStatusRepository statusRepository,
                                   InventoryItemRepository inventoryItemRepository) {
        this.supplyRequestRepository = supplyRequestRepository;
        this.statusRepository = statusRepository;
        this.inventoryItemRepository = inventoryItemRepository;
    }

    public SupplyDashboardResponse getDashboard() {
        SupplyDashboardResponse dashboard = new SupplyDashboardResponse();

        // подсчёт заявок по статусам
        dashboard.setPendingApprovalCount(
                countByStatus(SupplyStatusConstants.NEW)
                + countByStatus(SupplyStatusConstants.AUTO_FORMED));
        dashboard.setAutoFormedCount(countByStatus(SupplyStatusConstants.AUTO_FORMED));
        dashboard.setOrderedCount(countByStatus(SupplyStatusConstants.ORDERED));
        dashboard.setInTransitCount(countByStatus(SupplyStatusConstants.IN_TRANSIT));

        // активные = всё кроме DELIVERED, PARTIALLY_DELIVERED, CANCELLED
        long delivered = countByStatus(SupplyStatusConstants.DELIVERED);
        long partiallyDelivered = countByStatus(SupplyStatusConstants.PARTIALLY_DELIVERED);
        long cancelled = countByStatus(SupplyStatusConstants.CANCELLED);
        long total = supplyRequestRepository.count();
        dashboard.setTotalActiveRequests(total - delivered - partiallyDelivered - cancelled);

        // просроченные доставки
        List<SupplyRequest> overdue = supplyRequestRepository.findOverdueDeliveries(
                SupplyStatusConstants.IN_TRANSIT, LocalDateTime.now());
        overdue.addAll(supplyRequestRepository.findOverdueDeliveries(
                SupplyStatusConstants.ORDERED, LocalDateTime.now()));
        dashboard.setOverdueCount(overdue.size());

        // складские метрики
        dashboard.setLowStockItemsCount(inventoryItemRepository.findLowStockItems().size());
        dashboard.setOutOfStockItemsCount(inventoryItemRepository.findOutOfStockItems().size());

        // общая сумма активных заявок
        BigDecimal pendingAmount = BigDecimal.ZERO;
        SupplyRequestStatus newStatus = statusRepository.findByName(SupplyStatusConstants.NEW).orElse(null);
        SupplyRequestStatus autoFormed = statusRepository.findByName(SupplyStatusConstants.AUTO_FORMED).orElse(null);
        SupplyRequestStatus approved = statusRepository.findByName(SupplyStatusConstants.APPROVED).orElse(null);
        SupplyRequestStatus ordered = statusRepository.findByName(SupplyStatusConstants.ORDERED).orElse(null);

        for (SupplyRequestStatus s : List.of(newStatus, autoFormed, approved, ordered)) {
            if (s != null) {
                for (SupplyRequest sr : supplyRequestRepository.findByStatus(s)) {
                    if (sr.getTotalAmount() != null) {
                        pendingAmount = pendingAmount.add(sr.getTotalAmount());
                    }
                }
            }
        }
        dashboard.setTotalPendingAmount(pendingAmount);

        return dashboard;
    }

    private long countByStatus(String statusName) {
        return statusRepository.findByName(statusName)
                .map(s -> supplyRequestRepository.countByStatus(s))
                .orElse(0L);
    }
}
