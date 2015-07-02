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
package com.constellio.app.services.schemas.bulkImport.data.xml;

import com.constellio.app.services.schemas.bulkImport.data.ImportDataIterator;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.io.File;
import java.io.Reader;

public class XMLImportSchemaTypesDataProvider extends XMLImportDataProvider implements ImportDataProvider {

	private static final String FILE_READER_STREAM_NAME = "XMLImportSchemaTypeDataProvider-TempFolder";
	private static final String TEMP_FOLDER_STREAM_NAME = "XMLImportSchemaTypeDataProvider-FileReader";

	public XMLImportSchemaTypesDataProvider(File xmlFile, IOServicesFactory ioServicesFactory, boolean isZip, String fileName) {
		super(xmlFile, ioServicesFactory, isZip, fileName);
	}

	public static XMLImportDataProvider forZipFile(ModelLayerFactory modelLayerFactory, File zipFile) {
		return new XMLImportSchemaTypesDataProvider(zipFile, modelLayerFactory.getIOServicesFactory(), true, "");
	}

	public static XMLImportDataProvider forSingleXMLFile(ModelLayerFactory modelLayerFactory, File xmlFile, String xmlFileName) {
		return new XMLImportSchemaTypesDataProvider(xmlFile, modelLayerFactory.getIOServicesFactory(), false, xmlFileName);
	}

	@Override
	protected ImportDataIterator getXMLFileImportDataIterator(Reader reader, IOServices ioServices, String fileName) {
		if(fileName.contains("valueDomain")){
			return new XMLFileImportTypesDataIterator(reader, ioServices);
		}else{
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

}
