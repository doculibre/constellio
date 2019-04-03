package com.constellio.app.modules.rm.ui.components.document.newFile;

import java.io.Serializable;

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

	boolean isOpened();

	SessionContext getSessionContext();

	ConstellioFactories getConstellioFactories();


	void addNewFileCreatedListener(NewFileCreatedListener listener);

	void removeNewFileCreatedListener(NewFileCreatedListener listener);

	void notifyNewFileCreated(Content content, String documentTypeId);

	void setDocumentTypeId(String documentTypeId);

	String getDocumentTypeId();

	public static interface NewFileCreatedListener extends Serializable {
		void newFileCreated(Content content, String documentTypeId);
	}

}
