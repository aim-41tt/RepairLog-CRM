package ru.papkov.repairlog.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.papkov.repairlog.application.dto.client.ClientResponse;
import ru.papkov.repairlog.application.dto.client.CreateClientRequest;
import ru.papkov.repairlog.domain.model.Client;

import java.util.List;

/**
 * MapStruct маппер для сущности {@link Client}.
 * <p>
 * Поле {@code fullName} маппится из вычисляемого метода {@link Client#getFullName()}.
 * </p>
 *
 * @author aim-41tt
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClientMapper {

    /**
     * Конвертирует entity {@link Client} в {@link ClientResponse}.
     * fullName берётся автоматически из Client.getFullName().
     */
    ClientResponse toResponse(Client client);

    /**
     * Конвертирует список entity в список DTO.
     */
    List<ClientResponse> toResponseList(List<Client> clients);

    /**
     * Создаёт новую entity {@link Client} из запроса {@link CreateClientRequest}.
     * Игнорирует id, поля 152-ФЗ и аудит-поля BaseEntity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "consentGiven", constant = "false")
    @Mapping(target = "consentDate", ignore = true)
    @Mapping(target = "dataRetentionUntil", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    Client toEntity(CreateClientRequest request);

    /**
     * Обновляет существующую entity {@link Client} данными из запроса.
     * Игнорирует id, поля 152-ФЗ и аудит-поля BaseEntity.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "consentGiven", ignore = true)
    @Mapping(target = "consentDate", ignore = true)
    @Mapping(target = "dataRetentionUntil", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    void updateEntity(CreateClientRequest request, @MappingTarget Client client);
}
