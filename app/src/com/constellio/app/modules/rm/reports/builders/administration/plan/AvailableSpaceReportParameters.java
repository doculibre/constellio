package com.constellio.app.modules.rm.reports.builders.administration.plan;

public class AvailableSpaceReportParameters {

    private boolean showFullSpaces;

    public AvailableSpaceReportParameters(boolean showFullSpaces) {
        this.showFullSpaces = showFullSpaces;
    }

    public boolean isShowFullSpaces() {
        return showFullSpaces;
    }
}