package com.constellio.app.ui.pages.management.labels;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.SessionContext;

public abstract class LabelFormImpl extends RecordForm implements LabelForm {
    public LabelFormImpl(RecordVO record) {
        super(record);
    }

    public LabelFormImpl(RecordVO record, RecordFieldFactory factory) {
        super(record, factory);
    }

    @Override
    public CustomLabelField<?> getCustomField(String metadataCode) {
        return (CustomLabelField<?>) getField(metadataCode);
    }

    @Override
    public ConstellioFactories getConstellioFactories() {
        return ConstellioFactories.getInstance();
    }

    @Override
    public SessionContext getSessionContext() {
        return ConstellioUI.getCurrentSessionContext();
    }

}
