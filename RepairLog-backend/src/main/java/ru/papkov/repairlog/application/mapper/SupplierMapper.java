package ru.papkov.repairlog.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.papkov.repairlog.application.dto.supplier.CreateSupplierRequest;
import ru.papkov.repairlog.application.dto.supplier.SupplierResponse;
import ru.papkov.repairlog.domain.model.Supplier;
import ru.papkov.repairlog.domain.model.enums.IntegrationType;
import ru.papkov.repairlog.domain.model.enums.OrderMethod;
import ru.papkov.repairlog.domain.model.enums.PriceSource;

import java.util.List;

/**
 * MapStruct маппер для сущности {@link Supplier}.
 *
 * @author aim-41tt
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {IntegrationType.class, PriceSource.class, OrderMethod.class})
public interface SupplierMapper {

    @Mapping(target = "integrationType", expression = "java(s.getIntegrationType() != null ? s.getIntegrationType().name() : null)")
    @Mapping(target = "priceSource", expression = "java(s.getPriceSource() != null ? s.getPriceSource().name() : null)")
    @Mapping(target = "orderMethod", expression = "java(s.getOrderMethod() != null ? s.getOrderMethod().name() : null)")
    SupplierResponse toResponse(Supplier s);

    List<SupplierResponse> toResponseList(List<Supplier> suppliers);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "integrationType", expression = "java(request.getIntegrationType() != null ? IntegrationType.valueOf(request.getIntegrationType()) : null)")
    @Mapping(target = "priceSource", expression = "java(request.getPriceSource() != null ? PriceSource.valueOf(request.getPriceSource()) : null)")
    @Mapping(target = "orderMethod", expression = "java(request.getOrderMethod() != null ? OrderMethod.valueOf(request.getOrderMethod()) : null)")
    Supplier toEntity(CreateSupplierRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "integrationType", expression = "java(request.getIntegrationType() != null ? IntegrationType.valueOf(request.getIntegrationType()) : supplier.getIntegrationType())")
    @Mapping(target = "priceSource", expression = "java(request.getPriceSource() != null ? PriceSource.valueOf(request.getPriceSource()) : supplier.getPriceSource())")
    @Mapping(target = "orderMethod", expression = "java(request.getOrderMethod() != null ? OrderMethod.valueOf(request.getOrderMethod()) : supplier.getOrderMethod())")
    void updateEntity(CreateSupplierRequest request, @MappingTarget Supplier supplier);
}
