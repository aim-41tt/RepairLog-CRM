package ru.papkov.repairlog.infrastructure.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.papkov.repairlog.application.service.ClientService;
import ru.papkov.repairlog.application.service.SupplySettingService;
import ru.papkov.repairlog.domain.model.Client;
import ru.papkov.repairlog.domain.model.Employee;
import ru.papkov.repairlog.domain.model.SecurityAuditLog;
import ru.papkov.repairlog.domain.repository.ClientRepository;
import ru.papkov.repairlog.domain.repository.EmployeeRepository;
import ru.papkov.repairlog.infrastructure.security.audit.AuditLogger;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Планировщик автоматической анонимизации персональных данных клиентов (152-ФЗ).
 * <p>
 * Обрабатывает две категории клиентов:
 * <ol>
 *   <li>Клиенты с истёкшим сроком хранения ПДн ({@code dataRetentionUntil < now()})</li>
 *   <li>Клиенты, отозвавшие согласие (consentGiven = false, consentDate != null)</li>
 * </ol>
 * <p>
 * Согласно ст. 21 ч. 4-5 152-ФЗ, оператор обязан уничтожить (обезличить)
 * персональные данные в течение 30 дней после истечения срока хранения
 * или отзыва согласия субъектом. Срок 30 дней учитывается через поле
 * {@code dataRetentionUntil}, которое устанавливается при отзыве согласия
 * как {@code now() + 30 дней}.
 *
 * @author aim-41tt
 */
@Component
public class DataRetentionScheduler {

    private static final Logger log = LoggerFactory.getLogger(DataRetentionScheduler.class);

    private final ClientRepository clientRepository;
    private final ClientService clientService;
    private final SupplySettingService settingService;
    private final EmployeeRepository employeeRepository;
    private final AuditLogger auditLogger;

    public DataRetentionScheduler(ClientRepository clientRepository,
                                   ClientService clientService,
                                   SupplySettingService settingService,
                                   EmployeeRepository employeeRepository,
                                   AuditLogger auditLogger) {
        this.clientRepository = clientRepository;
        this.clientService = clientService;
        this.settingService = settingService;
        this.employeeRepository = employeeRepository;
        this.auditLogger = auditLogger;
    }

    /**
     * Основной метод обработки — вызывается по расписанию.
     * Находит клиентов с истёкшими сроками хранения ПДн и анонимизирует их данные.
     */
    @Transactional
    public void processExpiredData() {
        if (!settingService.getBooleanValue("data.retention.enabled", false)) {
            log.debug("Автоматическая анонимизация ПДн отключена в настройках");
            return;
        }

        // Системный пользователь для аудит-лога
        String systemLogin = settingService.getValue("system.user.login", "admin");
        Employee systemUser = employeeRepository.findByLogin(systemLogin).orElse(null);
        if (systemUser == null) {
            log.error("Системный пользователь '{}' не найден, анонимизация невозможна", systemLogin);
            return;
        }

        // Собираем клиентов, подлежащих анонимизации (без дубликатов)
        Set<Client> clientsToAnonymize = new LinkedHashSet<>();

        // 1. Клиенты с истёкшим сроком хранения ПДн
        List<Client> expired = clientRepository.findByDataRetentionUntilBefore(LocalDate.now());
        clientsToAnonymize.addAll(expired);

        // 2. Клиенты, отозвавшие согласие (только те, кто ранее давал и затем отозвал)
        List<Client> noConsent = clientRepository.findByConsentGivenFalse();
        for (Client client : noConsent) {
            // Фильтруем: consentDate != null означает, что согласие было дано и потом отозвано.
            // Клиенты, которые никогда не давали согласие (consentDate == null), пропускаются.
            if (client.getConsentDate() != null) {
                clientsToAnonymize.add(client);
            }
        }

        log.info("Анонимизация ПДн: найдено {} клиентов для обработки", clientsToAnonymize.size());

        int anonymized = 0;
        int skipped = 0;

        for (Client client : clientsToAnonymize) {
            // Пропускаем уже анонимизированных (идемпотентность)
            if (clientService.isAnonymized(client)) {
                skipped++;
                continue;
            }

            try {
                clientService.anonymizeClient(client);

                // Аудит-лог: фиксируем факт анонимизации (акт уничтожения по 152-ФЗ)
                auditLogger.logDataAccess(
                        systemUser,
                        "CLIENT",
                        client.getId(),
                        "ANONYMIZE",
                        SecurityAuditLog.Result.SUCCESS
                );

                anonymized++;
                log.debug("Анонимизированы данные клиента id={}", client.getId());
            } catch (Exception e) {
                log.error("Ошибка анонимизации клиента id={}: {}", client.getId(), e.getMessage());

                auditLogger.logDataAccess(
                        systemUser,
                        "CLIENT",
                        client.getId(),
                        "ANONYMIZE",
                        SecurityAuditLog.Result.FAILURE
                );
            }
        }

        log.info("Анонимизация ПДн завершена: обработано {}, пропущено {} (уже анонимизированы)",
                anonymized, skipped);
    }
}
