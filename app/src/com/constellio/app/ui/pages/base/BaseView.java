package com.constellio.app.ui.pages.base;

import java.io.Serializable;
import java.util.List;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI.Navigation;
import com.constellio.app.ui.application.CoreViews;

public interface BaseView extends Serializable, SessionContextProvider {

	String getCollection();

	CoreViews navigateTo();

	void showMessage(String message);

	void showErrorMessage(String errorMessage);

	SessionContext getSessionContext();

	ConstellioFactories getConstellioFactories();

	void addViewEnterListener(ViewEnterListener listener);

	List<ViewEnterListener> getViewEnterListeners();

	<T extends CoreViews> T navigateTo(Class<T> navigatorClass);

	Navigation navigate();

	void updateUI();

	void invalidate();

	void removeViewEnterListener(ViewEnterListener listener);

	public interface ViewEnterListener extends Serializable {

		void viewEntered(String params);

		void afterInit(String parameters);

	}

}
