package com.constellio.app.ui.pages.search.batchProcessing.components;

import com.constellio.model.entities.EnumWithSmallCode;

public enum BatchProcessingAction implements EnumWithSmallCode {
    DIRECT_VALUE("d"), FIXED_VALUE("f"), NO_VALUE("n"), INDIRECT_VALUE_USING_MAPPING_TABLE("i");

    private final String code;

    BatchProcessingAction(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public boolean isDirectOrIndirect() {
        return this == DIRECT_VALUE || this == INDIRECT_VALUE_USING_MAPPING_TABLE;
    }

    public boolean isFixed() {
        return this == FIXED_VALUE;
    }

    public boolean isIndirect() {
        return this == INDIRECT_VALUE_USING_MAPPING_TABLE;
    }

    public boolean isMapped() {
        return this != NO_VALUE;
    }
}
