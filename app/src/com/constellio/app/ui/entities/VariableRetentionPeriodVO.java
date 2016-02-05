package com.constellio.app.ui.entities;

import java.io.Serializable;

public class VariableRetentionPeriodVO implements Serializable {
    String recordId;
    String code;
    String title;

    public String getRecordId() {
        return recordId;
    }

    public VariableRetentionPeriodVO setRecordId(String recordId) {
        this.recordId = recordId;
        return this;
    }

    public String getCode() {
        return code;
    }

    public VariableRetentionPeriodVO setCode(String code) {
        this.code = code;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public VariableRetentionPeriodVO setTitle(String title) {
        this.title = title;
        return this;
    }
}
