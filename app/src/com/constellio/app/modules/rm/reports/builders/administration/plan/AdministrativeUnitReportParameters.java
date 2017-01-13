package com.constellio.app.modules.rm.reports.builders.administration.plan;

public class AdministrativeUnitReportParameters {
    private boolean withUsers = false;

    public AdministrativeUnitReportParameters(boolean withUsers) {
        this.withUsers = withUsers;
    }

    public boolean isWithUsers() {
        return withUsers;
    }
}