package com.constellio.app.modules.rm.ui.components.userDocument;

import com.constellio.app.ui.entities.UserDocumentVO;

public interface DeclareUserDocumentHandler {

	boolean isSupporting(String filename);

	String getModalWidth();

	String getModalHeight();

	void onDeclareClicked(UserDocumentVO userDocumentVO);
}
