package com.constellio.data.service.background;

import com.constellio.data.dao.services.factories.DataLayerFactory;

public class ReadFileSystemContentDaoRecoveryLogsAndRepairs implements Runnable {
	private DataLayerFactory dataLayerFactory;

	public ReadFileSystemContentDaoRecoveryLogsAndRepairs(DataLayerFactory dataLayerFactory) {
		this.dataLayerFactory = dataLayerFactory;
	}

	@Override
	public void run() {
		this.dataLayerFactory.getContentsDao().readLogsAndRepairs();
	}
}
