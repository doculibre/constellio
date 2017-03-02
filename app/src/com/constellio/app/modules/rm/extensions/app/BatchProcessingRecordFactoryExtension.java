package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.api.extensions.RecordFieldFactoryExtension;
import com.constellio.app.api.extensions.params.RecordFieldFactoryExtensionParams;
import com.constellio.app.modules.rm.ui.components.copyRetentionRule.RecordWithCopyRetentionRuleFieldFactory;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.ui.framework.components.MetadataFieldFactory;
import com.constellio.app.ui.framework.components.RecordFieldFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.List;

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
						, params.getSelectedTypeId(), params.getQuery(), params.getSelectedRecords());
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
		private LogicalSearchQuery query;

		public BatchProcessingFieldFactoryExtensionParams(String key, MetadataFieldFactory metadataFieldFactory,
														  String schemaType, LogicalSearchQuery query) {
			super(key, metadataFieldFactory);
			this.schemaType = schemaType;
			this.query = query;
		}

		public BatchProcessingFieldFactoryExtensionParams(String key, MetadataFieldFactory metadataFieldFactory,
														  String schemaType, List<String> selectedRecords) {
			super(key, metadataFieldFactory);
			this.schemaType = schemaType;
			this.selectedRecords = selectedRecords;
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

		public LogicalSearchQuery getQuery() {
			return query;
		}

		public BatchProcessingFieldFactoryExtensionParams setQuery(LogicalSearchQuery query) {
			this.query = query;
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
