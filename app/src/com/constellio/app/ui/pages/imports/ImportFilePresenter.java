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

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.io.FileUtils;

import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.bulkImport.BulkImportProgressionListener;
import com.constellio.model.services.records.bulkImport.BulkImportResults;
import com.constellio.model.services.records.bulkImport.ImportError;
import com.constellio.model.services.records.bulkImport.LoggerBulkImportProgressionListener;
import com.constellio.model.services.records.bulkImport.RecordsImportServices;
import com.constellio.model.services.records.bulkImport.data.ImportDataProvider;
import com.constellio.model.services.records.bulkImport.data.ImportServices;
import com.constellio.model.services.records.bulkImport.data.excel.ExcelImportDataProvider;
import com.constellio.model.services.records.bulkImport.data.xml.XMLImportDataProvider;

public class ImportFilePresenter extends BasePresenter<ImportFileView> {

	private transient ImportServices importServices;

	public ImportFilePresenter(ImportFileView view) {
		super(view);
		initTransient();

		FoldersLocator foldersLocator = new FoldersLocator();
		File resourcesFolder = foldersLocator.getModuleResourcesFolder("rm");
		File exampleExcelFile = new File(resourcesFolder, "Fichier test.xls");
		view.setExampleFile(exampleExcelFile);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_SYSTEM_DATA_IMPORTS).globally();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransient();
	}

	private void initTransient() {
		importServices = newImportServices(modelLayerFactory);
	}

	protected ImportServices newImportServices(ModelLayerFactory modelLayerFactory) {
		return new RecordsImportServices(modelLayerFactory);
	}

	public void uploadButtonClicked(TempFileUpload upload) {
		if (upload != null && upload.getTempFile() != null) {
			File file = upload.getTempFile();
			try {
				User currentUser = getCurrentUser();
				BulkImportProgressionListener progressionListener = new LoggerBulkImportProgressionListener();
				//				BulkImportProgressionListener progressionListener = new BulkImportProgressionListener() {
				//					@Override
				//					public void updateTotal(int newTotal) {
				//						view.setTotal(newTotal);
				//					}
				//
				//					@Override
				//					public void updateProgression(int stepProgression, int totalProgression) {
				//						view.setProgress(totalProgression);
				//					}
				//
				//					@Override
				//					public void updateCurrentStepTotal(int newTotal) {
				//
				//					}
				//
				//					@Override
				//					public void updateCurrentStepName(String stepName) {
				//
				//					}
				//
				//				};

				ImportDataProvider importDataProvider = null;
				if (upload.getFileName().endsWith(".xls")) {
					importDataProvider = ExcelImportDataProvider.fromFile(file);

				} else if (upload.getFileName().endsWith(".zip")) {
					importDataProvider = XMLImportDataProvider.forZipFile(modelLayerFactory, file);
				} else if (upload.getFileName().endsWith(".xml")) {
					importDataProvider = XMLImportDataProvider.forSingleXMLFile(modelLayerFactory, file);
				} else {
					view.showErrorMessage("Only xml, zip or xls formats are accepted");
				}
				if (importDataProvider != null) {
					BulkImportResults errors = importServices
							.bulkImport(importDataProvider, progressionListener, currentUser, view.getSelectedCollections());
					for (ImportError error : errors.getImportErrors()) {
						view.showErrorMessage(format(error));
					}
					view.showImportCompleteMessage();
				}
			} catch (Exception e) {
				e.printStackTrace();

				StringWriter writer = new StringWriter();
				PrintWriter pWriter = new PrintWriter(writer);
				e.printStackTrace(pWriter);
				view.showErrorMessage(writer.toString());

			} finally {
				FileUtils.deleteQuietly(file);
			}
		}
	}

	private String format(ImportError error) {
		return $("ImportUsersFileViewImpl.errorWithUser") + " " + error.getInvalidElementId() + " : " + error.getErrorMessage();
	}

	public void backButtonClicked() {
		view.navigateTo().adminModule();
	}

}
