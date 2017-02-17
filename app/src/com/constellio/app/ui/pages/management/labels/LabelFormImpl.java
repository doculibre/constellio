package com.constellio.app.ui.pages.management.labels;

import com.constellio.app.modules.rm.ui.components.document.DocumentForm;
import com.constellio.app.modules.rm.ui.components.document.fields.CustomDocumentField;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.framework.components.RecordForm;
import com.constellio.app.ui.pages.base.SessionContext;
import com.google.gwt.thirdparty.javascript.rhino.head.ast.Label;

/**
 * Created by Marco on 2017-01-20.
 */
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
