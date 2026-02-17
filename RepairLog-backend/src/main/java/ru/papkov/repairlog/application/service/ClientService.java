package ru.papkov.repairlog.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.client.*;
import ru.papkov.repairlog.domain.exception.BusinessLogicException;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.Client;
import ru.papkov.repairlog.domain.repository.ClientRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис управления клиентами.
 * Включает управление согласием на обработку ПДн (152-ФЗ).
 *
 * @author aim-41tt
 */
@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> getAll() {
        return clientRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClientResponse getById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> search(String query) {
        // поиск по ФИО или телефону
        if (query.matches("^[0-9+() -]+$")) {
            return clientRepository.findByPhone(query)
                    .map(c -> List.of(toResponse(c)))
                    .orElse(List.of());
        }
        return clientRepository.searchByFullName(query).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ClientResponse create(CreateClientRequest request) {
        clientRepository.findByPhone(request.getPhone()).ifPresent(c -> {
            throw new BusinessLogicException("Клиент с таким телефоном уже существует");
        });

        Client client = new Client();
        client.setName(request.getName());
        client.setSurname(request.getSurname());
        client.setPatronymic(request.getPatronymic());
        client.setDateBirth(request.getDateBirth());
        client.setPhone(request.getPhone());
        client.setEmail(request.getEmail());

        if (request.isConsentGiven()) {
            client.giveConsent();
            // ПДн хранятся 3 года с момента согласия
            client.setDataRetentionUntil(LocalDate.now().plusYears(3));
        }

        Client saved = clientRepository.save(client);
        return toResponse(saved);
    }

    @Transactional
    public ClientResponse update(Long id, CreateClientRequest request) {
        Client client = findById(id);
        client.setName(request.getName());
        client.setSurname(request.getSurname());
        client.setPatronymic(request.getPatronymic());
        client.setDateBirth(request.getDateBirth());
        client.setPhone(request.getPhone());
        client.setEmail(request.getEmail());
        return toResponse(clientRepository.save(client));
    }

    /**
     * Предоставление согласия на обработку ПДн (152-ФЗ).
     */
    @Transactional
    public void giveConsent(Long clientId) {
        Client client = findById(clientId);
        client.giveConsent();
        client.setDataRetentionUntil(LocalDate.now().plusYears(3));
        clientRepository.save(client);
    }

    /**
     * Отзыв согласия на обработку ПДн.
     */
    @Transactional
    public void revokeConsent(Long clientId) {
        Client client = findById(clientId);
        client.revokeConsent();
        clientRepository.save(client);
    }

    // ========== Helpers ==========

    private Client findById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Клиент не найден: " + id));
    }

    private ClientResponse toResponse(Client c) {
        ClientResponse r = new ClientResponse();
        r.setId(c.getId());
        r.setName(c.getName());
        r.setSurname(c.getSurname());
        r.setPatronymic(c.getPatronymic());
        r.setFullName(c.getFullName());
        r.setDateBirth(c.getDateBirth());
        r.setPhone(c.getPhone());
        r.setEmail(c.getEmail());
        r.setConsentGiven(c.getConsentGiven());
        r.setConsentDate(c.getConsentDate());
        r.setDataRetentionUntil(c.getDataRetentionUntil());
        r.setCreatedAt(c.getCreatedAt());
        return r;
    }
}
