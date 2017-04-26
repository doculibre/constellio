package com.constellio.app.modules.rm.ui.components.document.fields;

import com.constellio.app.modules.rm.ui.components.document.newFile.NewFileWindow;
import com.constellio.app.ui.entities.ContentVersionVO;

import java.io.Serializable;

public interface DocumentContentField extends CustomDocumentField<ContentVersionVO> {

	void setNewFileButtonVisible(boolean visible);

	void addNewFileClickListener(NewFileClickListener listener);

	void addNewFileClickListenerIfEmpty(NewFileClickListener listener);

	void removeNewFileClickListener(NewFileClickListener listener);

	void addContentUploadedListener(ContentUploadedListener listener);

	void removeContentUploadedListener(ContentUploadedListener listener);

	NewFileWindow getNewFileWindow();

	boolean isMajorVersionFieldVisible();

	void setMajorVersionFieldVisible(boolean visible);

	static interface ContentUploadedListener extends Serializable {

		void newContentUploaded();

	}

	static interface NewFileClickListener extends Serializable {

		void newFileClicked();

	}

}
