package ru.papkov.repairlog.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.papkov.repairlog.application.dto.client.*;
import ru.papkov.repairlog.application.dto.device.*;
import ru.papkov.repairlog.application.dto.order.*;
import ru.papkov.repairlog.application.dto.receipt.*;
import ru.papkov.repairlog.application.service.*;

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

    public ReceptionistController(ClientService clientService,
                                  DeviceService deviceService,
                                  RepairOrderService repairOrderService,
                                  ReceiptService receiptService) {
        this.clientService = clientService;
        this.deviceService = deviceService;
        this.repairOrderService = repairOrderService;
        this.receiptService = receiptService;
    }

    // ========== Клиенты ==========

    @GetMapping("/clients/search")
    @Operation(summary = "Поиск клиентов по ФИО или телефону")
    public ResponseEntity<List<ClientResponse>> searchClients(@RequestParam String query) {
        return ResponseEntity.ok(clientService.search(query));
    }

    @GetMapping("/clients/{id}")
    @Operation(summary = "Получить клиента по ID")
    public ResponseEntity<ClientResponse> getClient(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getById(id));
    }

    @PostMapping("/clients")
    @Operation(summary = "Зарегистрировать нового клиента")
    public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody CreateClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.create(request));
    }

    @PutMapping("/clients/{id}")
    @Operation(summary = "Обновить данные клиента")
    public ResponseEntity<ClientResponse> updateClient(@PathVariable Long id,
                                                        @Valid @RequestBody CreateClientRequest request) {
        return ResponseEntity.ok(clientService.update(id, request));
    }

    @PostMapping("/clients/{id}/consent")
    @Operation(summary = "Подтвердить согласие на обработку ПДн (152-ФЗ)")
    public ResponseEntity<Map<String, String>> giveConsent(@PathVariable Long id) {
        clientService.giveConsent(id);
        return ResponseEntity.ok(Map.of("message", "Согласие получено"));
    }

    // ========== Устройства ==========

    @GetMapping("/devices/client/{clientId}")
    @Operation(summary = "Устройства клиента")
    public ResponseEntity<List<DeviceResponse>> getClientDevices(@PathVariable Long clientId) {
        return ResponseEntity.ok(deviceService.getByClient(clientId));
    }

    @PostMapping("/devices")
    @Operation(summary = "Зарегистрировать устройство")
    public ResponseEntity<DeviceResponse> createDevice(@Valid @RequestBody CreateDeviceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.create(request));
    }

    // ========== Заказы ==========

    @PostMapping("/orders")
    @Operation(summary = "Создать заказ на ремонт")
    public ResponseEntity<RepairOrderResponse> createOrder(@Valid @RequestBody CreateRepairOrderRequest request,
                                                            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repairOrderService.create(request, user.getUsername()));
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "Получить заказ по ID")
    public ResponseEntity<RepairOrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(repairOrderService.getById(id));
    }

    @GetMapping("/orders/search")
    @Operation(summary = "Найти заказ по номеру")
    public ResponseEntity<RepairOrderResponse> findOrder(@RequestParam String orderNumber) {
        return ResponseEntity.ok(repairOrderService.getByOrderNumber(orderNumber));
    }

    @PostMapping("/orders/{id}/status")
    @Operation(summary = "Изменить статус заказа (при выдаче)")
    public ResponseEntity<RepairOrderResponse> changeStatus(@PathVariable Long id,
                                                             @Valid @RequestBody ChangeStatusRequest request,
                                                             @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(repairOrderService.changeStatus(id, request, user.getUsername()));
    }

    // ========== Чеки и оплата ==========

    @GetMapping("/receipts/order/{orderId}")
    @Operation(summary = "Получить чек по заказу")
    public ResponseEntity<ReceiptResponse> getReceipt(@PathVariable Long orderId) {
        return ResponseEntity.ok(receiptService.getByOrderId(orderId));
    }

    @PostMapping("/payments")
    @Operation(summary = "Принять оплату")
    public ResponseEntity<Map<String, String>> processPayment(@Valid @RequestBody CreatePaymentRequest request,
                                                               @AuthenticationPrincipal UserDetails user) {
        receiptService.processPayment(request, user.getUsername());
        return ResponseEntity.ok(Map.of("message", "Оплата принята"));
    }
}
