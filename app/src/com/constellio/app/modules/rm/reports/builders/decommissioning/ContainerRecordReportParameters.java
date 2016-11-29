package com.constellio.app.modules.rm.reports.builders.decommissioning;

public class ContainerRecordReportParameters {
    String containerId;
    boolean transfer;

    public ContainerRecordReportParameters(String containerId, boolean transfer) {
        this.containerId = containerId;
        this.transfer = transfer;
    }

    public String getContainerId() {
        return containerId;
    }

    public boolean isTransfer() {
        return transfer;
    }
}