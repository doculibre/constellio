package com.constellio.app.api.extensions;

import com.constellio.app.api.extensions.params.DecorateMainComponentAfterInitExtensionParams;
import com.constellio.app.api.extensions.params.PagesComponentsExtensionParams;
import com.constellio.app.api.extensions.params.RecordFieldsExtensionParams;
import com.constellio.app.api.extensions.params.UpdateComponentExtensionParams;
import com.constellio.app.ui.framework.components.fields.ExtraTabAdditionalRecordField;

import java.util.ArrayList;
import java.util.List;

public class PagesComponentsExtension {

	public void decorateView(PagesComponentsExtensionParams params) {

	}

	public void decorateMainComponentBeforeViewInstanciated(DecorateMainComponentAfterInitExtensionParams params) {

	}

	public void decorateMainComponentAfterViewAssembledOnViewEntered(
			DecorateMainComponentAfterInitExtensionParams params) {

	}

	public void decorateMainComponentBeforeViewAssembledOnViewEntered(
			DecorateMainComponentAfterInitExtensionParams params) {

	}

	public void updateComponent(UpdateComponentExtensionParams params) {

	}

	public List<ExtraTabAdditionalRecordField> getExtraTabAdditionalRecordFields(RecordFieldsExtensionParams params) {
		return new ArrayList<>();
	}
}
