package com.constellio.data.threads;

import com.constellio.data.dao.managers.StatefulService;
import org.quartz.CronScheduleBuilder;
import org.quartz.DateBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.KeyMatcher;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 *
 */
public class ConstellioJobManager implements StatefulService {

    private Scheduler scheduler;

    @Override
    public void initialize() {
        try {
            scheduler = new StdSchedulerFactory().getScheduler();

            scheduler.start();
        } catch (final SchedulerException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        if (scheduler != null) {
            //
            try {
                scheduler.shutdown(true);
            } catch (final SchedulerException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void addJob(final Class<? extends Job> jobClass, final String name, final int period, final ConstellioJob.Action action, final boolean unscheduleOnException, final boolean cleanPreviousTriggers) throws SchedulerException {
        //
        final JobDetail jobDetail = buildJobDetail(jobClass, name);

        //
        final Set<Trigger> triggers = new HashSet<>();

        if (period != 0) {
            triggers.add(buildTrigger(name, period, DateBuilder.newDate().build()));
        }

        //
        addJob(jobDetail, triggers, action, unscheduleOnException, cleanPreviousTriggers);
    }

    private JobDetail buildJobDetail(final Class<? extends Job> jobClass, final String name) {
        return JobBuilder.
                newJob(jobClass).
                withIdentity(name + "-Job", Scheduler.DEFAULT_GROUP).
                build();
    }

    private Trigger buildTrigger(final String name, final int period, final Date startTime) {
        return TriggerBuilder.
                newTrigger().
                withIdentity(name + "-" + period + "-Trigger", Scheduler.DEFAULT_GROUP).
                startAt(startTime).
                withSchedule(SimpleScheduleBuilder.
                        simpleSchedule().
                        withIntervalInSeconds(period).
                        repeatForever()).
                build();
    }

    private void addJob(final JobDetail jobDetail, final Set<Trigger> triggers, final ConstellioJob.Action action, final boolean unscheduleOnException, final boolean cleanPreviousTriggers) throws SchedulerException {
        //
        if (!triggers.isEmpty()) {
            //
            if (cleanPreviousTriggers) {
                scheduler.deleteJob(jobDetail.getKey());
            }

            //
            scheduler.getListenerManager().addJobListener(new SetActionJobListener(action, unscheduleOnException), KeyMatcher.keyEquals(jobDetail.getKey()));

            //
            scheduler.scheduleJob(jobDetail, triggers, true);
        }
    }

    public void addJob(final Class<? extends Job> jobClass, final String name, final List<String> cronExpressions, final ConstellioJob.Action action, final boolean unscheduleOnException, final boolean cleanPreviousTriggers) throws SchedulerException {
        //
        final JobDetail jobDetail = buildJobDetail(jobClass, name);

        //
        final Set<Trigger> triggers = buildTriggers(name, cronExpressions, DateBuilder.newDate().build());

        //
        addJob(jobDetail, triggers, action, unscheduleOnException, cleanPreviousTriggers);
    }

    private Set<Trigger> buildTriggers(final String name, final List<String> cronExpressions, final Date startTime) {
        final Set<Trigger> triggers = new HashSet<>();

        if (cronExpressions != null) {
            for (final String cronExpression : cronExpressions) {
                triggers.add(buildTrigger(name, cronExpression, startTime));
            }
        }
        return triggers;
    }

    private Trigger buildTrigger(final String name, final String cronExpression, final Date startTime) {
        return TriggerBuilder.
                newTrigger().
                withIdentity(name + "-" + cronExpression + "-Trigger", Scheduler.DEFAULT_GROUP).
                startAt(startTime).
                withSchedule(CronScheduleBuilder.
                        cronSchedule(cronExpression)).
                build();
    }

    private static class SetActionJobListener implements JobListener {

        private final ConstellioJob.Action action;

        private final boolean unscheduleOnException;

        public SetActionJobListener(final ConstellioJob.Action action, final boolean unscheduleOnException) {
            this.action = action;
            this.unscheduleOnException = unscheduleOnException;
        }

        @Override
        public String getName() {
            return getClass().getSimpleName();
        }

        @Override
        public void jobToBeExecuted(JobExecutionContext context) {
            final ConstellioJob job = (ConstellioJob) context.getJobInstance();
            job.setAction(action);
            job.setUnscheduleOnException(unscheduleOnException);
        }

        @Override
        public void jobExecutionVetoed(JobExecutionContext context) {
        }

        @Override
        public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        }

    }
}
