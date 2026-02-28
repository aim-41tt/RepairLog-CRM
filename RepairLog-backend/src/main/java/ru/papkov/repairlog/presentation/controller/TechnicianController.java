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
import ru.papkov.repairlog.application.dto.diagnostic.*;
import ru.papkov.repairlog.application.dto.inventory.InventoryItemResponse;
import ru.papkov.repairlog.application.dto.order.*;
import ru.papkov.repairlog.application.dto.receipt.*;
import ru.papkov.repairlog.application.dto.supply.CreateSupplyRequestRequest;
import ru.papkov.repairlog.application.dto.supply.SupplyRequestResponse;
import ru.papkov.repairlog.application.service.*;

import java.util.List;
import java.util.Map;

/**
 * Контроллер для роли TECHNICIAN (Техник/Мастер).
 * Просмотр заявок, диагностика, ремонт, работа со складом.
 *
 * @author aim-41tt
 */
@RestController
@RequestMapping("/api/technician")
@PreAuthorize("hasRole('TECHNICIAN')")
@Tag(name = "Техник", description = "Диагностика, ремонт, склад")
public class TechnicianController {

    private final RepairOrderService repairOrderService;
    private final DiagnosticService diagnosticService;
    private final ReceiptService receiptService;
    private final InventoryService inventoryService;
    private final DeviceService deviceService;
    private final SupplyRequestService supplyRequestService;
    private final ru.papkov.repairlog.domain.repository.EmployeeRepository employeeRepository;

    public TechnicianController(RepairOrderService repairOrderService,
                                DiagnosticService diagnosticService,
                                ReceiptService receiptService,
                                InventoryService inventoryService,
                                DeviceService deviceService,
                                SupplyRequestService supplyRequestService,
                                ru.papkov.repairlog.domain.repository.EmployeeRepository employeeRepository) {
        this.repairOrderService = repairOrderService;
        this.diagnosticService = diagnosticService;
        this.receiptService = receiptService;
        this.inventoryService = inventoryService;
        this.deviceService = deviceService;
        this.supplyRequestService = supplyRequestService;
        this.employeeRepository = employeeRepository;
    }

    // ========== Заказы ==========

    @GetMapping("/orders/unassigned")
    @Operation(summary = "Список заявок без мастера")
    public ResponseEntity<List<RepairOrderResponse>> getUnassignedOrders() {
        return ResponseEntity.ok(repairOrderService.getUnassigned());
    }

    @GetMapping("/orders/my")
    @Operation(summary = "Мои текущие заказы")
    public ResponseEntity<List<RepairOrderResponse>> getMyOrders(@AuthenticationPrincipal UserDetails user) {
        var employee = employeeRepository.findByLogin(user.getUsername()).orElseThrow();
        return ResponseEntity.ok(repairOrderService.getByMaster(employee.getId()));
    }

    @GetMapping("/orders/{id}")
    @Operation(summary = "Детали заказа")
    public ResponseEntity<RepairOrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(repairOrderService.getById(id));
    }

    @PostMapping("/orders/{id}/take")
    @Operation(summary = "Взять заказ в работу")
    public ResponseEntity<RepairOrderResponse> takeOrder(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        var employee = employeeRepository.findByLogin(user.getUsername()).orElseThrow();
        return ResponseEntity.ok(repairOrderService.assignMaster(id, employee.getId()));
    }

    @PostMapping("/orders/{id}/status")
    @Operation(summary = "Изменить статус заказа")
    public ResponseEntity<RepairOrderResponse> changeStatus(@PathVariable Long id,
                                                             @Valid @RequestBody ChangeStatusRequest request,
                                                             @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(repairOrderService.changeStatus(id, request, user.getUsername()));
    }

    // ========== Диагностика ==========

    @GetMapping("/diagnostics/order/{orderId}")
    @Operation(summary = "Получить диагностику по заказу")
    public ResponseEntity<DiagnosticResponse> getDiagnostic(@PathVariable Long orderId) {
        return ResponseEntity.ok(diagnosticService.getByOrderId(orderId));
    }

    @PostMapping("/diagnostics")
    @Operation(summary = "Записать результат диагностики")
    public ResponseEntity<DiagnosticResponse> createDiagnostic(@Valid @RequestBody CreateDiagnosticRequest request,
                                                                @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(diagnosticService.create(request, user.getUsername()));
    }

    // ========== Работы ==========

    @PostMapping("/works")
    @Operation(summary = "Добавить выполненную работу")
    public ResponseEntity<Map<String, String>> addWork(@Valid @RequestBody AddRepairWorkRequest request,
                                                        @AuthenticationPrincipal UserDetails user) {
        receiptService.addWork(request, user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Работа добавлена"));
    }

    @GetMapping("/receipts/order/{orderId}")
    @Operation(summary = "Получить чек заказа")
    public ResponseEntity<ru.papkov.repairlog.application.dto.receipt.ReceiptResponse> getReceipt(@PathVariable Long orderId) {
        return ResponseEntity.ok(receiptService.getByOrderId(orderId));
    }

    // ========== Склад ==========

    @GetMapping("/inventory")
    @Operation(summary = "Все товары на складе")
    public ResponseEntity<List<InventoryItemResponse>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAll());
    }

    @GetMapping("/inventory/search")
    @Operation(summary = "Поиск по складу")
    public ResponseEntity<List<InventoryItemResponse>> searchInventory(@RequestParam String query) {
        return ResponseEntity.ok(inventoryService.search(query));
    }

    @GetMapping("/inventory/low-stock")
    @Operation(summary = "Товары с низким остатком")
    public ResponseEntity<List<InventoryItemResponse>> getLowStock() {
        return ResponseEntity.ok(inventoryService.getLowStock());
    }

    @PostMapping("/inventory/{itemId}/consume")
    @Operation(summary = "Списать товар для ремонта")
    public ResponseEntity<Map<String, String>> consumeItem(@PathVariable Long itemId,
                                                            @RequestParam int quantity,
                                                            @RequestParam Long orderId,
                                                            @AuthenticationPrincipal UserDetails user) {
        inventoryService.consumeForRepair(itemId, quantity, orderId, user.getUsername());
        return ResponseEntity.ok(Map.of("message", "Товар списан"));
    }

    // ========== Перемещение устройств ==========

    @PostMapping("/devices/{deviceId}/move")
    @Operation(summary = "Переместить устройство")
    public ResponseEntity<Map<String, String>> moveDevice(@PathVariable Long deviceId,
                                                           @RequestParam String location,
                                                           @RequestParam(required = false) String comment,
                                                           @AuthenticationPrincipal UserDetails user) {
        deviceService.moveDevice(deviceId, location, user.getUsername(), comment);
        return ResponseEntity.ok(Map.of("message", "Устройство перемещено в: " + location));
    }

    // ========== Заявки на поставку ==========

    @PostMapping("/supply-requests")
    @Operation(summary = "Создать заявку на закупку компонентов")
    public ResponseEntity<SupplyRequestResponse> createSupplyRequest(
            @Valid @RequestBody CreateSupplyRequestRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplyRequestService.create(request, user.getUsername()));
    }

    @GetMapping("/supply-requests/my")
    @Operation(summary = "Мои заявки на поставку")
    public ResponseEntity<List<SupplyRequestResponse>> getMySupplyRequests(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(supplyRequestService.getByEmployee(user.getUsername()));
    }
}
