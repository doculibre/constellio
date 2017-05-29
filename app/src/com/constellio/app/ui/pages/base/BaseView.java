package com.constellio.app.ui.pages.base;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.Navigation;

import java.io.Serializable;
import java.util.List;

public interface BaseView extends Serializable, SessionContextProvider, UIContextProvider {

	String getCollection();

	//@Deprecated
	//CoreViews navigateTo();

	void showMessage(String message);

	void showClickableMessage(String message);

	void showErrorMessage(String errorMessage);

	SessionContext getSessionContext();

	ConstellioFactories getConstellioFactories();

	void addViewEnterListener(ViewEnterListener listener);

	List<ViewEnterListener> getViewEnterListeners();

	Navigation navigate();

	void updateUI();

	void invalidate();

	void removeViewEnterListener(ViewEnterListener listener);

	public interface ViewEnterListener extends Serializable {

		void viewEntered(String params);

		void afterInit(String parameters);
	}
}
