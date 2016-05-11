package com.constellio.app.services.schemas.bulkImport.extensions;

import static com.constellio.app.modules.rm.extensions.imports.RetentionRuleImportExtension.INVALID_CODE_VALUE;
import static com.constellio.app.modules.rm.extensions.imports.RetentionRuleImportExtension.INVALID_ENUM_VALUE;
import static com.constellio.app.modules.rm.extensions.imports.RetentionRuleImportExtension.INVALID_NUMBER_VALUE;
import static com.constellio.app.modules.rm.extensions.imports.RetentionRuleImportExtension.MISSING_METADATA;
import static com.constellio.app.modules.rm.extensions.imports.RetentionRuleImportExtension.REQUIRED_VALUE;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.COPY_RETENTION_RULES;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.DOCUMENT_TYPES_DETAILS;
import static com.constellio.sdk.tests.TestUtils.asMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.Mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.constellio.app.modules.rm.extensions.imports.RetentionRuleImportExtension;
import com.constellio.app.services.schemas.bulkImport.ImportDataErrors;
import com.constellio.app.services.schemas.bulkImport.RecordsImportServices;
import com.constellio.app.services.schemas.bulkImport.data.ImportData;
import com.constellio.model.extensions.events.recordsImport.PrevalidationParams;
import com.constellio.model.extensions.events.recordsImport.ValidationParams;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.sdk.tests.ConstellioTest;

public class RetentionRuleExtensionAcceptanceTest extends ConstellioTest {

	private RetentionRuleImportExtension retentionRuleExtension;
	private List<Object> copyRetentionRules;
	private List<Object> documentTypes;
	@Mock ImportData importData;

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(
				withZeCollection().withAllTestUsers().withConstellioRMModule()
		);

		retentionRuleExtension = new RetentionRuleImportExtension(zeCollection, getModelLayerFactory());
		copyRetentionRules = new ArrayList<>();
		documentTypes = new ArrayList<>();

		when(importData.getList(COPY_RETENTION_RULES)).thenReturn(copyRetentionRules);
		when(importData.getList(DOCUMENT_TYPES_DETAILS)).thenReturn(documentTypes);
	}

	@Test
	public void givenRetentionRuleThenInvalidIdValueOnDocumentType() {
		ValidationErrors validationErrors = new ValidationErrors();
		ImportDataErrors importErrors = new ImportDataErrors("retentionRule", validationErrors, importData);

		givenDocumentTypes(documentTypes).code("documentPapier").addField("archivisticStatus", "C").build();

		retentionRuleExtension.validate(new ValidationParams(importErrors, importData));
		List<ValidationError> errors = validationErrors.getValidationErrors();

		assertThat(errors).containsOnly(newValidationError(INVALID_CODE_VALUE,
				asMap("code", "documentPapier", "documentTypeIndex", "0")));
	}

	@Test
	public void givenRetentionRuleThenInvalidCopyTypeOnCopyRetentionRule() {
		ValidationErrors validationErrors = new ValidationErrors();
		ImportDataErrors importErrors = new ImportDataErrors("retentionRule", validationErrors, importData);

		givenCopyRetentionRule(copyRetentionRules).addField("copyType", "F'`,,,").addField("inactiveDisposalType", "S")
				.addField("mediumType", "PA,FI");

		retentionRuleExtension.prevalidate(new PrevalidationParams(importErrors, importData));

		List<ValidationError> errors = validationErrors.getValidationErrors();

		assertThat(errors.size()).isEqualTo(5);
		assertThat(errors).contains(
				newValidationError(INVALID_ENUM_VALUE, asMap("inactiveDisposalType", "S", "copyRetentionRuleIndex", "0")));
		assertThat(errors)
				.contains(newValidationError(INVALID_ENUM_VALUE, asMap("mediumTypes", "F'`,,,", "copyRetentionRuleIndex", "0")));
		assertThat(errors)
				.contains(newValidationError(MISSING_METADATA, asMap("value", "mediumTypes", "copyRetentionRuleIndex", "0")));
		assertThat(errors).contains(
				newValidationError(MISSING_METADATA, asMap("value", "activeRetentionPeriod", "copyRetentionRuleIndex", "0")));
		assertThat(errors)
				.contains(newValidationError(MISSING_METADATA,
						asMap("value", "semiActiveRetentionPeriod", "copyRetentionRuleIndex", "0")));
	}

	@Test
	public void givenALargeAmountOfScrapDataWhenValidateThenAFullStackOfError() {
		ValidationErrors validationErrors = new ValidationErrors();
		ImportDataErrors importErrors = new ImportDataErrors("retentionRule", validationErrors, importData);

		givenCopyRetentionRule(copyRetentionRules).code(String.valueOf(2)).addField("copyType", "S")
				.addField("inactiveDisposalType", "C")
				.addField("mediumType", "PA");
		givenCopyRetentionRule(copyRetentionRules).code(String.valueOf(3)).addField("copyType", "S")
				.addField("inactiveDisposalType", "T").addField("mediumTypes", "PA,MD")
				.addField("mediumType", "test").addField("activeRetentionPeriod", "-25");
		givenCopyRetentionRule(copyRetentionRules).code(String.valueOf(10)).addField("copyType", "P")
				.addField("activeRetentionPeriod", "999");
		givenCopyRetentionRule(copyRetentionRules).code(String.valueOf(1)).addField("copyType", "Principale").addField(
				"activeRetentionPeriod",
				"888").addField("contentTypesComment", "value unknown").addField("inactiveDisposalType", "D");
		givenCopyRetentionRule(copyRetentionRules).code(String.valueOf(1)).addField("copyType", "S")
				.addField("activeRetentionPeriod", "awer")
				.addField("contentTypesComment", "value unknown with full stack").addField("inactiveDisposalType", "T");

		givenDocumentTypes(documentTypes).code("emailDocumentType").addField("archivisticStatus", "Conservation");
		givenDocumentTypes(documentTypes).code("documentType").addField("archivisticStatus", "T");
		givenDocumentTypes(documentTypes).code(String.valueOf(3)).addField("archivisticStatus", "t");
		givenDocumentTypes(documentTypes).addField("archivisticStatus", "D");
		givenDocumentTypes(documentTypes).code("emailDocumentType").code(String.valueOf(1)).addField("archivisticStatus", "T");

		retentionRuleExtension.prevalidate(new PrevalidationParams(importErrors, importData));

		List<ValidationError> errors = validationErrors.getValidationErrors();
		List<ValidationError> expectedErrors = fullSetOfExpectedErrors();

		assertThat(errors).containsOnly(expectedErrors.toArray(new ValidationError[0]));
	}

	@Test
	public void givenEmptyMetadatasWhenPrevalidateThenInvalidStringValueError() {
		ValidationErrors validationErrors = new ValidationErrors();
		ImportDataErrors importErrors = new ImportDataErrors("retentionRule", validationErrors, importData);

		givenCopyRetentionRule(copyRetentionRules).code(String.valueOf(1)).addField("copyType", "")
				.addField("inactiveDisposalType", "")
				.addField("mediumTypes", "").addField("activeRetentionPeriod", "").addField("contentTypesComment", "")
				.addField("semiActiveRetentionPeriod", "");

		givenDocumentTypes(documentTypes).code("emailDocumentType").addField("archivisticStatus", "");

		retentionRuleExtension.prevalidate(new PrevalidationParams(importErrors, importData));

		List<ValidationError> expected = new ArrayList<>();

		expected.add(newValidationError(INVALID_ENUM_VALUE, asMap("documentTypeIndex", "0", "archivisticStatus", "")));
		expected.add(newValidationError(INVALID_ENUM_VALUE, asMap("copyRetentionRuleIndex", "0", "inactiveDisposalType", "")));
		expected.add(newValidationError(INVALID_ENUM_VALUE, asMap("mediumTypes", "", "copyRetentionRuleIndex", "0")));
		expected.add(newValidationError(INVALID_CODE_VALUE, asMap("mediumTypes", "empty", "copyRetentionRuleIndex", "0")));

		Map<String, Object> values = new HashMap<>();
		values.put("activeRetentionPeriod", "");
		values.put("semiActiveRetentionPeriod", "");
		values.put("mediumTypes", "");
		values.put("copyType", "");
		values.put("code", "1");
		values.put("contentTypesComment", "");
		values.put("inactiveDisposalType", "");

		expected.add(newValidationError(REQUIRED_VALUE, values));

		List<ValidationError> errors = validationErrors.getValidationErrors();

		assertThat(errors).containsOnly(expected.toArray(new ValidationError[0]));

	}

	private List<ValidationError> fullSetOfExpectedErrors() {
		ArrayList<ValidationError> expectedErrors = new ArrayList<>();

		expectedErrors.add(newValidationError(INVALID_ENUM_VALUE,
				asMap("documentTypeIndex", "0", "archivisticStatus", "CONSERVATION")));
		expectedErrors
				.add(newValidationError(INVALID_ENUM_VALUE, asMap("mediumTypes", "PRINCIPALE", "copyRetentionRuleIndex", "3")));

		//		expectedErrors.add(newValidationError(INVALID_CODE_VALUE, asMap("mediumTypes", "MD", "copyRetentionRuleIndex", "1")));
		//		expectedErrors.add(newValidationError(INVALID_CODE_VALUE, asMap("documentTypeIndex", "1", "code", "documentType")));
		//		expectedErrors.add(newValidationError(INVALID_CODE_VALUE, asMap("documentTypeIndex", "2", "code", "3")));
		//		expectedErrors.add(newValidationError(INVALID_CODE_VALUE, asMap("documentTypeIndex", "4", "code", "1")));

		expectedErrors.add(newValidationError(MISSING_METADATA, asMap("documentTypeIndex", "3", "value", "code")));
		expectedErrors.add(
				newValidationError(MISSING_METADATA, asMap("value", "mediumTypes", "copyRetentionRuleIndex", "0")));
		expectedErrors.add(newValidationError(MISSING_METADATA,
				asMap("value", "activeRetentionPeriod", "copyRetentionRuleIndex", "0")));
		expectedErrors.add(newValidationError(MISSING_METADATA,
				asMap("value", "semiActiveRetentionPeriod", "copyRetentionRuleIndex", "0")));
		expectedErrors.add(newValidationError(MISSING_METADATA,
				asMap("value", "semiActiveRetentionPeriod", "copyRetentionRuleIndex", "1")));
		expectedErrors.add(newValidationError(MISSING_METADATA,
				asMap("value", "semiActiveRetentionPeriod", "copyRetentionRuleIndex", "2")));
		expectedErrors.add(newValidationError(MISSING_METADATA,
				asMap("value", "semiActiveRetentionPeriod", "copyRetentionRuleIndex", "3")));
		expectedErrors.add(newValidationError(MISSING_METADATA,
				asMap("value", "semiActiveRetentionPeriod", "copyRetentionRuleIndex", "4")));
		expectedErrors.add(
				newValidationError(MISSING_METADATA, asMap("value", "mediumTypes", "copyRetentionRuleIndex", "2")));
		expectedErrors.add(
				newValidationError(MISSING_METADATA, asMap("value", "mediumTypes", "copyRetentionRuleIndex", "3")));
		expectedErrors.add(
				newValidationError(MISSING_METADATA, asMap("value", "mediumTypes", "copyRetentionRuleIndex", "4")));

		expectedErrors.add(newValidationError(INVALID_NUMBER_VALUE, asMap("copyRetentionRuleIndex", "1", "value", "-25")));
		expectedErrors.add(newValidationError(INVALID_NUMBER_VALUE, asMap("copyRetentionRuleIndex", "4", "value", "awer")));

		return expectedErrors;
	}

	private Map<String, Object> asMap(String key1, String value1, String key2, String value2) {
		Map<String, Object> parameters = new HashMap<>();
		parameters.put(key1, value1);
		parameters.put(key2, value2);
		return parameters;
	}

	//----------------------------

	private class StructureMapBuilder {

		private Map<String, String> fields = new HashMap<>();

		public Map<String, String> build() {
			return fields;
		}

		public StructureMapBuilder addField(String key, String value) {
			fields.put(key, value);
			return this;
		}

		public StructureMapBuilder code(String code) {
			fields.put("code", code);
			return this;
		}

	}

	private ValidationError newValidationError(String code, Map<String, Object> parameters) {
		parameters.put("index", "1");
		parameters.put("legacyId", null);
		parameters.put("schemaType", "retentionRule");
		return new ValidationError(RecordsImportServices.class.getName() + "_" + code, parameters);
	}

	private StructureMapBuilder givenCopyRetentionRule(List<Object> copyRetentionRule) {
		StructureMapBuilder copyRetentionRuleMapBuilder = new StructureMapBuilder();
		copyRetentionRule.add(copyRetentionRuleMapBuilder.build());
		return copyRetentionRuleMapBuilder;
	}

	private StructureMapBuilder givenDocumentTypes(List<Object> documentTypes) {
		StructureMapBuilder documentTypeDetailMapBuilder = new StructureMapBuilder();
		documentTypes.add(documentTypeDetailMapBuilder.build());
		return documentTypeDetailMapBuilder;
	}

}
