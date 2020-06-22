package com.constellio.app.ui.pages.imports;

import com.constellio.app.services.schemas.bulkImport.UserImportServices;
import com.constellio.app.services.schemas.bulkImport.data.ImportServices;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.services.factories.ModelLayerFactory;

import java.io.File;
import java.util.List;

public class ImportUsersFilePresenter extends ImportFilePresenter {
	public ImportUsersFilePresenter(ImportFileView view) {
		super(view);
		FoldersLocator foldersLocator = new FoldersLocator();
		File resourcesFolder = foldersLocator.getResourcesFolder();
		File exampleExcelFile = new File(resourcesFolder, "UserImportServices-user.xml");
		view.setExampleFile(exampleExcelFile);
	}

	@Override
	public boolean isLegacyIdIndexDisabledWarningVisible() {
		return false;
	}

	@Override
	protected ImportServices newImportServices(ModelLayerFactory modelLayerFactory) {
		return new UserImportServices(modelLayerFactory);
	}

	public List<String> getAllCollections() {
		return appLayerFactory.getCollectionsManager().getCollectionCodesExcludingSystem();
	}
}
