package com.constellio.model.services.records.utils;

import com.constellio.data.dao.services.transactionLog.SecondTransactionLogManager;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavestateServices {

	public static final String TEMP_FOLDER_RESOURCE_NAME = "SavestateServices-tempFolder";
	private static final String TEMP_FILE_RESOURCE = "SavestateServices-tempFile";
	private static final String TEMP_ZIP_FILE_RESOURCE = "SavestateServices-tempZipFile";

	private static final Logger LOGGER = LoggerFactory.getLogger(SavestateServices.class);

	ModelLayerFactory modelLayerFactory;
	IOServices ioServices;
	ZipService zipService;
	SecondTransactionLogManager secondTransactionLogManager;

	public SavestateServices(ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
		this.zipService = modelLayerFactory.getIOServicesFactory().newZipService();
	}


}
