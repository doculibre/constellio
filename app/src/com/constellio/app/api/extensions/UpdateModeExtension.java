package com.constellio.app.api.extensions;

import com.constellio.app.ui.pages.management.updates.UpdateManagerView;
import com.vaadin.ui.Component;

import java.io.Serializable;

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
