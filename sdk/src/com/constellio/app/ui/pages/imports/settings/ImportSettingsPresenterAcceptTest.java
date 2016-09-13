package com.constellio.app.ui.pages.imports.settings;

import static com.constellio.app.ui.i18n.i18n.$;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.extensions.AppLayerExtensions;
import com.constellio.app.extensions.AppLayerSystemExtensions;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.importExport.settings.SettingsImportServices;
import com.constellio.app.services.importExport.settings.model.ImportedSettings;
import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.imports.ImportFileView;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.frameworks.validation.ValidationException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.sdk.tests.ConstellioTest;

public class ImportSettingsPresenterAcceptTest extends ConstellioTest {
	
	private static final String VALID_FILENAME = "test.xml";

	private static final String INVALID_FILENAME = "test-invalid.xml";
	
	private static final String NOT_XML_FILENAME = "test.txt";

	private static final String EXCEPTION_FILENAME = "test-runtime.xml.txt";
	
	private IOServices ioServices;
	
	private File tempFolder;
	
	@Mock
	private ImportFileView view;
	
	@Mock
	SessionContext sessionContext;
	
	@Mock
	private ConstellioFactories constellioFactories;
	
	@Mock
	private AppLayerFactory appLayerFactory;
	
	@Mock
	private ModelLayerFactory modelLayerFactory;
	
	@Mock
	private AppLayerExtensions extensions;

	@Mock
	private AppLayerCollectionExtensions appCollectionExtentions;

	@Mock
	private AppLayerSystemExtensions appSystemExtentions;
	
	@Mock
	private SettingsImportServices settingsImportServices;

	@Before
	public void setUp() {
		ioServices = getIOLayerFactory().newIOServices();
		tempFolder = ioServices.newTemporaryFolder("ImportConfigsPresenterAcceptTest");
		
		when(view.getSessionContext()).thenReturn(sessionContext);
		when(view.getConstellioFactories()).thenReturn(constellioFactories);
		when(constellioFactories.getAppLayerFactory()).thenReturn(appLayerFactory);
		when(constellioFactories.getModelLayerFactory()).thenReturn(modelLayerFactory);
		
		when(appLayerFactory.getExtensions()).thenReturn(extensions);
//		when(extensions.forCollection(collection))
	}

	@After
	public void tearDown() {
		tempFolder.delete();
	}
	
	private File copyToTempDirAsXML(File testFile) {
		try {
			File tempFile = new File(tempFolder, testFile.getName() + ".xml");
			FileUtils.copyFile(testFile, tempFile);
			return tempFile;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private File copyToTempDir(File testFile) {
		try {
			File tempFile = new File(tempFolder, testFile.getName());
			FileUtils.copyFile(testFile, tempFile);
			return tempFile;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void givenValidFileWhenUploadButtonClickedThenShowMessage() {
		File testFile = copyToTempDir(getTestResourceFile(VALID_FILENAME)); 
		TempFileUpload upload = new TempFileUpload(testFile.getName(), "text/xml", testFile.length(), testFile);
		
		ImportSettingsPresenter presenter = new ImportSettingsPresenter(view);
		presenter.setSettingsImportServices(settingsImportServices);
		presenter.uploadButtonClicked(upload);
		verify(view).showImportCompleteMessage();
	}
	
	@Test
	public void givenNonXMLFileWhenUploadButtonClickedThenShowErrorMessage() {
		File testFile = copyToTempDir(getTestResourceFile(NOT_XML_FILENAME)); 
		TempFileUpload upload = new TempFileUpload(testFile.getName(), "text/xml", testFile.length(), testFile);
		
		ImportSettingsPresenter presenter = new ImportSettingsPresenter(view);
		presenter.setSettingsImportServices(settingsImportServices);
		presenter.uploadButtonClicked(upload);
		verify(view).showErrorMessage($("ImportConfigsView.OnlyXmlAccepted"));
	}

	@Test
	public void givenInvalidFileWhenUploadButtonClickedThenShowErrorMessage() {
		try {
			doThrow(new ValidationException(new ValidationErrors())).when(settingsImportServices).importSettings(any(ImportedSettings.class));
		} catch (ValidationException e) {
			throw new RuntimeException(e);
		}
		
		File testFile = copyToTempDir(getTestResourceFile(INVALID_FILENAME)); 
		TempFileUpload upload = new TempFileUpload(testFile.getName(), "text/xml", testFile.length(), testFile);
		
		ImportSettingsPresenter presenter = new ImportSettingsPresenter(view);
		presenter.setSettingsImportServices(settingsImportServices);
		presenter.uploadButtonClicked(upload);
		verify(view).showErrorMessage(any(String.class));
	}

	@Test
	public void givenRuntimeExceptionWhenUploadButtonClickedThenShowErrorMessage() {
		try {
			doThrow(new RuntimeException()).when(settingsImportServices).importSettings(any(ImportedSettings.class));
		} catch (ValidationException e) {
			throw new RuntimeException(e);
		}
		
		File testFile = copyToTempDirAsXML(getTestResourceFile(EXCEPTION_FILENAME)); 
		TempFileUpload upload = new TempFileUpload(testFile.getName(), "text/xml", testFile.length(), testFile);
		
		ImportSettingsPresenter presenter = new ImportSettingsPresenter(view);
		presenter.setSettingsImportServices(settingsImportServices);
		presenter.uploadButtonClicked(upload);
		verify(view).showErrorMessage(any(String.class));
	}
}
