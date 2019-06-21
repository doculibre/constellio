package com.constellio.app.ui.pages.login;

import com.constellio.app.ui.application.CoreViews;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;

public interface LoginView extends BaseView {

	CoreViews navigateTo();

	void updateUIContent();

	SessionContext getSessionContext();

	void showUserHasNoCollectionMessage();

	void showBadLoginMessage();

	void setUsername(String username);

	String getUsernameCookieValue();

	void setUsernameCookie(String username);

	void popPrivacyPolicyWindow(final ModelLayerFactory modelLayerFactory, final User userInLastCollection,
								final String lastCollection);

	void popLastAlertWindow(final ModelLayerFactory modelLayerFactory, final User userInLastCollection,
								final String lastCollection);

}
