package com.constellio.data.threads;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@DisallowConcurrentExecution
public abstract class ConstellioJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstellioJob.class);

    private Action action;

    private boolean unscheduleOnException;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        final JobKey jobKey = context.getJobDetail().getKey();
        try {
            LOGGER.info(jobKey + " fired");

            action.run();

            LOGGER.info(jobKey + " finished");
        } catch (final Throwable t) {
            final JobExecutionException jobExecutionException = new JobExecutionException(t);
            jobExecutionException.setUnscheduleFiringTrigger(unscheduleOnException);
            throw jobExecutionException;
        }
    }

    public void setAction(final Action action) {
        this.action = action;
    }

    public void setUnscheduleOnException(final boolean unscheduleOnException) {
        this.unscheduleOnException = unscheduleOnException;
    }

    public interface Action {
        void run();
    }

}
