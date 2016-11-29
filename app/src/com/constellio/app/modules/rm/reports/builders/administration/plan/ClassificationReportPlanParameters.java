package com.constellio.app.modules.rm.reports.builders.administration.plan;

public class ClassificationReportPlanParameters {

    private boolean detail;
    private String administrativeUnitId;

    public ClassificationReportPlanParameters(boolean detail, String administrativeUnitId) {
        this.detail = detail;
        this.administrativeUnitId = administrativeUnitId;
    }

    public boolean isDetail() {
        return detail;
    }

    public String getAdministrativeUnitId() {
        return administrativeUnitId;
    }
}