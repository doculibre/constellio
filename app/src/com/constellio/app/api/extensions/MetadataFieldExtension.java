package com.constellio.app.api.extensions;

import com.constellio.app.ui.entities.MetadataVO;
import com.vaadin.ui.Field;

public interface MetadataFieldExtension {
	Field<?> getMetadataField(MetadataVO metadataVO);
}
