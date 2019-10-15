package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.extensions.behaviors.BatchProcessingSpecialCaseExtension;
import com.constellio.model.extensions.params.BatchProcessingSpecialCaseParams;
import com.constellio.model.services.records.RecordServices;
import org.joda.time.LocalDateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.modules.rm.wrappers.Folder.MAIN_COPY_RULE_ID_ENTERED;

public class RMBatchProcessingSpecialCaseExtension extends BatchProcessingSpecialCaseExtension {
	String collection;
	AppLayerFactory appLayerFactory;
	RecordServices recordServices;

	public RMBatchProcessingSpecialCaseExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
	}

	@Override
	public Map<String, Object> processSpecialCase(BatchProcessingSpecialCaseParams batchProcessingSpecialCaseParams) {
		RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
		Record record = batchProcessingSpecialCaseParams.getRecord();
		Map<String, Object> modifiedMetadatas = new HashMap<>();
		MetadataSchemaTypes metadataSchemaTypes = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);

		if (record.isOfSchemaType(Folder.SCHEMA_TYPE)) {
			recordServices.recalculate(record);
			Folder folder = rmSchemasRecordsServices.wrapFolder(record);

			Metadata administrativeUnitEnteredMetadata = record.getModifiedMetadatas(metadataSchemaTypes).getMetadataWithLocalCode(Folder.ADMINISTRATIVE_UNIT_ENTERED);
			if (administrativeUnitEnteredMetadata != null) {


				RetentionRule retentionRule = rmSchemasRecordsServices.getRetentionRule(folder.getRetentionRule());
				List<CopyRetentionRule> copyRetentionRuleList = retentionRule.getCopyRetentionRules();

				if (folder.getCopyStatus() == CopyType.PRINCIPAL) {
					int numberOfPrincipal = 0;
					CopyRetentionRule lastCopyRetentionRule = null;
					for (CopyRetentionRule copyRetentionRule : copyRetentionRuleList) {
						if (copyRetentionRule.getCopyType() == CopyType.PRINCIPAL) {
							lastCopyRetentionRule = copyRetentionRule;
							numberOfPrincipal++;
						}
					}

					if (numberOfPrincipal == 1) {
						if (!LangUtils.areNullableEqual(folder.getMainCopyRuleIdEntered(), lastCopyRetentionRule.getId())) {
							modifiedMetadatas.put(folder.getSchemaCode() + "_" + MAIN_COPY_RULE_ID_ENTERED, lastCopyRetentionRule.getId());
							folder.setMainCopyRuleEntered(lastCopyRetentionRule.getId());
						}
					}
				} else {
					CopyRetentionRule lastCopyRetentionRule = null;
					for (CopyRetentionRule copyRetentionRule : copyRetentionRuleList) {
						if (copyRetentionRule.getCopyType() == CopyType.SECONDARY) {
							lastCopyRetentionRule = copyRetentionRule;
							break;
						}
					}

					if (lastCopyRetentionRule != null) {
						if (!LangUtils.areNullableEqual(folder.getMainCopyRuleIdEntered(), lastCopyRetentionRule.getId())) {
							modifiedMetadatas.put(folder.getSchemaCode() + "_" + MAIN_COPY_RULE_ID_ENTERED, lastCopyRetentionRule.getId());
							folder.setMainCopyRuleEntered(lastCopyRetentionRule.getId());
						}
					}
				}
			}
			Metadata mainCopyRuleEnteredMetadata = record.getModifiedMetadatas(metadataSchemaTypes).getMetadataWithLocalCode(Folder.MAIN_COPY_RULE_ID_ENTERED);
			if (mainCopyRuleEnteredMetadata != null) {

				String sRetentionRule = folder.getRetentionRule();
				if (sRetentionRule != null) {
					RetentionRule retentionRule = rmSchemasRecordsServices.getRetentionRule(sRetentionRule);

					if (folder.getCopyStatus() == CopyType.SECONDARY) {
						if (!record.get(mainCopyRuleEnteredMetadata).equals(retentionRule.getId())) {
							if (!LangUtils.areNullableEqual(record.get(mainCopyRuleEnteredMetadata), retentionRule.getSecondaryCopy().getId())) {
								modifiedMetadatas.put(mainCopyRuleEnteredMetadata.getCode(), retentionRule.getSecondaryCopy().getId());
								record.set(mainCopyRuleEnteredMetadata, retentionRule.getSecondaryCopy().getId());
							}
						}
					}
				}
			}
			Metadata retentionRuleMetadata = record.getModifiedMetadatas(metadataSchemaTypes).getMetadataWithLocalCode(Folder.RETENTION_RULE);
			if (retentionRuleMetadata != null) {
				String retentionRuleId = folder.getRetentionRule();
				if (retentionRuleId != null) {
					RetentionRule retentionRule = rmSchemasRecordsServices.getRetentionRule(retentionRuleId);
					if (folder.getCopyStatus() == CopyType.SECONDARY) {
						if (!LangUtils.areNullableEqual(folder.getMainCopyRuleIdEntered(), retentionRule.getSecondaryCopy().getId())) {
							modifiedMetadatas.put(record.getSchemaCode() + "_" + Folder.MAIN_COPY_RULE_ID_ENTERED, retentionRule.getSecondaryCopy().getId());
							folder.setMainCopyRuleEntered(retentionRule.getSecondaryCopy().getId());
						}
					} else if (retentionRule.getPrincipalCopies().size() == 1) {
						modifiedMetadatas.put(record.getSchemaCode() + "_" + Folder.MAIN_COPY_RULE_ID_ENTERED, retentionRule.getPrincipalCopies().get(0).getId());
						folder.setMainCopyRuleEntered(retentionRule.getPrincipalCopies().get(0).getId());
					}
				}
			}
		}

		MetadataSchema schema = metadataSchemaTypes.getSchemaOf(record);
		if (schema.hasMetadataWithCode(RMObject.FORM_MODIFIED_ON) && modifiedMetadatas != null && modifiedMetadatas.size() > 0) {
			record.set(schema.get(RMObject.FORM_MODIFIED_ON), LocalDateTime.now());
			record.set(schema.get(RMObject.FORM_MODIFIED_BY), batchProcessingSpecialCaseParams.getUser());
			modifiedMetadatas.put(record.getSchemaCode() + "_" + Folder.FORM_MODIFIED_ON, LocalDateTime.now());
			modifiedMetadatas.put(record.getSchemaCode() + "_" + Folder.FORM_MODIFIED_BY, batchProcessingSpecialCaseParams.getUser());
		}

		return modifiedMetadatas;
	}
}
