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
package com.constellio.app.modules.rm.model.validators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.model.RetentionPeriod;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.app.modules.rm.model.enums.DisposalType;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.structures.RetentionRuleDocumentType;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.sdk.tests.ConstellioTest;

public class RetentionRuleValidatorTest extends ConstellioTest {

	CopyRetentionRule copy0_analogicPrincipal = new CopyRetentionRule();
	CopyRetentionRule copy1_numericPrincipal = new CopyRetentionRule();
	CopyRetentionRule copy2_secondary = new CopyRetentionRule();

	RetentionRuleValidator validator = new RetentionRuleValidator();

	@Mock Metadata uniformSubdivisionMetadata, categoriesMetadata, copyRetentionRuleMetadata;
	@Mock MetadataSchema schema;

	@Mock RetentionRule retentionRule;
	@Mock ConfigProvider configProvider;

	ValidationErrors errors = new ValidationErrors();

	@Before
	public void setUp()
			throws Exception {

		when(retentionRule.getCode()).thenReturn("zeCode");
		when(retentionRule.getAdministrativeUnits())
				.thenReturn(Arrays.asList("firstAdministrativeUnitId", "secondAdministrativeUnitId"));
		when(retentionRule.getCopyRetentionRules())
				.thenReturn(Arrays.asList(copy0_analogicPrincipal, copy1_numericPrincipal, copy2_secondary));

		List<RetentionRuleDocumentType> types = Arrays.asList(
				new RetentionRuleDocumentType("firstDocumentTypeId", DisposalType.DEPOSIT),
				new RetentionRuleDocumentType("secondDocumentTypeId", DisposalType.DESTRUCTION)
		);
		when(retentionRule.getDocumentTypesDetails()).thenReturn(types);
		when(configProvider.get(RMConfigs.COPY_RULE_PRINCIPAL_REQUIRED)).thenReturn(true);

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

		when(schema.getMetadata(RetentionRule.COPY_RETENTION_RULES)).thenReturn(copyRetentionRuleMetadata);
		when(copyRetentionRuleMetadata.getLabel()).thenReturn("zeCopyRules");
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

		assertThat(errors).has(size(1))
				.has(error(RetentionRuleValidator.MUST_SPECIFY_ADMINISTRATIVE_UNITS_XOR_RESPONSIBLES_FLAG));

	}

	@Test
	public void givenAdministrativeUnitsAndResponsibleAdministrativeUnitsFlagSetToTrueWhenValidateThenError()
			throws Exception {

		when(retentionRule.getAdministrativeUnits()).thenReturn(Arrays.asList("administrativeUnitId"));
		when(retentionRule.isResponsibleAdministrativeUnits()).thenReturn(true);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors).has(size(1))
				.has(error(RetentionRuleValidator.MUST_SPECIFY_ADMINISTRATIVE_UNITS_XOR_RESPONSIBLES_FLAG));

	}

	@Test
	public void givenCopyRetentionRuleHasNoCopyTypeThenError()
			throws Exception {

		copy0_analogicPrincipal.setCopyType(null);
		copy2_secondary.setCopyType(null);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors).has(size(2));
		assertThat(errors).has(copyRetentionRuleFieldRequiredError("0",
				RetentionRuleValidator.COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_COPY_TYPE));
		assertThat(errors).has(copyRetentionRuleFieldRequiredError("2",
				RetentionRuleValidator.COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_COPY_TYPE));

	}

	@Test
	public void givenCopyRetentionRuleHasNoContentTypeThenError()
			throws Exception {

		copy1_numericPrincipal.setMediumTypeIds(new ArrayList<String>());
		copy1_numericPrincipal.setCopyType(null);

		copy2_secondary.setMediumTypeIds(null);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors).has(size(3));
		assertThat(errors).has(copyRetentionRuleFieldRequiredError("1",
				RetentionRuleValidator.COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_COPY_TYPE));
		assertThat(errors).has(copyRetentionRuleFieldRequiredError("1",
				RetentionRuleValidator.COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_MEDIUM_TYPE));
		assertThat(errors).has(copyRetentionRuleFieldRequiredError("2",
				RetentionRuleValidator.COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_MEDIUM_TYPE));

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

		assertThat(errors).has(size(1)).has(error(RetentionRuleValidator.MUST_SPECIFY_AT_LEAST_ONE_PRINCIPAL_COPY_RETENTON_RULE));
	}

	@Test
	public void	givenCopyRetentionRuleNotNeededAndHasNoPrincipalCopyRetentionRuleThenNoError() {
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

		assertThat(errors).has(size(2));
		assertThat(errors).has(copyRetentionRuleFieldRequiredError("1",
				RetentionRuleValidator.COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_DISPOSAL));
		assertThat(errors).has(copyRetentionRuleFieldRequiredError("2",
				RetentionRuleValidator.COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_DISPOSAL));

	}

	@Test
	public void givenIntegrityErrorInSecondaryCopyAndNoPrincipalThenNoMissingPrincialError()
			throws Exception {

		when(retentionRule.getCopyRetentionRules()).thenReturn(Arrays.asList(copy2_secondary));

		copy2_secondary.setCopyType(null);

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors).has(size(1)).has(copyRetentionRuleFieldRequiredError("0",
				RetentionRuleValidator.COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD_COPY_TYPE));

	}

	@Test
	public void givenNoSecondaryCopyRetentionRuleWhenValidateThenError()
			throws Exception {

		when(retentionRule.getCopyRetentionRules()).thenReturn(Arrays.asList(copy1_numericPrincipal));

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors).has(size(1)).has(copyRetentionRuleFieldError(
				RetentionRuleValidator.MUST_SPECIFY_ONE_SECONDARY_COPY_RETENTON_RULE));

	}

	@Test
	public void givenNoPrincipalCopyRetentionRuleWhenValidateThenError()
			throws Exception {

		when(retentionRule.getCopyRetentionRules()).thenReturn(Arrays.asList(copy2_secondary));

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors).has(size(1)).has(copyRetentionRuleFieldError(
				RetentionRuleValidator.MUST_SPECIFY_AT_LEAST_ONE_PRINCIPAL_COPY_RETENTON_RULE));

	}

	@Test
	public void givenTwoSecondaryCopyRetentionRuleWhenValidateThenError()
			throws Exception {

		when(retentionRule.getCopyRetentionRules())
				.thenReturn(Arrays.asList(copy2_secondary, copy2_secondary, copy0_analogicPrincipal));

		validator.validate(retentionRule, schema, configProvider, errors);

		assertThat(errors).has(size(1)).has(copyRetentionRuleFieldError(
				RetentionRuleValidator.MUST_SPECIFY_ONE_SECONDARY_COPY_RETENTON_RULE));

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

		assertThat(errors).has(size(1)).has(error(RetentionRuleValidator.MISSING_DOCUMENT_TYPE_DISPOSAL));

	}

	private Condition<? super ValidationErrors> copyRetentionRuleFieldRequiredError(String index, String field) {
		return error(RetentionRuleValidator.COPY_RETENTION_RULE_FIELD_REQUIRED,
				RetentionRuleValidator.COPY_RETENTION_RULE_FIELD_REQUIRED_INDEX, index,
				RetentionRuleValidator.COPY_RETENTION_RULE_FIELD_REQUIRED_FIELD, field,
				RecordMetadataValidator.METADATA_CODE, RetentionRule.COPY_RETENTION_RULES,
				RecordMetadataValidator.METADATA_LABEL, "zeCopyRules");
	}

	private Condition<? super ValidationErrors> copyRetentionRuleFieldError(String code) {
		return error(code,
				RecordMetadataValidator.METADATA_CODE, RetentionRule.COPY_RETENTION_RULES,
				RecordMetadataValidator.METADATA_LABEL, "zeCopyRules");
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
