package com.constellio.app.ui.pages.setup;

import java.util.List;
import java.util.Locale;

import com.constellio.app.ui.pages.base.BaseView;

public interface ConstellioSetupView extends BaseView {
	
	void setLocaleCodes(List<String> localeCodes);
	
	void setModuleIds(List<String> moduleIds);
	
	void setLocale(Locale locale);
	
	void reloadForm();

}
