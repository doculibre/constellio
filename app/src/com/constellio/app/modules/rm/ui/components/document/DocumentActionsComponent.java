package com.constellio.app.modules.rm.ui.components.document;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.application.Navigation;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.base.ViewComponent;

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

	void setDisplayDocumentButtonState(ComponentState state);

	void setOpenDocumentButtonState(ComponentState state);

	void setDownloadDocumentButtonState(ComponentState state);

	void setCopyDocumentButtonState(ComponentState state);

	void setStartWorkflowButtonState(ComponentState state);

	void setEditDocumentButtonState(ComponentState state);

	void setAddDocumentButtonState(ComponentState state);

	void setDeleteDocumentButtonState(ComponentState state);

	void setAddAuthorizationButtonState(ComponentState state);

	void setCreatePDFAButtonState(ComponentState state);

	void setShareDocumentButtonState(ComponentState state);

	void setUploadButtonState(ComponentState state);

	void setCheckInButtonState(ComponentState state);

	void setAlertWhenAvailableButtonState(ComponentState state);

	void setCheckOutButtonState(ComponentState state);

	void setCartButtonState(ComponentState state);

	void setAddToOrRemoveFromSelectionButtonState(ComponentState state);

	void setGenerateMetadataButtonState(ComponentState state);

	void setPublishButtonState(ComponentState state);

	void setFinalizeButtonState(ComponentState state);

	void setBorrowedMessage(String borrowedMessageKey, String... args);

	void openAgentURL(String agentURL);

	void refreshParent();
}
