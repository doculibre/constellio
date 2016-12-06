package com.constellio.app.services.importExport;

import static java.util.Arrays.asList;

import java.io.File;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.importExport.records.writers.ImportRecordWriter;
import com.constellio.app.services.importExport.records.writers.ModifiableImportRecord;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipService;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.model.services.factories.ModelLayerFactory;

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
