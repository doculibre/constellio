package com.constellio.app.modules.complementary.esRmRobots.extensions;

import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.complementary.ESRMRobotsModule;
import com.constellio.app.modules.es.extensions.api.ConnectorMappingExtension;
import com.constellio.app.modules.es.extensions.api.params.CustomTargetFlagsParams;
import com.constellio.app.modules.es.extensions.api.params.TargetMetadataCreationParams;
import com.constellio.app.modules.es.services.mapping.ConnectorMappingService.ConnectorMappingTransaction;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;

public class EsRmRobotsMappingExtension extends ConnectorMappingExtension {
	public static final String MAP_IN_RM_MODULE = ESRMRobotsModule.ID + ".mapInRM";

	private final RMSchemasRecordsServices rm;

	public EsRmRobotsMappingExtension(RMSchemasRecordsServices rm) {
		this.rm = rm;
	}

	@Override
	public List<String> getCustomTargetFlags(CustomTargetFlagsParams params) {
		return Arrays.asList(MAP_IN_RM_MODULE);
	}

	@Override
	public void beforeTargetMetadataCreation(TargetMetadataCreationParams params) {
		if (params.hasTargetFlag(MAP_IN_RM_MODULE)) {
			ConnectorMappingTransaction transaction = params.getTransaction();
			transaction.createTargetUserMetadata(rm.defaultFolderSchema(), params.getTarget());
			transaction.createTargetUserMetadata(rm.defaultDocumentSchema(), params.getTarget());
		}
	}
}
