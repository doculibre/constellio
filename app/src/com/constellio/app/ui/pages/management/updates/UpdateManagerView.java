package com.constellio.app.ui.pages.management.updates;

import com.constellio.app.api.extensions.UpdateModeExtension.UpdateModeHandler;
import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.utils.ManualUpdateHandlerView;

public interface UpdateManagerView extends BaseView, ManualUpdateHandlerView {
	void showStandardUpdatePanel();

	void showLicenseUploadPanel();

	void showAlternateUpdatePanel(UpdateModeHandler handler);

	void showRestartRequiredPanel();

	void showLastAlertDownloadPanel();

	ProgressInfo openProgressPopup();

	void closeProgressPopup();

}
