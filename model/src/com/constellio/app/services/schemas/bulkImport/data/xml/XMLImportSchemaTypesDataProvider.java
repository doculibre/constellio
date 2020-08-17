package com.constellio.app.services.schemas.bulkImport.data.xml;

import com.constellio.app.services.schemas.bulkImport.data.ImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.io.File;
import java.io.Reader;
import java.util.List;

public class XMLImportSchemaTypesDataProvider extends XMLImportDataProvider implements ImportDataProvider {

	private static final String FILE_READER_STREAM_NAME = "XMLImportSchemaTypeDataProvider-TempFolder";
	private static final String TEMP_FOLDER_STREAM_NAME = "XMLImportSchemaTypeDataProvider-FileReader";

	public XMLImportSchemaTypesDataProvider(File xmlFile, IOServicesFactory ioServicesFactory, boolean isZip,
											String fileName) {
		super(xmlFile, ioServicesFactory, isZip, fileName);
	}

	public static XMLImportDataProvider forZipFile(ModelLayerFactory modelLayerFactory, File zipFile) {
		return new XMLImportSchemaTypesDataProvider(zipFile, modelLayerFactory.getIOServicesFactory(), true, "");
	}

	public static XMLImportDataProvider forSingleXMLFile(ModelLayerFactory modelLayerFactory, File xmlFile,
														 String xmlFileName) {
		return new XMLImportSchemaTypesDataProvider(xmlFile, modelLayerFactory.getIOServicesFactory(), false, xmlFileName);
	}

	@Override
	protected ImportDataIterator getXMLFileImportDataIterator(Reader reader, IOServices ioServices, String fileName) {
		if (fileName.contains("valueDomain")) {
			return new XMLFileImportTypesDataIterator(reader, ioServices);
		} else {
			return new XMLFileImportSchemasDataIterator(reader, ioServices);
		}
	}

	@Override
	protected String getFileReaderStreamName() {
		return FILE_READER_STREAM_NAME;
	}

	@Override
	protected String getTempFolderStreamName() {
		return TEMP_FOLDER_STREAM_NAME;
	}

	@Override
	public List<File> getImportedContents() {
		return null;
	}
}
