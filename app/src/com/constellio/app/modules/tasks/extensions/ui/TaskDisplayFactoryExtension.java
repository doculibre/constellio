package com.constellio.app.modules.tasks.extensions.ui;

import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.vaadin.ui.Component;
import lombok.AllArgsConstructor;
import lombok.Getter;


public class TaskDisplayFactoryExtension {

	public Component getDisplayComponent(TaskDisplayFactoryExtensionParams params) {
		return null;
	}

	@Getter
	@AllArgsConstructor
	public static class TaskDisplayFactoryExtensionParams {
		RecordVO recordVO;
		MetadataVO metadata;
		Object displayValue;
	}
}
