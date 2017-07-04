package com.constellio.app.api.extensions;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.api.extensions.params.GetAvailableExtraMetadataAttributesParam;

public class SchemaTypesPageExtension {

	/**
	 * Return available custom attributes for a given metadata
	 * A custom attribute will appear has a checkbox in the metadata form
	 * Then it can be used to filter metadatas
	 *
	 * @param param
	 * @return
	 */
	public List<String> getAvailableExtraMetadataAttributes(GetAvailableExtraMetadataAttributesParam param) {
		return new ArrayList<>();
	}
}
