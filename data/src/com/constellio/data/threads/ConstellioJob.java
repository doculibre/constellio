package com.constellio.data.threads;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 */
@DisallowConcurrentExecution
public abstract class ConstellioJob implements Job {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Override
    public final void execute(JobExecutionContext context) throws JobExecutionException {
        final JobKey jobKey = context.getJobDetail().getKey();

        try {
            LOGGER.info("job fired");

            action().run();

            LOGGER.info("job finished");
        } catch (final Throwable t) {
            final JobExecutionException jobExecutionException = new JobExecutionException(t);
            jobExecutionException.setUnscheduleFiringTrigger(unscheduleOnException());
            throw jobExecutionException;
        }
    }

    protected abstract String name();

    protected abstract Runnable action();

    protected abstract boolean unscheduleOnException();

    protected abstract Set<Integer> intervals();

    protected abstract Set<String> cronExpressions();

    protected Date startTime() {
        return null;
    }

    JobDetail buildJobDetail() {
        return JobBuilder.
                newJob(getClass()).
                withIdentity(name(), Scheduler.DEFAULT_GROUP).
                build();
    }

    Set<Trigger> buildIntervalTriggers() {
        final Set<Trigger> triggers = new HashSet<>();

        Set<Integer> intervals = intervals();

        if (CollectionUtils.isNotEmpty(intervals)) {
            intervals = new TreeSet<>(intervals);

            Date startTime = startTime();

            for (final int interval : intervals) {
                if (startTime == null) {
                    startTime = DateTime.now().plusSeconds(interval).toDate();
                }

                final TriggerBuilder triggerBuilder = TriggerBuilder.
                        newTrigger().
                        withIdentity(name() + "-Trigger-" + interval, Scheduler.DEFAULT_GROUP).
                        withSchedule(SimpleScheduleBuilder.
                                simpleSchedule().
                                withIntervalInSeconds(interval).
                                repeatForever());

                triggerBuilder.startAt(startTime);

                triggers.add(triggerBuilder.build());
            }
        }

        return triggers;
    }

    Set<Trigger> buildCronTriggers() {
        final Set<Trigger> triggers = new HashSet<>();

        Set<String> cronExpressions = cronExpressions();

        if (CollectionUtils.isNotEmpty(cronExpressions)) {
            cronExpressions = new TreeSet<>(cronExpressions);

            Date startTime = startTime();

            for (final String cronExpression : cronExpressions) {
                if (startTime == null) {
                    try {
                        startTime = new CronExpression(cronExpression).getNextValidTimeAfter(new Date());
                    } catch (final ParseException e) {
                        LOGGER.error("invalid cron expression " + cronExpression , e);
                    }
                }

                final TriggerBuilder triggerBuilder = TriggerBuilder.
                        newTrigger().
                        withIdentity(name() + "-Trigger-" + cronExpression, Scheduler.DEFAULT_GROUP).
                        withSchedule(CronScheduleBuilder.
                                cronSchedule(cronExpression));

                triggerBuilder.startAt(startTime);

                triggers.add(triggerBuilder.build());
            }
        }

        return triggers;
    }

}
