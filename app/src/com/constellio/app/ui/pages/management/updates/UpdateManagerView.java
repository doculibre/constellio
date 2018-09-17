package com.constellio.app.ui.pages.management.updates;

import com.constellio.app.api.extensions.UpdateModeExtension.UpdateModeHandler;
import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.ui.pages.base.BaseView;

public interface UpdateManagerView extends BaseView {
	void showStandardUpdatePanel();

	void showLicenseUploadPanel();

	void showAlternateUpdatePanel(UpdateModeHandler handler);

	void showRestartRequiredPanel();

	ProgressInfo openProgressPopup();

	void closeProgressPopup();

}
