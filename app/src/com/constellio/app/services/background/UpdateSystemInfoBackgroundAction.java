package com.constellio.app.services.background;

import com.constellio.app.entities.system.SystemInfo;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;

public class UpdateSystemInfoBackgroundAction implements Runnable {
	@Override
	public synchronized void run() {
		SystemInfo.getInstance().recalculate();
	}
}
