package com.constellio.data.events;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SDKEventBusSendingDelayedService extends SDKEventBusSendingService {

	private int delayInSeconds;

	public SDKEventBusSendingDelayedService(int delayInSeconds) {
		this.delayInSeconds = delayInSeconds;
	}

	@Override
	public void sendRemotely(final Event event) {
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
		executor.schedule(new Runnable() {
			public void run() {
				sendAsyncRemotely(event);
			}
		}, delayInSeconds, TimeUnit.SECONDS);
	}

	private void sendAsyncRemotely(Event event) {
		super.sendRemotely(event);
	}
}
