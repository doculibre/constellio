package com.constellio.app.ui.pages.imports.authorization;

import com.constellio.app.services.schemas.bulkImport.BulkImportResults;
import com.constellio.app.services.schemas.bulkImport.ImportError;
import com.constellio.app.services.schemas.bulkImport.authorization.AuthorizationImportServices;
import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.imports.ImportFilePresenterInterface;
import com.constellio.app.ui.pages.imports.ImportFileView;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import static com.constellio.app.ui.i18n.i18n.$;

public class ImportAuthorizationsFilePresenter extends BasePresenter<ImportFileView> implements ImportFilePresenterInterface {

	public ImportAuthorizationsFilePresenter(ImportFileView view) {
		super(view);
		FoldersLocator foldersLocator = new FoldersLocator();
		File resourcesFolder = foldersLocator.getResourcesFolder();
		File exampleExcelFile = new File(resourcesFolder, "AuthorizationsImport.xml");
		view.setExampleFile(exampleExcelFile);
	}

	@Override
	public boolean isLegacyIdIndexDisabledWarningVisible() {
		return !modelLayerFactory.getSystemConfigs().isLegacyIdentifierIndexedInMemory();
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_SYSTEM_DATA_IMPORTS).globally();
	}

	@Override
	public void uploadButtonClicked(TempFileUpload upload) {
		if (upload != null && upload.getTempFile() != null) {
			File file = upload.getTempFile();
			try {
				if (upload.getFileName().endsWith(".xml")) {
					AuthorizationImportServices importServices = new AuthorizationImportServices();
					BulkImportResults errors = importServices
							.bulkImport(file, getCurrentUser().getCollection(), modelLayerFactory);
					for (ImportError error : errors.getImportErrors()) {
						view.showErrorMessage(
								$("ImportAuthorizationsFileViewImpl.errorWith") + " " + error.getInvalidElementId() + " : "
								+ error
										.getErrorMessage());
					}
					view.showImportCompleteMessage();
				} else {
					view.showErrorMessage($("ImportAuthorizationsFileViewImpl.OnlyXmlAccepted"));
				}

			} catch (Exception e) {
				StringWriter writer = new StringWriter();
				PrintWriter pWriter = new PrintWriter(writer);
				e.printStackTrace(pWriter);
				view.showErrorMessage(writer.toString());

			} finally {
				FileUtils.deleteQuietly(file);
			}
		}
	}

	@Override
	public void backButtonClicked() {
		view.navigate().to().adminModule();
	}
}
