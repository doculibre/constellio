package com.constellio.app.modules.rm.extensions.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.api.extensions.RecordExportExtension;
import com.constellio.app.api.extensions.params.OnWriteRecordParams;
import com.constellio.app.modules.rm.extensions.imports.RetentionRuleImportExtension;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.extensions.behaviors.RecordExtension;

public class RMRecordExportExtension extends RecordExportExtension {

	String collection;
	AppLayerFactory appLayerFactory;

	public RMRecordExportExtension(String collection, AppLayerFactory appLayerFactory) {
		this.collection = collection;
		this.appLayerFactory = appLayerFactory;
	}

	@Override
	public void onWriteRecord(OnWriteRecordParams params) {

		if (params.isRecordOfType(RetentionRule.SCHEMA_TYPE)) {

			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
			RetentionRule retentionRule = rm.wrapRetentionRule(params.getRecord());

			List<Map<String, String>> importedCopyRetentionRules = new ArrayList<>();

			for (CopyRetentionRule copyRetentionRule : retentionRule.getCopyRetentionRules()) {
				//copyRetentionRule.etc

				Map<String, String> map = writeCopyRetentionRule(rm, copyRetentionRule);

				importedCopyRetentionRules.add(map);
			}

			params.getModifiableImportRecord().addField(RetentionRule.COPY_RETENTION_RULES, importedCopyRetentionRules);
		}

	}

	private Map<String, String> writeCopyRetentionRule(RMSchemasRecordsServices rm, CopyRetentionRule copyRetentionRule) {
		Map<String, String> map = new HashMap<>();

		List<String> mediumTypesCodes = new ArrayList<>();
		for (String mediumTypeId : copyRetentionRule.getMediumTypeIds()) {
			mediumTypesCodes.add(rm.getMediumType(mediumTypeId).getCode());
		}

		map.put(RetentionRuleImportExtension.CODE, copyRetentionRule.getCode());
		map.put(RetentionRuleImportExtension.TITLE, copyRetentionRule.getTitle());
		map.put(RetentionRuleImportExtension.COPY_TYPE, copyTypeToString(copyRetentionRule.getCopyType()));
		map.put(RetentionRuleImportExtension.DESCRIPTION, copyRetentionRule.getDescription());
		map.put(RetentionRuleImportExtension.CONTENT_TYPES_COMMENT, copyRetentionRule.getContentTypesComment());
		map.put(RetentionRuleImportExtension.ACTIVE_RETENTION_PERIOD,
				Integer.toString(copyRetentionRule.getActiveRetentionPeriod().getValue()));
		map.put(RetentionRuleImportExtension.SEMI_ACTIVE_RETENTION_PERIOD_COMMENT,
				copyRetentionRule.getSemiActiveRetentionComment());
		map.put(RetentionRuleImportExtension.SEMI_ACTIVE_RETENTION_PERIOD,
				Integer.toString(copyRetentionRule.getSemiActiveRetentionPeriod().getValue()));
		map.put(RetentionRuleImportExtension.INACTIVE_DISPOSAL_COMMENT, copyRetentionRule.getInactiveDisposalComment());
		map.put(RetentionRuleImportExtension.INACTIVE_DISPOSAL_TYPE, copyRetentionRule.getInactiveDisposalType().getCode());
		map.put(RetentionRuleImportExtension.OPEN_ACTIVE_RETENTION_PERIOD,
				Integer.toString(copyRetentionRule.getActiveRetentionPeriod().getValue()));
		map.put(RetentionRuleImportExtension.REQUIRED_COPYRULE_FIELD, Boolean.toString(copyRetentionRule.isEssential()));
		map.put(RetentionRuleImportExtension.COPY_RETENTION_RULE_ID, copyRetentionRule.getId());
		map.put(RetentionRuleImportExtension.MEDIUM_TYPES, StringUtils.join(mediumTypesCodes, ','));
		map.put(RetentionRuleImportExtension.IGNORE_ACTIVE_PERIOD, Boolean.toString(copyRetentionRule.isIgnoreActivePeriod()));

		if (copyRetentionRule.getTypeId() != null) {
			map.put(RetentionRuleImportExtension.TYPE_ID, rm.getFolderType(copyRetentionRule.getTypeId()).getCode());
		}

		return map;
	}

	public static String copyTypeToString(CopyType copyType) {
		String copyTypeStr = "NOTHING";

		if (copyType == CopyType.PRINCIPAL) {
			copyTypeStr = "P";
		} else if (copyType == CopyType.SECONDARY) {
			copyTypeStr = "S";
		}

		return copyTypeStr;
	}
}
