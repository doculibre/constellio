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
package com.constellio.app.ui.pages.imports;

import java.io.File;

import com.constellio.app.services.schemas.bulkImport.SchemaTypeImportServices;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.ImportServices;
import com.constellio.app.services.schemas.bulkImport.data.xml.XMLImportSchemaTypesDataProvider;
import com.constellio.model.services.factories.ModelLayerFactory;

public class ImportSchemaTypesFilePresenter extends ImportFilePresenter {

	public ImportSchemaTypesFilePresenter(ImportFileView view) {
		super(view);
	}

	@Override
	protected ImportServices newImportServices(ModelLayerFactory modelLayerFactory) {
		return new SchemaTypeImportServices(appLayerFactory, view.getCollection());
	}

	@Override
	protected ImportDataProvider getXMLImportDataProviderForSingleXMLFile(ModelLayerFactory modelLayerFactory, File file,
			String fileName) {
		return XMLImportSchemaTypesDataProvider.forSingleXMLFile(modelLayerFactory, file, fileName);
	}

	@Override
	protected ImportDataProvider getXMLImportDataProviderForZipFile(ModelLayerFactory modelLayerFactory, File file) {
		return XMLImportSchemaTypesDataProvider.forZipFile(modelLayerFactory, file);
	}

	@Override
	protected ImportDataProvider getExcelImportDataProviderFromFile(File file) {
		return null;
	}
}
