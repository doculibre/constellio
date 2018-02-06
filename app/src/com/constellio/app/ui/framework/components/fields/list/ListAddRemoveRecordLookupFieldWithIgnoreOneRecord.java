package com.constellio.app.ui.framework.components.fields.list;

import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.app.ui.framework.components.fields.lookup.LookupRecordField;
import com.constellio.app.ui.framework.data.RecordTextInputDataProvider;
import com.constellio.app.ui.framework.data.RecordTextInputDataProviderWithOneRecordIgnore;

import static com.constellio.app.services.factories.ConstellioFactories.getInstance;
import static com.constellio.app.ui.application.ConstellioUI.getCurrentSessionContext;

public class ListAddRemoveRecordLookupFieldWithIgnoreOneRecord extends ListAddRemoveRecordLookupField {
    private String schemaTypeCode;
    private String schemaCode;
    private boolean ignoreLinkability;
    private String ignoredRecordId;

    public ListAddRemoveRecordLookupFieldWithIgnoreOneRecord(String schemaTypeCode, String ignoredRecordId) {
        super(schemaTypeCode);
        this.schemaTypeCode = schemaTypeCode;
        this.ignoredRecordId = ignoredRecordId;
    }


    public void setIgnoreLinkability(boolean ignoreLinkability) {
        this.ignoreLinkability = ignoreLinkability;
        if (addEditField != null) {
            addEditField.setIgnoreLinkability(ignoreLinkability);
        }
    }

    @Override
    protected LookupRecordField newAddEditField() {
        LookupRecordField field = new LookupRecordField(schemaTypeCode, schemaCode, false,
                new RecordTextInputDataProviderWithOneRecordIgnore(getInstance(),
                        getCurrentSessionContext(), schemaTypeCode, false,
                        ignoredRecordId));
        field.setIgnoreLinkability(ignoreLinkability);
        return field;
    }
}
