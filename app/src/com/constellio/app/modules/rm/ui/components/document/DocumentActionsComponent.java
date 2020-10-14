package com.constellio.app.modules.rm.ui.components.document;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.ViewComponent;

@Deprecated
public interface DocumentActionsComponent extends ViewComponent {

	@Deprecated
	CoreViews navigateTo();

	@Override
	Navigation navigate();

	@Override
	void showMessage(String message);

	@Override
	void showErrorMessage(String errorMessage);

	SessionContext getSessionContext();

	ConstellioFactories getConstellioFactories();

	void setRecordVO(RecordVO documentVO);

	void openUploadWindow(boolean checkingIn);

	default void setDisplayDocumentButtonState(ComponentState state) {
	}

	default void setOpenDocumentButtonState(ComponentState state) {
	}

	default void setDownloadDocumentButtonState(ComponentState state) {
	}

	default void setEditDocumentButtonState(ComponentState state) {
	}

	void setBorrowedMessage(String borrowedMessageKey, String... args);

	void openAgentURL(String agentURL);

	void refreshParent();
}
