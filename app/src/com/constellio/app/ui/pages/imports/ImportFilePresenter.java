package com.constellio.app.ui.pages.imports;

import com.constellio.app.services.schemas.bulkImport.*;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.ImportServices;
import com.constellio.app.services.schemas.bulkImport.data.excel.Excel2003ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.xml.XMLImportDataProvider;
import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static com.constellio.app.ui.i18n.i18n.$;

public class ImportFilePresenter extends BasePresenter<ImportFileView> implements ImportFilePresenterInterface{

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
					importDataProvider = getExcelImportDataProviderFromFile(file);

				} else if (upload.getFileName().endsWith(".zip")) {
					importDataProvider = getXMLImportDataProviderForZipFile(modelLayerFactory, file);
				} else if(upload.getFileName().endsWith(".xml")){
					importDataProvider = getXMLImportDataProviderForSingleXMLFile(modelLayerFactory, file, upload.getFileName());
				}else{
					view.showErrorMessage("Only xml, zip or xls formats are accepted");
				}

				if(importDataProvider != null){
					BulkImportResults errors = importServices.bulkImport(importDataProvider, progressionListener, currentUser, view.getSelectedCollections());
					for(ImportError error :errors.getImportErrors()){
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

	protected ImportDataProvider getXMLImportDataProviderForSingleXMLFile(ModelLayerFactory modelLayerFactory, File file, String fileName) {
		return XMLImportDataProvider.forSingleXMLFile(modelLayerFactory, file, fileName);
	}

	protected ImportDataProvider getXMLImportDataProviderForZipFile(ModelLayerFactory modelLayerFactory, File file) {
		return XMLImportDataProvider.forZipFile(modelLayerFactory, file);
	}

	protected ImportDataProvider getExcelImportDataProviderFromFile(File file) {
		return Excel2003ImportDataProvider.fromFile(file);
	}

	protected String format(ImportError error) {
		return $("ImportUsersFileViewImpl.errorWith") + " " + error.getInvalidElementId() + " : " + error.getErrorMessage();
	}

	public void backButtonClicked() {
		view.navigateTo().adminModule();
	}

}
