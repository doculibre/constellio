package com.constellio.app.modules.rm.model.validators;

import static com.constellio.app.modules.rm.model.validators.RetentionRuleValidator.MUST_SPECIFY_ADMINISTRATIVE_UNITS_XOR_RESPONSIBLES_FLAG;
import static com.constellio.app.modules.rm.model.validators.RetentionRuleValidator.NO_ADMINISTRATIVE_UNITS_OR_RESPONSIBLES_FLAG;
import static com.constellio.app.modules.rm.wrappers.RetentionRule.COPY_RETENTION_RULES;
import static com.constellio.model.entities.schemas.validation.RecordMetadataValidator.METADATA_CODE;
import static com.constellio.sdk.tests.TestUtils.englishMessage;
import static com.constellio.sdk.tests.TestUtils.englishMessages;
import static com.constellio.sdk.tests.TestUtils.extractingSimpleCode;
import static com.constellio.sdk.tests.TestUtils.extractingSimpleCodeAndParameters;
import static com.constellio.sdk.tests.TestUtils.frenchMessage;
import static com.constellio.sdk.tests.TestUtils.frenchMessages;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.CopyRetentionRuleBuilder;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.model.enums.RetentionRuleScope;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.FakeSessionContext;

public class RetentionRuleValidatorAcceptTest extends ConstellioTest {

	//	RetentionRuleValidator validator = new RetentionRuleValidator();
	RetentionRuleValidator validator;
	@Mock Metadata uniformSubdivisionMetadata, categoriesMetadata, copyRetentionRuleMetadata;
	@Mock MetadataSchema schema;

	@Mock RetentionRule retentionRule;
	@Mock ConfigProvider configProvider;

	CopyRetentionRuleBuilder copyBuilder = CopyRetentionRuleBuilder.UUID();
	CopyRetentionRule copy0_analogicPrincipal = copyBuilder.newCopyRetentionRule();
	CopyRetentionRule copy1_numericPrincipal = copyBuilder.newCopyRetentionRule();
	CopyRetentionRule copy2_secondary = copyBuilder.newCopyRetentionRule();

	CopyRetentionRule docCopy1_principal = copyBuilder.newCopyRetentionRule();
	CopyRetentionRule docCopy2_principal = copyBuilder.newCopyRetentionRule();

	ValidationErrors errors = new ValidationErrors();
	private List<RetentionRuleDocumentType> types;

	SessionContext sessionContext;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(
				withZeCollection()
		);

		sessionContext = FakeSessionContext.adminInCollection(zeCollection);
		sessionContext.setCurrentLocale(Locale.FRENCH);

		validator = new RetentionRuleValidator();

		when(retentionRule.getCode()).thenReturn("zeCode");
		when(retentionRule.getAdministrativeUnits())
				.thenReturn(Arrays.asList("firstAdministrativeUnitId", "secondAdministrativeUnitId"));
		when(retentionRule.getCopyRetentionRules())
				.thenReturn(Arrays.asList(copy0_analogicPrincipal, copy1_numericPrincipal, copy2_secondary));
		when(retentionRule.getDocumentCopyRetentionRules()).thenReturn(Arrays.asList(docCopy1_principal, docCopy2_principal));

		types = Arrays.asList(
				new RetentionRuleDocumentType("firstDocumentTypeId", DisposalType.DEPOSIT),
				new RetentionRuleDocumentType("secondDocumentTypeId", DisposalType.DESTRUCTION)
		);
		when(retentionRule.getDocumentTypesDetails()).thenReturn(types);
		when(configProvider.get(RMConfigs.COPY_RULE_PRINCIPAL_REQUIRED)).thenReturn(true);
		when(configProvider.get(RMConfigs.DOCUMENT_RETENTION_RULES)).thenReturn(false);

		copy0_analogicPrincipal.setCode("Copy1");
		copy0_analogicPrincipal.setMediumTypeIds(Arrays.asList("type1", "type2"));
		copy0_analogicPrincipal.setCopyType(CopyType.PRINCIPAL);
		copy0_analogicPrincipal.setActiveRetentionPeriod(RetentionPeriod.OPEN_888);
		copy0_analogicPrincipal.setSemiActiveRetentionPeriod(RetentionPeriod.fixed(4));
		copy0_analogicPrincipal.setInactiveDisposalType(DisposalType.DEPOSIT);

		copy1_numericPrincipal.setCode("Copy2");
		copy1_numericPrincipal.setMediumTypeIds(Arrays.asList("type3", "type4"));
		copy1_numericPrincipal.setCopyType(CopyType.PRINCIPAL);
		copy1_numericPrincipal.setActiveRetentionPeriod(RetentionPeriod.OPEN_999);
		copy1_numericPrincipal.setSemiActiveRetentionPeriod(RetentionPeriod.fixed(1));
		copy1_numericPrincipal.setInactiveDisposalType(DisposalType.DEPOSIT);

		copy2_secondary.setCode("Copy3");
		copy2_secondary.setMediumTypeIds(Arrays.asList("type5", "type6"));
		copy2_secondary.setCopyType(CopyType.SECONDARY);
		copy2_secondary.setActiveRetentionPeriod(RetentionPeriod.OPEN_888);
		copy2_secondary.setSemiActiveRetentionPeriod(RetentionPeriod.fixed(4));
		copy2_secondary.setInactiveDisposalType(DisposalType.DEPOSIT);

		docCopy1_principal.setCode("DocCopy1");
		docCopy1_principal.setMediumTypeIds(Arrays.asList("type3", "type4"));
		docCopy1_principal.setCopyType(CopyType.PRINCIPAL);
		docCopy1_principal.setTypeId("docType1");
		docCopy1_principal.setActiveRetentionPeriod(RetentionPeriod.OPEN_999);
		docCopy1_principal.setSemiActiveRetentionPeriod(RetentionPeriod.fixed(1));
		docCopy1_principal.setInactiveDisposalType(DisposalType.DEPOSIT);

		docCopy2_principal.setCode("DocCopy2");
		docCopy2_principal.setMediumTypeIds(Arrays.asList("type5", "type6"));
		docCopy2_principal.setCopyType(CopyType.PRINCIPAL);
		docCopy2_principal.setTypeId("docType2");
		docCopy2_principal.setActiveRetentionPeriod(RetentionPeriod.OPEN_888);
		docCopy2_principal.setSemiActiveRetentionPeriod(RetentionPeriod.fixed(4));
		docCopy2_principal.setInactiveDisposalType(DisposalType.DEPOSIT);

		when(schema.getMetadata(COPY_RETENTION_RULES)).thenReturn(copyRetentionRuleMetadata);
	}

	@Test
	public void givenValidWhenValidateThenNoErrors()
			throws Exception {

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors.getValidationErrors()).isEmpty();

	}

	@Test
	public void givenDisabledUniformSubdivisionsAndNoCategoriesWhenValidateThenErrorCode()
			throws Exception {

		when(uniformSubdivisionMetadata.isEnabled()).thenReturn(false);

		validator.validate(retentionRule, schema, configProvider, errors);

	}

	@Test
	public void givenNoUniformSubdivisionAndNoCategoryWhenValidateThenErrorCode()
			throws Exception {

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenNoAdministrativeUnitsAndResponsibleAdministrativeUnitsFlagSetToTrueWhenValidateThenNoError()
			throws Exception {

		when(retentionRule.getAdministrativeUnits()).thenReturn(new ArrayList<String>());
		when(retentionRule.isResponsibleAdministrativeUnits()).thenReturn(true);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors.getValidationErrors()).isEmpty();

	}

	@Test
	public void givenOpenHolderActivatedAndAdministrativeUnitsAndResponsibleAdministrativeUnitsFlagSetToFalseWhenValidateThenNoError()
			throws Exception {
		when(configProvider.get(RMConfigs.OPEN_HOLDER)).thenReturn(true);
		when(retentionRule.getAdministrativeUnits()).thenReturn(Arrays.asList("administrativeUnitId"));
		when(retentionRule.isResponsibleAdministrativeUnits()).thenReturn(false);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors.getValidationErrors()).isEmpty();

	}

	@Test
	public void givenOpenHolderActivatedAndNoAdministrativeUnitsAndResponsibleAdministrativeUnitsFlagSetToFalseWhenValidateThenError()
			throws Exception {
		when(configProvider.get(RMConfigs.OPEN_HOLDER)).thenReturn(true);
		when(retentionRule.getAdministrativeUnits()).thenReturn(new ArrayList<String>());
		when(retentionRule.isResponsibleAdministrativeUnits()).thenReturn(false);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCode(errors)).containsOnly(
				"RetentionRuleValidator_noAdministrativeUnitsOrResponsibles");
		assertThat(frenchMessages(errors)).containsOnly(
				"Il est nécessaire de définir une unité administrative détentrice ou d'activer l'option 'Toutes unités administratives responsables'.");

	}

	@Test
	public void givenOpenHolderActivatedAndAdministrativeUnitsAndResponsibleAdministrativeUnitsFlagSetToTrueWhenValidateThenNoError()
			throws Exception {
		when(configProvider.get(RMConfigs.OPEN_HOLDER)).thenReturn(true);
		when(retentionRule.getAdministrativeUnits()).thenReturn(Arrays.asList("administrativeUnitId"));
		when(retentionRule.isResponsibleAdministrativeUnits()).thenReturn(true);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors.getValidationErrors()).isEmpty();

	}

	@Test
	public void givenAdministrativeUnitsAndResponsibleAdministrativeUnitsFlagSetToFalseWhenValidateThenNoError()
			throws Exception {

		when(retentionRule.getAdministrativeUnits()).thenReturn(Arrays.asList("administrativeUnitId"));
		when(retentionRule.isResponsibleAdministrativeUnits()).thenReturn(false);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors.getValidationErrors()).isEmpty();

	}

	@Test
	public void givenNoAdministrativeUnitsAndResponsibleAdministrativeUnitsFlagSetToFalseWhenValidateThenError()
			throws Exception {

		when(retentionRule.getAdministrativeUnits()).thenReturn(new ArrayList<String>());
		when(retentionRule.isResponsibleAdministrativeUnits()).thenReturn(false);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCode(errors))
				.containsOnly("RetentionRuleValidator_" + NO_ADMINISTRATIVE_UNITS_OR_RESPONSIBLES_FLAG);
		assertThat(frenchMessages(errors)).containsOnly(
				"Il est nécessaire de définir une unité administrative détentrice ou d'activer l'option 'Toutes unités administratives responsables'.");
		assertThat(englishMessages(errors)).containsOnly(
				"It is required to define an administrative unit or to activate the 'Responsible administrative unit' status.");

	}

	@Test
	public void givenAdministrativeUnitsAndResponsibleAdministrativeUnitsFlagSetToTrueWhenValidateThenError()
			throws Exception {

		when(retentionRule.getAdministrativeUnits()).thenReturn(Arrays.asList("administrativeUnitId"));
		when(retentionRule.isResponsibleAdministrativeUnits()).thenReturn(true);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCode(errors))
				.containsOnly("RetentionRuleValidator_" + MUST_SPECIFY_ADMINISTRATIVE_UNITS_XOR_RESPONSIBLES_FLAG);
		assertThat(frenchMessages(errors)).containsOnly(
				"La liste d'unités administratives détentrices doit être vide si 'Toutes unités administratives responsables' est activé.");
		assertThat(englishMessages(errors)).containsOnly(
				"The Field 'Name of administrative unit responsible for main folder' must be empty if the field 'Responsible administrative unit' is checked.");

	}

	@Test
	public void validateMessagesProducedByDynamicI18n_RetentionRuleValidator_invalidCopyRule()
			throws Exception {
		String key = "com.constellio.app.modules.rm.model.validators.RetentionRuleValidator_invalidCopyRuleField";

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("code", null);
		parameters.put("index", "2");
		parameters.put("field", "inactive");
		parameters.put("errorType", "required");
		parameters.put("metadata", RetentionRule.COPY_RETENTION_RULES);
		assertThat(frenchMessage(key, parameters)).isEqualTo(
				"Le champ «I» de l'exemplaire à la position 2 est requis.");
		assertThat(englishMessage(key, parameters)).isEqualTo(
				"Field 'Inactive' of copy at index 2 is required.");

		parameters = new HashMap<>();
		parameters.put("code", null);
		parameters.put("index", "3");
		parameters.put("field", "semiActive");
		parameters.put("value", "*");
		parameters.put("errorType", "invalid");
		parameters.put("metadata", RetentionRule.COPY_RETENTION_RULES);
		assertThat(frenchMessage(key, parameters)).isEqualTo(
				"La valeur «*» du champ «SA» de l'exemplaire à la position 3 est invalide.");
		assertThat(englishMessage(key, parameters)).isEqualTo(
				"Value '*' of field 'Semi-active' of copy at index 3 is invalid.");

		parameters = new HashMap<>();
		parameters.put("code", "Ze code");
		parameters.put("index", "666");
		parameters.put("field", "semiActive");
		parameters.put("value", "-1");
		parameters.put("errorType", "invalid");
		parameters.put("metadata", RetentionRule.COPY_RETENTION_RULES);
		assertThat(frenchMessage(key, parameters)).isEqualTo(
				"La valeur «-1» du champ «SA» de l'exemplaire «Ze code» est invalide.");
		assertThat(englishMessage(key, parameters)).isEqualTo(
				"Value '-1' of field 'Semi-active' of copy 'Ze code' is invalid.");

		parameters = new HashMap<>();
		parameters.put("code", "Ze code");
		parameters.put("index", "666");
		parameters.put("field", "semiActive");
		parameters.put("value", "-1");
		parameters.put("errorType", "invalid");
		parameters.put("metadata", RetentionRule.PRINCIPAL_DEFAULT_DOCUMENT_COPY_RETENTION_RULE);
		assertThat(frenchMessage(key, parameters)).isEqualTo(
				"La valeur «-1» du champ «SA» de l'exemplaire principal par défaut pour documents est invalide.");
		assertThat(englishMessage(key, parameters)).isEqualTo(
				"Value '-1' of field 'Semi-active' of default principal document copy is invalid.");

		parameters = new HashMap<>();
		parameters.put("code", "Ze code");
		parameters.put("index", "666");
		parameters.put("field", "active");
		parameters.put("errorType", "required");
		parameters.put("metadata", RetentionRule.SECONDARY_DEFAULT_DOCUMENT_COPY_RETENTION_RULE);
		assertThat(frenchMessage(key, parameters)).isEqualTo(
				"Le champ «A» de l'exemplaire secondaire pour documents est requis.");
		assertThat(englishMessage(key, parameters)).isEqualTo(
				"Field 'Active' of secondary document copy is required.");

		parameters = new HashMap<>();
		parameters.put("code", "Ze ultimate code");
		parameters.put("index", "666");
		parameters.put("field", "type");
		parameters.put("value", "-1");
		parameters.put("errorType", "invalid");
		parameters.put("metadata", RetentionRule.DOCUMENT_COPY_RETENTION_RULES);
		assertThat(frenchMessage(key, parameters)).isEqualTo(
				"La valeur «-1» du champ «Type» de l'exemplaire principal pour documents «Ze ultimate code» est invalide.");
		assertThat(englishMessage(key, parameters)).isEqualTo(
				"Value '-1' of field 'Type' of principal document copy 'Ze ultimate code' is invalid.");

		parameters = new HashMap<>();
		parameters.put("code", null);
		parameters.put("index", "666");
		parameters.put("field", "type");
		parameters.put("value", "-1");
		parameters.put("errorType", "required");
		parameters.put("metadata", RetentionRule.DOCUMENT_COPY_RETENTION_RULES);
		assertThat(frenchMessage(key, parameters)).isEqualTo(
				"Le champ «Type» de l'exemplaire principal pour documents à la position 666 est requis.");
		assertThat(englishMessage(key, parameters)).isEqualTo(
				"Field 'Type' of principal document copy at index 666 is required.");
	}

	@Test
	public void givenCopyRetentionRuleHasNoCopyTypeThenError()
			throws Exception {

		copy0_analogicPrincipal.setCopyType(null);
		copy2_secondary.setCopyType(null);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCodeAndParameters(errors, "code", "index", "field", "value", "errorType", "metadata"))
				.containsOnly(
						tuple("RetentionRuleValidator_invalidCopyRuleField", "Copy1", "1", "copyType", null, "required",
								"copyRetentionRules"),
						tuple("RetentionRuleValidator_invalidCopyRuleField", "Copy3", "3", "copyType", null, "required",
								"copyRetentionRules")
				);

		assertThat(frenchMessages(errors)).containsOnly("Le champ «Exemplaire» de l'exemplaire «Copy1» est requis.",
				"Le champ «Exemplaire» de l'exemplaire «Copy3» est requis.");
		//		assertThat(errors).has(size(2));
		//		assertThat(errors).has(copyRetentionRuleFieldRequiredError("0",
		//				RetentionRuleValidator.COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_COPY_TYPE));
		//		assertThat(errors).has(copyRetentionRuleFieldRequiredError("2",
		//				RetentionRuleValidator.COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_COPY_TYPE));

	}

	@Test
	public void givenCopyRetentionRuleHasNoContentTypeThenError()
			throws Exception {

		copy1_numericPrincipal.setMediumTypeIds(new ArrayList<String>());
		copy1_numericPrincipal.setCopyType(null);

		copy2_secondary.setMediumTypeIds(null);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCodeAndParameters(errors, "code", "index", "field", "value", "errorType", "metadata"))
				.containsOnly(
						tuple("RetentionRuleValidator_invalidCopyRuleField", "Copy2", "2", "mediumTypes", null, "required",
								"copyRetentionRules"),
						tuple("RetentionRuleValidator_invalidCopyRuleField", "Copy3", "3",
								"mediumTypes", null, "required", "copyRetentionRules"),
						tuple("RetentionRuleValidator_invalidCopyRuleField", "Copy2", "2", "copyType", null, "required",
								"copyRetentionRules")
				);

		assertThat(frenchMessages(errors)).containsOnly("Le champ «Supports» de l'exemplaire «Copy3» est requis.",
				"Le champ «Supports» de l'exemplaire «Copy2» est requis.",
				"Le champ «Exemplaire» de l'exemplaire «Copy2» est requis.");

		//assertThat(errors).has(size(3));
		//		assertThat(errors).has(copyRetentionRuleFieldRequiredError("1",
		//				RetentionRuleValidator.COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_COPY_TYPE));
		//		assertThat(errors).has(copyRetentionRuleFieldRequiredError("1",
		//				RetentionRuleValidator.COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_MEDIUM_TYPE));
		//		assertThat(errors).has(copyRetentionRuleFieldRequiredError("2",
		//				RetentionRuleValidator.COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_MEDIUM_TYPE));

	}

	@Test
	public void givenCopyRetentionRuleHasNoActivePeriodThenNoError()
			throws Exception {

		copy1_numericPrincipal.setActiveRetentionPeriod(null);
		copy2_secondary.setActiveRetentionPeriod(null);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors).has(size(0));

	}

	@Test
	public void givenPrincipalCopyRetentionRuleNotNeededThenNoError() {
		when(configProvider.get(RMConfigs.COPY_RULE_PRINCIPAL_REQUIRED)).thenReturn(false);

		when(retentionRule.getCopyRetentionRules())
				.thenReturn(Arrays.asList(copy2_secondary));

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors).has(size(0));
	}

	@Test
	public void givenPrincipalCopyRetentionRuleNeededAndHasNoPrincipalCopyRetentionRuleThenError() {
		when(configProvider.get(RMConfigs.COPY_RULE_PRINCIPAL_REQUIRED)).thenReturn(true);

		when(retentionRule.getCopyRetentionRules())
				.thenReturn(Arrays.asList(copy2_secondary));

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCode(errors)).containsOnly(
				"RetentionRuleValidator_" + RetentionRuleValidator.MUST_SPECIFY_AT_LEAST_ONE_PRINCIPAL_COPY_RETENTON_RULE);
		assertThat(frenchMessages(errors)).containsOnly("Au moins un exemplaire principal doit être renseigné");
	}

	@Test
	public void givenCopyRetentionRuleNotNeededAndHasNoPrincipalCopyRetentionRuleThenNoError() {
		when(configProvider.get(RMConfigs.COPY_RULE_PRINCIPAL_REQUIRED)).thenReturn(true);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors).has(size(0));
	}

	@Test
	public void givenCopyRetentionRuleHasNoSemiActivePeriodThenNoError()
			throws Exception {

		copy1_numericPrincipal.setSemiActiveRetentionPeriod(null);
		copy2_secondary.setSemiActiveRetentionPeriod(null);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors).has(size(0));

	}

	@Test
	public void givenCopyRetentionRuleHasNoDisposalTypeThenError()
			throws Exception {

		copy1_numericPrincipal.setInactiveDisposalType(null);
		copy2_secondary.setInactiveDisposalType(null);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCodeAndParameters(errors, "code", "index", "field", "value", "errorType", "metadata"))
				.containsOnly(
						tuple("RetentionRuleValidator_invalidCopyRuleField", "Copy3", "3", "inactive", null, "required",
								"copyRetentionRules"),
						tuple("RetentionRuleValidator_invalidCopyRuleField", "Copy2", "2", "inactive",
								null, "required", "copyRetentionRules")
				);

		assertThat(frenchMessages(errors)).containsOnly("Le champ «I» de l'exemplaire «Copy3» est requis.",
				"Le champ «I» de l'exemplaire «Copy2» est requis.");

	}

	@Test
	public void givenIntegrityErrorInSecondaryCopyAndNoPrincipalThenNoMissingPrincialError()
			throws Exception {

		when(retentionRule.getCopyRetentionRules()).thenReturn(Arrays.asList(copy2_secondary));

		copy2_secondary.setCopyType(null);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCodeAndParameters(errors, "code", "index", "field", "value", "errorType", "metadata"))
				.containsOnly(
						tuple("RetentionRuleValidator_invalidCopyRuleField", "Copy3", "1", "copyType", null, "required",
								"copyRetentionRules")
				);

		assertThat(frenchMessages(errors)).containsOnly("Le champ «Exemplaire» de l'exemplaire «Copy3» est requis.");

		//		assertThat(errors).has(size(1)).has(copyRetentionRuleFieldRequiredError("0",
		//				RetentionRuleValidator.COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_COPY_TYPE));

	}

	@Test
	public void givenNoSecondaryCopyRetentionRuleWhenValidateThenError()
			throws Exception {

		when(retentionRule.getCopyRetentionRules()).thenReturn(Arrays.asList(copy1_numericPrincipal));

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCode(errors)).containsOnly(
				"RetentionRuleValidator_mustSpecifyOneSecondaryRetentionRule");
		assertThat(frenchMessages(errors)).containsOnly("Un (et un seul) exemplaire secondaire doit être renseigné");

	}

	@Test
	public void givenNoPrincipalCopyRetentionRuleWhenValidateThenError()
			throws Exception {

		when(retentionRule.getCopyRetentionRules()).thenReturn(Arrays.asList(copy2_secondary));

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCode(errors)).containsOnly(
				"RetentionRuleValidator_mustSpecifyAtLeastOnePrincipalRetentionRule");
		assertThat(frenchMessages(errors)).containsOnly("Au moins un exemplaire principal doit être renseigné");

	}

	@Test
	public void givenTwoSecondaryCopyRetentionRuleWhenValidateThenError()
			throws Exception {

		when(retentionRule.getCopyRetentionRules())
				.thenReturn(Arrays.asList(copy2_secondary, copy2_secondary, copy0_analogicPrincipal));

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCode(errors)).containsOnly(
				"RetentionRuleValidator_mustSpecifyOneSecondaryRetentionRule");
		assertThat(frenchMessages(errors)).containsOnly("Un (et un seul) exemplaire secondaire doit être renseigné");

	}

	@Test
	public void givenNoSortDisposalTypeInAnyCopyRetentionRulesAndNoDocumentTypesDisposalWhenValidateThenNoError()
			throws Exception {

		copy0_analogicPrincipal.setInactiveDisposalType(DisposalType.DEPOSIT);
		copy1_numericPrincipal.setInactiveDisposalType(DisposalType.DESTRUCTION);
		copy2_secondary.setInactiveDisposalType(DisposalType.DEPOSIT);

		List<RetentionRuleDocumentType> types = Arrays.asList(
				new RetentionRuleDocumentType("zeDocumentType", DisposalType.DEPOSIT),
				new RetentionRuleDocumentType("otherDocumentType", null)
		);
		when(retentionRule.getDocumentTypesDetails()).thenReturn(types);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors).has(size(0));

	}

	@Test
	public void givenSortDisposalTypeInPrincipalCopyRetentionRulesAndIncompleteDocumentTypesDisposalWhenValidateThenError()
			throws Exception {

		copy0_analogicPrincipal.setInactiveDisposalType(DisposalType.DEPOSIT);
		copy1_numericPrincipal.setInactiveDisposalType(DisposalType.SORT);
		copy2_secondary.setInactiveDisposalType(DisposalType.DESTRUCTION);

		List<RetentionRuleDocumentType> types = Arrays.asList(
				new RetentionRuleDocumentType("zeDocumentType", DisposalType.DEPOSIT),
				new RetentionRuleDocumentType("otherDocumentType", null)
		);
		when(retentionRule.getDocumentTypesDetails()).thenReturn(types);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors).has(size(1)).has(missingDocumentTypeDisposalErrorAtIndex(1));
		assertThat(frenchMessages(errors)).containsOnly("Le mode de disposition n'a pas été configuré pour un type de document.");

	}

	@Test
	public void givenSortDisposalTypeInSecondaryCopyRetentionRulesAndCompleteDocumentTypesDisposalWhenValidateThenNoError()
			throws Exception {

		copy0_analogicPrincipal.setInactiveDisposalType(DisposalType.DEPOSIT);
		copy1_numericPrincipal.setInactiveDisposalType(DisposalType.DESTRUCTION);

		List<RetentionRuleDocumentType> types = Arrays.asList(
				new RetentionRuleDocumentType("zeDocumentType", DisposalType.DEPOSIT),
				new RetentionRuleDocumentType("otherDocumentType", DisposalType.DESTRUCTION)
		);
		when(retentionRule.getDocumentTypesDetails()).thenReturn(types);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors).has(size(0));

	}

	@Test
	public void givenSortDisposalTypeInSecondaryCopyRetentionRulesAndInvalidSortDisposalTypeWhenValidateThenError()
			throws Exception {

		copy0_analogicPrincipal.setInactiveDisposalType(DisposalType.DEPOSIT);
		copy1_numericPrincipal.setInactiveDisposalType(DisposalType.DESTRUCTION);
		copy2_secondary.setInactiveDisposalType(DisposalType.SORT);

		List<RetentionRuleDocumentType> types = Arrays.asList(
				new RetentionRuleDocumentType("zeDocumentType", DisposalType.DEPOSIT),
				new RetentionRuleDocumentType("otherDocumentType", DisposalType.SORT)
		);
		when(retentionRule.getDocumentTypesDetails()).thenReturn(types);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCode(errors)).containsOnly("RetentionRuleValidator_" +
				RetentionRuleValidator.MISSING_DOCUMENT_TYPE_DISPOSAL);
		assertThat(frenchMessages(errors)).containsOnly("Le mode de disposition n'a pas été configuré pour un type de document.");
	}

	@Test
	public void givenFolderScopeWithDefaultCopyRulesThenError()
			throws Exception {
		when(configProvider.get(RMConfigs.DOCUMENT_RETENTION_RULES)).thenReturn(true);
		when(retentionRule.getPrincipalDefaultDocumentCopyRetentionRule()).thenReturn(copy1_numericPrincipal);
		when(retentionRule.getSecondaryDefaultDocumentCopyRetentionRule()).thenReturn(copy2_secondary);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCode(errors)).containsOnly(
				"RetentionRuleValidator_" + RetentionRuleValidator.PRINCIPAL_DEFAULT_COPY_RETENTION_RULE_IN_FOLDER_RULE,
				"RetentionRuleValidator_" + RetentionRuleValidator.SECONDARY_DEFAULT_COPY_RETENTION_RULE_IN_FOLDER_RULE);
		assertThat(frenchMessages(errors))
				.containsOnly("Un exemplaire principal est requis", "Un exemplaire secondaire est requis");
	}

	@Test
	public void givenDocumentScopeWithNoSecondaryDefaultCopyRuleThenError()
			throws Exception {
		givenValidDocumentScope();
		when(configProvider.get(RMConfigs.DOCUMENT_RETENTION_RULES)).thenReturn(true);
		when(retentionRule.getSecondaryDefaultDocumentCopyRetentionRule()).thenReturn(null);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCode(errors)).containsOnly("RetentionRuleValidator_" +
				RetentionRuleValidator.SECONDARY_DEFAULT_COPY_RETENTION_RULE_REQUIRED_IN_DOCUMENT_RULE);
		assertThat(frenchMessages(errors)).containsOnly("Un exemplaire secondaire est requis");
	}

	@Test
	public void givenDocumentScopeWithNoPrincipalDefaultCopyRuleThenError()
			throws Exception {
		givenValidDocumentScope();
		when(configProvider.get(RMConfigs.DOCUMENT_RETENTION_RULES)).thenReturn(true);
		when(retentionRule.getPrincipalDefaultDocumentCopyRetentionRule()).thenReturn(null);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCode(errors)).containsOnly("RetentionRuleValidator_" +
				RetentionRuleValidator.PRINCIPAL_DEFAULT_COPY_RETENTION_RULE_REQUIRED_IN_DOCUMENT_RULE);
		assertThat(frenchMessages(errors)).containsOnly("Un exemplaire principal est requis");
	}

	@Test
	public void givenValidRuleWithDocumentScopeThenNoError()
			throws Exception {
		givenValidDocumentScope();
		when(configProvider.get(RMConfigs.DOCUMENT_RETENTION_RULES)).thenReturn(true);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors).has(size(0));
	}

	@Test
	public void givenDocumentScopeWithDocumentTypesThenError()
			throws Exception {
		givenValidDocumentScope();
		when(configProvider.get(RMConfigs.DOCUMENT_RETENTION_RULES)).thenReturn(true);
		when(retentionRule.getDocumentTypesDetails()).thenReturn(types);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCode(errors)).containsOnly(
				"RetentionRuleValidator_" + RetentionRuleValidator.DOCUMENT_TYPES_IN_DOCUMENT_RULE);
		assertThat(frenchMessages(errors))
				.containsOnly("Le champ 'Types de document' doit être vide pour les règles de documents");
	}

	@Test
	public void givenDocumentCopyRetentionRulesWithNoDocumentTypeThenErrors()
			throws Exception {
		when(configProvider.get(RMConfigs.DOCUMENT_RETENTION_RULES)).thenReturn(true);
		docCopy1_principal.setTypeId((String) null);
		docCopy2_principal.setTypeId((String) null);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCodeAndParameters(errors, "code", "index", "field", "value", "errorType", "metadata"))
				.containsOnly(
						tuple("RetentionRuleValidator_invalidCopyRuleField", "DocCopy1", "1", "type", null, "required",
								"documentCopyRetentionRules"),
						tuple("RetentionRuleValidator_invalidCopyRuleField", "DocCopy2", "2", "type", null, "required",
								"documentCopyRetentionRules")
				);

		assertThat(frenchMessages(errors))
				.containsOnly("Le champ «Type» de l'exemplaire principal pour documents «DocCopy1» est requis.",
						"Le champ «Type» de l'exemplaire principal pour documents «DocCopy2» est requis.");
		//
		//		assertThat(errors).has(size(2))
		//				.has(copyRetentionRuleFieldRequiredError("0",
		//						RetentionRuleValidator.DOCUMENT_COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_DOCUMENT_TYPE))
		//				.has(copyRetentionRuleFieldRequiredError("1",
		//						RetentionRuleValidator.DOCUMENT_COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_DOCUMENT_TYPE));
	}

	@Test
	public void givenDocumentScopeNoPrincipalDocumentCopyRuleThenError()
			throws Exception {
		when(configProvider.get(RMConfigs.DOCUMENT_RETENTION_RULES)).thenReturn(true);
		givenValidDocumentScope();
		when(retentionRule.getDocumentCopyRetentionRules()).thenReturn(new ArrayList<CopyRetentionRule>());

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCode(errors)).containsOnly("RetentionRuleValidator_" +
				RetentionRuleValidator.MUST_SPECIFY_AT_LEAST_ONE_PRINCIPAL_DOCUMENT_COPY_RETENTON_RULE);
		assertThat(frenchMessages(errors)).containsOnly("Au moins un exemplaire principal de documents est requis");
	}

	@Test
	public void givenSecondaryDocumentCopyRuleThenError()
			throws Exception {
		when(configProvider.get(RMConfigs.DOCUMENT_RETENTION_RULES)).thenReturn(true);
		docCopy2_principal.setCopyType(CopyType.SECONDARY);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCode(errors)).containsOnly("RetentionRuleValidator_" +
				RetentionRuleValidator.MUST_NOT_SPECIFY_SECONDARY_DOCUMENT_COPY_RETENTON_RULE);
		assertThat(frenchMessages(errors))
				.containsOnly("La liste d'exemplaires principaux de la règle de documents contient un exemplaire secondaire");
	}

	@Test
	public void givenDocumentScopeWithCopyRulesThenError()
			throws Exception {
		when(configProvider.get(RMConfigs.DOCUMENT_RETENTION_RULES)).thenReturn(true);
		givenValidDocumentScope();
		when(retentionRule.getCopyRetentionRules()).thenReturn(Arrays.asList(copy0_analogicPrincipal));

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCode(errors)).containsOnly("RetentionRuleValidator_" +
				RetentionRuleValidator.DOCUMENT_RULE_MUST_HAVE_ONLY_DOCUMENT_COPY_RULES);
		assertThat(frenchMessages(errors))
				.containsOnly("Une règle de conservation avec portée sur documents ne peut pas avoir d'exemplaires de dossiers");
	}

	@Test
	public void whenSavingRetentionRuleWithNoPrincipalCopyRuleWithoutTypeThenValidationException()
			throws Exception {

		copy0_analogicPrincipal.setTypeId("zeType");
		copy1_numericPrincipal.setTypeId("zeType");
		validator.validate(retentionRule, schema, configProvider, errors);
		assertThat(extractingSimpleCode(errors)).containsOnly(
				"RetentionRuleValidator_" + RetentionRuleValidator.PRINCIPAL_COPY_WITHOUT_TYPE_REQUIRED);
		assertThat(frenchMessages(errors)).containsOnly("Un exemplaire principal sans type est requis");

	}

	@Test
	public void whenSavingRetentionRuleWithOnePrincipalCopyWithTypeAndOneWithoutThenOk()
			throws Exception {

		copy0_analogicPrincipal.setTypeId("zeType");
		validator.validate(retentionRule, schema, configProvider, errors);
		assertThat(errors.getValidationErrors()).isEmpty();

	}

	@Test
	public void whenSavingRetentionRuleWithSecondaryCopyWithTypeThenException()
			throws Exception {

		copy2_secondary.setTypeId("zeType");
		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(extractingSimpleCodeAndParameters(errors, "code", "index", "field", "value", "errorType", "metadata"))
				.containsOnly(
						tuple("RetentionRuleValidator_invalidCopyRuleField", "Copy3", "3", "type", null, "nullRequired",
								"copyRetentionRules")
				);
		assertThat(frenchMessages(errors)).containsOnly("Le champ «Type» de l'exemplaire «Copy3» ne doit pas être spécifié.");

	}

	public void givenValidDocumentScope() {
		when(retentionRule.getScope()).thenReturn(RetentionRuleScope.DOCUMENTS);
		when(retentionRule.getCopyRetentionRules()).thenReturn(new ArrayList<CopyRetentionRule>());
		when(retentionRule.getDocumentTypesDetails()).thenReturn(new ArrayList<RetentionRuleDocumentType>());
		when(retentionRule.getPrincipalDefaultDocumentCopyRetentionRule()).thenReturn(copy1_numericPrincipal);
		when(retentionRule.getSecondaryDefaultDocumentCopyRetentionRule()).thenReturn(copy2_secondary);
	}

	private Condition<? super ValidationErrors> copyRetentionRuleFieldRequiredError(String index, String field) {
		return error("TODO", "index", index, "field", field,
				METADATA_CODE, COPY_RETENTION_RULES);
	}

	private Condition<? super ValidationErrors> copyRetentionRuleFieldError(String code) {
		return error(code,
				METADATA_CODE, COPY_RETENTION_RULES);
	}

	private Condition<? super ValidationErrors> missingDocumentTypeDisposalErrorAtIndex(int index) {
		return error(RetentionRuleValidator.MISSING_DOCUMENT_TYPE_DISPOSAL,
				RetentionRuleValidator.MISSING_DOCUMENT_TYPE_DISPOSAL_INDEX, "" + index);
	}

	//	private Condition<? super ValidationErrors> copyRetentionRuleFieldErrorDuplicatedContentTypes(String contentTypes) {
	//		return error(RetentionRuleValidator.PRINCIPAL_COPIES_MUST_HAVE_DIFFERENT_CONTENT_TYPES,
	//				RetentionRuleValidator.PRINCIPAL_COPIES_MUST_HAVE_DIFFERENT_CONTENT_TYPES_DUPLICATES, contentTypes,
	//				RecordMetadataValidator.METADATA_CODE, RetentionRule.COPY_RETENTION_RULES,
	//				RecordMetadataValidator.METADATA_LABEL, "zeCopyRules");
	//	}

	private Condition<? super ValidationErrors> size(final int size) {
		return new Condition<ValidationErrors>() {
			@Override
			public boolean matches(ValidationErrors value) {
				assertThat(value.getValidationErrors()).hasSize(size);
				return true;
			}
		};
	}

	private Condition<? super ValidationErrors> error(final String code, final String... parameters) {
		return new Condition<ValidationErrors>() {
			@Override
			public boolean matches(ValidationErrors value) {
				for (ValidationError error : value.getValidationErrors()) {
					if (error.getCode().endsWith(code)) {
						boolean sameParameters = true;

						for (int i = 0; i < parameters.length; i += 2) {
							String paramKey = parameters[i];
							String paramValue = parameters[i + 1];
							if (!paramValue.equals(error.getParameters().get(paramKey))) {
								sameParameters = false;
							}
						}
						if (sameParameters) {
							return true;
						}
					}
				}

				return false;
			}
		}.describedAs("error '" + code + "' with parameters " + parameters);
	}

}
