package ru.papkov.repairlog.infrastructure.scheduling;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import ru.papkov.repairlog.application.service.SupplySettingService;

/**
 * Конфигурация расписания задач обработки персональных данных (152-ФЗ).
 * Cron-выражение берётся из таблицы supply_settings и может меняться динамически.
 * <p>
 * По умолчанию: ежедневно в 02:00 ({@code 0 0 2 * * *}).
 *
 * @author aim-41tt
 */
@Configuration
public class DataRetentionSchedulingConfig implements SchedulingConfigurer {

    private final DataRetentionScheduler dataRetentionScheduler;
    private final SupplySettingService settingService;

    public DataRetentionSchedulingConfig(DataRetentionScheduler dataRetentionScheduler,
                                          SupplySettingService settingService) {
        this.dataRetentionScheduler = dataRetentionScheduler;
        this.settingService = settingService;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // Анонимизация ПДн — cron из настроек
        taskRegistrar.addTriggerTask(
                dataRetentionScheduler::processExpiredData,
                triggerContext -> {
                    String cron = settingService.getValue("data.retention.cron", "0 0 2 * * *");
                    return new CronTrigger(cron).nextExecution(triggerContext);
                }
        );
    }
}
