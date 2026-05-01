package ru.papkov.repairlog.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.papkov.repairlog.application.dto.inventory.InventoryItemResponse;
import ru.papkov.repairlog.domain.model.InventoryItem;

import java.util.List;

/**
 * MapStruct маппер для сущности {@link InventoryItem}.
 *
 * @author aim-41tt
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InventoryItemMapper {

    /**
     * Конвертирует entity {@link InventoryItem} в {@link InventoryItemResponse}.
     */
    @Mapping(target = "degreeWearName", source = "degreeWear.name")
    @Mapping(target = "device", source = "isDevice")
    @Mapping(target = "preferredSupplierId", source = "preferredSupplier.id")
    @Mapping(target = "preferredSupplierName", source = "preferredSupplier.name")
    @Mapping(target = "stockStatus", source = ".", qualifiedByName = "calcStockStatus")
    InventoryItemResponse toResponse(InventoryItem item);

    /**
     * Конвертирует список entity в список DTO.
     */
    List<InventoryItemResponse> toResponseList(List<InventoryItem> items);

    /**
     * Вычисляет статус наличия на складе.
     *
     * @param item позиция склада
     * @return строковое представление статуса ("GOOD_STOCK", "LOW_STOCK", "OUT_OF_STOCK")
     */
    @Named("calcStockStatus")
    default String calcStockStatus(InventoryItem item) {
        if (item.getQuantity() == null || item.getQuantity() == 0) {
            return "OUT_OF_STOCK";
        }
        if (item.isBelowMinStock()) {
            return "LOW_STOCK";
        }
        return "GOOD_STOCK";
    }
}
