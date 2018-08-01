package com.constellio.app.ui.pages.base;

import com.constellio.app.ui.application.Navigation;

import java.io.Serializable;

public interface ViewComponent extends Serializable, SessionContextProvider, UIContextProvider {

	Navigation navigate();

	void showMessage(String message);

	void showErrorMessage(String errorMessage);

}
