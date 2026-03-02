package ru.papkov.repairlog.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit-тесты для бизнес-логики доменных моделей.
 */
class DomainModelTest {

    // ========== Client ==========

    @Nested
    @DisplayName("Client")
    class ClientTests {

        @Test
        @DisplayName("getFullName - формат 'Фамилия И.О.'")
        void getFullName_withPatronymic() {
            Client client = new Client();
            client.setName("Иван");
            client.setSurname("Петров");
            client.setPatronymic("Сергеевич");

            assertThat(client.getFullName()).isEqualTo("Петров И.С.");
        }

        @Test
        @DisplayName("getFullName - без отчества")
        void getFullName_withoutPatronymic() {
            Client client = new Client();
            client.setName("Иван");
            client.setSurname("Петров");
            client.setPatronymic(null);

            assertThat(client.getFullName()).isEqualTo("Петров И.");
        }

        @Test
        @DisplayName("getFullNameExpanded - полное имя с отчеством")
        void getFullNameExpanded_withPatronymic() {
            Client client = new Client();
            client.setName("Иван");
            client.setSurname("Петров");
            client.setPatronymic("Сергеевич");

            assertThat(client.getFullNameExpanded()).isEqualTo("Петров Иван Сергеевич");
        }

        @Test
        @DisplayName("getFullNameExpanded - без отчества")
        void getFullNameExpanded_withoutPatronymic() {
            Client client = new Client();
            client.setName("Иван");
            client.setSurname("Петров");
            client.setPatronymic("");

            assertThat(client.getFullNameExpanded()).isEqualTo("Петров Иван");
        }

        @Test
        @DisplayName("giveConsent - устанавливает согласие и дату")
        void giveConsent_setsFields() {
            Client client = new Client();
            assertThat(client.getConsentGiven()).isFalse();

            client.giveConsent();

            assertThat(client.getConsentGiven()).isTrue();
            assertThat(client.getConsentDate()).isNotNull();
        }

        @Test
        @DisplayName("revokeConsent - отзывает согласие")
        void revokeConsent_setsConsentFalse() {
            Client client = new Client();
            client.giveConsent();
            assertThat(client.getConsentGiven()).isTrue();

            client.revokeConsent();

            assertThat(client.getConsentGiven()).isFalse();
        }
    }

    // ========== Employee ==========

    @Nested
    @DisplayName("Employee")
    class EmployeeTests {

        @Test
        @DisplayName("getFullName - формат 'Фамилия И.О.'")
        void getFullName_withPatronymic() {
            Employee employee = new Employee();
            employee.setName("Анна");
            employee.setSurname("Сидорова");
            employee.setPatronymic("Петровна");

            assertThat(employee.getFullName()).isEqualTo("Сидорова А.П.");
        }

        @Test
        @DisplayName("getFullNameExpanded - полное развернутое имя")
        void getFullNameExpanded() {
            Employee employee = new Employee();
            employee.setName("Анна");
            employee.setSurname("Сидорова");
            employee.setPatronymic("Петровна");

            assertThat(employee.getFullNameExpanded()).isEqualTo("Сидорова Анна Петровна");
        }

        @Test
        @DisplayName("hasRole - проверяет наличие роли")
        void hasRole_returnsTrue() {
            Employee employee = new Employee();
            Role role = new Role();
            role.setName("ADMIN");
            employee.getRoles().add(role);

            assertThat(employee.hasRole("ADMIN")).isTrue();
            assertThat(employee.hasRole("TECHNICIAN")).isFalse();
        }

        @Test
        @DisplayName("isAccountLocked - заблокирован в будущем")
        void isAccountLocked_lockedInFuture() {
            Employee employee = new Employee();
            employee.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));

            assertThat(employee.isAccountLocked()).isTrue();
        }

        @Test
        @DisplayName("isAccountLocked - блокировка истекла")
        void isAccountLocked_lockExpired() {
            Employee employee = new Employee();
            employee.setAccountLockedUntil(LocalDateTime.now().minusMinutes(1));

            assertThat(employee.isAccountLocked()).isFalse();
        }

        @Test
        @DisplayName("isAccountLocked - null значение")
        void isAccountLocked_nullValue() {
            Employee employee = new Employee();
            employee.setAccountLockedUntil(null);

            assertThat(employee.isAccountLocked()).isFalse();
        }
    }

    // ========== RepairOrder ==========

    @Nested
    @DisplayName("RepairOrder")
    class RepairOrderTests {

        @Test
        @DisplayName("isCompleted - завершён если есть дата завершения")
        void isCompleted_true() {
            RepairOrder order = new RepairOrder();
            order.complete();

            assertThat(order.isCompleted()).isTrue();
            assertThat(order.getActualCompletionDate()).isNotNull();
        }

        @Test
        @DisplayName("isCompleted - не завершён без даты")
        void isCompleted_false() {
            RepairOrder order = new RepairOrder();

            assertThat(order.isCompleted()).isFalse();
        }

        @Test
        @DisplayName("hasAssignedMaster - мастер назначен")
        void hasAssignedMaster_true() {
            Employee master = new Employee();
            master.setName("Мастер");
            master.setSurname("Техников");

            RepairOrder order = new RepairOrder();
            order.assignMaster(master);

            assertThat(order.hasAssignedMaster()).isTrue();
            assertThat(order.getAssignedMaster()).isEqualTo(master);
        }

        @Test
        @DisplayName("hasAssignedMaster - мастер не назначен")
        void hasAssignedMaster_false() {
            RepairOrder order = new RepairOrder();

            assertThat(order.hasAssignedMaster()).isFalse();
        }
    }

    // ========== Notification ==========

    @Nested
    @DisplayName("Notification")
    class NotificationTests {

        @Test
        @DisplayName("markAsSent - помечает как отправленное")
        void markAsSent_setsStatusAndDate() {
            Notification notification = new Notification();

            notification.markAsSent();

            assertThat(notification.getStatus()).isEqualTo(Notification.NotificationStatus.SENT);
            assertThat(notification.getSentAt()).isNotNull();
        }

        @Test
        @DisplayName("markAsFailed - помечает как неудачное")
        void markAsFailed_setsStatusAndIncrementsRetry() {
            Notification notification = new Notification();

            notification.markAsFailed("Connection timeout");

            assertThat(notification.getStatus()).isEqualTo(Notification.NotificationStatus.FAILED);
            assertThat(notification.getErrorMessage()).isEqualTo("Connection timeout");
            assertThat(notification.getRetryCount()).isEqualTo(1);
            assertThat(notification.getLastAttempt()).isNotNull();
        }

        @Test
        @DisplayName("canRetry - можно повторить если не превышен лимит")
        void canRetry_withinLimit() {
            Notification notification = new Notification();
            notification.markAsFailed("Error");

            assertThat(notification.canRetry(3)).isTrue();
        }

        @Test
        @DisplayName("canRetry - нельзя повторить если лимит исчерпан")
        void canRetry_limitReached() {
            Notification notification = new Notification();
            notification.markAsFailed("Error 1");
            notification.markAsFailed("Error 2");
            notification.markAsFailed("Error 3");

            assertThat(notification.canRetry(3)).isFalse();
        }

        @Test
        @DisplayName("canRetry - нельзя повторить если статус PENDING")
        void canRetry_wrongStatus() {
            Notification notification = new Notification();
            // статус по умолчанию — PENDING

            assertThat(notification.canRetry(3)).isFalse();
        }
    }

    // ========== Receipt ==========

    @Nested
    @DisplayName("Receipt")
    class ReceiptTests {

        @Test
        @DisplayName("isEditable - редактируемый если не заблокирован")
        void isEditable_true() {
            Receipt receipt = new Receipt();

            assertThat(receipt.isEditable()).isTrue();
        }

        @Test
        @DisplayName("isEditable - не редактируемый после блокировки")
        void isEditable_afterLock() {
            Receipt receipt = new Receipt();
            Employee employee = new Employee();
            employee.setName("Тест");
            employee.setSurname("Тестов");

            receipt.lock(employee);

            assertThat(receipt.isEditable()).isFalse();
            assertThat(receipt.getLocked()).isTrue();
            assertThat(receipt.getLockedAt()).isNotNull();
            assertThat(receipt.getLockedBy()).isEqualTo(employee);
        }

        @Test
        @DisplayName("isFullyPaid - оплачен полностью")
        void isFullyPaid_true() {
            Receipt receipt = new Receipt();
            receipt.setPaymentStatus(Receipt.PaymentStatus.FULLY_PAID);

            assertThat(receipt.isFullyPaid()).isTrue();
        }

        @Test
        @DisplayName("isFullyPaid - не оплачен")
        void isFullyPaid_false() {
            Receipt receipt = new Receipt();

            assertThat(receipt.isFullyPaid()).isFalse();
        }
    }

    // ========== InventoryItem ==========

    @Nested
    @DisplayName("InventoryItem")
    class InventoryItemTests {

        @Test
        @DisplayName("isBelowMinStock - ниже минимального уровня")
        void isBelowMinStock_true() {
            InventoryItem item = new InventoryItem();
            item.setQuantity(2);
            item.setMinStockLevel(5);

            assertThat(item.isBelowMinStock()).isTrue();
        }

        @Test
        @DisplayName("isBelowMinStock - на уровне минимума")
        void isBelowMinStock_atMinLevel() {
            InventoryItem item = new InventoryItem();
            item.setQuantity(5);
            item.setMinStockLevel(5);

            assertThat(item.isBelowMinStock()).isFalse();
        }

        @Test
        @DisplayName("decreaseQuantity - уменьшает количество")
        void decreaseQuantity_success() {
            InventoryItem item = new InventoryItem();
            item.setQuantity(10);

            item.decreaseQuantity(3);

            assertThat(item.getQuantity()).isEqualTo(7);
            assertThat(item.getInStock()).isTrue();
        }

        @Test
        @DisplayName("decreaseQuantity - до нуля ставит inStock=false")
        void decreaseQuantity_toZero() {
            InventoryItem item = new InventoryItem();
            item.setQuantity(5);

            item.decreaseQuantity(5);

            assertThat(item.getQuantity()).isZero();
            assertThat(item.getInStock()).isFalse();
        }

        @Test
        @DisplayName("decreaseQuantity - ошибка если недостаточно товара")
        void decreaseQuantity_insufficientStock() {
            InventoryItem item = new InventoryItem();
            item.setQuantity(2);

            assertThatThrownBy(() -> item.decreaseQuantity(5))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Недостаточно");
        }

        @Test
        @DisplayName("increaseQuantity - увеличивает количество")
        void increaseQuantity_success() {
            InventoryItem item = new InventoryItem();
            item.setQuantity(0);
            item.setInStock(false);

            item.increaseQuantity(10);

            assertThat(item.getQuantity()).isEqualTo(10);
            assertThat(item.getInStock()).isTrue();
        }
    }
}
