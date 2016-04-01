package com.constellio.app.ui.application;

import java.lang.reflect.InvocationTargetException;

import com.constellio.data.utils.ImpossibleRuntimeException;
import com.vaadin.navigator.Navigator;
import com.vaadin.ui.UI;

public class Navigation {
	public CoreViews to() {
		return to(CoreViews.class);
	}

	public <T extends CoreViews> T to(Class<T> navigatorClass) {
		try {
			return navigatorClass.getConstructor(Navigator.class).newInstance(UI.getCurrent().getNavigator());
		} catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
			throw new ImpossibleRuntimeException("The navigator does not provide a valid constructor");
		}
	}
}
