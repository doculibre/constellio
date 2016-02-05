package com.constellio.app.modules.es.extensions.api.params;

import com.constellio.app.modules.es.services.mapping.ConnectorMappingService.ConnectorMappingTransaction;
import com.constellio.app.modules.es.services.mapping.TargetParams;

public class TargetMetadataCreationParams {
	private final ConnectorMappingTransaction transaction;
	private final TargetParams target;

	public TargetMetadataCreationParams(ConnectorMappingTransaction transaction, TargetParams target) {
		this.transaction = transaction;
		this.target = target;
	}

	public ConnectorMappingTransaction getTransaction() {
		return transaction;
	}

	public TargetParams getTarget() {
		return target;
	}

	public boolean hasTargetFlag(String flag) {
		return target.hasCustomFlag(flag);
	}
}
