package com.constellio.app.modules.rm.ui.components.document;

import java.io.Serializable;

import com.constellio.app.modules.rm.ui.entities.DocumentVO;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI.Navigation;
import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.framework.components.ComponentState;
import com.constellio.app.ui.pages.base.SessionContext;

public interface DocumentActionsComponent extends Serializable {

	@Deprecated
	CoreViews navigateTo();

	Navigation navigate();

	void showMessage(String message);

	void showErrorMessage(String errorMessage);

	SessionContext getSessionContext();

	ConstellioFactories getConstellioFactories();

	void setDocumentVO(DocumentVO documentVO);

	void openUploadWindow(boolean checkingIn);

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

	void setFinalizeButtonVisible(boolean visible);

	void setBorrowedMessage(String borrowedMessageKey, String... args);

	void openAgentURL(String agentURL);
}
