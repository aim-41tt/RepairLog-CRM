package ru.papkov.repairlog.infrastructure.scheduling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.papkov.repairlog.application.service.ClientService;
import ru.papkov.repairlog.application.service.SupplySettingService;
import ru.papkov.repairlog.domain.model.Client;
import ru.papkov.repairlog.domain.model.Employee;
import ru.papkov.repairlog.domain.model.SecurityAuditLog;
import ru.papkov.repairlog.domain.repository.ClientRepository;
import ru.papkov.repairlog.domain.repository.EmployeeRepository;
import ru.papkov.repairlog.infrastructure.security.audit.AuditLogger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Юнит-тесты для планировщика анонимизации ПДн (152-ФЗ).
 *
 * @author aim-41tt
 */
@ExtendWith(MockitoExtension.class)
class DataRetentionSchedulerTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientService clientService;

    @Mock
    private SupplySettingService settingService;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AuditLogger auditLogger;

    @InjectMocks
    private DataRetentionScheduler scheduler;

    private Employee systemUser;
    private Client expiredClient;
    private Client revokedClient;

    @BeforeEach
    void setUp() {
        systemUser = new Employee();
        systemUser.setId(1L);
        systemUser.setLogin("admin");
        systemUser.setName("Системный");
        systemUser.setSurname("Администратор");
        systemUser.setDateBirth(LocalDate.of(1990, 1, 1));

        expiredClient = new Client();
        expiredClient.setId(10L);
        expiredClient.setName("Иван");
        expiredClient.setSurname("Петров");
        expiredClient.setPatronymic("Сергеевич");
        expiredClient.setDateBirth(LocalDate.of(1990, 5, 15));
        expiredClient.setPhone("+79001234567");
        expiredClient.setDataRetentionUntil(LocalDate.now().minusDays(1));

        revokedClient = new Client();
        revokedClient.setId(20L);
        revokedClient.setName("Мария");
        revokedClient.setSurname("Сидорова");
        revokedClient.setDateBirth(LocalDate.of(1985, 3, 10));
        revokedClient.setPhone("+79005555555");
        revokedClient.setConsentGiven(false);
        revokedClient.setConsentDate(LocalDateTime.of(2023, 1, 15, 10, 0));
    }

    @Test
    @DisplayName("processExpiredData - не выполняется если отключено в настройках")
    void processExpiredData_disabledInSettings() {
        when(settingService.getBooleanValue("data.retention.enabled", false)).thenReturn(false);

        scheduler.processExpiredData();

        verifyNoInteractions(clientRepository, clientService, auditLogger);
    }

    @Test
    @DisplayName("processExpiredData - ошибка если системный пользователь не найден")
    void processExpiredData_systemUserNotFound() {
        when(settingService.getBooleanValue("data.retention.enabled", false)).thenReturn(true);
        when(settingService.getValue("system.user.login", "admin")).thenReturn("admin");
        when(employeeRepository.findByLogin("admin")).thenReturn(Optional.empty());

        scheduler.processExpiredData();

        verifyNoInteractions(clientService, auditLogger);
    }

    @Test
    @DisplayName("processExpiredData - анонимизирует клиента с истёкшим сроком хранения")
    void processExpiredData_anonymizesExpiredClient() {
        when(settingService.getBooleanValue("data.retention.enabled", false)).thenReturn(true);
        when(settingService.getValue("system.user.login", "admin")).thenReturn("admin");
        when(employeeRepository.findByLogin("admin")).thenReturn(Optional.of(systemUser));
        when(clientRepository.findByDataRetentionUntilBefore(any(LocalDate.class)))
                .thenReturn(List.of(expiredClient));
        when(clientRepository.findByConsentGivenFalse()).thenReturn(List.of());
        when(clientService.isAnonymized(expiredClient)).thenReturn(false);

        scheduler.processExpiredData();

        verify(clientService).anonymizeClient(expiredClient);
        verify(auditLogger).logDataAccess(
                eq(systemUser), eq("CLIENT"), eq(10L),
                eq("ANONYMIZE"), eq(SecurityAuditLog.Result.SUCCESS));
    }

    @Test
    @DisplayName("processExpiredData - анонимизирует клиента с отозванным согласием")
    void processExpiredData_anonymizesRevokedConsentClient() {
        when(settingService.getBooleanValue("data.retention.enabled", false)).thenReturn(true);
        when(settingService.getValue("system.user.login", "admin")).thenReturn("admin");
        when(employeeRepository.findByLogin("admin")).thenReturn(Optional.of(systemUser));
        when(clientRepository.findByDataRetentionUntilBefore(any(LocalDate.class)))
                .thenReturn(List.of());
        when(clientRepository.findByConsentGivenFalse()).thenReturn(List.of(revokedClient));
        when(clientService.isAnonymized(revokedClient)).thenReturn(false);

        scheduler.processExpiredData();

        verify(clientService).anonymizeClient(revokedClient);
        verify(auditLogger).logDataAccess(
                eq(systemUser), eq("CLIENT"), eq(20L),
                eq("ANONYMIZE"), eq(SecurityAuditLog.Result.SUCCESS));
    }

    @Test
    @DisplayName("processExpiredData - пропускает клиента без согласия (consentDate == null)")
    void processExpiredData_skipsClientWithoutConsentDate() {
        Client neverConsentedClient = new Client();
        neverConsentedClient.setId(30L);
        neverConsentedClient.setName("Алексей");
        neverConsentedClient.setSurname("Новиков");
        neverConsentedClient.setDateBirth(LocalDate.of(1995, 7, 20));
        neverConsentedClient.setPhone("+79009999999");
        neverConsentedClient.setConsentGiven(false);
        neverConsentedClient.setConsentDate(null); // никогда не давал согласие

        when(settingService.getBooleanValue("data.retention.enabled", false)).thenReturn(true);
        when(settingService.getValue("system.user.login", "admin")).thenReturn("admin");
        when(employeeRepository.findByLogin("admin")).thenReturn(Optional.of(systemUser));
        when(clientRepository.findByDataRetentionUntilBefore(any(LocalDate.class)))
                .thenReturn(List.of());
        when(clientRepository.findByConsentGivenFalse()).thenReturn(List.of(neverConsentedClient));

        scheduler.processExpiredData();

        verify(clientService, never()).anonymizeClient(any());
        verifyNoInteractions(auditLogger);
    }

    @Test
    @DisplayName("processExpiredData - пропускает уже анонимизированного клиента")
    void processExpiredData_skipsAlreadyAnonymized() {
        when(settingService.getBooleanValue("data.retention.enabled", false)).thenReturn(true);
        when(settingService.getValue("system.user.login", "admin")).thenReturn("admin");
        when(employeeRepository.findByLogin("admin")).thenReturn(Optional.of(systemUser));
        when(clientRepository.findByDataRetentionUntilBefore(any(LocalDate.class)))
                .thenReturn(List.of(expiredClient));
        when(clientRepository.findByConsentGivenFalse()).thenReturn(List.of());
        when(clientService.isAnonymized(expiredClient)).thenReturn(true);

        scheduler.processExpiredData();

        verify(clientService, never()).anonymizeClient(any());
        verifyNoInteractions(auditLogger);
    }

    @Test
    @DisplayName("processExpiredData - дедупликация: клиент попадает и по сроку, и по отзыву — обрабатывается один раз")
    void processExpiredData_deduplicatesClients() {
        revokedClient.setDataRetentionUntil(LocalDate.now().minusDays(5));

        when(settingService.getBooleanValue("data.retention.enabled", false)).thenReturn(true);
        when(settingService.getValue("system.user.login", "admin")).thenReturn("admin");
        when(employeeRepository.findByLogin("admin")).thenReturn(Optional.of(systemUser));
        // Один и тот же клиент попадает из обоих источников
        when(clientRepository.findByDataRetentionUntilBefore(any(LocalDate.class)))
                .thenReturn(List.of(revokedClient));
        when(clientRepository.findByConsentGivenFalse()).thenReturn(List.of(revokedClient));
        when(clientService.isAnonymized(revokedClient)).thenReturn(false);

        scheduler.processExpiredData();

        // Должен быть обработан только один раз
        verify(clientService, times(1)).anonymizeClient(revokedClient);
        verify(auditLogger, times(1)).logDataAccess(
                eq(systemUser), eq("CLIENT"), eq(20L),
                eq("ANONYMIZE"), eq(SecurityAuditLog.Result.SUCCESS));
    }

    @Test
    @DisplayName("processExpiredData - ошибка анонимизации одного клиента не ломает обработку остальных")
    void processExpiredData_handlesExceptionGracefully() {
        Client secondClient = new Client();
        secondClient.setId(30L);
        secondClient.setName("Алексей");
        secondClient.setSurname("Козлов");
        secondClient.setDateBirth(LocalDate.of(1988, 11, 5));
        secondClient.setPhone("+79007777777");
        secondClient.setDataRetentionUntil(LocalDate.now().minusDays(10));

        when(settingService.getBooleanValue("data.retention.enabled", false)).thenReturn(true);
        when(settingService.getValue("system.user.login", "admin")).thenReturn("admin");
        when(employeeRepository.findByLogin("admin")).thenReturn(Optional.of(systemUser));
        when(clientRepository.findByDataRetentionUntilBefore(any(LocalDate.class)))
                .thenReturn(List.of(expiredClient, secondClient));
        when(clientRepository.findByConsentGivenFalse()).thenReturn(List.of());
        when(clientService.isAnonymized(any())).thenReturn(false);

        // Первый клиент — ошибка, второй — успешно
        doThrow(new RuntimeException("DB error")).when(clientService).anonymizeClient(expiredClient);
        doNothing().when(clientService).anonymizeClient(secondClient);

        scheduler.processExpiredData();

        // Оба клиента должны быть обработаны
        verify(clientService).anonymizeClient(expiredClient);
        verify(clientService).anonymizeClient(secondClient);

        // Первый — FAILURE, второй — SUCCESS
        verify(auditLogger).logDataAccess(
                eq(systemUser), eq("CLIENT"), eq(10L),
                eq("ANONYMIZE"), eq(SecurityAuditLog.Result.FAILURE));
        verify(auditLogger).logDataAccess(
                eq(systemUser), eq("CLIENT"), eq(30L),
                eq("ANONYMIZE"), eq(SecurityAuditLog.Result.SUCCESS));
    }

    @Test
    @DisplayName("processExpiredData - пустой список клиентов — ничего не делает")
    void processExpiredData_noClientsToProcess() {
        when(settingService.getBooleanValue("data.retention.enabled", false)).thenReturn(true);
        when(settingService.getValue("system.user.login", "admin")).thenReturn("admin");
        when(employeeRepository.findByLogin("admin")).thenReturn(Optional.of(systemUser));
        when(clientRepository.findByDataRetentionUntilBefore(any(LocalDate.class)))
                .thenReturn(List.of());
        when(clientRepository.findByConsentGivenFalse()).thenReturn(List.of());

        scheduler.processExpiredData();

        verify(clientService, never()).anonymizeClient(any());
        verifyNoInteractions(auditLogger);
    }
}
