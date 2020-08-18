package com.constellio.app.services.schemas.bulkImport.data.xml;

import com.constellio.app.services.schemas.bulkImport.data.ImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XMLImportDataProvider implements ImportDataProvider {
	//TODO lock mechanism when importing data

	private static final String TEMP_FOLDER_STREAM_NAME = "XMLImportDataProvider-TempFolder";

	private static final String FILE_READER_STREAM_NAME = "XMLImportDataProvider-FileReader";

	private static final String DATA_DIRECTORY_NAME = "data";

	private IOServicesFactory ioServicesFactory;

	private boolean deleteOnClose = true;

	private File xmlFile;

	private File zipFile;

	private File tempFolder;
	private String fileName;

	XMLImportDataProvider(File file, IOServicesFactory ioServicesFactory, boolean isZipFile, String fileName) {
		if (isZipFile) {
			this.zipFile = file;
		} else {
			this.xmlFile = file;
		}
		this.fileName = fileName;
		this.ioServicesFactory = ioServicesFactory;
	}

	public XMLImportDataProvider() {
	}

	public static XMLImportDataProvider forZipFile(ModelLayerFactory modelLayerFactory, File zipFile) {
		XMLImportDataProvider instance = new XMLImportDataProvider();
		instance.zipFile = zipFile;
		instance.ioServicesFactory = modelLayerFactory.getIOServicesFactory();
		return instance;
	}

	public static XMLImportDataProvider forFolderOfXml(ModelLayerFactory modelLayerFactory, File folder) {
		XMLImportDataProvider instance = new XMLImportDataProvider();
		instance.tempFolder = folder;
		instance.deleteOnClose = false;
		instance.ioServicesFactory = modelLayerFactory.getIOServicesFactory();
		return instance;
	}

	public static XMLImportDataProvider forSingleXMLFile(ModelLayerFactory modelLayerFactory, File xmlFile) {
		return forSingleXMLFile(modelLayerFactory, xmlFile, xmlFile.getName());
	}

	public static XMLImportDataProvider forSingleXMLFile(ModelLayerFactory modelLayerFactory, File xmlFile,
														 String fileName) {
		XMLImportDataProvider instance = new XMLImportDataProvider();
		instance.xmlFile = xmlFile;
		instance.fileName = fileName;
		instance.deleteOnClose = false;
		instance.ioServicesFactory = modelLayerFactory.getIOServicesFactory();
		return instance;
	}

	@Override
	public void initialize() {

		if (zipFile != null) {
			this.tempFolder = ioServicesFactory.newFileService().newTemporaryFolder(getTempFolderStreamName());
			try {
				ioServicesFactory.newZipService().unzip(zipFile, tempFolder);
			} catch (ZipServiceException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void close() {
		if (deleteOnClose) {
			ioServicesFactory.newIOServices().deleteQuietly(tempFolder);
		}
	}

	@Override
	public int size(String schemaType) {
		int size = 0;
		ImportDataIterator iterator = newDataIterator(schemaType);
		try {
			while (iterator.hasNext()) {
				iterator.next();
				size++;
			}
		} finally {
			iterator.close();
		}
		return size;
	}

	@Override
	public List<String> getAvailableSchemaTypes() {
		List<String> schemaTypes = new ArrayList<>();
		if (tempFolder != null && tempFolder.listFiles() != null) {
			for (File file : tempFolder.listFiles()) {
				if (file.getName().endsWith(".xml")) {
					schemaTypes.add(file.getName().replace(".xml", ""));
				}
			}
		}
		if (xmlFile != null) {
			schemaTypes.add(xmlFile.getName().replace(".xml", ""));
		}

		return schemaTypes;
	}


	@Override
	public List<File> getImportedContents() {
		File dataFolder = findDataFolder();
		if (dataFolder != null) {
			return Arrays.asList(dataFolder.listFiles());
		}
		return null;
	}


	@Override
	public ImportDataIterator newDataIterator(String schemaType) {
		IOServices ioServices = ioServicesFactory.newIOServices();
		File file;
		String currentFileName;
		if (xmlFile != null) {
			file = xmlFile;
			currentFileName = fileName;
		} else {
			file = new File(tempFolder, schemaType + ".xml");
			currentFileName = schemaType;
		}
		Reader reader = ioServices.newBufferedFileReader(file, getFileReaderStreamName());
		return getXMLFileImportDataIterator(reader, ioServices, currentFileName);
	}

	private File findDataFolder() {
		if (tempFolder != null && tempFolder.listFiles() != null) {
			FileFilter fileFilter = file -> file.isDirectory();
			for (File dir : tempFolder.listFiles(fileFilter)) {
				if (DATA_DIRECTORY_NAME.equals(dir.getName())) {
					return dir;
				}
			}
		}
		return null;
	}

	protected ImportDataIterator getXMLFileImportDataIterator(Reader reader, IOServices ioServices, String name) {
		return new XMLFileImportDataIterator(reader, ioServices);
	}

	protected String getFileReaderStreamName() {
		return FILE_READER_STREAM_NAME;
	}

	protected String getTempFolderStreamName() {
		return TEMP_FOLDER_STREAM_NAME;
	}

}
