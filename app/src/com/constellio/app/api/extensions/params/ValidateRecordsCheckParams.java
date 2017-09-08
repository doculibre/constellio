package com.constellio.app.api.extensions.params;

import com.constellio.app.services.records.SystemCheckResultsBuilder;
import com.constellio.model.entities.records.Record;

/**
 * Created by constellios on 2017-05-02.
 */
public class ValidateRecordsCheckParams {

    private Record record;
    private boolean repair;
    SystemCheckResultsBuilder resultsBuilder;

    public ValidateRecordsCheckParams(Record record, boolean repair, SystemCheckResultsBuilder resultsBuilder) {
        this.record = record;
        this.repair = repair;
        this.resultsBuilder = resultsBuilder;
    }

    public SystemCheckResultsBuilder getResultsBuilder() {
        return resultsBuilder;
    }

    public Record getRecord() {
        return record;
    }

    public boolean isRepair() {
        return repair;
    }
}
