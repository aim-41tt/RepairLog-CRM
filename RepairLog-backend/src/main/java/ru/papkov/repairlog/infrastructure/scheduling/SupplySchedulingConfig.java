package ru.papkov.repairlog.infrastructure.scheduling;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import ru.papkov.repairlog.application.service.SupplySettingService;

/**
 * Конфигурация расписания задач поставок.
 * Cron-выражения берутся из таблицы supply_settings и могут меняться динамически.
 */
@Configuration
public class SupplySchedulingConfig implements SchedulingConfigurer {

    private final PriceUpdateScheduler priceUpdateScheduler;
    private final AutoReorderScheduler autoReorderScheduler;
    private final SupplySettingService settingService;

    public SupplySchedulingConfig(PriceUpdateScheduler priceUpdateScheduler,
                                   AutoReorderScheduler autoReorderScheduler,
                                   SupplySettingService settingService) {
        this.priceUpdateScheduler = priceUpdateScheduler;
        this.autoReorderScheduler = autoReorderScheduler;
        this.settingService = settingService;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // Обновление цен — cron из настроек
        taskRegistrar.addTriggerTask(
                priceUpdateScheduler::updatePrices,
                triggerContext -> {
                    String cron = settingService.getValue("price.update.cron", "0 0 6 * * *");
                    return new CronTrigger(cron).nextExecution(triggerContext);
                }
        );

        // Авто-заказ — cron из настроек
        taskRegistrar.addTriggerTask(
                autoReorderScheduler::checkAndReorder,
                triggerContext -> {
                    String cron = settingService.getValue("auto.reorder.cron", "0 0 7 * * *");
                    return new CronTrigger(cron).nextExecution(triggerContext);
                }
        );
    }
}
