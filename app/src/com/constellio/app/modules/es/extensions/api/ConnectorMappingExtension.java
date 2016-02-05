package com.constellio.app.modules.es.extensions.api;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.es.extensions.api.params.CustomTargetFlagsParams;
import com.constellio.app.modules.es.extensions.api.params.TargetMetadataCreationParams;

public class ConnectorMappingExtension {
	public List<String> getCustomTargetFlags(CustomTargetFlagsParams params) {
		return new ArrayList<>();
	}

	public void beforeTargetMetadataCreation(TargetMetadataCreationParams params) {

	}
}
