package com.constellio.app.ui.pages.login;

import com.constellio.app.ui.application.ConstellioNavigator;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;

public interface LoginView extends BaseView {
	
	ConstellioNavigator navigateTo();

	void updateUIContent();

	SessionContext getSessionContext();

	void showUserHasNoCollectionMessage();

	void showBadLoginMessage();
}
