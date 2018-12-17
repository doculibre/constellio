package com.constellio.app.ui.pages.setup;

import com.constellio.app.ui.pages.base.BaseView;

import java.util.List;
import java.util.Locale;

public interface ConstellioSetupView extends BaseView {

	void setLocaleCodes(List<String> localeCodes);

	void setModuleIds(List<String> moduleIds);

	void setLocale(Locale locale);

	void reloadForm();

	void navigateToMonitoring();

	void setSubmitButtonEnabled(boolean enabled);
}
