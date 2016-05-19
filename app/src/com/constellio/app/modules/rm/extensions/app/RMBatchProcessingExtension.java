package com.constellio.app.modules.rm.extensions.app;

import static java.util.Arrays.asList;

import java.util.List;

import com.constellio.app.api.extensions.BatchProcessingExtension;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.ui.components.folder.fields.FolderCopyRuleFieldImpl;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.data.frameworks.extensions.ExtensionBooleanResult;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.entities.records.wrappers.User;
import com.vaadin.ui.Field;

public class RMBatchProcessingExtension extends BatchProcessingExtension {

	RMConfigs configs;
	AppLayerFactory appLayerFactory;
	String collection;

	public RMBatchProcessingExtension(String collection, AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		this.collection = collection;
		this.configs = new RMConfigs(appLayerFactory.getModelLayerFactory().getSystemConfigurationsManager());
	}

	final List<String> hiddenFolderMetadatasInReport = asList(Folder.CATEGORY_ENTERED, Folder.ADMINISTRATIVE_UNIT_ENTERED,
			Folder.UNIFORM_SUBDIVISION_ENTERED, Folder.RETENTION_RULE_ENTERED, Folder.COPY_STATUS_ENTERED,
			Folder.MAIN_COPY_RULE_ID_ENTERED, Folder.APPLICABLE_COPY_RULES, Folder.ACTIVE_RETENTION_CODE,
			Folder.ACTIVE_RETENTION_TYPE, Folder.SEMIACTIVE_RETENTION_CODE, Folder.SEMIACTIVE_RETENTION_TYPE,
			Folder.INACTIVE_DISPOSAL_TYPE, Folder.RETENTION_RULE_ADMINISTRATIVE_UNITS, Folder.COPY_RULES_EXPECTED_DEPOSIT_DATES,
			Folder.COPY_RULES_EXPECTED_TRANSFER_DATES, Folder.COPY_RULES_EXPECTED_DESTRUCTION_DATES, Folder.DECOMMISSIONING_DATE,
			Folder.FOLDER_TYPE);

	final List<String> hiddenDocumentMetadatasInReport = asList(Document.ALERT_USERS_WHEN_AVAILABLE,
			Document.APPLICABLE_COPY_RULES,
			Document.CALENDAR_YEAR, Document.ACTUAL_DEPOSIT_DATE_ENTERED, Document.ACTUAL_DESTRUCTION_DATE_ENTERED,
			Document.ACTUAL_TRANSFER_DATE_ENTERED, Document.INHERITED_FOLDER_RETENTION_RULE, Document.MAIN_COPY_RULE_ID_ENTERED);

	@Override
	public ExtensionBooleanResult isMetadataDisplayedWhenModified(IsMetadataDisplayedWhenModifiedParams params) {

		if (params.isSchemaType(Folder.SCHEMA_TYPE)) {
			return ExtensionBooleanResult.falseIf(hiddenFolderMetadatasInReport.contains(params.getMetadata().getLocalCode()));
		}

		if (params.isSchemaType(Document.SCHEMA_TYPE)) {
			return ExtensionBooleanResult.falseIf(hiddenDocumentMetadatasInReport.contains(params.getMetadata().getLocalCode()));
		}

		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	@Override
	public ExtensionBooleanResult isMetadataModifiable(IsMetadataModifiableParams params) {

		User user = params.getUser();

		if (params.isSchemaType(Folder.SCHEMA_TYPE)) {
		}

		if (params.isSchemaType(Document.SCHEMA_TYPE)) {

		}

		return ExtensionBooleanResult.NOT_APPLICABLE;
	}

	@Override
	public void addCustomLabel(AddCustomLabelsParams params) {
		params.setCustomPrefixLabelWithKey(Folder.ADMINISTRATIVE_UNIT_ENTERED, "batchProceesing.only.root.folders");
		params.setCustomPrefixLabelWithKey(Folder.CATEGORY_ENTERED, "batchProceesing.only.root.folders");
		params.setCustomPrefixLabelWithKey(Folder.RETENTION_RULE_ENTERED, "batchProceesing.only.root.folders");

		params.setCustomPrefixLabelWithKey(Folder.MAIN_COPY_RULE_ID_ENTERED, "batchProceesing.only.not.calculated");

		if (!configs.isCopyRuleTypeAlwaysModifiable()) {
			params.setCustomPrefixLabelWithKey(Folder.COPY_STATUS_ENTERED, "batchProceesing.only.not.calculated");
		}
	}

	@Override
	public Field buildMetadataField(MetadataVO metadataVO, RecordVO recordVO) {
		String metadataLocalCode = metadataVO.getLocalCode();
		if (metadataLocalCode.equals(Folder.TYPE) || metadataLocalCode.equals(Document.TYPE) || metadataLocalCode
				.equals(ContainerRecord.TYPE)) {
			return null;
		}
		if (metadataLocalCode.equals(Folder.MAIN_COPY_RULE_ID_ENTERED)) {
			CopyRetentionRule rule = getSelectedRule(recordVO);
			if (rule != null && isMainCopy(recordVO)) {
				return new FolderCopyRuleFieldImpl(asList(rule));
			}
		}
		throw new ImpossibleRuntimeException("No specific field for metadata " + metadataLocalCode);
	}

	private boolean isMainCopy(RecordVO recordVO) {
		CopyType copyType = recordVO.get(Folder.COPY_STATUS);
		return copyType != null && copyType.equals(CopyType.PRINCIPAL);
	}

	private CopyRetentionRule getSelectedRule(RecordVO recordVO) {
		return recordVO.get(Folder.RETENTION_RULE_ENTERED);
	}

	@Override
	public boolean hasMetadataSpecificAssociatedField(MetadataVO metadataVO) {
		String metadataLocalCode = metadataVO.getLocalCode();
		return metadataLocalCode.equals(Folder.TYPE)
				|| metadataLocalCode.equals(Document.TYPE)
				|| metadataLocalCode.equals(ContainerRecord.TYPE)
				|| metadataLocalCode.equals(Folder.MAIN_COPY_RULE_ID_ENTERED);
	}
}
