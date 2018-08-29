package com.constellio.app.ui.pages.base;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.CoreViews;

import java.io.Serializable;
import java.util.Locale;

public interface ConstellioMenu extends Serializable {

	CoreViews navigateTo();

	SessionContext getSessionContext();

	void updateUIContent();

	ConstellioFactories getConstellioFactories();

	void setLocale(Locale locale);

	void refreshBadges();

}
