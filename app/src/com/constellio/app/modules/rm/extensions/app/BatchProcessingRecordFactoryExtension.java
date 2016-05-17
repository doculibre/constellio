package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.api.extensions.RecordFieldFactoryExtension;
import com.constellio.app.api.extensions.params.RecordFieldFactoryExtensionParams;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.RecordWithCopyRetentionRuleFieldFactory;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.app.ui.pages.search.batchProcessing.entities.BatchProcessRequest;

import java.util.List;

public class BatchProcessingRecordFactoryExtension extends RecordFieldFactoryExtension<BatchProcessingRecordFactoryExtension.BatchProcessingFieldFactoryExtensionParams> {
    public static final String BATCH_PROCESSING_FIELD_FACTORY_KEY = BatchProcessingRecordFactoryExtension.class.getName();

    @Override
    public RecordFieldFactory newRecordFieldFactory(BatchProcessingFieldFactoryExtensionParams params) {
        RecordFieldFactory recordFieldFactory;
        String key = params.getKey();
        String schemaType = params.getSchemaType();
        if (BATCH_PROCESSING_FIELD_FACTORY_KEY.equals(key) &&
                (schemaType.equals(Folder.SCHEMA_TYPE) || schemaType.equals(Document.SCHEMA_TYPE))) {
            recordFieldFactory = new RecordWithCopyRetentionRuleFieldFactory(schemaType, params.getRecordIdThatCopyRetentionRuleDependantOn()
                    , params.getSelectedTypeId(), params.getSelectedRecords());
        } else {
            recordFieldFactory = super.newRecordFieldFactory(params);
        }
        return recordFieldFactory;
    }

    public static class BatchProcessingFieldFactoryExtensionParams extends RecordFieldFactoryExtensionParams{
        private String selectedTypeId;
        private final String schemaType;
        private String recordIdThatCopyRetentionRuleDependantOn;
        private List<String> selectedRecords;

        public BatchProcessingFieldFactoryExtensionParams(String key, MetadataFieldFactory metadataFieldFactory, String schemaType) {
            super(key, metadataFieldFactory);
            this.schemaType = schemaType;
        }

        public String getSchemaType() {
            return schemaType;
        }

        public String getSelectedTypeId() {
            return selectedTypeId;
        }

        public BatchProcessingFieldFactoryExtensionParams setSelectedTypeId(String selectedTypeId) {
            this.selectedTypeId = selectedTypeId;
            return this;
        }

        public String getRecordIdThatCopyRetentionRuleDependantOn() {
            return recordIdThatCopyRetentionRuleDependantOn;
        }

        public BatchProcessingFieldFactoryExtensionParams setRecordIdThatCopyRetentionRuleDependantOn(String recordIdThatCopyRetentionRuleDependantOn) {
            this.recordIdThatCopyRetentionRuleDependantOn = recordIdThatCopyRetentionRuleDependantOn;
            return this;
        }

        public List<String> getSelectedRecords() {
            return selectedRecords;
        }

        public BatchProcessingFieldFactoryExtensionParams setSelectedRecords(List<String> selectedRecords) {
            this.selectedRecords = selectedRecords;
            return this;
        }
    }
}
