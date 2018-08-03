package com.constellio.app.modules.rm.reports.builders.decommissioning;

import com.constellio.app.modules.rm.model.enums.DecommissioningType;

public class ContainerRecordReportParameters {
	private String containerId;
	private DecommissioningType decommissioningType;

	public ContainerRecordReportParameters(String containerId, DecommissioningType decommissioningType) {
		this.containerId = containerId;
		this.decommissioningType = decommissioningType;
	}

	public String getContainerId() {
		return containerId;
	}

	public boolean isTransfer() {
		return decommissioningType == DecommissioningType.TRANSFERT_TO_SEMI_ACTIVE;
	}

	public boolean isDestruction() {
		return decommissioningType == DecommissioningType.DESTRUCTION;
	}
}