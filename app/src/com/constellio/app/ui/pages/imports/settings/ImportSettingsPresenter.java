package com.constellio.app.ui.pages.imports.settings;

import com.constellio.app.services.importExport.settings.SettingsImportServices;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.app.services.importExport.settings.utils.SettingsXMLFileReader;
import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.imports.ImportFilePresenterInterface;
import com.constellio.app.ui.pages.imports.ImportFileView;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.frameworks.validation.ValidationRuntimeException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static com.constellio.app.ui.i18n.i18n.$;

public class ImportSettingsPresenter extends BasePresenter<ImportFileView> implements ImportFilePresenterInterface {

	private static final Logger LOGGER = LogManager.getLogger(ImportSettingsPresenter.class);

	private transient SettingsImportServices settingsImportServices;

	public ImportSettingsPresenter(ImportFileView view) {
		super(view);
		initTransientObjects();
	}

	@Override
	public boolean isLegacyIdIndexDisabledWarningVisible() {
		return false;
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		this.settingsImportServices = new SettingsImportServices(appLayerFactory);
	}

	public void setSettingsImportServices(SettingsImportServices settingsImportServices) {
		this.settingsImportServices = settingsImportServices;
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
					Document settingsDocument = getDocumentFromFile(file); //jdom document
					ImportedSettings settings = new SettingsXMLFileReader(settingsDocument, collection, modelLayerFactory).read();
					try {
						settingsImportServices.importSettings(settings);
						view.showImportCompleteMessage();
					} catch (ValidationException e) {
						view.showErrorMessage($(e.getValidationErrors()));
					} catch (ValidationRuntimeException e) {
						view.showErrorMessage($(e.getValidationErrors()));
					} catch (Throwable t) {
						LOGGER.error("Error while importing configurations", t);
						view.showErrorMessage(ExceptionUtils.getStackTrace(t));
					}
				} else {
					view.showErrorMessage($("ImportConfigsView.OnlyXmlAccepted"));
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

	private Document getDocumentFromFile(File file) {
		SAXBuilder builder = new SAXBuilder();
		try {
			return builder.build(file);
		} catch (JDOMException e) {
			throw new RuntimeException("JDOM2 Exception", e);
		} catch (IOException e) {
			throw new RuntimeException("build Document JDOM2 from file", e);
		}
	}

}
