package com.constellio.app.utils;

import com.constellio.app.entities.modules.ProgressInfo;

public interface ManualUpdateHandlerView {
	void showRestartRequiredPanel();

	void showErrorMessage(String errorMessage);

	void closeProgressPopup();

	ProgressInfo openProgressPopup();
}
