package com.constellio.app.modules.rm.extensions.api;

import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class ValidateDecommissioningListProcessableParams {

    private DecommissioningList decommissioningList;
    private ValidationErrors validationErrors = new ValidationErrors();

    public ValidateDecommissioningListProcessableParams(DecommissioningList decommissioningList) {
        this.decommissioningList = decommissioningList;
    }

    public DecommissioningList getDecommissioningList() {
        return decommissioningList;
    }

    public ValidationErrors getValidationErrors() {
        return validationErrors;
    }

}
