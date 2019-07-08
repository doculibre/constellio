package com.constellio.app.services.background;

import com.constellio.app.entities.system.SystemInfo;
import com.constellio.data.utils.TimeProvider;
import org.joda.time.LocalDateTime;

public class UpdateSystemInfoBackgroundAction implements Runnable {
	@Override
	public synchronized void run() {
		SystemInfo instance = SystemInfo.getInstance();
		instance.appendConstellioFreeMemory();

		LocalDateTime lastTimeUpdated = instance.getLastTimeUpdated();
		if (lastTimeUpdated.isBefore(TimeProvider.getLocalDateTime().minusMinutes(5))) {
			instance.recalculate();
		}
	}
}
