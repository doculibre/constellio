/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.rm.extensions.imports;

import static com.constellio.data.utils.LangUtils.asMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.services.schemas.bulkImport.ImportDataErrors;
import com.constellio.model.entities.records.wrappers.RecordWrapperRuntimeException.WrappedRecordMustBeNotNull;
import com.constellio.model.extensions.behaviors.RecordImportExtension;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.model.extensions.events.recordsImport.PrevalidationParams;
import com.constellio.model.extensions.events.recordsImport.ValidationParams;
import com.constellio.model.services.factories.ModelLayerFactory;

public class RetentionRuleImportExtension extends RecordImportExtension {

	public static final String INVALID_STRING_VALUE = "invalidStringValue";
	public static final String INVALID_NUMBER_VALUE = "invalidNumberValue";
	public static final String INVALID_CODE_VALUE = "invalidCodeValue";
	public static final String INVALID_DATE_VALUE = "invalidDateValue";
	public static final String INVALID_ENUM_VALUE = "invalidEnumValue";
	public static final String MISSING_METADATA = "missingMetadata";
	public static final String REQUIRED_VALUE = "requiredValue";

	public static final String SEMI_ACTIVE_RETENTION_PERIOD_COMMENT = "semiActiveRetentionPeriodComment";
	public static final String ACTIVE_RETENTION_PERIOD_COMMENT = "activeRetentionPeriodComment";
	public static final String SEMI_ACTIVE_RETENTION_PERIOD = "semiActiveRetentionPeriod";
	public static final String INACTIVE_DISPOSAL_COMMENT = "inactiveDisposalComment";
	public static final String COPY_RETENTION_RULE_INDEX = "copyRetentionRuleIndex";
	public static final String ACTIVE_RETENTION_PERIOD = "activeRetentionPeriod";
	public static final String INACTIVE_DISPOSAL_TYPE = "inactiveDisposalType";
	public static final String CONTENT_TYPES_COMMENT = "contentTypesComment";
	public static final String DOCUMENT_TYPE_INDEX = "documentTypeIndex";
	public static final String ARCHIVISTIC_STATUS = "archivisticStatus";
	public static final String MEDIUM_TYPES = "mediumTypes";
	public static final String COPY_TYPE = "copyType";
	public static final String CODE = "code";

	private final RMSchemasRecordsServices rm;

	public RetentionRuleImportExtension(String collection, ModelLayerFactory modelLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	@Override
	public String getDecoratedSchemaType() {
		return RetentionRule.SCHEMA_TYPE;
	}

	@Override
	public void prevalidate(PrevalidationParams params) {
		int index = 0;
		List<Map<String, String>> copyRetentionRules = params.getImportRecord().getList(RetentionRule.COPY_RETENTION_RULES);
		boolean copyRetentionRulePrincipalAndNotSortExist = false;
		for (Map<String, String> copyRetentionRule : copyRetentionRules) {
			copyRetentionRulePrincipalAndNotSortExist |= prevalidateCopyRetentionRule(copyRetentionRule, params.getErrors(),
					String.valueOf(index));
			index++;
		}

		index = 0;
		List<Map<String, String>> documentTypes = params.getImportRecord().getList(RetentionRule.DOCUMENT_TYPES_DETAILS);
		for (Map<String, String> documentType : documentTypes) {
			prevalidateDocumentType(documentType, params.getErrors(), String.valueOf(index),
					copyRetentionRulePrincipalAndNotSortExist);
			index++;
		}
	}

	@Override
	public void validate(ValidationParams params) {
		int index = 0;

		DocumentTypeResolver documentTypeResolver = new DocumentTypeResolver(rm);
		MediumTypeResolver mediumTypeResolver = new MediumTypeResolver(rm);

		List<Map<String, String>> documentTypes = params.getImportRecord().getList(RetentionRule.DOCUMENT_TYPES_DETAILS);
		for (Map<String, String> documentType : documentTypes) {
			validateDocumentType(documentTypeResolver, documentType, params.getErrors(), String.valueOf(index));
			index++;
		}

		List<Map<String, String>> copyRetentionRules = params.getImportRecord().getList(RetentionRule.COPY_RETENTION_RULES);

		for (Map<String, String> copyRetentionRule : copyRetentionRules) {
			String value = (String) copyRetentionRule.get(MEDIUM_TYPES);
			String[] mediumTypes = value.split(",");
			for (String code : mediumTypes) {
				if (mediumTypeResolver.getMediumTypeByCode(code) == null) {
					params.getErrors()
							.error(INVALID_CODE_VALUE, asMap(MEDIUM_TYPES, code, COPY_RETENTION_RULE_INDEX, "" + index));
				}
			}
		}
	}

	private boolean prevalidateCopyRetentionRule(Map<String, String> copyRetentionRule, ImportDataErrors errors, String index) {
		boolean copyRetentionRulePrincipalAndNotSortExist = false;
		if (copyRetentionRule.containsKey(CODE)) {
			String value = copyRetentionRule.get(CODE);
			if (value.isEmpty()) {
				errors.error(INVALID_STRING_VALUE, copyRetentionRule);
			}
		} else {
			errors.error(MISSING_METADATA, asMap("value", CODE, COPY_RETENTION_RULE_INDEX, index));
		}

		if (copyRetentionRule.containsKey(INACTIVE_DISPOSAL_TYPE)) {
			String value = copyRetentionRule.get(INACTIVE_DISPOSAL_TYPE).toUpperCase();
			if (value.isEmpty() || !(isDisposalTypeValid(value))) {
				errors.error(INVALID_ENUM_VALUE, asMap(INACTIVE_DISPOSAL_TYPE, value, COPY_RETENTION_RULE_INDEX, index));
			}
		}

		if (copyRetentionRule.containsKey(COPY_TYPE)) {
			String value = copyRetentionRule.get(COPY_TYPE).toUpperCase();
			if (value.isEmpty() || (!value.equals("P") && !value.equals("S"))) {
				errors.error(INVALID_ENUM_VALUE, asMap(MEDIUM_TYPES, value, COPY_RETENTION_RULE_INDEX, index));
			} else if (value.equals("P") && copyRetentionRule.containsKey(INACTIVE_DISPOSAL_TYPE) && !copyRetentionRule
					.get(INACTIVE_DISPOSAL_TYPE).equals("T")) {
				copyRetentionRulePrincipalAndNotSortExist = true;
			}
		} else {
			errors.error(MISSING_METADATA, asMap("value", COPY_TYPE, COPY_RETENTION_RULE_INDEX, index));
		}

		if (copyRetentionRule.containsKey(MEDIUM_TYPES)) {
			String value = copyRetentionRule.get(MEDIUM_TYPES);
			if (value.isEmpty()) {
				errors.error(INVALID_CODE_VALUE, asMap(MEDIUM_TYPES, "empty", COPY_RETENTION_RULE_INDEX, index));
			}
		} else {
			errors.error(MISSING_METADATA, asMap("value", MEDIUM_TYPES, COPY_RETENTION_RULE_INDEX, index));
		}

		if (copyRetentionRule.containsKey(ACTIVE_RETENTION_PERIOD)) {
			String value = copyRetentionRule.get(ACTIVE_RETENTION_PERIOD);
			if (value.isEmpty()) {
				errors.error(REQUIRED_VALUE, copyRetentionRule);
			} else {
				try {
					int convertedValue = Integer.valueOf(value);
					if (convertedValue < 0) {
						errors.error(INVALID_NUMBER_VALUE, asMap("value", value, DOCUMENT_TYPE_INDEX, index));
					}
				} catch (NullPointerException | NumberFormatException np) {
					errors.error(INVALID_NUMBER_VALUE, asMap("value", value, DOCUMENT_TYPE_INDEX, index));
				}
			}
		} else {
			errors.error(MISSING_METADATA, asMap("value", ACTIVE_RETENTION_PERIOD, COPY_RETENTION_RULE_INDEX, index));
		}

		if (copyRetentionRule.containsKey(SEMI_ACTIVE_RETENTION_PERIOD)) {
			if (copyRetentionRule.get(SEMI_ACTIVE_RETENTION_PERIOD) == null) {
				errors.error(INVALID_DATE_VALUE, asMap("value", "null", COPY_RETENTION_RULE_INDEX, index));
			}
		} else {
			errors.error(MISSING_METADATA, asMap("value", SEMI_ACTIVE_RETENTION_PERIOD, COPY_RETENTION_RULE_INDEX, index));
		}

		return copyRetentionRulePrincipalAndNotSortExist;
	}

	private void prevalidateDocumentType(Map<String, String> documentType, ImportDataErrors errors, String index,
			boolean copyRetentionRulePrincipalAndNotSortExist) {

		if (!copyRetentionRulePrincipalAndNotSortExist) {
			if (documentType.containsKey(ARCHIVISTIC_STATUS)) {
				String value = documentType.get(ARCHIVISTIC_STATUS).toUpperCase();
				if (value.isEmpty() || !(isDisposalTypeValid(value))) {
					errors.error(INVALID_ENUM_VALUE, asMap(ARCHIVISTIC_STATUS, value, DOCUMENT_TYPE_INDEX, index));
				}
			} else {
				errors.error(MISSING_METADATA, asMap("value", ARCHIVISTIC_STATUS, DOCUMENT_TYPE_INDEX, index));
			}
		}

		if (!documentType.containsKey(CODE)) {
			errors.error(MISSING_METADATA, asMap("value", CODE, DOCUMENT_TYPE_INDEX, index));
		}
	}

	private void validateDocumentType(DocumentTypeResolver resolver, Map<String, String> documentType, ImportDataErrors errors,
			String index) {

		String value = documentType.get(CODE);
		if (value.isEmpty()) {
			errors.error(INVALID_CODE_VALUE, asMap("value", "empty", DOCUMENT_TYPE_INDEX, index));
		} else {
			String documentTypeId = null;
			try {
				documentTypeId = resolver.getDocumentTypeByCode(value);
			} catch (WrappedRecordMustBeNotNull | NullPointerException re) {
			}
			if (documentTypeId == null) {
				errors.error(INVALID_CODE_VALUE, asMap(CODE, value, DOCUMENT_TYPE_INDEX, index));
			}
		}
	}

	@Override
	public void build(BuildParams buildParams) {
		DocumentTypeResolver documentTypeResolver = new DocumentTypeResolver(rm);
		MediumTypeResolver mediumTypeResolver = new MediumTypeResolver(rm);
		List<Map<String, String>> copyRetentionRules = buildParams.getImportRecord().getList(RetentionRule.COPY_RETENTION_RULES);
		List<Map<String, String>> documentTypes = buildParams.getImportRecord().getList(RetentionRule.DOCUMENT_TYPES_DETAILS);

		RetentionRule retentionRule = new RetentionRule(buildParams.getRecord(), buildParams.getTypes());
		List<CopyRetentionRule> copyRetentionRuleList = new ArrayList<>();

		for (Map<String, String> copyRetentionRule : copyRetentionRules) {
			copyRetentionRuleList.add(buildCopyRetentionRule(mediumTypeResolver, copyRetentionRule));
		}

		Collections.sort(copyRetentionRuleList, new Comparator<CopyRetentionRule>() {
			@Override
			public int compare(CopyRetentionRule o1, CopyRetentionRule o2) {
				return o1.getCopyType().compareTo(o2.getCopyType());
			}
		});
		retentionRule.setCopyRetentionRules(copyRetentionRuleList);

		List<RetentionRuleDocumentType> documentTypeList = new ArrayList<>();
		for (Map<String, String> documentType : documentTypes) {
			documentTypeList.add(buildDocumentType(documentTypeResolver, documentType));
		}

		retentionRule.setDocumentTypesDetails(documentTypeList);
	}

	private CopyRetentionRule buildCopyRetentionRule(MediumTypeResolver resolver, Map<String, String> mapCopyRetentionRule) {
		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();

		copyRetentionRule.setCode(mapCopyRetentionRule.get(CODE));

		CopyType copyType = (mapCopyRetentionRule.get(COPY_TYPE).toUpperCase()).equals("P") ?
				CopyType.PRINCIPAL :
				CopyType.SECONDARY;
		copyRetentionRule.setCopyType(copyType);

		List<String> mediumTypesId = getMediumTypesId(resolver, mapCopyRetentionRule.get(MEDIUM_TYPES));
		copyRetentionRule.setMediumTypeIds(mediumTypesId);

		if (!mapCopyRetentionRule.get(CONTENT_TYPES_COMMENT).equals("")) {
			copyRetentionRule.setContentTypesComment(mapCopyRetentionRule.get(CONTENT_TYPES_COMMENT));
		}

		int activeRetentionPeriod = Integer.parseInt(mapCopyRetentionRule.get(ACTIVE_RETENTION_PERIOD));
		if (activeRetentionPeriod == 999) {
			copyRetentionRule.setActiveRetentionPeriod(RetentionPeriod.OPEN_999);
		} else if (activeRetentionPeriod == 888) {
			copyRetentionRule.setActiveRetentionPeriod(RetentionPeriod.OPEN_888);
		} else {
			copyRetentionRule.setActiveRetentionPeriod(RetentionPeriod.fixed(activeRetentionPeriod));
		}

		if (!mapCopyRetentionRule.get(ACTIVE_RETENTION_PERIOD_COMMENT).equals("")) {
			copyRetentionRule.setActiveRetentionComment(mapCopyRetentionRule.get(ACTIVE_RETENTION_PERIOD_COMMENT));
		}

		int semiActiveRetentionPeriod = Integer.parseInt(mapCopyRetentionRule.get(SEMI_ACTIVE_RETENTION_PERIOD));
		if (semiActiveRetentionPeriod == 999) {
			copyRetentionRule.setSemiActiveRetentionPeriod(RetentionPeriod.OPEN_999);
		} else if (semiActiveRetentionPeriod == 888) {
			copyRetentionRule.setSemiActiveRetentionPeriod(RetentionPeriod.OPEN_888);
		} else {
			copyRetentionRule.setSemiActiveRetentionPeriod(RetentionPeriod.fixed(semiActiveRetentionPeriod));
		}

		if (!mapCopyRetentionRule.get(SEMI_ACTIVE_RETENTION_PERIOD_COMMENT).equals("")) {
			copyRetentionRule.setSemiActiveRetentionComment(mapCopyRetentionRule.get(SEMI_ACTIVE_RETENTION_PERIOD_COMMENT));
		}

		DisposalType disposalType = disposalTypeFromString(mapCopyRetentionRule.get(INACTIVE_DISPOSAL_TYPE).toUpperCase());
		copyRetentionRule.setInactiveDisposalType(disposalType);

		if (!mapCopyRetentionRule.get(INACTIVE_DISPOSAL_COMMENT).equals("")) {
			copyRetentionRule.setInactiveDisposalComment(mapCopyRetentionRule.get(INACTIVE_DISPOSAL_COMMENT));
		}

		return copyRetentionRule;
	}

	private RetentionRuleDocumentType buildDocumentType(DocumentTypeResolver resolver, Map<String, String> documentType) {
		RetentionRuleDocumentType retentionRuleDocumentType = new RetentionRuleDocumentType();

		if (!documentType.get(CODE).isEmpty()) {
			retentionRuleDocumentType.setDocumentTypeId(resolver.getDocumentTypeByCode(documentType.get(CODE)));
		}

		DisposalType disposalType = disposalTypeFromString(documentType.get(ARCHIVISTIC_STATUS).toUpperCase());

		retentionRuleDocumentType.setDisposalType(disposalType);

		return retentionRuleDocumentType;
	}

	private DisposalType disposalTypeFromString(String disposalType) {
		switch (disposalType) {
		case "T":
			return DisposalType.SORT;
		case "D":
			return DisposalType.DESTRUCTION;
		case "C":
			return DisposalType.DEPOSIT;
		default:
			//errors.error(INVALID_ENUM_VALUE, asMap(INACTIVE_DISPOSAL_TYPE, disposalType));
			throw new RuntimeException("Invalid disposal type: " + disposalType);
		}
	}

	private boolean isDisposalTypeValid(String value) {
		return value.equals("T") || value.equals("D") || value.equals("C");
	}

	private List<String> getMediumTypesId(MediumTypeResolver resolver, String mediumTypesString) {
		String[] mediumTypesInTab = mediumTypesString.split(",");
		List<String> mediumTypes = new ArrayList<>();

		for (String mediumType : mediumTypesInTab) {
			if (resolver.getMediumTypeByCode(mediumType) != null) {
				mediumTypes.add(resolver.getMediumTypeByCode(mediumType));
			}
		}
		return mediumTypes;
	}

	private static class MediumTypeResolver {

		Map<String, String> typesByCode = new HashMap<>();

		RMSchemasRecordsServices rm;

		private MediumTypeResolver(RMSchemasRecordsServices rm) {
			this.rm = rm;
		}

		public String getMediumTypeByCode(String code) {
			if (typesByCode.containsKey(code)) {
				return typesByCode.get(code);
			} else {
				MediumType type = rm.getMediumTypeByCode(code);
				String typeId = type == null ? null : type.getId();
				typesByCode.put(code, typeId);
				return typeId;
			}
		}
	}

	private static class DocumentTypeResolver {

		Map<String, String> typesByCode = new HashMap<>();

		RMSchemasRecordsServices rm;

		private DocumentTypeResolver(RMSchemasRecordsServices rm) {
			this.rm = rm;
		}

		public String getDocumentTypeByCode(String code) {
			if (typesByCode.containsKey(code)) {
				return typesByCode.get(code);
			} else {
				DocumentType type = rm.getDocumentTypeByCode(code);
				String typeId = type == null ? null : type.getId();
				typesByCode.put(code, typeId);
				return typeId;
			}
		}
	}
}
