package com.constellio.model.services.background;

import com.constellio.data.io.IOServicesFactory;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.utils.SavestateServices;

import java.io.File;

public class BaseSaveStateExportBackgroundAction implements Runnable {


	private static final String TEMP_FILE_RESOURCE = "BaseSaveStateExportBackgroundAction-tempFile";

	private ModelLayerFactory modelLayerFactory;
	private IOServicesFactory ioServicesFactory;

	public BaseSaveStateExportBackgroundAction(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.ioServicesFactory = modelLayerFactory.getDataLayerFactory().getIOServicesFactory();
	}

	@Override
	public void run() {
		File tempFile = ioServicesFactory.newIOServices().newTemporaryFile(TEMP_FILE_RESOURCE);
		exportToFile(tempFile);

		new SavestateServices(modelLayerFactory).createSavestateBaseFile(tempFile);

	}

	public void exportToFile(File file) {


	}
}
