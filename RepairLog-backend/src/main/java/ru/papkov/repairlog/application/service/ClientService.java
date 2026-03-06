package ru.papkov.repairlog.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.dto.client.*;
import ru.papkov.repairlog.domain.exception.BusinessLogicException;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.Client;
import ru.papkov.repairlog.domain.model.Device;
import ru.papkov.repairlog.domain.model.Notification;
import ru.papkov.repairlog.domain.model.RepairOrder;
import ru.papkov.repairlog.domain.repository.ClientRepository;
import ru.papkov.repairlog.domain.repository.DeviceRepository;
import ru.papkov.repairlog.domain.repository.NotificationRepository;
import ru.papkov.repairlog.domain.repository.RepairOrderRepository;

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
    private final DeviceRepository deviceRepository;
    private final RepairOrderRepository repairOrderRepository;
    private final NotificationRepository notificationRepository;

    public ClientService(ClientRepository clientRepository,
                         DeviceRepository deviceRepository,
                         RepairOrderRepository repairOrderRepository,
                         NotificationRepository notificationRepository) {
        this.clientRepository = clientRepository;
        this.deviceRepository = deviceRepository;
        this.repairOrderRepository = repairOrderRepository;
        this.notificationRepository = notificationRepository;
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
     * Устанавливает срок уничтожения данных — 30 дней (ст. 21 ч. 5 152-ФЗ).
     */
    @Transactional
    public void revokeConsent(Long clientId) {
        Client client = findById(clientId);
        client.revokeConsent();
        // 152-ФЗ ст. 21: 30 дней на уничтожение после отзыва согласия
        client.setDataRetentionUntil(LocalDate.now().plusDays(30));
        clientRepository.save(client);
    }

    /**
     * Анонимизация персональных данных клиента (152-ФЗ).
     * Заменяет все ПДн на маскированные значения.
     * Также анонимизирует связанные устройства, заказы и уведомления.
     *
     * @param client клиент для анонимизации (должен быть managed entity)
     */
    @Transactional
    public void anonymizeClient(Client client) {
        // 1. Анонимизация ПДн клиента
        client.anonymize();
        // Phone: UNIQUE + CHECK ^[0-9+() -]{6,20}$ → "0000000" + id гарантирует уникальность и валидность
        client.setPhone("0000000" + client.getId());
        clientRepository.save(client);

        // 2. Анонимизация серийных номеров клиентских устройств
        List<Device> devices = deviceRepository.findByClient(client);
        for (Device device : devices) {
            if (device.getSerialNumber() != null) {
                device.setSerialNumber("ANON-" + device.getId());
            }
            deviceRepository.save(device);
        }

        // 3. Анонимизация жалоб клиента в заказах на ремонт
        List<RepairOrder> orders = repairOrderRepository.findByClient(client);
        for (RepairOrder order : orders) {
            if (order.getClientComplaint() != null) {
                order.setClientComplaint("[Данные удалены по 152-ФЗ]");
            }
            repairOrderRepository.save(order);
        }

        // 4. Анонимизация текстов уведомлений
        List<Notification> notifications = notificationRepository.findByClient(client);
        for (Notification notification : notifications) {
            notification.setMessage("[Данные удалены по 152-ФЗ]");
            notificationRepository.save(notification);
        }
    }

    /**
     * Проверить, анонимизированы ли данные клиента.
     */
    public boolean isAnonymized(Client client) {
        return client.isAnonymized();
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
