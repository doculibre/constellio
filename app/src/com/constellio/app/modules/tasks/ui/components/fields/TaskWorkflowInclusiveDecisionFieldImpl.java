package com.constellio.app.modules.tasks.ui.components.fields;

import com.constellio.app.modules.rm.ui.components.converters.DocumentIdToContextCaptionConverter;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.components.fields.lookup.LookupStringField;
import com.vaadin.ui.ComboBox;

/**
 * Created by constellios on 2017-07-19.
 */
public class TaskWorkflowInclusiveDecisionFieldImpl extends ComboBox implements TaskWorkflowInclusiveDecisionField {

    public TaskWorkflowInclusiveDecisionFieldImpl() {
    }


    @Override
    public String getFieldValue() {
        return (String) getConvertedValue();
    }

    @Override
    public void setFieldValue(Object value) {
        setInternalValue((String) value);
    }
}
