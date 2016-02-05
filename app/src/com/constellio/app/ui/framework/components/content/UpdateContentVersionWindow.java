package com.constellio.app.ui.framework.components.content;

import java.io.Serializable;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.pages.base.SessionContext;

public interface UpdateContentVersionWindow extends Serializable {
	
	boolean isFormVisible();
	
	void setFormVisible(boolean visible);
	
	void showErrorMessage(String key, Object...args);
	
	void close();
	
	SessionContext getSessionContext();
	
	ConstellioFactories getConstellioFactories();

	void addMajorMinorSameOptions();

	void addMajorMinorOptions();

	void setUploadFieldVisible(boolean visible);
	
}
