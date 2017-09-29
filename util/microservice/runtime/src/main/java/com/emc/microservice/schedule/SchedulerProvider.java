package com.emc.microservice.schedule;

import com.emc.microservice.Context;
import com.emc.ocopea.microservice.schedule.SchedulerApi;

public interface SchedulerProvider<SchedulerConfT extends SchedulerConfiguration> {

    SchedulerApi getScheduler(
            SchedulerConfT schedulerConfiguration,
            Context context);

    Class<SchedulerConfT> getConfClass();
}
