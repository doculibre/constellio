package com.constellio.app.modules.rm.extensions.imports;

import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.FolderStatus;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.modules.rm.wrappers.type.YearType;
import com.constellio.app.services.schemas.bulkImport.RecordsImportServices;
import com.constellio.model.entities.records.wrappers.RecordWrapperRuntimeException.WrappedRecordMustBeNotNull;
import com.constellio.model.extensions.behaviors.RecordImportExtension;
import com.constellio.model.extensions.events.recordsImport.BuildParams;
import com.constellio.model.extensions.events.recordsImport.PrevalidationParams;
import com.constellio.model.extensions.events.recordsImport.ValidationParams;
import com.constellio.model.frameworks.validation.DecoratedValidationsErrors;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.utils.EnumWithSmallCodeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.constellio.app.services.schemas.bulkImport.RecordsImportValidator.INVALID_NUMBER_VALUE;
import static org.apache.commons.lang3.StringUtils.join;

public class RetentionRuleImportExtension extends RecordImportExtension {

	public static final String INVALID_MEDIUM_TYPE_CODE = "invalidMediumType";
	public static final String INVALID_DOCUMENT_TYPE_CODE = "invalidDocumentType";
	public static final String INVALID_COPYRULE_ENUM_FIELD = "invalidCopyRuleEnumField";
	public static final String INVALID_COPYRULE_NUMBER_FIELD = "invalidCopyRuleNumberField";
	public static final String INVALID_DOCUMENT_TYPE_ENUM_FIELD = "invalidDocumentTypeEnumField";
	public static final String REQUIRED_COPYRULE_FIELD = "requiredCopyRuleField";
	public static final String REQUIRED_DOCUMENT_TYPE_FIELD = "requiredDocumentTypeField";

	public static final String SEMI_ACTIVE_YEAR_TYPE = "semiActiveYearType";
	public static final String INACTIVE_YEAR_TYPE = "inactiveYearType";

	public static final String SEMI_ACTIVE_RETENTION_PERIOD_COMMENT = "semiActiveRetentionComment";
	public static final String ACTIVE_RETENTION_PERIOD_COMMENT = "activeRetentionComment";
	public static final String SEMI_ACTIVE_RETENTION_PERIOD = "semiActiveRetentionPeriod";
	public static final String INACTIVE_DISPOSAL_COMMENT = "inactiveDisposalComment";
	public static final String ACTIVE_RETENTION_PERIOD = "activeRetentionPeriod";
	public static final String IGNORE_ACTIVE_PERIOD = "ignoreActivePeriod";
	public static final String INACTIVE_DISPOSAL_TYPE = "inactiveDisposalType";
	public static final String CONTENT_TYPES_COMMENT = "contentTypesComment";
	public static final String ARCHIVISTIC_STATUS = "archivisticStatus";
	public static final String MEDIUM_TYPES = "mediumTypes";
	public static final String COPY_TYPE = "copyType";
	public static final String CODE = "code";
	public static final String COPY_RETENTION_RULE_ID = "id";
	public static final String OPEN_ACTIVE_RETENTION_PERIOD = "openActiveRetentionPeriod";
	public static final String ACTIVE_DATE_METADATA = "activeDateMetadata";
	public static final String SEMI_ACTIVE_DATE_METADATA = "semiActiveDateMetadata";
	public static final String TYPE_ID = "typeId";
	public static final String RULES_TYPE_DOCUMENTS = "documentRules";
	public static final String RULES_TYPE_FOLDER = "folderRules";
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String ESSENTIAL = "essential";

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
			if (hasValue(copyRetentionRule)) {
				final String strIndex = String.valueOf(index);
				ValidationErrors errors = decorateForIndex(params.getErrors(), index);
				copyRetentionRulePrincipalAndNotSortExist |= prevalidateCopyRetentionRule(copyRetentionRule, errors);
			}
			index++;
		}

		index = 0;
		List<Map<String, String>> documentTypes = params.getImportRecord().getList(RetentionRule.DOCUMENT_TYPES_DETAILS);
		for (Map<String, String> documentType : documentTypes) {
			if (hasValue(documentType)) {
				ValidationErrors errors = decorateForIndex(params.getErrors(), index);
				prevalidateDocumentType(documentType, errors, copyRetentionRulePrincipalAndNotSortExist);
			}
			index++;
		}
	}

	private boolean hasValue(Map<String, String> copyRetentionRule) {
		for (Map.Entry<String, String> entry : copyRetentionRule.entrySet()) {
			if (StringUtils.isNotBlank(entry.getValue()) && !"null".equals(entry.getValue().toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	ValidationErrors decorateForIndex(ValidationErrors errors, int index) {
		final String strIndex = String.valueOf(index);
		return new DecoratedValidationsErrors(errors) {
			@Override
			public void buildExtraParams(Map<String, Object> params) {
				params.put("index", strIndex);
			}
		};
	}

	@Override
	public void validate(ValidationParams params) {
		int index = 0;

		DocumentTypeResolver documentTypeResolver = new DocumentTypeResolver(rm);
		MediumTypeResolver mediumTypeResolver = new MediumTypeResolver(rm);

		List<Map<String, String>> documentTypes = params.getImportRecord().getList(RetentionRule.DOCUMENT_TYPES_DETAILS);
		for (Map<String, String> documentType : documentTypes) {
			ValidationErrors errors = decorateForIndex(params.getErrors(), index);
			validateDocumentType(documentTypeResolver, documentType, errors);
			index++;
		}

		List<Map<String, String>> copyRetentionRules = params.getImportRecord().getList(RetentionRule.COPY_RETENTION_RULES);

		index = 0;
		for (Map<String, String> copyRetentionRule : copyRetentionRules) {
			if (hasValue(copyRetentionRule)) {
				ValidationErrors errors = decorateForIndex(params.getErrors(), index);
				String value = (String) copyRetentionRule.get(MEDIUM_TYPES);
				String[] mediumTypes = value.split(",");
				for (String code : mediumTypes) {
					if (mediumTypeResolver.getMediumTypeByCode(code) == null) {
						errors.add(RetentionRuleImportExtension.class, INVALID_MEDIUM_TYPE_CODE, asMap("value", code));
					}
				}
			}
			index++;
		}
	}

	private boolean prevalidateCopyRetentionRule(Map<String, String> copyRetentionRule, ValidationErrors errors) {
		boolean copyRetentionRulePrincipalAndNotSortExist = false;

		if (copyRetentionRule.containsKey(INACTIVE_DISPOSAL_TYPE)) {
			String value = copyRetentionRule.get(INACTIVE_DISPOSAL_TYPE) != null ?
						   copyRetentionRule.get(INACTIVE_DISPOSAL_TYPE).toUpperCase() : "";
			if (value.isEmpty() || !(isDisposalTypeValid(value))) {
				addInvalidCopyRuleEnumValueError(errors, value, INACTIVE_DISPOSAL_TYPE, DisposalType.class);
			}
		}

		if (copyRetentionRule.containsKey(COPY_TYPE)) {
			String value = copyRetentionRule.get(COPY_TYPE) != null ? copyRetentionRule.get(COPY_TYPE).toUpperCase() : "";
			if (value.isEmpty() || (!value.equals("P") && !value.equals("S"))) {
				addInvalidCopyRuleEnumValueError(errors, value, COPY_TYPE, CopyType.class);
			} else if (value.equals("P") && copyRetentionRule.containsKey(INACTIVE_DISPOSAL_TYPE) && !copyRetentionRule
					.get(INACTIVE_DISPOSAL_TYPE).equals("T")) {
				copyRetentionRulePrincipalAndNotSortExist = true;
			}
		} else {
			addRequiredCopyRuleValueError(errors, COPY_TYPE);
		}

		if (copyRetentionRule.containsKey(MEDIUM_TYPES)) {
			String value = copyRetentionRule.get(MEDIUM_TYPES);
			if (value.isEmpty()) {
				addRequiredCopyRuleValueError(errors, MEDIUM_TYPES);
			}
		} else {
			addRequiredCopyRuleValueError(errors, MEDIUM_TYPES);
		}

		if (copyRetentionRule.containsKey(ACTIVE_RETENTION_PERIOD)) {
			String value =
					copyRetentionRule.get(ACTIVE_RETENTION_PERIOD) != null ? copyRetentionRule.get(ACTIVE_RETENTION_PERIOD) : "";
			if (value.isEmpty()) {
				Map<String, Object> convertedRetentionRule = new HashMap<>();
				for (String parameterKey : copyRetentionRule.keySet()) {
					convertedRetentionRule.put(parameterKey, copyRetentionRule.get(parameterKey));
				}
				addRequiredCopyRuleValueError(errors, ACTIVE_RETENTION_PERIOD);
			} else if (!value.startsWith("var:")) {
				try {
					int convertedValue = Integer.valueOf(value);
					if (convertedValue < 0) {
						addInvalidCopyRuleNumberValueError(errors, value, ACTIVE_RETENTION_PERIOD);
					}
				} catch (NullPointerException | NumberFormatException np) {
					addInvalidCopyRuleNumberValueError(errors, value, ACTIVE_RETENTION_PERIOD);
				}
			}
		} else {
			addRequiredCopyRuleValueError(errors, ACTIVE_RETENTION_PERIOD);
		}

		if (copyRetentionRule.containsKey(SEMI_ACTIVE_RETENTION_PERIOD)) {
			if (copyRetentionRule.get(SEMI_ACTIVE_RETENTION_PERIOD) == null) {
				errors.add(RecordsImportServices.class, INVALID_NUMBER_VALUE, asMap("value", "null"));
			}
		} else {
			addRequiredCopyRuleValueError(errors, SEMI_ACTIVE_RETENTION_PERIOD);
		}

		return copyRetentionRulePrincipalAndNotSortExist;
	}

	private void addRequiredCopyRuleValueError(ValidationErrors errors, String field) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("field", field);
		errors.add(RetentionRuleImportExtension.class, REQUIRED_COPYRULE_FIELD, parameters);
	}

	private void addRequiredDocumentTypeValueError(ValidationErrors errors, String field) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("field", field);
		errors.add(RetentionRuleImportExtension.class, REQUIRED_DOCUMENT_TYPE_FIELD, parameters);
	}

	private void addInvalidCopyRuleNumberValueError(ValidationErrors errors, String value, String field) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("field", field);
		parameters.put("value", value);
		errors.add(RetentionRuleImportExtension.class, INVALID_COPYRULE_NUMBER_FIELD, parameters);
	}

	private void addInvalidCopyRuleEnumValueError(ValidationErrors errors, String value, String field,
												  Class<? extends Enum<?>> enumClass) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("field", field);
		parameters.put("value", value);
		parameters.put("acceptedValues", join(EnumWithSmallCodeUtils.toSmallCodeList(enumClass), ", "));
		errors.add(RetentionRuleImportExtension.class, INVALID_COPYRULE_ENUM_FIELD, parameters);
	}

	private void addInvalidDocumentTypeEnumValueError(ValidationErrors errors, String value, String field,
													  Class<? extends Enum<?>> enumClass) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("field", field);
		parameters.put("value", value);
		parameters.put("acceptedValues", join(EnumWithSmallCodeUtils.toSmallCodeList(enumClass), ", "));
		errors.add(RetentionRuleImportExtension.class, INVALID_DOCUMENT_TYPE_ENUM_FIELD, parameters);
	}

	private void prevalidateDocumentType(Map<String, String> documentType, ValidationErrors errors,
										 boolean copyRetentionRulePrincipalAndNotSortExist) {

		if (!copyRetentionRulePrincipalAndNotSortExist) {
			if (documentType.containsKey(ARCHIVISTIC_STATUS)) {
				String value = documentType.get(ARCHIVISTIC_STATUS).toUpperCase();
				if (value.isEmpty() || !(isDisposalTypeValid(value))) {
					addInvalidDocumentTypeEnumValueError(errors, value, ARCHIVISTIC_STATUS, FolderStatus.class);
				}
			} else {
				addRequiredCopyRuleValueError(errors, ARCHIVISTIC_STATUS);
			}
		}

		if (!documentType.containsKey(CODE)) {
			addRequiredCopyRuleValueError(errors, CODE);
		}
	}

	private void validateDocumentType(DocumentTypeResolver resolver, Map<String, String> documentType,
									  ValidationErrors errors) {

		String value = documentType.get(CODE);
		if (value.isEmpty()) {
			addRequiredCopyRuleValueError(errors, CODE);
		} else {
			String documentTypeId = null;
			try {
				documentTypeId = resolver.getDocumentTypeByCode(value);
			} catch (WrappedRecordMustBeNotNull | NullPointerException re) {
			}
			if (documentTypeId == null) {
				errors.add(RetentionRuleImportExtension.class, INVALID_DOCUMENT_TYPE_CODE, asMap("value", value));
			}
		}
	}

	private Map<String, Object> asMap(String key1, String value1) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(key1, value1);
		return parameters;
	}

	private Map<String, Object> asMap(String key1, String value1, String key2, String value2) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(key1, value1);
		parameters.put(key2, value2);
		return parameters;
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
			if (hasValue(copyRetentionRule)) {
				copyRetentionRuleList.add(buildCopyRetentionRule(mediumTypeResolver, copyRetentionRule, RULES_TYPE_FOLDER));
			}
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
			if (!(documentType.get(CODE).isEmpty() && (documentType.get(ARCHIVISTIC_STATUS) == null))) {
				documentTypeList.add(buildDocumentType(documentTypeResolver, documentType));
			}
		}

		retentionRule.setDocumentTypesDetails(documentTypeList);

		if (retentionRule.getScope() != null && retentionRule.getScope().equals(RetentionRuleScope.DOCUMENTS)) {
			List<Map<String, String>> docCopyRetentionRules = buildParams.getImportRecord()
					.getList(RetentionRule.DOCUMENT_COPY_RETENTION_RULES);
			List<CopyRetentionRule> docCopyRetentionRulesBuilt = new ArrayList<>();
			for (Map<String, String> docCopyRetentionRule : docCopyRetentionRules) {
				docCopyRetentionRulesBuilt
						.add(buildCopyRetentionRule(mediumTypeResolver, docCopyRetentionRule, RULES_TYPE_DOCUMENTS));
			}
			retentionRule.setDocumentCopyRetentionRules(docCopyRetentionRulesBuilt);

			Map<String, String> principalDefaultDocumentCopyRetentionRule = buildParams.getImportRecord()
					.getMap(RetentionRule.PRINCIPAL_DEFAULT_DOCUMENT_COPY_RETENTION_RULE);
			retentionRule.setPrincipalDefaultDocumentCopyRetentionRule(
					buildCopyRetentionRule(mediumTypeResolver, principalDefaultDocumentCopyRetentionRule, RULES_TYPE_DOCUMENTS));

			Map<String, String> secondaryDefaultDocumentCopyRetentionRule = buildParams.getImportRecord()
					.getMap(RetentionRule.SECONDARY_DEFAULT_DOCUMENT_COPY_RETENTION_RULE);
			retentionRule.setSecondaryDefaultDocumentCopyRetentionRule(
					buildCopyRetentionRule(mediumTypeResolver, secondaryDefaultDocumentCopyRetentionRule, RULES_TYPE_DOCUMENTS));
		}
	}

	private CopyRetentionRule buildCopyRetentionRule(MediumTypeResolver resolver,
													 Map<String, String> mapCopyRetentionRule,
													 String rulesType) {

		CopyRetentionRuleBuilder builder = CopyRetentionRuleBuilder.sequential(rm);
		CopyRetentionRule copyRetentionRule;
		if (mapCopyRetentionRule.containsKey(COPY_RETENTION_RULE_ID) && StringUtils
				.isNotEmpty(mapCopyRetentionRule.get(COPY_RETENTION_RULE_ID))) {
			copyRetentionRule = builder.newCopyRetentionRuleWithId(mapCopyRetentionRule.get(COPY_RETENTION_RULE_ID));
		} else {
			copyRetentionRule = builder.newCopyRetentionRule();
		}

		copyRetentionRule.setCode(mapCopyRetentionRule.get(CODE));
		copyRetentionRule.setTitle(mapCopyRetentionRule.get(TITLE));
		copyRetentionRule.setDescription(mapCopyRetentionRule.get(DESCRIPTION));
		copyRetentionRule.setEssential("true".equals(mapCopyRetentionRule.get(ESSENTIAL)));

		if (mapCopyRetentionRule.get(SEMI_ACTIVE_YEAR_TYPE) != null) {
			YearType yearType = rm.getYearTypeWithLegacyId(mapCopyRetentionRule.get(SEMI_ACTIVE_YEAR_TYPE));
			copyRetentionRule.setSemiActiveYearTypeId(yearType.getId());
		}

		if (mapCopyRetentionRule.get(INACTIVE_YEAR_TYPE) != null) {
			YearType yearType = rm.getYearTypeWithLegacyId(mapCopyRetentionRule.get(INACTIVE_YEAR_TYPE));
			copyRetentionRule.setInactiveYearTypeId(yearType.getId());
		}

		CopyType copyType = (mapCopyRetentionRule.get(COPY_TYPE).toUpperCase()).equals("P") ?
							CopyType.PRINCIPAL :
							CopyType.SECONDARY;
		copyRetentionRule.setCopyType(copyType);

		List<String> mediumTypesId = getMediumTypesId(resolver, mapCopyRetentionRule.get(MEDIUM_TYPES));
		copyRetentionRule.setMediumTypeIds(mediumTypesId);

		if (StringUtils.isNotBlank(mapCopyRetentionRule.get(CONTENT_TYPES_COMMENT))) {
			//		if (!mapCopyRetentionRule.get(CONTENT_TYPES_COMMENT).equals("")) {
			copyRetentionRule.setContentTypesComment(mapCopyRetentionRule.get(CONTENT_TYPES_COMMENT));
		}

		String activeRetentionPeriodValue = mapCopyRetentionRule.get(ACTIVE_RETENTION_PERIOD);
		if (activeRetentionPeriodValue.startsWith("var:")) {
			activeRetentionPeriodValue = activeRetentionPeriodValue.replace("var:", "");
			copyRetentionRule.setActiveRetentionPeriod(RetentionPeriod.variable(activeRetentionPeriodValue));
		} else {
			int activeRetentionPeriod = Integer.parseInt(mapCopyRetentionRule.get(ACTIVE_RETENTION_PERIOD));
			if (activeRetentionPeriod == 999) {
				copyRetentionRule.setActiveRetentionPeriod(RetentionPeriod.OPEN_999);
			} else if (activeRetentionPeriod == 888) {
				copyRetentionRule.setActiveRetentionPeriod(RetentionPeriod.OPEN_888);
			} else {
				copyRetentionRule.setActiveRetentionPeriod(RetentionPeriod.fixed(activeRetentionPeriod));
			}
		}

		if (StringUtils.isNotBlank(mapCopyRetentionRule.get(ACTIVE_RETENTION_PERIOD_COMMENT))) {
			//		if (!mapCopyRetentionRule.get(ACTIVE_RETENTION_PERIOD_COMMENT).equals("")) {
			copyRetentionRule.setActiveRetentionComment(mapCopyRetentionRule.get(ACTIVE_RETENTION_PERIOD_COMMENT));
		}

		String ignoreActivePeriodStr = mapCopyRetentionRule.get(IGNORE_ACTIVE_PERIOD);
		if (ignoreActivePeriodStr != null) {
			copyRetentionRule.setIgnoreActivePeriod(Boolean.parseBoolean(ignoreActivePeriodStr));
		}

		String semiActiveRetentionPeriodValue = mapCopyRetentionRule.get(SEMI_ACTIVE_RETENTION_PERIOD);
		if (semiActiveRetentionPeriodValue.startsWith("var:")) {
			semiActiveRetentionPeriodValue = semiActiveRetentionPeriodValue.replace("var:", "");
			copyRetentionRule.setSemiActiveRetentionPeriod(RetentionPeriod.variable(semiActiveRetentionPeriodValue));
		} else {
			int semiActiveRetentionPeriod = Integer.parseInt(mapCopyRetentionRule.get(SEMI_ACTIVE_RETENTION_PERIOD));
			if (semiActiveRetentionPeriod == 999) {
				copyRetentionRule.setSemiActiveRetentionPeriod(RetentionPeriod.OPEN_999);
			} else if (semiActiveRetentionPeriod == 888) {
				copyRetentionRule.setSemiActiveRetentionPeriod(RetentionPeriod.OPEN_888);
			} else {
				copyRetentionRule.setSemiActiveRetentionPeriod(RetentionPeriod.fixed(semiActiveRetentionPeriod));
			}
		}

		if (StringUtils.isNotBlank(mapCopyRetentionRule.get(SEMI_ACTIVE_RETENTION_PERIOD_COMMENT))) {
			//		if (!mapCopyRetentionRule.get(SEMI_ACTIVE_RETENTION_PERIOD_COMMENT).equals("")) {
			copyRetentionRule.setSemiActiveRetentionComment(mapCopyRetentionRule.get(SEMI_ACTIVE_RETENTION_PERIOD_COMMENT));
		}

		DisposalType disposalType = disposalTypeFromString(mapCopyRetentionRule.get(INACTIVE_DISPOSAL_TYPE).toUpperCase());
		copyRetentionRule.setInactiveDisposalType(disposalType);

		if (StringUtils.isNotBlank(mapCopyRetentionRule.get(INACTIVE_DISPOSAL_COMMENT))) {
			//		if (!mapCopyRetentionRule.get(INACTIVE_DISPOSAL_COMMENT).equals("")) {
			copyRetentionRule.setInactiveDisposalComment(mapCopyRetentionRule.get(INACTIVE_DISPOSAL_COMMENT));
		}

		if (StringUtils.isNotBlank(mapCopyRetentionRule.get(OPEN_ACTIVE_RETENTION_PERIOD))) {
			copyRetentionRule
					.setOpenActiveRetentionPeriod(Integer.parseInt(mapCopyRetentionRule.get(OPEN_ACTIVE_RETENTION_PERIOD)));
		}

		if (StringUtils.isNotBlank(mapCopyRetentionRule.get(ACTIVE_DATE_METADATA))) {
			copyRetentionRule.setActiveDateMetadata(mapCopyRetentionRule.get(ACTIVE_DATE_METADATA));
		}

		if (StringUtils.isNotBlank(mapCopyRetentionRule.get(SEMI_ACTIVE_DATE_METADATA))) {
			copyRetentionRule.setSemiActiveDateMetadata(mapCopyRetentionRule.get(SEMI_ACTIVE_DATE_METADATA));
		}

		if (StringUtils.isNotBlank(mapCopyRetentionRule.get(ESSENTIAL))) {
			copyRetentionRule.setEssential(Boolean.parseBoolean(mapCopyRetentionRule.get(ESSENTIAL)));
		}

		String typeIdRawValue = mapCopyRetentionRule.get(TYPE_ID);
		if (StringUtils.isNotBlank(typeIdRawValue)) {
			if (typeIdRawValue.startsWith("code:")) {
				if (rulesType.equals(RULES_TYPE_FOLDER)) {
					copyRetentionRule.setTypeId(rm.getFolderTypeWithCode(typeIdRawValue.split(":")[1]));
				} else {
					copyRetentionRule.setTypeId(rm.getDocumentTypeWithCode(typeIdRawValue.split(":")[1]));
				}
			} else {
				copyRetentionRule.setTypeId(typeIdRawValue);
			}
		}

		return copyRetentionRule;
	}

	private RetentionRuleDocumentType buildDocumentType(DocumentTypeResolver resolver,
														Map<String, String> documentType) {
		RetentionRuleDocumentType retentionRuleDocumentType = new RetentionRuleDocumentType();

		if (!documentType.get(CODE).isEmpty()) {
			retentionRuleDocumentType.setDocumentTypeId(resolver.getDocumentTypeByCode(documentType.get(CODE)));
		}

		if ((!documentType.get(CODE).isEmpty()) && (documentType.get(ARCHIVISTIC_STATUS) == null)) {
			throw new RuntimeException("Invalid disposal type: archivisticStatus is empty but code exists.");
		}

		if (documentType.get(CODE).isEmpty() && (documentType.get(ARCHIVISTIC_STATUS) != null)) {
			throw new RuntimeException("Invalid disposal type: code is empty but archivisticStatus is defined.");
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
