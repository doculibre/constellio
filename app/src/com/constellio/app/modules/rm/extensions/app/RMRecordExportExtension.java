package com.constellio.app.modules.rm.extensions.app;

import com.constellio.app.api.extensions.RecordExportExtension;
import com.constellio.app.api.extensions.params.OnWriteRecordParams;
import com.constellio.app.modules.rm.extensions.imports.DecommissioningListImportExtension;
import com.constellio.app.modules.rm.extensions.imports.ReportImportExtension;
import com.constellio.app.modules.rm.extensions.imports.RetentionRuleImportExtension;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.structures.DecomListContainerDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListFolderDetail;
import com.constellio.app.modules.rm.wrappers.structures.DecomListValidation;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.structure.ReportedMetadata;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			manageRetentionRule(params);
		} else if (params.isRecordOfType(DecommissioningList.SCHEMA_TYPE)) {
			manageDecomissionList(params);
		} else if (params.isRecordOfType(Report.SCHEMA_TYPE)) {
			manageReport(params);
		}
	}


	private void manageReport(OnWriteRecordParams params) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		Report report = rm.wrapReport(params.getRecord());

		List<Map<String, String>> reportList = new ArrayList<>();

		for(ReportedMetadata reportedMetadata : report.getReportedMetadata())
		{
			reportList.add(writeReportedMetadata(reportedMetadata));
		}

		params.getModifiableImportRecord().addField(Report.REPORTED_METADATA, reportList);
	}

	private Map<String, String> writeReportedMetadata(ReportedMetadata reportedMetadata) {
		Map<String, String> map = new HashMap<>();

		map.put(ReportImportExtension.METADATA_CODE, reportedMetadata.getMetadataCode());
		map.put(ReportImportExtension.X_POSITION, Integer.toString(reportedMetadata.getXPosition()));
		map.put(ReportImportExtension.Y_POSITION, Integer.toString(reportedMetadata.getYPosition()));

		return map;
	}

	private void manageDecomissionList(OnWriteRecordParams params)
	{
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		DecommissioningList decommissioningList = rm.wrapDecommissioningList(params.getRecord());

		List<Map<String, String>> containerDetail = new ArrayList<>();

		for(DecomListContainerDetail decomListContainerDetail : decommissioningList.getContainerDetails())
		{
			Map<String, String> map = writedecomListContainerDetail(decomListContainerDetail);

			containerDetail.add(map);
		}

		params.getModifiableImportRecord().addField(DecommissioningList.CONTAINER_DETAILS, containerDetail);

		List<Map<String, String>> decomListFolderDetailList = new ArrayList<>();
		for(DecomListFolderDetail decomListFolderDetail : decommissioningList.getFolderDetails())
		{
			Map<String, String> map = writeDecomListFolderDetail(decomListFolderDetail);

			decomListFolderDetailList.add(map);
		}

		params.getModifiableImportRecord().addField(DecommissioningList.FOLDER_DETAILS, decomListFolderDetailList);

		List<Map<String, String>> decomListValidationList = new ArrayList<>();

		for(DecomListValidation decomListValidation : decommissioningList.getValidations())
		{
			Map<String, String> map = writeDecomListValidation(decomListValidation);

			decomListValidationList.add(map);
		}

		params.getModifiableImportRecord().addField(DecommissioningList.VALIDATIONS, decomListFolderDetailList);
	}

	private Map<String, String> writeDecomListValidation(DecomListValidation decomListValidation) {
		Map<String, String> map = new HashMap<>();

		map.put(DecommissioningListImportExtension.USER_ID, decomListValidation.getUserId());
		if(decomListValidation.getRequestDate() != null) {
			map.put(DecommissioningListImportExtension.REQUEST_DATE,
					decomListValidation.getRequestDate().toString("yyyy-MM-dd"));
		}
		if(decomListValidation.getValidationDate() != null) {
			map.put(DecommissioningListImportExtension.VALIDATION_DATE,
					decomListValidation.getValidationDate().toString("yyyy-MM-dd"));
		}

		return map;
	}

	private Map<String, String> writedecomListContainerDetail(DecomListContainerDetail decomListContainerDetail) {
		Map<String, String> map = new HashMap<>();

		map.put(DecommissioningListImportExtension.CONTAINER_RECORD_ID,decomListContainerDetail.getContainerRecordId());
		if(decomListContainerDetail.getAvailableSize() != null)
		{
			map.put(DecommissioningListImportExtension.AVAILABLE_SIZE, Double.toString(decomListContainerDetail.getAvailableSize()));
		}

		map.put(DecommissioningListImportExtension.BOOLEAN_FULL, Boolean.toString(decomListContainerDetail.isFull()));
		return map;
	}

	private Map<String, String> writeDecomListFolderDetail(DecomListFolderDetail decomListFolderDetail) {
		Map<String, String> map = new HashMap<>();

		map.put(DecommissioningListImportExtension.FOLDER_ID,decomListFolderDetail.getFolderId());
		map.put(DecommissioningListImportExtension.FOLDER_EXCLUDED, Boolean.toString(decomListFolderDetail.isFolderExcluded()));
		map.put(DecommissioningListImportExtension.CONTAINER_RECORD_ID, decomListFolderDetail.getContainerRecordId());
		map.put(DecommissioningListImportExtension.REVERSED_SORT, Boolean.toString(decomListFolderDetail.isReversedSort()));
		if(decomListFolderDetail.getFolderLinearSize() != null) {
			map.put(DecommissioningListImportExtension.FOLDER_LINEAR_SIZE, Double.toString(decomListFolderDetail.getFolderLinearSize()));
		}

		map.put(DecommissioningListImportExtension.IS_PLACED_IN_CONTAINER, Boolean.toString(decomListFolderDetail.isPlacedInContainer()));

		return map;
	}


	private void manageRetentionRule(OnWriteRecordParams params) {
		RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		RetentionRule retentionRule = rm.wrapRetentionRule(params.getRecord());

		List<Map<String, String>> importedCopyRetentionRules = new ArrayList<>();

		for (CopyRetentionRule copyRetentionRule : retentionRule.getCopyRetentionRules()) {
            //copyRetentionRule.etc

            Map<String, String> map = writeCopyRetentionRule(rm, copyRetentionRule, RetentionRuleImportExtension.RULES_TYPE_FOLDER);

            importedCopyRetentionRules.add(map);
        }

		params.getModifiableImportRecord().addField(RetentionRule.COPY_RETENTION_RULES, importedCopyRetentionRules);

		List<Map<String, String>> documentCopyRetentionRules = new ArrayList<>();

		for (CopyRetentionRule copyRetentionRule : retentionRule.getDocumentCopyRetentionRules()) {
			documentCopyRetentionRules.add(writeCopyRetentionRule(rm, copyRetentionRule, RetentionRuleImportExtension.RULES_TYPE_DOCUMENTS));
		}

		params.getModifiableImportRecord().addField(RetentionRule.DOCUMENT_COPY_RETENTION_RULES, documentCopyRetentionRules);

		if(retentionRule.getPrincipalDefaultDocumentCopyRetentionRule() != null)
		{
			params.getModifiableImportRecord().addField(RetentionRule.PRINCIPAL_DEFAULT_DOCUMENT_COPY_RETENTION_RULE,
					writeCopyRetentionRule(rm, retentionRule.getPrincipalDefaultDocumentCopyRetentionRule(),
							RetentionRuleImportExtension.RULES_TYPE_DOCUMENTS));
		}

		if(retentionRule.getSecondaryDefaultDocumentCopyRetentionRule() != null) {
			params.getModifiableImportRecord().addField(RetentionRule.SECONDARY_DEFAULT_DOCUMENT_COPY_RETENTION_RULE,
					writeCopyRetentionRule(rm, retentionRule.getSecondaryDefaultDocumentCopyRetentionRule(),
							RetentionRuleImportExtension.RULES_TYPE_DOCUMENTS));
		}
	}

	private Map<String, String> writeCopyRetentionRule(RMSchemasRecordsServices rm, CopyRetentionRule copyRetentionRule, String ruleType) {
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
		map.put(RetentionRuleImportExtension.ESSENTIAL, Boolean.toString(copyRetentionRule.isEssential()));
		map.put(RetentionRuleImportExtension.COPY_RETENTION_RULE_ID, copyRetentionRule.getId());
		map.put(RetentionRuleImportExtension.MEDIUM_TYPES, StringUtils.join(mediumTypesCodes, ','));
		map.put(RetentionRuleImportExtension.IGNORE_ACTIVE_PERIOD, Boolean.toString(copyRetentionRule.isIgnoreActivePeriod()));

		if (copyRetentionRule.getTypeId() != null) {
			if(RetentionRuleImportExtension.RULES_TYPE_FOLDER.equals(ruleType))
			{
				map.put(RetentionRuleImportExtension.TYPE_ID, rm.getFolderType(copyRetentionRule.getTypeId()).getCode());
			} else {
				map.put(RetentionRuleImportExtension.TYPE_ID, rm.getDocumentType(copyRetentionRule.getTypeId()).getCode());
			}
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
