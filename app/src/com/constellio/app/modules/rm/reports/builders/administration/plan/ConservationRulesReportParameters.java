package com.constellio.app.modules.rm.reports.builders.administration.plan;

/**
 * Created by Constelio on 2016-11-29.
 */
public class ConservationRulesReportParameters {

    private boolean byAdministrativeUnit = false;
    private String administrativeUnit = null;

    public ConservationRulesReportParameters(boolean byAdministrativeUnit, String administrativeUnit) {
        this.byAdministrativeUnit = byAdministrativeUnit;
        this.administrativeUnit = administrativeUnit;
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
}
