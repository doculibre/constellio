package com.constellio.app.modules.rm.reports.builders.decommissioning;

public class DecommissioningListReportParameters {

    private String decommissioningListId;

    public DecommissioningListReportParameters(String decommissioningListId) {
        this.decommissioningListId = decommissioningListId;
    }

    public String getDecommissioningListId() {
        return decommissioningListId;
    }
}