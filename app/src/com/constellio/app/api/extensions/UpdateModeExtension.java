package com.constellio.app.api.extensions;

import java.io.Serializable;

import com.constellio.app.ui.pages.management.updates.UpdateManagerView;
import com.vaadin.ui.Component;

public class UpdateModeExtension {
	public boolean isActive() {
		return false;
	}

	public String getCode() {
		return "";
	}

	public UpdateModeHandler getHandler(UpdateManagerView view) {
		return null;
	}

	public interface UpdateModeHandler extends Serializable {
		Component buildUpdatePanel();
	}
}
