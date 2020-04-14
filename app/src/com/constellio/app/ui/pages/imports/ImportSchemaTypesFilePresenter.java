package com.constellio.app.ui.pages.imports;

import com.constellio.app.services.schemas.bulkImport.SchemaTypeImportServices;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.ImportServices;
import com.constellio.app.services.schemas.bulkImport.data.xml.XMLImportSchemaTypesDataProvider;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.io.File;

public class ImportSchemaTypesFilePresenter extends ImportFilePresenter {

	public ImportSchemaTypesFilePresenter(ImportFileView view) {
		super(view);
	}

	@Override
	protected ImportServices newImportServices(ModelLayerFactory modelLayerFactory) {
		return new SchemaTypeImportServices(appLayerFactory, view.getCollection());
	}

	@Override
	public boolean isLegacyIdIndexDisabledWarningVisible() {
		return false;
	}

	@Override
	protected ImportDataProvider getXMLImportDataProviderForSingleXMLFile(ModelLayerFactory modelLayerFactory,
																		  File file,
																		  String fileName) {
		return XMLImportSchemaTypesDataProvider.forSingleXMLFile(modelLayerFactory, file, fileName);
	}

	@Override
	protected ImportDataProvider getXMLImportDataProviderForZipFile(ModelLayerFactory modelLayerFactory, File file) {
		return XMLImportSchemaTypesDataProvider.forZipFile(modelLayerFactory, file);
	}

	@Override
	protected ImportDataProvider getExcel2003ImportDataProviderFromFile(File file) {
		return null;
	}

	@Override
	protected ImportDataProvider getExcel2007ImportDataProviderFromFile(File file) {
		return null;
	}
}
