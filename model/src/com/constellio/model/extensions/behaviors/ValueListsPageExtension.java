package com.constellio.model.extensions.behaviors;

import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.model.extensions.params.ValueListExtensionParams;

public class ValueListsPageExtension {

	public ExtensionBooleanResult isManageable(ValueListExtensionParams param) {
		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

}
