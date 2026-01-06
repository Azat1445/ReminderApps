package org.example.reminderapp.config;

import org.example.reminderapp.job.ReminderJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail reminderJobDetail() {
        return JobBuilder.newJob(ReminderJob.class)
                .withIdentity("reminderJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger reminderTrigger(JobDetail reminderJob) {
        return TriggerBuilder.newTrigger()
                .forJob(reminderJob)
                .withIdentity("reminderTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(1)
                        .repeatForever())
                .build();
    }

}
