package com.constellio.app.api.extensions;

import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordForm;
import com.vaadin.ui.Field;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

public class SchemaRecordFormExtension {

	public List<String> getFieldsToListenTo() {
		return Collections.EMPTY_LIST;
	}

	public boolean adjustFields(SchemaRecordFormExtensionParams params) {
		return false;
	}

	@Getter
	@AllArgsConstructor
	public static class SchemaRecordFormExtensionParams {
		Field<?> field;
		RecordVO recordVO;
		RecordForm form;
	}
}
