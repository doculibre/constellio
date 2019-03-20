package com.constellio.app.api.extensions;

import java.util.List;

import com.constellio.app.api.extensions.params.MetadataFieldExtensionParams;
import com.vaadin.ui.Field;

public interface MetadataFieldExtension {
	Field<?> getMetadataField(MetadataFieldExtensionParams metadataFieldExtensionParams);

	List<String> getMetadataFieldToHide(MetadataFieldExtensionParams metadataFieldExtensionParams);
}
