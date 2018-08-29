package com.constellio.app.services.importExport;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.io.File;

public class ExportServices {

	public static final String RECORDS_EXPORT_TEMP_FOLDER = "RecordsExportServices_recordsExportTempFolder";
	public static final String RECORDS_EXPORT_ZIPS_TEMP_FOLDER = "RecordsExportServices_recordsExportZipsTempFolder";

	AppLayerFactory appLayerFactory;
	ModelLayerFactory modelLayerFactory;
	ZipService zipService;
	IOServices ioServices;

	public ExportServices(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.modelLayerFactory = appLayerFactory.getModelLayerFactory();
		this.zipService = modelLayerFactory.getIOServicesFactory().newZipService();
		this.ioServices = modelLayerFactory.getIOServicesFactory().newIOServices();
	}

	public File exportRecords(String resourceKey, ExportOptions options) {
		return null;
	}

}
