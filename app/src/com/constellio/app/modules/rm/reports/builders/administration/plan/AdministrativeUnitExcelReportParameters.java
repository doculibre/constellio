package com.constellio.app.modules.rm.reports.builders.administration.plan;

import java.util.List;

public class AdministrativeUnitExcelReportParameters  {
	private List<String> administrativeUnitIdList = null;

	public AdministrativeUnitExcelReportParameters(List<String> administrativeUnitIdList) {
		this.administrativeUnitIdList = administrativeUnitIdList;
	}

	public List<String> getAdministrativeUnitIdList() {
		return administrativeUnitIdList;
	}
}
