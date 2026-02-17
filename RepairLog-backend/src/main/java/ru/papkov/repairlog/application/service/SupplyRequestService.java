package ru.papkov.repairlog.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.supply.*;
import ru.papkov.repairlog.domain.exception.BusinessLogicException;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.*;
import ru.papkov.repairlog.domain.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис управления заявками на поставку.
 * Создание заявок техниками, подтверждение администратором, приёмка на склад.
 *
 * @author aim-41tt
 */
@Service
@Transactional(readOnly = true)
public class SupplyRequestService {

    private static final Logger log = LoggerFactory.getLogger(SupplyRequestService.class);

    private final SupplyRequestRepository supplyRequestRepository;
    private final SupplyRequestItemRepository supplyRequestItemRepository;
    private final SupplyRequestStatusRepository statusRepository;
    private final SupplierRepository supplierRepository;
    private final EmployeeRepository employeeRepository;

    public SupplyRequestService(SupplyRequestRepository supplyRequestRepository,
                                SupplyRequestItemRepository supplyRequestItemRepository,
                                SupplyRequestStatusRepository statusRepository,
                                SupplierRepository supplierRepository,
                                EmployeeRepository employeeRepository) {
        this.supplyRequestRepository = supplyRequestRepository;
        this.supplyRequestItemRepository = supplyRequestItemRepository;
        this.statusRepository = statusRepository;
        this.supplierRepository = supplierRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * Получить все заявки на поставку.
     *
     * @return список заявок
     */
    public List<SupplyRequestResponse> getAll() {
        return supplyRequestRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Получить заявки по статусу.
     *
     * @param statusName название статуса (NEW, APPROVED, ORDERED, DELIVERED, CANCELLED)
     * @return список заявок с указанным статусом
     */
    public List<SupplyRequestResponse> getByStatus(String statusName) {
        SupplyRequestStatus status = statusRepository.findByName(statusName)
                .orElseThrow(() -> new EntityNotFoundException("Статус не найден: " + statusName));
        return supplyRequestRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Получить заявку по ID.
     *
     * @param id идентификатор заявки
     * @return данные заявки
     */
    public SupplyRequestResponse getById(Long id) {
        return toResponse(findById(id));
    }

    /**
     * Создать новую заявку на поставку.
     * Доступно для TECHNICIAN (заявка на закупку) и ADMIN (прямой заказ).
     *
     * @param request       данные заявки с позициями
     * @param requestedByLogin логин сотрудника, создающего заявку
     * @return созданная заявка
     */
    @Transactional
    public SupplyRequestResponse create(CreateSupplyRequestRequest request, String requestedByLogin) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new EntityNotFoundException("Поставщик не найден"));

        Employee requestedBy = employeeRepository.findByLogin(requestedByLogin)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден"));

        SupplyRequestStatus newStatus = statusRepository.findByName("NEW")
                .orElseThrow(() -> new EntityNotFoundException("Статус NEW не найден"));

        // генерация номера заявки: SR-YYYYMMDD-XXXX
        String requestNumber = "SR-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + String.format("%04d", supplyRequestRepository.count() + 1);

        SupplyRequest supplyRequest = new SupplyRequest();
        supplyRequest.setRequestNumber(requestNumber);
        supplyRequest.setSupplier(supplier);
        supplyRequest.setStatus(newStatus);
        supplyRequest.setRequestedBy(requestedBy);
        supplyRequest.setComment(request.getComment());

        SupplyRequest saved = supplyRequestRepository.save(supplyRequest);

        // создаём позиции заявки
        BigDecimal total = BigDecimal.ZERO;
        for (CreateSupplyRequestItemRequest itemReq : request.getItems()) {
            SupplyRequestItem item = new SupplyRequestItem();
            item.setSupplyRequest(saved);
            item.setItemName(itemReq.getItemName());
            item.setPartNumber(itemReq.getPartNumber());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : BigDecimal.ZERO);

            BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setTotalPrice(lineTotal);
            total = total.add(lineTotal);

            supplyRequestItemRepository.save(item);
        }

        saved.setTotalAmount(total);
        supplyRequestRepository.save(saved);

        log.info("Создана заявка на поставку {} сотрудником {}", requestNumber, requestedByLogin);
        return toResponse(saved);
    }

    /**
     * Подтвердить заявку (ADMIN).
     *
     * @param id            идентификатор заявки
     * @param approvedByLogin логин администратора
     * @return обновлённая заявка
     */
    @Transactional
    public SupplyRequestResponse approve(Long id, String approvedByLogin) {
        SupplyRequest request = findById(id);
        validateStatusTransition(request, "NEW", "APPROVED");

        Employee approvedBy = employeeRepository.findByLogin(approvedByLogin)
                .orElseThrow(() -> new EntityNotFoundException("Сотрудник не найден"));

        SupplyRequestStatus approved = statusRepository.findByName("APPROVED")
                .orElseThrow(() -> new EntityNotFoundException("Статус APPROVED не найден"));

        request.setStatus(approved);
        request.setApprovedBy(approvedBy);

        log.info("Заявка {} подтверждена администратором {}", request.getRequestNumber(), approvedByLogin);
        return toResponse(supplyRequestRepository.save(request));
    }

    /**
     * Отменить заявку.
     *
     * @param id идентификатор заявки
     * @return обновлённая заявка
     */
    @Transactional
    public SupplyRequestResponse cancel(Long id) {
        SupplyRequest request = findById(id);
        if ("DELIVERED".equals(request.getStatus().getName())) {
            throw new BusinessLogicException("Нельзя отменить доставленную заявку");
        }

        SupplyRequestStatus cancelled = statusRepository.findByName("CANCELLED")
                .orElseThrow(() -> new EntityNotFoundException("Статус CANCELLED не найден"));

        request.setStatus(cancelled);
        log.info("Заявка {} отменена", request.getRequestNumber());
        return toResponse(supplyRequestRepository.save(request));
    }

    /**
     * Отметить заявку как доставленную.
     *
     * @param id идентификатор заявки
     * @return обновлённая заявка
     */
    @Transactional
    public SupplyRequestResponse markDelivered(Long id) {
        SupplyRequest request = findById(id);
        SupplyRequestStatus delivered = statusRepository.findByName("DELIVERED")
                .orElseThrow(() -> new EntityNotFoundException("Статус DELIVERED не найден"));

        request.setStatus(delivered);
        log.info("Заявка {} отмечена как доставленная", request.getRequestNumber());
        return toResponse(supplyRequestRepository.save(request));
    }

    // ========== Вспомогательные методы ==========

    private SupplyRequest findById(Long id) {
        return supplyRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Заявка на поставку не найдена: " + id));
    }

    private void validateStatusTransition(SupplyRequest request, String expectedCurrent, String target) {
        if (!expectedCurrent.equals(request.getStatus().getName())) {
            throw new BusinessLogicException(
                    "Нельзя перевести заявку из статуса '" + request.getStatus().getName()
                            + "' в '" + target + "'");
        }
    }

    private SupplyRequestResponse toResponse(SupplyRequest sr) {
        SupplyRequestResponse r = new SupplyRequestResponse();
        r.setId(sr.getId());
        r.setRequestNumber(sr.getRequestNumber());
        r.setSupplierName(sr.getSupplier() != null ? sr.getSupplier().getName() : null);
        r.setSupplierId(sr.getSupplier() != null ? sr.getSupplier().getId() : null);
        r.setStatusName(sr.getStatus() != null ? sr.getStatus().getName() : null);
        r.setRequestedByName(sr.getRequestedBy() != null ? sr.getRequestedBy().getFullName() : null);
        r.setApprovedByName(sr.getApprovedBy() != null ? sr.getApprovedBy().getFullName() : null);
        r.setTotalAmount(sr.getTotalAmount());
        r.setComment(sr.getComment());
        r.setCreatedAt(sr.getCreatedAt());
        r.setExpectedDeliveryDate(sr.getExpectedDeliveryDate());

        // маппинг items, если они загружены
        if (sr.getItems() != null) {
            r.setItems(sr.getItems().stream().map(item -> {
                SupplyRequestItemResponse ir = new SupplyRequestItemResponse();
                ir.setId(item.getId());
                ir.setItemName(item.getItemName());
                ir.setPartNumber(item.getPartNumber());
                ir.setQuantity(item.getQuantity());
                ir.setUnitPrice(item.getUnitPrice());
                ir.setTotalPrice(item.getTotalPrice());
                return ir;
            }).collect(Collectors.toList()));
        }

        return r;
    }
}
