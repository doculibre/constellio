package com.constellio.app.modules.rm.reports.factories.labels;

import java.util.List;

/**
 * Created by Constelio on 2016-11-29.
 */
public class ExampleReportParameters {
    private final List<String> recordIds;

    public ExampleReportParameters(List<String> recordIds) {
        this.recordIds = recordIds;
    }

    public List<String> getRecordIds() {
        return recordIds;
    }
}
