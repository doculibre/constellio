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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordWrapperRuntimeException.WrappedRecordMustBeNotNull;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.extensions.behaviors.RecordImportExtension;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.bulkImport.ImportDataErrors;
import com.constellio.model.services.records.bulkImport.data.ImportData;

// TODO Refactorisation
public class RetentionRuleImportExtension implements RecordImportExtension {

	public static final String INVALID_RESOLVER_METADATA_CODE = "invalidResolverMetadataCode";
	public static final String INVALID_METADATA_CODE = "invalidMetadataCode";
	public static final String INVALID_SCHEMA_CODE = "invalidSchemaCode";
	public static final String LEGACY_ID_NOT_UNIQUE = "legacyIdNotUnique";
	public static final String VALUE_NOT_UNIQUE = "valueNotUnique";

	public static final String REQUIRED_VALUE = "requiredValue";
	public static final String INVALID_SINGLEVALUE = "invalidSinglevalue";
	public static final String INVALID_MULTIVALUE = "invalidMultivalue";
	public static final String INVALID_STRING_VALUE = "invalidStringValue";
	public static final String INVALID_NUMBER_VALUE = "invalidNumberValue";
	public static final String INVALID_CONTENT_VALUE = "invalidContentValue";
	public static final String INVALID_BOOLEAN_VALUE = "invalidBooleanValue";
	public static final String INVALID_DATE_VALUE = "invalidDateValue";
	public static final String INVALID_DATETIME_VALUE = "invalidDatetimeValue";
	public static final String INVALID_ENUM_VALUE = "invalidEnumValue";
	public static final String INVALID_ID_VALUE = "invalidIdValue";

	private RMSchemasRecordsServices rm;
	private boolean copyRetentionRulePrincipalAndNotSortExist = false;

	public RetentionRuleImportExtension(String collection, ModelLayerFactory modelLayerFactory) {
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	@Override
	public String getDecoratedSchemaType() {
		return RetentionRule.SCHEMA_TYPE;
	}

	@Override
	public void prevalidate(ImportDataErrors errors, ImportData importRecord) {
		List<Map<String, String>> copyRetentionRules = importRecord.getList(RetentionRule.COPY_RETENTION_RULES);
		for (Map<String, String> copyRetentionRule : copyRetentionRules) {
			validateCopyRetentionRule(copyRetentionRule, errors);
		}
	}

	@Override
	public void validate(ImportDataErrors errors, ImportData importRecord) {
		List<Map<String, String>> documentTypes = importRecord.getList(RetentionRule.DOCUMENT_TYPES_DETAILS);
		for (Map<String, String> documentType : documentTypes) {
			validateDocumentType(documentType, errors);
		}

	}

	private void validateCopyRetentionRule(Map<String, String> copyRetentionRule, ImportDataErrors errors) {
		if (copyRetentionRule.containsKey("code")) {
			String value = copyRetentionRule.get("code");
			if (value.isEmpty()) {
				errors.error(INVALID_STRING_VALUE, copyRetentionRule);
			}
		} else {
			errors.error(INVALID_METADATA_CODE, copyRetentionRule);
		}

		if (copyRetentionRule.containsKey("copyType")) {
			String value = copyRetentionRule.get("copyType").toUpperCase();
			if (value.equals("P") && !copyRetentionRule.get("inactiveDisposalType").equals("T")) {
				copyRetentionRulePrincipalAndNotSortExist = true;
			}
			if (value.isEmpty() || (!value.equals("P") && !value.equals("S"))) {
				errors.error(INVALID_ENUM_VALUE, copyRetentionRule);
			}
		} else {
			errors.error(INVALID_METADATA_CODE, copyRetentionRule);
		}

		if (copyRetentionRule.containsKey("mediumTypes")) {
			String value = copyRetentionRule.get("mediumTypes");
			if (value.isEmpty()) {
				errors.error(INVALID_ENUM_VALUE, copyRetentionRule);
			} else {
				String[] mediumTypes = value.split(",");
				for (String mediumType : mediumTypes) {
					if (rm.getMediumTypeByCode(mediumType) == null) {
						errors.error(INVALID_ENUM_VALUE, copyRetentionRule);
					}
				}
			}
		} else {
			errors.error(INVALID_METADATA_CODE, copyRetentionRule);
		}

		if (copyRetentionRule.containsKey("activeRetentionPeriod")) {
			String value = copyRetentionRule.get("activeRetentionPeriod");
			//TODO devrait verifier si c'est un code ou un nombre d'année
			if (value.isEmpty()) {
				errors.error(INVALID_NUMBER_VALUE, copyRetentionRule);
			}
		} else {
			errors.error(INVALID_METADATA_CODE, copyRetentionRule);
		}

		if (copyRetentionRule.containsKey("semiActiveRetentionPeriod")) {
			if (copyRetentionRule.get("semiActiveRetentionPeriod") == null) {
				errors.error(INVALID_DATE_VALUE, copyRetentionRule);
			}
		} else {
			errors.error(INVALID_DATE_VALUE, copyRetentionRule);
		}

		if (copyRetentionRule.containsKey("inactiveDisposalType")) {
			String value = copyRetentionRule.get("inactiveDisposalType").toUpperCase();
			if (value.isEmpty() || (!value.equals("C") && !value.equals("D") && !value.equals("T"))) {
				errors.error(INVALID_ENUM_VALUE, copyRetentionRule);
			}
		}

	}

	private void validateDocumentType(Map<String, String> documentType, ImportDataErrors errors) {
		if (!copyRetentionRulePrincipalAndNotSortExist) {
			if (documentType.containsKey("archivisticStatus")) {
				String value = documentType.get("archivisticStatus").toUpperCase();
				if (value.isEmpty() || (!value.equals("C") && !value.equals("D") && !value
						.equals("T"))) {
					errors.error(INVALID_ENUM_VALUE, documentType);
				}
			} else {
				errors.error(INVALID_METADATA_CODE, documentType);
			}
		}

		if (documentType.containsKey("code")) {
			String value = documentType.get("code");
			if (value.isEmpty()) {
				errors.error(INVALID_STRING_VALUE, documentType);
			} else {
				try {
					rm.getDocumentTypeByCode(value).getId();
				} catch (WrappedRecordMustBeNotNull | NullPointerException re) {
					errors.error(INVALID_ID_VALUE + ": " + value, documentType);
				}
			}
		} else {
			errors.error(INVALID_METADATA_CODE, documentType);
		}
	}

	@Override
	public void build(Record record, MetadataSchemaTypes types, ImportData importRecord) {
		List<Map<String, String>> copyRetentionRules = importRecord.getList(RetentionRule.COPY_RETENTION_RULES);
		List<Map<String, String>> documentTypes = importRecord.getList(RetentionRule.DOCUMENT_TYPES_DETAILS);

		RetentionRule retentionRule = new RetentionRule(record, types);
		List<CopyRetentionRule> copyRetentionRuleList = new ArrayList<>();

		for (Map<String, String> copyRetentionRule : copyRetentionRules) {
			copyRetentionRuleList.add(buildCopyRetentionRule(copyRetentionRule));
		}

		retentionRule.setCopyRetentionRules(copyRetentionRuleList);

		List<RetentionRuleDocumentType> documentTypeList = new ArrayList<>();
		for (Map<String, String> documentType : documentTypes) {
			documentTypeList.add(buildDocumentType(documentType));
		}

		retentionRule.setDocumentTypesDetails(documentTypeList);
	}

	private CopyRetentionRule buildCopyRetentionRule(Map<String, String> MapCopyRetentionRule) {
		CopyRetentionRule copyRetentionRule = new CopyRetentionRule();

		copyRetentionRule.setCode(MapCopyRetentionRule.get("code"));

		CopyType copyType = (MapCopyRetentionRule.get("copyType").toUpperCase()).equals("P") ?
				CopyType.PRINCIPAL :
				CopyType.SECONDARY;
		copyRetentionRule.setCopyType(copyType);

		List<String> mediumTypesId = getMediumTypesId(MapCopyRetentionRule.get("mediumTypes"));
		copyRetentionRule.setMediumTypeIds(mediumTypesId);

		if (!MapCopyRetentionRule.get("contentTypesComment").equals("")) {
			copyRetentionRule.setContentTypesComment(MapCopyRetentionRule.get("contentTypesComment"));
		}

		int activeRetentionPeriod = Integer.parseInt(MapCopyRetentionRule.get("activeRetentionPeriod"));
		if (activeRetentionPeriod == 999) {
			copyRetentionRule.setActiveRetentionPeriod(RetentionPeriod.OPEN_999);
		} else if (activeRetentionPeriod == 888) {
			copyRetentionRule.setActiveRetentionPeriod(RetentionPeriod.OPEN_888);
		} else {
			copyRetentionRule.setActiveRetentionPeriod(RetentionPeriod.fixed(activeRetentionPeriod));
		}

		if (!MapCopyRetentionRule.get("activeRetentionPeriodComment").equals("")) {
			copyRetentionRule.setActiveRetentionComment(MapCopyRetentionRule.get("activeRetentionPeriodComment"));
		}

		int semiActiveRetentionPeriod = Integer.parseInt(MapCopyRetentionRule.get("activeRetentionPeriod"));
		if (semiActiveRetentionPeriod == 999) {
			copyRetentionRule.setActiveRetentionPeriod(RetentionPeriod.OPEN_999);
		} else if (semiActiveRetentionPeriod == 888) {
			copyRetentionRule.setActiveRetentionPeriod(RetentionPeriod.OPEN_888);
		} else {
			copyRetentionRule.setActiveRetentionPeriod(RetentionPeriod.fixed(semiActiveRetentionPeriod));
		}

		if (!MapCopyRetentionRule.get("semiActiveRetentionPeriodComment").equals("")) {
			copyRetentionRule.setSemiActiveRetentionComment(MapCopyRetentionRule.get("semiActiveRetentionPeriodComment"));
		}

		DisposalType disposalType = disposalTypeFromString(MapCopyRetentionRule.get("inactiveDisposalType").toUpperCase());
		copyRetentionRule.setInactiveDisposalType(disposalType);

		if (!MapCopyRetentionRule.get("inactiveDisposalComment").equals("")) {
			copyRetentionRule.setInactiveDisposalComment(MapCopyRetentionRule.get("inactiveDisposalComment"));
		}

		return copyRetentionRule;
	}

	private RetentionRuleDocumentType buildDocumentType(Map<String, String> documentType) {
		RetentionRuleDocumentType retentionRuleDocumentType = new RetentionRuleDocumentType();

		if (!documentType.get("code").isEmpty()) {
			retentionRuleDocumentType.setDocumentTypeId(rm.getDocumentTypeByCode(documentType.get("code")).getId());
		}

		DisposalType disposalType = disposalTypeFromString(documentType.get("archivisticStatus").toUpperCase());
		retentionRuleDocumentType.setDisposalType(disposalType);

		return retentionRuleDocumentType;
	}

	private DisposalType disposalTypeFromString(String inactiveDisposalType) {
		switch (inactiveDisposalType) {
		case "T":
			return DisposalType.SORT;
		case "D":
			return DisposalType.DESTRUCTION;
		case "C":
			return DisposalType.DEPOSIT;
		default:
			throw new RuntimeException();
		}
	}

	private List<String> getMediumTypesId(String mediumTypesString) {
		String[] mediumTypesInTab = mediumTypesString.split(",");
		List<String> mediumTypes = new ArrayList<>();

		for (String mediumType : mediumTypesInTab) {
			if (rm.getMediumTypeByCode(mediumType) != null) {
				mediumTypes.add(rm.getMediumTypeByCode(mediumType).getId());
			}
		}
		return mediumTypes;
	}
}
