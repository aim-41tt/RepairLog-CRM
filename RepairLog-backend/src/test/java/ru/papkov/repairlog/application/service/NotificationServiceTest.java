package ru.papkov.repairlog.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.papkov.repairlog.application.dto.notification.NotificationResponse;
import ru.papkov.repairlog.domain.exception.EntityNotFoundException;
import ru.papkov.repairlog.domain.model.Client;
import ru.papkov.repairlog.domain.model.Notification;
import ru.papkov.repairlog.domain.model.RepairOrder;
import ru.papkov.repairlog.domain.repository.NotificationRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification testNotification;
    private Client testClient;
    private RepairOrder testOrder;

    @BeforeEach
    void setUp() {
        testClient = new Client();
        testClient.setId(1L);
        testClient.setName("Иван");
        testClient.setSurname("Петров");

        testOrder = new RepairOrder();
        testOrder.setId(1L);
        testOrder.setOrderNumber("RO-20260228-0001");

        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setClient(testClient);
        testNotification.setRepairOrder(testOrder);
        testNotification.setNotificationType(Notification.NotificationType.SMS);
        testNotification.setMessage("Ваш заказ RO-20260228-0001 принят");
        testNotification.setStatus(Notification.NotificationStatus.PENDING);
    }

    @Test
    @DisplayName("getByClient - возвращает уведомления клиента")
    void getByClient_returnsList() {
        when(notificationRepository.findByClientId(1L)).thenReturn(List.of(testNotification));

        List<NotificationResponse> result = notificationService.getByClient(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClientId()).isEqualTo(1L);
        assertThat(result.get(0).getType()).isEqualTo("SMS");
    }

    @Test
    @DisplayName("getByClient - возвращает пустой список если уведомлений нет")
    void getByClient_returnsEmptyList() {
        when(notificationRepository.findByClientId(99L)).thenReturn(List.of());

        List<NotificationResponse> result = notificationService.getByClient(99L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getByOrder - возвращает уведомления по заказу")
    void getByOrder_returnsList() {
        when(notificationRepository.findByRepairOrderId(1L)).thenReturn(List.of(testNotification));

        List<NotificationResponse> result = notificationService.getByOrder(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRepairOrderId()).isEqualTo(1L);
        assertThat(result.get(0).getOrderNumber()).isEqualTo("RO-20260228-0001");
    }

    @Test
    @DisplayName("getPending - возвращает неотправленные уведомления")
    void getPending_returnsPendingNotifications() {
        when(notificationRepository.findByStatus(Notification.NotificationStatus.PENDING))
                .thenReturn(List.of(testNotification));

        List<NotificationResponse> result = notificationService.getPending();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("markAsSent - помечает уведомление как отправленное")
    void markAsSent_success() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        notificationService.markAsSent(1L);

        assertThat(testNotification.getStatus()).isEqualTo(Notification.NotificationStatus.SENT);
        assertThat(testNotification.getSentAt()).isNotNull();
        verify(notificationRepository).save(testNotification);
    }

    @Test
    @DisplayName("markAsSent - ошибка если уведомление не найдено")
    void markAsSent_throwsWhenNotFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsSent(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("getByClient - корректно обрабатывает уведомление без заказа")
    void getByClient_handlesNullRepairOrder() {
        Notification notifWithoutOrder = new Notification();
        notifWithoutOrder.setId(2L);
        notifWithoutOrder.setClient(testClient);
        notifWithoutOrder.setRepairOrder(null);
        notifWithoutOrder.setNotificationType(Notification.NotificationType.EMAIL);
        notifWithoutOrder.setMessage("Общее уведомление");
        notifWithoutOrder.setStatus(Notification.NotificationStatus.SENT);

        when(notificationRepository.findByClientId(1L)).thenReturn(List.of(notifWithoutOrder));

        List<NotificationResponse> result = notificationService.getByClient(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRepairOrderId()).isNull();
        assertThat(result.get(0).getOrderNumber()).isNull();
    }
}
