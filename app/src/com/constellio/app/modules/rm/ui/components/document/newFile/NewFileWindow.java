package com.constellio.app.modules.rm.ui.components.document.newFile;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.Content;

public interface NewFileWindow extends Serializable {

	String getFileName();

	String getExtension();

	Content getTemplate();

	void showErrorMessage(String key, Object... args);

	void open();

	void close();

	SessionContext getSessionContext();

	ConstellioFactories getConstellioFactories();

	void setSupportedExtensions(List<String> extensions);

	void setTemplates(List<Content> templates);

	void setDocumentTypeId(String documentTypeId);

	void addNewFileCreatedListener(NewFileCreatedListener listener);

	void removeNewFileCreatedListener(NewFileCreatedListener listener);

	void notifyNewFileCreated(Content content);

	public static interface NewFileCreatedListener extends Serializable {
		void newFileCreated(Content content);
	}

}
