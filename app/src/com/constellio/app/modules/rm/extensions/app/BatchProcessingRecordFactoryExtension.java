package com.constellio.app.modules.rm.extensions.app;

import java.util.List;

import com.constellio.app.api.extensions.RecordFieldFactoryExtension;
import com.constellio.app.api.extensions.params.RecordFieldFactoryExtensionParams;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.RecordWithCopyRetentionRuleFieldFactory;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.RecordFieldFactory;

public class BatchProcessingRecordFactoryExtension extends RecordFieldFactoryExtension {
	public static final String BATCH_PROCESSING_FIELD_FACTORY_KEY = BatchProcessingRecordFactoryExtension.class.getName();

	@Override
	public RecordFieldFactory newRecordFieldFactory(RecordFieldFactoryExtensionParams p) {

		if (p instanceof BatchProcessingFieldFactoryExtensionParams) {
			BatchProcessingFieldFactoryExtensionParams params = (BatchProcessingFieldFactoryExtensionParams) p;
			RecordFieldFactory recordFieldFactory;
			String key = params.getKey();
			String schemaType = params.getSchemaType();
			if (BATCH_PROCESSING_FIELD_FACTORY_KEY.equals(key) &&
					(schemaType.equals(Folder.SCHEMA_TYPE) || schemaType.equals(Document.SCHEMA_TYPE))) {
				recordFieldFactory = new RecordWithCopyRetentionRuleFieldFactory(schemaType,
						params.getRecordIdThatCopyRetentionRuleDependantOn()
						, params.getSelectedTypeId(), params.getSelectedRecords());
			} else {
				recordFieldFactory = super.newRecordFieldFactory(params);
			}
			return recordFieldFactory;
		} else {
			return null;
		}
	}

	public static class BatchProcessingFieldFactoryExtensionParams extends RecordFieldFactoryExtensionParams {
		private String selectedTypeId;
		private final String schemaType;
		private String recordIdThatCopyRetentionRuleDependantOn;
		private List<String> selectedRecords;

		public BatchProcessingFieldFactoryExtensionParams(String key, MetadataFieldFactory metadataFieldFactory,
				String schemaType) {
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

		public BatchProcessingFieldFactoryExtensionParams setRecordIdThatCopyRetentionRuleDependantOn(
				String recordIdThatCopyRetentionRuleDependantOn) {
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
