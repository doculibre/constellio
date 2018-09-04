package com.constellio.app.modules.rm.ui.components.document.newFile;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Content;

import java.io.Serializable;
import java.util.List;

public interface NewFileWindow extends Serializable {

	String getFileName();

	String getExtension();

	Content getTemplate();

	void showErrorMessage(String key, Object... args);

	void open();

	void close();

	boolean isOpened();

	SessionContext getSessionContext();

	ConstellioFactories getConstellioFactories();

	void setSupportedExtensions(List<String> extensions);

	void setTemplateOptions(List<Content> templates);

	void setDocumentTypeId(String documentTypeId);

	void addNewFileCreatedListener(NewFileCreatedListener listener);

	void removeNewFileCreatedListener(NewFileCreatedListener listener);

	void notifyNewFileCreated(Content content, String documentTypeId);

	void setExtensionFieldValue(String value);

	void setTemplateFieldValue(String value);

	public static interface NewFileCreatedListener extends Serializable {
		void newFileCreated(Content content, String documentTypeId);
	}

}
