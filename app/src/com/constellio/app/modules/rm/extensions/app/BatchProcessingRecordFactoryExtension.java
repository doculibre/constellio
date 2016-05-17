package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.api.extensions.RecordFieldFactoryExtension;
import com.constellio.app.api.extensions.params.RecordFieldFactoryExtensionParams;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.RecordWithCopyRetentionRuleFieldFactory;
import com.constellio.app.ui.framework.components.RecordFieldFactory;

public class BatchProcessingRecordFactoryExtension extends RecordFieldFactoryExtension {
    public static final String BATCH_PROCESSING_FIELD_FACTORY_KEY = BatchProcessingRecordFactoryExtension.class.getName();
    @Override
    public RecordFieldFactory newRecordFieldFactory(RecordFieldFactoryExtensionParams params) {
        RecordFieldFactory recordFieldFactory;
        String key = params.getKey();
        if (BATCH_PROCESSING_FIELD_FACTORY_KEY.equals(key)) {
            recordFieldFactory = new RecordWithCopyRetentionRuleFieldFactory();
        } else {
            recordFieldFactory = super.newRecordFieldFactory(params);
        }
        return recordFieldFactory;
    }
}
