package com.constellio.app.ui.pages.base;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.Navigation;

import java.io.Serializable;
import java.util.List;

public interface BaseView extends ViewComponent {

	String getCollection();

	//@Deprecated
	//CoreViews navigateTo();

	@Override
	void showMessage(String message);

	void showClickableMessage(String message);

	@Override
	void showErrorMessage(String errorMessage);

	SessionContext getSessionContext();

	ConstellioFactories getConstellioFactories();

	void addViewEnterListener(ViewEnterListener listener);

	List<ViewEnterListener> getViewEnterListeners();

	@Override
	Navigation navigate();

	void updateUI();

	void invalidate();
	boolean isInWindow();
	void closeAllWindows();

	MainLayout getMainLayout();

	void removeViewEnterListener(ViewEnterListener listener);
	
	void runAsync(Runnable runnable);
	
	void runAsync(Runnable runnable, int pollIntervall);

	void openURL(String url);

	void refreshActionMenu();

	public interface ViewEnterListener extends Serializable {

		void viewEntered(String params);

		void afterInit(String parameters);

		/**
		 * @param e Exception being thrown
		 * @return True if listener handled exception
		 */
		boolean exception(Exception e);

	}
}
