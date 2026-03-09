package ru.papkov.repairlog.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.papkov.repairlog.application.dto.audit.AuditLogResponse;
import ru.papkov.repairlog.application.dto.common.PageResponse;
import ru.papkov.repairlog.application.dto.employee.CreateEmployeeRequest;
import ru.papkov.repairlog.application.dto.employee.EmployeeResponse;
import ru.papkov.repairlog.application.dto.employee.UpdateEmployeeRequest;
import ru.papkov.repairlog.application.dto.inventory.CreateInventoryItemRequest;
import ru.papkov.repairlog.application.dto.inventory.InventoryItemResponse;
import ru.papkov.repairlog.application.dto.notification.NotificationResponse;
import ru.papkov.repairlog.application.dto.supplier.CreateSupplierRequest;
import ru.papkov.repairlog.application.dto.supplier.SupplierResponse;
import ru.papkov.repairlog.application.dto.supply.*;
import ru.papkov.repairlog.application.service.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Контроллер администратора.
 * Управление сотрудниками, поставщиками, заявками на поставку, складом, аудитом.
 *
 * @author aim-41tt
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Администратор", description = "Управление сотрудниками, складом, поставками, аудит")
public class AdminController {

    private final EmployeeService employeeService;
    private final AuthenticationService authService;
    private final InventoryService inventoryService;
    private final SupplierService supplierService;
    private final SupplyRequestService supplyRequestService;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final SupplyDashboardService supplyDashboardService;
    private final SupplySettingService supplySettingService;

    public AdminController(EmployeeService employeeService,
                           AuthenticationService authService,
                           InventoryService inventoryService,
                           SupplierService supplierService,
                           SupplyRequestService supplyRequestService,
                           NotificationService notificationService,
                           AuditLogService auditLogService,
                           SupplyDashboardService supplyDashboardService,
                           SupplySettingService supplySettingService) {
        this.employeeService = employeeService;
        this.authService = authService;
        this.inventoryService = inventoryService;
        this.supplierService = supplierService;
        this.supplyRequestService = supplyRequestService;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
        this.supplyDashboardService = supplyDashboardService;
        this.supplySettingService = supplySettingService;
    }

    // ==================== Сотрудники ====================

    @GetMapping("/employees")
    @Operation(summary = "Список всех сотрудников")
    public ResponseEntity<Page<EmployeeResponse>> getAllEmployees(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(employeeService.getAllEmployees(pageable));
    }

    @GetMapping("/employees/{id}")
    @Operation(summary = "Получить сотрудника по ID")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @GetMapping("/employees/role/{roleName}")
    @Operation(summary = "Получить сотрудников по роли")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByRole(@PathVariable String roleName) {
        return ResponseEntity.ok(employeeService.getByRole(roleName));
    }

    @PostMapping("/employees")
    @Operation(summary = "Создать сотрудника")
    public ResponseEntity<EmployeeResponse> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    @PutMapping("/employees/{id}")
    @Operation(summary = "Обновить данные сотрудника")
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateEmployeeRequest request) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    @PatchMapping("/employees/{id}/password")
    @Operation(summary = "Установить пароль сотруднику")
    public ResponseEntity<Map<String, String>> setPassword(@PathVariable Long id,
                                                            @RequestBody Map<String, String> body) {
        employeeService.setPassword(id, body.get("password"));
        return ResponseEntity.ok(Map.of("message", "Пароль установлен"));
    }

    @PatchMapping("/employees/{id}/block")
    @Operation(summary = "Заблокировать/разблокировать сотрудника")
    public ResponseEntity<Map<String, String>> toggleBlock(@PathVariable Long id,
                                                            @RequestBody Map<String, Boolean> body) {
        employeeService.toggleBlock(id, body.get("blocked"));
        return ResponseEntity.ok(Map.of("message", "Статус блокировки изменён"));
    }

    @PostMapping("/employees/{id}/terminate-session")
    @Operation(summary = "Принудительно завершить сессию сотрудника")
    public ResponseEntity<Map<String, String>> terminateSession(@PathVariable Long id) {
        authService.terminateSession(id);
        return ResponseEntity.ok(Map.of("message", "Сессия завершена"));
    }

    // ==================== Склад ====================

    @GetMapping("/inventory")
    @Operation(summary = "Полный список складских позиций")
    public ResponseEntity<Page<InventoryItemResponse>> getAllInventory(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(inventoryService.getAll(pageable));
    }

    @GetMapping("/inventory/low-stock")
    @Operation(summary = "Позиции с низким остатком")
    public ResponseEntity<List<InventoryItemResponse>> getLowStock() {
        return ResponseEntity.ok(inventoryService.getLowStock());
    }

    @PostMapping("/inventory")
    @Operation(summary = "Создать складскую позицию (подарок, возврат от клиента, излишки и т.д.)")
    public ResponseEntity<InventoryItemResponse> createInventoryItem(
            @Valid @RequestBody CreateInventoryItemRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createItem(request, user.getUsername()));
    }

    @PostMapping("/inventory/{itemId}/receive")
    @Operation(summary = "Приёмка товара на склад")
    public ResponseEntity<Map<String, String>> receiveStock(@PathVariable Long itemId,
                                                             @RequestBody Map<String, Object> body,
                                                             @AuthenticationPrincipal UserDetails user) {
        int qty = (int) body.get("quantity");
        String comment = (String) body.getOrDefault("comment", "");
        inventoryService.receiveStock(itemId, qty, user.getUsername(), comment);
        return ResponseEntity.ok(Map.of("message", "Товар принят на склад"));
    }

    // ==================== Поставщики ====================

    @GetMapping("/suppliers")
    @Operation(summary = "Список всех поставщиков")
    public ResponseEntity<Page<SupplierResponse>> getAllSuppliers(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(supplierService.getAll(pageable));
    }

    @GetMapping("/suppliers/{id}")
    @Operation(summary = "Получить поставщика по ID")
    public ResponseEntity<SupplierResponse> getSupplier(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getById(id));
    }

    @PostMapping("/suppliers")
    @Operation(summary = "Создать поставщика")
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody CreateSupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(request));
    }

    @PutMapping("/suppliers/{id}")
    @Operation(summary = "Обновить данные поставщика")
    public ResponseEntity<SupplierResponse> updateSupplier(@PathVariable Long id,
                                                            @Valid @RequestBody CreateSupplierRequest request) {
        return ResponseEntity.ok(supplierService.update(id, request));
    }

    @PatchMapping("/suppliers/{id}/active")
    @Operation(summary = "Активировать/деактивировать поставщика")
    public ResponseEntity<Map<String, String>> toggleSupplierActive(@PathVariable Long id,
                                                                     @RequestBody Map<String, Boolean> body) {
        supplierService.toggleActive(id, body.get("active"));
        return ResponseEntity.ok(Map.of("message", "Статус поставщика обновлён"));
    }

    @GetMapping("/suppliers/active")
    @Operation(summary = "Активные поставщики")
    public ResponseEntity<List<SupplierResponse>> getActiveSuppliers() {
        return ResponseEntity.ok(supplierService.getActive());
    }

    @GetMapping("/suppliers/integration-type/{type}")
    @Operation(summary = "Поставщики по типу интеграции")
    public ResponseEntity<List<SupplierResponse>> getSuppliersByIntegrationType(@PathVariable String type) {
        return ResponseEntity.ok(supplierService.getByIntegrationType(type));
    }

    // ==================== Заявки на поставку ====================

    @GetMapping("/supply-requests")
    @Operation(summary = "Все заявки на поставку")
    public ResponseEntity<Page<SupplyRequestResponse>> getAllSupplyRequests(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(supplyRequestService.getAll(pageable));
    }

    @GetMapping("/supply-requests/status/{statusName}")
    @Operation(summary = "Заявки по статусу")
    public ResponseEntity<List<SupplyRequestResponse>> getSupplyRequestsByStatus(@PathVariable String statusName) {
        return ResponseEntity.ok(supplyRequestService.getByStatus(statusName));
    }

    @GetMapping("/supply-requests/{id}")
    @Operation(summary = "Получить заявку по ID")
    public ResponseEntity<SupplyRequestResponse> getSupplyRequest(@PathVariable Long id) {
        return ResponseEntity.ok(supplyRequestService.getById(id));
    }

    @PostMapping("/supply-requests")
    @Operation(summary = "Создать заявку на поставку")
    public ResponseEntity<SupplyRequestResponse> createSupplyRequest(
            @Valid @RequestBody CreateSupplyRequestRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplyRequestService.create(request, user.getUsername()));
    }

    @PostMapping("/supply-requests/{id}/approve")
    @Operation(summary = "Подтвердить заявку на поставку")
    public ResponseEntity<SupplyRequestResponse> approveSupplyRequest(@PathVariable Long id,
                                                                       @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(supplyRequestService.approve(id, user.getUsername()));
    }

    @PostMapping("/supply-requests/{id}/cancel")
    @Operation(summary = "Отменить заявку на поставку")
    public ResponseEntity<SupplyRequestResponse> cancelSupplyRequest(@PathVariable Long id) {
        return ResponseEntity.ok(supplyRequestService.cancel(id));
    }

    @PostMapping("/supply-requests/{id}/ordered")
    @Operation(summary = "Отметить заявку как заказанную")
    public ResponseEntity<SupplyRequestResponse> markOrdered(@PathVariable Long id) {
        return ResponseEntity.ok(supplyRequestService.markOrdered(id));
    }

    @PostMapping("/supply-requests/{id}/in-transit")
    @Operation(summary = "Отметить заявку как 'в пути'")
    public ResponseEntity<SupplyRequestResponse> markInTransit(@PathVariable Long id) {
        return ResponseEntity.ok(supplyRequestService.markInTransit(id));
    }

    @PostMapping("/supply-requests/{id}/delivered")
    @Operation(summary = "Отметить заявку как доставленную (автоматически пополняет склад)")
    public ResponseEntity<SupplyRequestResponse> markDelivered(@PathVariable Long id,
                                                               @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(supplyRequestService.markDelivered(id, user.getUsername()));
    }

    @PostMapping("/supply-requests/{id}/assign-supplier")
    @Operation(summary = "Привязать поставщика к заявке")
    public ResponseEntity<SupplyRequestResponse> assignSupplier(
            @PathVariable Long id,
            @Valid @RequestBody AssignSupplierRequest request) {
        return ResponseEntity.ok(supplyRequestService.assignSupplier(id, request.getSupplierId()));
    }

    // ==================== Оплата поставщикам ====================

    @PostMapping("/supply-requests/{id}/payment")
    @Operation(summary = "Записать оплату поставщику")
    public ResponseEntity<SupplierPaymentResponse> recordPayment(
            @PathVariable Long id,
            @Valid @RequestBody CreateSupplierPaymentRequest request,
            @AuthenticationPrincipal UserDetails user) {
        request.setSupplyRequestId(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplyRequestService.recordPayment(request, user.getUsername()));
    }

    @GetMapping("/supply-requests/{id}/payments")
    @Operation(summary = "Список оплат по заявке")
    public ResponseEntity<List<SupplierPaymentResponse>> getPayments(@PathVariable Long id) {
        return ResponseEntity.ok(supplyRequestService.getPayments(id));
    }

    @PostMapping("/supply-requests/{id}/invoice")
    @Operation(summary = "Привязать счёт от поставщика")
    public ResponseEntity<SupplierInvoiceResponse> createInvoice(
            @PathVariable Long id,
            @Valid @RequestBody CreateSupplierInvoiceRequest request) {
        request.setSupplyRequestId(id);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplyRequestService.createInvoice(request));
    }

    @GetMapping("/supply-requests/{id}/invoices")
    @Operation(summary = "Список счетов по заявке")
    public ResponseEntity<List<SupplierInvoiceResponse>> getInvoices(@PathVariable Long id) {
        return ResponseEntity.ok(supplyRequestService.getInvoices(id));
    }

    // ==================== Дашборд поставок ====================

    @GetMapping("/supply-dashboard")
    @Operation(summary = "Дашборд управления поставками")
    public ResponseEntity<SupplyDashboardResponse> getSupplyDashboard() {
        return ResponseEntity.ok(supplyDashboardService.getDashboard());
    }

    // ==================== Настройки поставок ====================

    @GetMapping("/supply-settings")
    @Operation(summary = "Все настройки поставок")
    public ResponseEntity<List<SupplySettingResponse>> getSupplySettings() {
        return ResponseEntity.ok(supplySettingService.getAll());
    }

    @PutMapping("/supply-settings/{key}")
    @Operation(summary = "Обновить настройку поставок")
    public ResponseEntity<SupplySettingResponse> updateSupplySetting(
            @PathVariable String key,
            @Valid @RequestBody UpdateSupplySettingRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(supplySettingService.update(key, request, user.getUsername()));
    }

    // ==================== Уведомления ====================

    @GetMapping("/notifications/pending")
    @Operation(summary = "Непрочитанные уведомления")
    public ResponseEntity<List<NotificationResponse>> getPendingNotifications() {
        return ResponseEntity.ok(notificationService.getPending());
    }

    @PostMapping("/notifications/{id}/sent")
    @Operation(summary = "Отметить уведомление как отправленное")
    public ResponseEntity<Map<String, String>> markNotificationSent(@PathVariable Long id) {
        notificationService.markAsSent(id);
        return ResponseEntity.ok(Map.of("message", "Уведомление отмечено как отправленное"));
    }

    // ==================== Аудит безопасности (152-ФЗ) ====================

    @GetMapping("/audit")
    @Operation(summary = "Журнал аудита безопасности")
    public ResponseEntity<PageResponse<AuditLogResponse>> getAuditLog(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getAll(pageable));
    }

    @GetMapping("/audit/employee/{employeeId}")
    @Operation(summary = "Аудит по сотруднику")
    public ResponseEntity<PageResponse<AuditLogResponse>> getAuditByEmployee(
            @PathVariable Long employeeId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getByEmployee(employeeId, pageable));
    }

    @GetMapping("/audit/period")
    @Operation(summary = "Аудит за период")
    public ResponseEntity<PageResponse<AuditLogResponse>> getAuditByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getByPeriod(from, to, pageable));
    }
}
