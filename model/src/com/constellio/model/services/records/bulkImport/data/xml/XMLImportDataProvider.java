/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.records.bulkImport.data.xml;

import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.services.zip.ZipServiceException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.bulkImport.data.ImportDataIterator;
import com.constellio.model.services.records.bulkImport.data.ImportDataProvider;

public class XMLImportDataProvider implements ImportDataProvider {

	private static final String TEMP_FOLDER_STREAM_NAME = "XMLImportDataProvider-TempFolder";

	private static final String FILE_READER_STREAM_NAME = "XMLImportDataProvider-FileReader";

	private IOServicesFactory ioServicesFactory;

	private File xmlFile;

	private File zipFile;

	private File tempFolder;

	public static XMLImportDataProvider forZipFile(ModelLayerFactory modelLayerFactory, File zipFile) {

		XMLImportDataProvider instance = new XMLImportDataProvider();
		instance.zipFile = zipFile;
		instance.ioServicesFactory = modelLayerFactory.getIOServicesFactory();
		return instance;
	}

	public static XMLImportDataProvider forSingleXMLFile(ModelLayerFactory modelLayerFactory, File xmlFile) {

		XMLImportDataProvider instance = new XMLImportDataProvider();
		instance.xmlFile = xmlFile;
		instance.ioServicesFactory = modelLayerFactory.getIOServicesFactory();
		return instance;
	}

	@Override
	public void initialize() {

		if (zipFile != null) {
			this.tempFolder = ioServicesFactory.newFileService().newTemporaryFolder(TEMP_FOLDER_STREAM_NAME);
			try {
				ioServicesFactory.newZipService().unzip(zipFile, tempFolder);
			} catch (ZipServiceException e) {
				throw new RuntimeException(e);
			}
		}

	}

	@Override
	public void close() {
		ioServicesFactory.newIOServices().deleteQuietly(tempFolder);
	}

	@Override
	public List<String> getAvailableSchemaTypes() {

		List<String> schemaTypes = new ArrayList<>();
		if (tempFolder != null) {
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
	public ImportDataIterator newDataIterator(String schemaType) {
		IOServices ioServices = ioServicesFactory.newIOServices();
		File file = xmlFile != null ? xmlFile : new File(tempFolder, schemaType + ".xml");
		Reader reader = ioServices.newBufferedFileReader(file, FILE_READER_STREAM_NAME);
		return new XMLFileImportDataIterator(reader, ioServices);
	}

}
