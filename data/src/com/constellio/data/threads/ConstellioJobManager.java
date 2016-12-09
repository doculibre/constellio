package com.constellio.data.threads;

import com.constellio.data.dao.managers.StatefulService;
import org.joda.time.DateTime;
import org.quartz.JobDetail;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 *
 */
public class ConstellioJobManager implements StatefulService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstellioJobManager.class);

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

    public Date addJob(final ConstellioJob job, final boolean cleanPreviousTriggers) {
        //
        final Set<Trigger> triggers = job.buildIntervalTriggers();
        triggers.addAll(job.buildCronTriggers());

        //
        if (triggers.isEmpty()) {
            LOGGER.warn(job.name() + " has no trigger");
        } else {
            try {
                //
                final JobDetail jobDetail = job.buildJobDetail();

                //
                if (cleanPreviousTriggers) {
                    scheduler.deleteJob(jobDetail.getKey());
                }

                //
                scheduler.scheduleJob(jobDetail, triggers, true);

                //
                LOGGER.info(job.name() + " successfully scheduled");

                //
                final List<Date> nextFireTimes = new ArrayList<>();
                for (final Trigger trigger : triggers) {
                    nextFireTimes.add(trigger.getFireTimeAfter(DateTime.now().toDate()));
                }
                Collections.sort(nextFireTimes);

                //
                return nextFireTimes.get(0);
            } catch (final SchedulerException e) {
                LOGGER.error(job.name() + " can't be scheduled", e);
            }

        }

        return null;
    }
}
