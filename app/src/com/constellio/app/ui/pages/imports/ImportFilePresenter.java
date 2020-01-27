package com.constellio.app.ui.pages.imports;

import com.constellio.app.services.schemas.bulkImport.BulkImportParams;
import com.constellio.app.services.schemas.bulkImport.BulkImportProgressionListener;
import com.constellio.app.services.schemas.bulkImport.BulkImportResults;
import com.constellio.app.services.schemas.bulkImport.ImportError;
import com.constellio.app.services.schemas.bulkImport.LoggerBulkImportProgressionListener;
import com.constellio.app.services.schemas.bulkImport.RecordsImportServices;
import com.constellio.app.services.schemas.bulkImport.data.ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.ImportServices;
import com.constellio.app.services.schemas.bulkImport.data.excel.Excel2003ImportDataProvider;
import com.constellio.app.services.schemas.bulkImport.data.xml.XMLImportDataProvider;
import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.ImportAudit;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDateTime;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;
import static java.util.Arrays.asList;

public class ImportFilePresenter extends BasePresenter<ImportFileView> implements ImportFilePresenterInterface {

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
	public boolean isLegacyIdIndexDisabledWarningVisible() {
		return !modelLayerFactory.getSystemConfigs().isLegacyIdentifierIndexedInMemory();
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
			ImportAudit importAudit = newImportAudit();
			try {
				User currentUser = getCurrentUser();
				BulkImportProgressionListener progressionListener = new LoggerBulkImportProgressionListener();

				ImportDataProvider importDataProvider = null;
				if (upload.getFileName().endsWith(".xls")) {
					importDataProvider = getExcelImportDataProviderFromFile(file);

				} else if (upload.getFileName().endsWith(".zip")) {
					importDataProvider = getXMLImportDataProviderForZipFile(modelLayerFactory, file);
				} else if (upload.getFileName().endsWith(".xml")) {
					importDataProvider = getXMLImportDataProviderForSingleXMLFile(modelLayerFactory, file, upload.getFileName());
				} else {
					view.showErrorMessage("Only xml, zip or xls formats are accepted");
				}

				if (importDataProvider != null) {

					BulkImportParams params;
					if (view.getImportFileMode() == ImportFileMode.PERMISSIVE) {
						params = BulkImportParams.PERMISSIVE();
					} else {
						params = BulkImportParams.STRICT();
					}
					params.setAllowingReferencesToNonExistingUsers(view.isAllowingReferencesToNonExistingUsers());
					//					params = params.setThreads(1);

					BulkImportResults errors = importServices
							.bulkImport(importDataProvider, progressionListener, currentUser, view.getSelectedCollections(), params);
					List<String> formattedErrors = new ArrayList<>();
					for (ImportError error : errors.getImportErrors()) {
						formattedErrors.add(format(error));
						view.showErrorMessage(format(error));
					}
					completeImportationAudit(importAudit, formattedErrors);

					view.showImportCompleteMessage();
				}

			} catch (ValidationException e) {
				String formattedError = i18n.$(e.getValidationErrors());
				view.showErrorMessage(formattedError);
				completeImportationAudit(importAudit, asList(formattedError));
			} catch (Exception e) {
				e.printStackTrace();

				StringWriter writer = new StringWriter();
				PrintWriter pWriter = new PrintWriter(writer);
				e.printStackTrace(pWriter);
				String formattedError = writer.toString();
				view.showErrorMessage(formattedError);
				completeImportationAudit(importAudit, asList(formattedError));
			} finally {
				FileUtils.deleteQuietly(file);
			}
		}
	}

	private ImportAudit newImportAudit() {
		ImportAudit importAudit = coreSchemas().newImportAudit();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		importAudit.setCreatedOn(LocalDateTime.now());
		importAudit.setCreatedBy(getCurrentUser().getId());
		try {
			recordServices.add(importAudit);
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
		return importAudit;
	}

	private void completeImportationAudit(ImportAudit importAudit, List<String> errors) {
		StringBuilder formattedError = new StringBuilder();
		for (String error : errors) {
			formattedError.append(error);
		}

		if (!errors.isEmpty()) {
			importAudit.setErrors(formattedError.toString().replaceAll("<br/>", ""));
		}

		importAudit.setEndDate(LocalDateTime.now());
		try {
			modelLayerFactory.newRecordServices().update(importAudit);
		} catch (RecordServicesException e) {
			e.printStackTrace();
		}
	}

	protected ImportDataProvider getXMLImportDataProviderForSingleXMLFile(ModelLayerFactory modelLayerFactory,
																		  File file,
																		  String fileName) {
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
		view.navigate().to().adminModule();
	}

}
