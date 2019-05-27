package com.constellio.app.modules.rm.reports.builders.decommissioning;

public class DecommissioningListExcelReportParameters {
    private String decommissioningListId;

    public DecommissioningListExcelReportParameters(String decommissioningListId) {
        this.decommissioningListId = decommissioningListId;
    }

    public String getDecommissioningListId() {
        return decommissioningListId;
    }
}
