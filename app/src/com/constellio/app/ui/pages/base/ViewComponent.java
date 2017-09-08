package com.constellio.app.ui.pages.base;

import java.io.Serializable;

import com.constellio.app.ui.application.Navigation;

public interface ViewComponent extends Serializable, SessionContextProvider, UIContextProvider {

	Navigation navigate();

	void showMessage(String message);

	void showErrorMessage(String errorMessage);

}
