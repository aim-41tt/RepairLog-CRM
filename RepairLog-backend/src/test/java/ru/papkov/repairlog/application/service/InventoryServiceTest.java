package ru.papkov.repairlog.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.papkov.repairlog.application.dto.inventory.InventoryItemResponse;
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
class InventoryServiceTest {

    @Mock private InventoryItemRepository inventoryItemRepository;
    @Mock private InventoryMovementRepository inventoryMovementRepository;
    @Mock private RepairOrderRepository repairOrderRepository;
    @Mock private EmployeeRepository employeeRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private InventoryItem testItem;
    private Employee testEmployee;
    private RepairOrder testOrder;

    @BeforeEach
    void setUp() {
        DegreeWear degreeWear = new DegreeWear();
        degreeWear.setId(1L);
        degreeWear.setName("Новое");

        testItem = new InventoryItem();
        testItem.setId(1L);
        testItem.setName("Экран iPhone 15");
        testItem.setSerialNumber("SCR-001");
        testItem.setDegreeWear(degreeWear);
        testItem.setIsDevice(false);
        testItem.setUnitPrice(new BigDecimal("5000.00"));
        testItem.setQuantity(10);
        testItem.setInStock(true);
        testItem.setMinStockLevel(3);

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setName("Мастер");
        testEmployee.setSurname("Техников");
        testEmployee.setLogin("tech1");

        Client client = new Client();
        client.setId(1L);
        client.setName("Иван");
        client.setSurname("Петров");
        client.setPatronymic("С");

        testOrder = new RepairOrder();
        testOrder.setId(1L);
        testOrder.setOrderNumber("RO-20260228-0001");
        testOrder.setClient(client);
    }

    @Test
    @DisplayName("getAll - возвращает товары в наличии")
    void getAll_returnsInStockItems() {
        when(inventoryItemRepository.findByInStockTrue()).thenReturn(List.of(testItem));

        List<InventoryItemResponse> result = inventoryService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Экран iPhone 15");
        assertThat(result.get(0).getQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("getById - возвращает товар по ID")
    void getById_returnsItem() {
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        InventoryItemResponse result = inventoryService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Экран iPhone 15");
        assertThat(result.getStockStatus()).isEqualTo("GOOD_STOCK");
    }

    @Test
    @DisplayName("getById - ошибка если товар не найден")
    void getById_throwsWhenNotFound() {
        when(inventoryItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("getById - LOW_STOCK статус")
    void getById_lowStockStatus() {
        testItem.setQuantity(2); // ниже minStockLevel=3
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        InventoryItemResponse result = inventoryService.getById(1L);

        assertThat(result.getStockStatus()).isEqualTo("LOW_STOCK");
    }

    @Test
    @DisplayName("getById - OUT_OF_STOCK статус")
    void getById_outOfStockStatus() {
        testItem.setQuantity(0);
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        InventoryItemResponse result = inventoryService.getById(1L);

        assertThat(result.getStockStatus()).isEqualTo("OUT_OF_STOCK");
    }

    @Test
    @DisplayName("search - поиск по имени")
    void search_returnsByName() {
        when(inventoryItemRepository.findByNameContainingIgnoreCase("Экран"))
                .thenReturn(List.of(testItem));

        List<InventoryItemResponse> result = inventoryService.search("Экран");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("consumeForRepair - успешное списание")
    void consumeForRepair_success() {
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(repairOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(employeeRepository.findByLogin("tech1")).thenReturn(Optional.of(testEmployee));

        inventoryService.consumeForRepair(1L, 3, 1L, "tech1");

        assertThat(testItem.getQuantity()).isEqualTo(7);
        verify(inventoryItemRepository).save(testItem);
        verify(inventoryMovementRepository).save(any(InventoryMovement.class));
    }

    @Test
    @DisplayName("consumeForRepair - ошибка при недостатке товара")
    void consumeForRepair_throwsWhenInsufficient() {
        testItem.setQuantity(2);
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(repairOrderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(employeeRepository.findByLogin("tech1")).thenReturn(Optional.of(testEmployee));

        assertThatThrownBy(() -> inventoryService.consumeForRepair(1L, 5, 1L, "tech1"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("receiveStock - успешный приход товара")
    void receiveStock_success() {
        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(employeeRepository.findByLogin("tech1")).thenReturn(Optional.of(testEmployee));

        inventoryService.receiveStock(1L, 20, "tech1", "Поставка от Samsung");

        assertThat(testItem.getQuantity()).isEqualTo(30);
        verify(inventoryItemRepository).save(testItem);
        verify(inventoryMovementRepository).save(any(InventoryMovement.class));
    }

    @Test
    @DisplayName("getLowStock - возвращает товары с низким запасом")
    void getLowStock_returnsList() {
        testItem.setQuantity(1);
        when(inventoryItemRepository.findLowStockItems()).thenReturn(List.of(testItem));

        List<InventoryItemResponse> result = inventoryService.getLowStock();

        assertThat(result).hasSize(1);
    }
}
