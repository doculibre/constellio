package com.constellio.app.modules.rm.ui.components.document.newFile;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.ui.util.NewFileUtils;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.contents.icap.IcapException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.app.ui.i18n.i18n.$;

public class NewFilePresenter implements Serializable {

	private NewFileWindow window;
	private String documentTypeId;
	private String filename;
	private transient Content fileContent;
	private transient ContentManager contentManager;
	private transient UserServices userServices;

	public NewFilePresenter(NewFileWindow window) {
		this.window = window;

		List<String> supportedExtensions = NewFileUtils.getSupportedExtensions();
		window.setSupportedExtensions(supportedExtensions);

		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		ModelLayerFactory modelLayerFactory = window.getConstellioFactories().getModelLayerFactory();
		contentManager = modelLayerFactory.getContentManager();
		userServices = modelLayerFactory.newUserServices();
	}

	public void newFileNameSubmitted() {
		filename = window.getFileName();
		Content templateContent = window.getTemplate();
		String extension = window.getExtension();
		if (StringUtils.isNotBlank(extension)) {
			if (StringUtils.isNotBlank(filename) && !filename.endsWith("." + extension)) {
				filename += "." + extension;
			}
		} else {
			if (StringUtils.isNotBlank(filename)) {
				String fileExtension = StringUtils.lowerCase(FilenameUtils.getExtension(templateContent.getCurrentVersion().getFilename()));
				filename += "." + fileExtension;
			}
		}
		if ((StringUtils.isNotBlank(extension) && isFilenameValid(filename)) || (templateContent != null && StringUtils
				.isNotBlank(filename))) {
			if (templateContent != null) {
				String collection = window.getSessionContext().getCurrentCollection();
				String username = window.getSessionContext().getCurrentUser().getUsername();
				User user = userServices.getUserRecordInCollection(username, collection);
				InputStream inputStream = contentManager
						.getContentInputStream(templateContent.getCurrentVersion().getHash(), "newFilePresenterInputStream");
				try {
					ContentManager.ContentVersionDataSummaryResponse uploadResponse = contentManager.upload(inputStream, filename);
					ContentVersionDataSummary dataSummary = uploadResponse.getContentVersionDataSummary();
					if(uploadResponse.hasFoundDuplicate()) {
						window.showErrorMessage($("ContentManager.hasFoundDuplicate"));
					}
					fileContent = contentManager.createMinor(user, filename, dataSummary);
				} catch (final IcapException e) {
                    final String message;
                    if (e instanceof IcapException.ThreatFoundException) {
                        message = $(e.getMessage(), e.getFileName(), ((IcapException.ThreatFoundException) e).getThreatName());
                    } else {
                        message = $(e.getMessage(), e.getFileName());
                    }

					window.showErrorMessage(message);
				} finally {
					IOUtils.closeQuietly(inputStream);
				}
			} else {
				fileContent = createNewFile(filename);
			}
			window.notifyNewFileCreated(fileContent);
		} else {
			window.showErrorMessage("NewFileWindow.invalidFileName", filename != null ? filename : "");
		}
	}

	private boolean isFilenameValid(String fileName) {
		List<String> supportedExtensions = NewFileUtils.getSupportedExtensions();
		String extension = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));
		return supportedExtensions.contains(extension);
	}

	private Content createNewFile(String fileName) {
		String extension = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));

		String collection = window.getSessionContext().getCurrentCollection();
		String username = window.getSessionContext().getCurrentUser().getUsername();
		User user = userServices.getUserRecordInCollection(username, collection);

		InputStream newFileInput = NewFileUtils.newFile(extension);
		try {
			ContentManager.ContentVersionDataSummaryResponse uploadResponse = contentManager.upload(newFileInput, fileName);
			ContentVersionDataSummary dataSummary = uploadResponse.getContentVersionDataSummary();
			if(uploadResponse.hasFoundDuplicate()) {
				window.showErrorMessage($("ContentManager.hasFoundDuplicate"));
			}
			return contentManager.createMinor(user, fileName, dataSummary);
		} finally {
			IOUtils.closeQuietly(newFileInput);
		}
	}

	public void setTemplatesByDocumentTypeId(String documentTypeId) {
		this.documentTypeId = documentTypeId;
		window.setTemplates(getTemplates());
	}

	private List<Content> getTemplates() {
		List<Content> templates = new ArrayList<>();
		if (documentTypeId != null) {
			AppLayerFactory appLayerFactory = window.getConstellioFactories().getAppLayerFactory();
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(window.getSessionContext().getCurrentCollection(),
					appLayerFactory);
			templates = rm.getDocumentType(documentTypeId).getTemplates();
		}
		return templates;
	}

	public String getDocumentTypeId() {
		return documentTypeId;
	}

	public String getFilename() {
		return filename;
	}

	public Content getFileContent() {
		return fileContent;
	}
}
