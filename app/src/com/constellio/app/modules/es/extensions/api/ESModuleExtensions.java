package com.constellio.app.modules.es.extensions.api;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.extensions.ModuleExtensions;
import com.constellio.app.modules.es.extensions.api.params.CustomTargetFlagsParams;
import com.constellio.app.modules.es.extensions.api.params.TargetMetadataCreationParams;
import com.constellio.data.frameworks.extensions.VaultBehaviorsList;

public class ESModuleExtensions implements ModuleExtensions {
	public VaultBehaviorsList<ConnectorMappingExtension> connectorMappingExtensions = new VaultBehaviorsList<>();

	public List<String> getCustomTargetFlags(CustomTargetFlagsParams params) {
		ArrayList<String> result = new ArrayList<>();
		for (ConnectorMappingExtension extension : connectorMappingExtensions) {
			result.addAll(extension.getCustomTargetFlags(params));
		}
		return result;
	}

	public void beforeTargetMetadataCreation(TargetMetadataCreationParams params) {
		for (ConnectorMappingExtension extension : connectorMappingExtensions) {
			extension.beforeTargetMetadataCreation(params);
		}
	}
}
