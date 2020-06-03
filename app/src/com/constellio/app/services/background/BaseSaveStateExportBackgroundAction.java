package com.constellio.app.services.background;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.importExport.systemStateExport.SystemStateExporter;
import com.constellio.data.io.IOServicesFactory;

public class BaseSaveStateExportBackgroundAction implements Runnable {


	private AppLayerFactory appLayerFactory;
	private IOServicesFactory ioServicesFactory;

	public BaseSaveStateExportBackgroundAction(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.ioServicesFactory = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getIOServicesFactory();
	}

	@Override
	public void run() {
		new SystemStateExporter(appLayerFactory).createSavestateBaseFileInVault();

	}

}
