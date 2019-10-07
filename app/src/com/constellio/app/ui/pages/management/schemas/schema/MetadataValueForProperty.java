package com.constellio.app.ui.pages.management.schemas.schema;

import com.constellio.app.ui.entities.MetadataVO;

import java.util.Locale;

public interface MetadataValueForProperty {
	Object getValue(Object propertyIdObj, MetadataVO metadata);

	Locale getCurrentLocale();
}
