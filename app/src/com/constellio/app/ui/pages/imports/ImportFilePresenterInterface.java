package com.constellio.app.ui.pages.imports;

import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;

public interface ImportFilePresenterInterface {
	void uploadButtonClicked(TempFileUpload upload);

	void backButtonClicked();

	boolean isLegacyIdIndexDisabledWarningVisible();
	
}
