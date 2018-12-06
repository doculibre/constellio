package com.constellio.app.modules.rm.reports.builders.administration.plan;

import java.util.List;

/**
 * Created by Constelio on 2016-11-29.
 */
public class ConservationRulesReportParameters {

	private boolean byAdministrativeUnit;
	private String administrativeUnit;
	private List<String> conservationRules;

	public ConservationRulesReportParameters(boolean byAdministrativeUnit, String administrativeUnit, List<String> conservationRules) {
		this.byAdministrativeUnit = byAdministrativeUnit;
		this.administrativeUnit = administrativeUnit;
		this.conservationRules = conservationRules;
	}

	public boolean isByAdministrativeUnit() {
		return byAdministrativeUnit;
	}

	public String getAdministrativeUnit() {
		return administrativeUnit;
	}

	public void setAdministrativeUnit(String administrativeUnit) {
		this.administrativeUnit = administrativeUnit;
	}

	public List<String> getConservationRules() {
		return conservationRules;
	}
}
