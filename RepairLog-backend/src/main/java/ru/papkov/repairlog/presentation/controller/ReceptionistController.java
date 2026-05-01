package ru.papkov.repairlog.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import ru.papkov.repairlog.application.dto.client.*;
import ru.papkov.repairlog.application.dto.device.*;
import ru.papkov.repairlog.application.dto.diagnostic.DiagnosticResponse;
import ru.papkov.repairlog.application.dto.order.*;
import ru.papkov.repairlog.application.dto.receipt.*;
import ru.papkov.repairlog.application.mapper.ClientMapper;
import ru.papkov.repairlog.application.mapper.DeviceMapper;
import ru.papkov.repairlog.application.mapper.DiagnosticMapper;
import ru.papkov.repairlog.application.mapper.ReceiptMapper;
import ru.papkov.repairlog.application.mapper.RepairOrderMapper;
import ru.papkov.repairlog.application.service.*;
import ru.papkov.repairlog.domain.model.Client;
import ru.papkov.repairlog.domain.model.RepairOrder;
import ru.papkov.repairlog.domain.model.SecurityAuditLog;
import ru.papkov.repairlog.infrastructure.security.audit.AuditLogger;

import java.util.List;
import java.util.Map;

/**
 * Контроллер для роли RECEPTIONIST (Приёмщик).
 * Регистрация клиентов, создание заказов, приём оплаты, выдача устройств.
 *
 * @author aim-41tt
 */
@RestController
@RequestMapping("/api/receptionist")
@PreAuthorize("hasRole('RECEPTIONIST')")
@Tag(name = "Приёмщик", description = "Работа с клиентами, заказами и оплатой")
public class ReceptionistController {

    private final ClientService clientService;
    private final DeviceService deviceService;
    private final RepairOrderService repairOrderService;
    private final ReceiptService receiptService;
    private final DiagnosticService diagnosticService;
    private final DocumentService documentService;
    private final AuditLogger auditLogger;
    private final ClientMapper clientMapper;
    private final DeviceMapper deviceMapper;
    private final RepairOrderMapper repairOrderMapper;
    private final ReceiptMapper receiptMapper;
    private final DiagnosticMapper diagnosticMapper;

    public ReceptionistController(ClientService clientService,
                                  DeviceService deviceService,
                                  RepairOrderService repairOrderService,
                                  ReceiptService receiptService,
                                  DiagnosticService diagnosticService,
                                  DocumentService documentService,
                                  AuditLogger auditLogger,
                                  ClientMapper clientMapper,
                                  DeviceMapper deviceMapper,
                                  RepairOrderMapper repairOrderMapper,
                                  ReceiptMapper receiptMapper,
                                  DiagnosticMapper diagnosticMapper) {
        this.clientService = clientService;
        this.deviceService = deviceService;
        this.repairOrderService = repairOrderService;
        this.receiptService = receiptService;
        this.diagnosticService = diagnosticService;
        this.documentService = documentService;
        this.auditLogger = auditLogger;
        this.clientMapper = clientMapper;
        this.deviceMapper = deviceMapper;
        this.repairOrderMapper = repairOrderMapper;
        this.receiptMapper = receiptMapper;
        this.diagnosticMapper = diagnosticMapper;
    }

    // ========== Клиенты ==========

    @GetMapping("/clients/search")
    @Operation(summary = "Поиск клиентов по ФИО или телефону")
    public ResponseEntity<List<ClientResponse>> searchClients(@RequestParam String query) {
        return ResponseEntity.ok(clientMapper.toResponseList(clientService.search(query)));
    }

    @GetMapping("/clients/{id}")
    @Operation(summary = "Получить клиента по ID")
    public ResponseEntity<ClientResponse> getClient(@PathVariable Long id) {
        Client client = clientService.getById(id);
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogger.logEvent(SecurityAuditLog.EventType.DATA_ACCESS, login, "CLIENT", id, "READ", SecurityAuditLog.Result.SUCCESS);
        return ResponseEntity.ok(clientMapper.toResponse(client));
    }

    @PostMapping("/clients")
    @Operation(summary = "Зарегистрировать нового клиента")
    public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody CreateClientRequest request) {
        Client saved = clientService.create(request);
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogger.logEvent(SecurityAuditLog.EventType.DATA_CREATE, login, "CLIENT", saved.getId(), "CREATE", SecurityAuditLog.Result.SUCCESS);
        return ResponseEntity.status(HttpStatus.CREATED).body(clientMapper.toResponse(saved));
    }

    @PutMapping("/clients/{id}")
    @Operation(summary = "Обновить данные клиента")
    public ResponseEntity<ClientResponse> updateClient(@PathVariable Long id,
                                                        @Valid @RequestBody CreateClientRequest request) {
        Client updated = clientService.update(id, request);
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogger.logEvent(SecurityAuditLog.EventType.DATA_UPDATE, login, "CLIENT", id, "UPDATE", SecurityAuditLog.Result.SUCCESS);
        return ResponseEntity.ok(clientMapper.toResponse(updated));
    }

    @PostMapping("/clients/{id}/consent")
    @Operation(summary = "Подтвердить согласие на обработку ПДн (152-ФЗ)")
    public ResponseEntity<Map<String, String>> giveConsent(@PathVariable Long id) {
        clientService.giveConsent(id);
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogger.logEvent(SecurityAuditLog.EventType.DATA_UPDATE, login, "CLIENT", id, "CONSENT_GIVEN", SecurityAuditLog.Result.SUCCESS);
        return ResponseEntity.ok(Map.of("message", "Согласие получено"));
    }

    // ========== Устройства ==========

    @GetMapping("/devices/client/{clientId}")
    @Operation(summary = "Устройства клиента")
    public ResponseEntity<List<DeviceResponse>> getClientDevices(@PathVariable Long clientId) {
        return ResponseEntity.ok(deviceMapper.toResponseList(deviceService.getByClient(clientId)));
    }

    @PostMapping("/devices")
    @Operation(summary = "Зарегистрировать устройство")
    public ResponseEntity<DeviceResponse> createDevice(@Valid @RequestBody CreateDeviceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deviceMapper.toResponse(deviceService.create(request)));
    }

    // ========== Заказы ==========

    @PostMapping("/orders")
    @Operation(summary = "Создать заказ на ремонт")
    public ResponseEntity<RepairOrderResponse> createOrder(@Valid @RequestBody CreateRepairOrderRequest request,
                                                            @AuthenticationPrincipal UserDetails user) {
        RepairOrder order = repairOrderService.create(request, user.getUsername());
        auditLogger.logEvent(SecurityAuditLog.EventType.DATA_CREATE, user.getUsername(), "REPAIR_ORDER", order.getId(), "CREATE", SecurityAuditLog.Result.SUCCESS);
        return ResponseEntity.status(HttpStatus.CREATED).body(repairOrderMapper.toResponse(order));
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "Получить заказ по ID")
    public ResponseEntity<RepairOrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(repairOrderMapper.toResponse(repairOrderService.getById(id)));
    }

    @GetMapping("/orders/search")
    @Operation(summary = "Найти заказ по номеру")
    public ResponseEntity<RepairOrderResponse> findOrder(@RequestParam String orderNumber) {
        return ResponseEntity.ok(repairOrderMapper.toResponse(repairOrderService.getByOrderNumber(orderNumber)));
    }

    @GetMapping("/orders/search/multi")
    @Operation(summary = "Поиск заказов по нескольким полям (номер, фамилия, телефон, серийный номер)")
    public ResponseEntity<List<RepairOrderResponse>> searchMulti(@RequestParam String query) {
        return ResponseEntity.ok(repairOrderMapper.toResponseList(repairOrderService.searchMultiField(query)));
    }

    @PostMapping("/orders/{id}/status")
    @Operation(summary = "Изменить статус заказа (при выдаче)")
    public ResponseEntity<RepairOrderResponse> changeStatus(@PathVariable Long id,
                                                             @Valid @RequestBody ChangeStatusRequest request,
                                                             @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(repairOrderMapper.toResponse(
                repairOrderService.changeStatus(id, request, user.getUsername())));
    }

    @GetMapping("/orders/{id}/diagnostics")
    @Operation(summary = "Получить диагностику заказа")
    public ResponseEntity<DiagnosticResponse> getOrderDiagnostics(@PathVariable Long id) {
        return ResponseEntity.ok(diagnosticMapper.toResponse(diagnosticService.getByOrderId(id)));
    }

    @GetMapping("/orders/{id}/status-history")
    @Operation(summary = "История статусов заказа")
    public ResponseEntity<List<StatusHistoryResponse>> getStatusHistory(@PathVariable Long id) {
        return ResponseEntity.ok(repairOrderMapper.toStatusHistoryList(repairOrderService.getStatusHistory(id)));
    }

    // ========== Чеки и оплата ==========

    @GetMapping("/receipts/order/{orderId}")
    @Operation(summary = "Получить чек по заказу со списком работ и платежей")
    public ResponseEntity<ReceiptResponse> getReceipt(@PathVariable Long orderId) {
        // B-05: Маппер намеренно игнорирует works/payments (@Mapping ignore = true).
        // Заполняем их здесь отдельными вызовами, как предписано javadoc маппера.
        ru.papkov.repairlog.domain.model.Receipt receipt = receiptService.getByOrderId(orderId);
        ReceiptResponse response = receiptMapper.toResponse(receipt);
        response.setWorks(receiptMapper.toWorkResponseList(receiptService.getWorksByReceipt(receipt)));
        response.setPayments(receiptMapper.toPaymentResponseList(receiptService.getPaymentsByReceipt(receipt)));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payments")
    @Operation(summary = "Принять оплату")
    public ResponseEntity<Map<String, String>> processPayment(@Valid @RequestBody CreatePaymentRequest request,
                                                               @AuthenticationPrincipal UserDetails user) {
        receiptService.processPayment(request, user.getUsername());
        return ResponseEntity.ok(Map.of("message", "Оплата принята"));
    }

    // ========== Документы (PDF) ==========

    @GetMapping("/orders/{id}/documents/receipt")
    @Operation(summary = "Сформировать квитанцию приёмки (PDF)")
    public ResponseEntity<byte[]> generateReceipt(@PathVariable Long id) {
        byte[] pdf = documentService.generateReceipt(id);
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogger.logEvent(SecurityAuditLog.EventType.DATA_ACCESS, login, "DOCUMENT", id, "EXPORT", SecurityAuditLog.Result.SUCCESS);
        return buildPdfResponse(pdf);
    }

    @GetMapping("/orders/{id}/documents/completion-act")
    @Operation(summary = "Сформировать акт выполненных работ (PDF)")
    public ResponseEntity<byte[]> generateCompletionAct(@PathVariable Long id) {
        byte[] pdf = documentService.generateCompletionAct(id);
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogger.logEvent(SecurityAuditLog.EventType.DATA_ACCESS, login, "DOCUMENT", id, "EXPORT", SecurityAuditLog.Result.SUCCESS);
        return buildPdfResponse(pdf);
    }

    @GetMapping("/orders/{id}/documents/warranty-card")
    @Operation(summary = "Сформировать гарантийный талон (PDF)")
    public ResponseEntity<byte[]> generateWarrantyCard(@PathVariable Long id) {
        byte[] pdf = documentService.generateWarrantyCard(id);
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogger.logEvent(SecurityAuditLog.EventType.DATA_ACCESS, login, "DOCUMENT", id, "EXPORT", SecurityAuditLog.Result.SUCCESS);
        return buildPdfResponse(pdf);
    }

    @GetMapping("/orders/{id}/documents/rejection-sheet")
    @Operation(summary = "Сформировать отказной лист (PDF)")
    public ResponseEntity<byte[]> generateRejectionSheet(@PathVariable Long id,
                                                          @RequestParam String reason) {
        byte[] pdf = documentService.generateRejectionSheet(id, reason);
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogger.logEvent(SecurityAuditLog.EventType.DATA_ACCESS, login, "DOCUMENT", id, "EXPORT", SecurityAuditLog.Result.SUCCESS);
        return buildPdfResponse(pdf);
    }

    private ResponseEntity<byte[]> buildPdfResponse(byte[] pdf) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}
