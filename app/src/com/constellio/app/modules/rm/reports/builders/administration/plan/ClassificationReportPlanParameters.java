package com.constellio.app.modules.rm.reports.builders.administration.plan;

import java.util.List;

public class ClassificationReportPlanParameters {

	private boolean showDeactivated;
	private boolean detail;
	private String administrativeUnitId;
	private List<String> listOfCategory;

	public ClassificationReportPlanParameters(boolean detail, String administrativeUnitId, List<String> listOfCategory,
			boolean showDeactivated) {
		this.showDeactivated = showDeactivated;
		this.detail = detail;
		this.administrativeUnitId = administrativeUnitId;
		this.listOfCategory = listOfCategory;
	}

	public boolean isDetail() {
		return detail;
	}

	public String getAdministrativeUnitId() {
		return administrativeUnitId;
	}

	public List<String> getListOfCategory() {
		return listOfCategory;
	}

	public boolean isShowDeactivated() {
		return showDeactivated;
	}
}