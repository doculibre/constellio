package com.constellio.app.modules.complementary.esRmRobots.validators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.assertj.core.api.Condition;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters;
import com.constellio.app.modules.rm.model.enums.CopyType;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.frameworks.validation.ValidationError;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.sdk.tests.ConstellioTest;

public class ClassifyConnectorTaxonomyActionParametersValidatorTest extends ConstellioTest {

	ClassifyConnectorTaxonomyActionParametersValidator validator = new ClassifyConnectorTaxonomyActionParametersValidator();

	@Mock MetadataSchema schema;

	@Mock ClassifyConnectorFolderInTaxonomyActionParameters parameters;
	@Mock ConfigProvider configProvider;

	ValidationErrors errors = new ValidationErrors();

	@Before
	public void setUp()
			throws Exception {

	}

	@Test
	public void givenParentFolderAndNoTaxoOrDefaultValuesThenNoErrors()
			throws Exception {
		when(parameters.getDefaultParentFolder()).thenReturn("folderId");
		when(parameters.getDefaultOpenDate()).thenReturn(new LocalDate());

		validator.validate(parameters, schema, configProvider, errors);

		assertThat(errors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenNoTaxoAndNoParentFolderButAllDefaultValuesThenNoErrors()
			throws Exception {
		when(parameters.getDefaultAdminUnit()).thenReturn("unitId");
		when(parameters.getDefaultCategory()).thenReturn("categoryId");
		when(parameters.getDefaultRetentionRule()).thenReturn("retRuleId");
		when(parameters.getDefaultCopyStatus()).thenReturn(CopyType.PRINCIPAL);
		when(parameters.getDefaultOpenDate()).thenReturn(new LocalDate());

		validator.validate(parameters, schema, configProvider, errors);

		assertThat(errors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenTaxoAndNoParentFolderButAllDefaultValuesThenNoErrors()
			throws Exception {
		when(parameters.getInTaxonomy()).thenReturn("taxo");
		when(parameters.getPathPrefix()).thenReturn("prefix");
		when(parameters.getDefaultAdminUnit()).thenReturn("unitId");
		when(parameters.getDefaultCategory()).thenReturn("categoryId");
		when(parameters.getDefaultRetentionRule()).thenReturn("retRuleId");
		when(parameters.getDefaultCopyStatus()).thenReturn(CopyType.PRINCIPAL);
		when(parameters.getDefaultOpenDate()).thenReturn(new LocalDate());

		validator.validate(parameters, schema, configProvider, errors);

		assertThat(errors.getValidationErrors()).isEmpty();
	}

	@Test
	public void givenParentFolderAndTaxoThenError()
			throws Exception {
		when(parameters.getInTaxonomy()).thenReturn("taxo");
		when(parameters.getPathPrefix()).thenReturn("prefix");
		when(parameters.getDefaultParentFolder()).thenReturn("folderId");
		when(parameters.getDefaultOpenDate()).thenReturn(new LocalDate());

		validator.validate(parameters, schema, configProvider, errors);

		assertThat(errors).has(size(1))
				.has(error(ClassifyConnectorTaxonomyActionParametersValidator.MUST_SPECIFY_TAXO_XOR_DEFAULT_PARENT_FOLDER));
	}

	@Test
	public void givenParentFolderAndAllDefaultValuesThenError()
			throws Exception {
		when(parameters.getDefaultParentFolder()).thenReturn("folderId");
		when(parameters.getDefaultAdminUnit()).thenReturn("unitId");
		when(parameters.getDefaultCategory()).thenReturn("categoryId");
		when(parameters.getDefaultRetentionRule()).thenReturn("retRuleId");
		when(parameters.getDefaultCopyStatus()).thenReturn(CopyType.PRINCIPAL);
		when(parameters.getDefaultOpenDate()).thenReturn(new LocalDate());

		validator.validate(parameters, schema, configProvider, errors);

		assertThat(errors).has(size(1)).has(error(
				ClassifyConnectorTaxonomyActionParametersValidator.MUST_NOT_SPECIFY_DEFAULT_VALUES_WITH_PARENT_FOLDER));
	}

	@Test
	public void givenParentFolderWithNoMappingAndNoOpenDateThenError()
			throws Exception {
		when(parameters.getDefaultParentFolder()).thenReturn("folderId");

		validator.validate(parameters, schema, configProvider, errors);

		assertThat(errors).has(size(1)).has(error(
				ClassifyConnectorTaxonomyActionParametersValidator.MUST_SPECIFY_DEFAULT_OPENDATE_OR_MAPPING));
	}

	@Test
	public void givenTaxoAndNoMappingAndMissingDefaultValueThenError()
			throws Exception {
		when(parameters.getInTaxonomy()).thenReturn("taxo");
		when(parameters.getPathPrefix()).thenReturn("prefix");
		when(parameters.getDefaultCategory()).thenReturn("categoryId");
		when(parameters.getDefaultRetentionRule()).thenReturn("retRuleId");
		when(parameters.getDefaultCopyStatus()).thenReturn(CopyType.PRINCIPAL);
		when(parameters.getDefaultOpenDate()).thenReturn(new LocalDate());

		validator.validate(parameters, schema, configProvider, errors);

		assertThat(errors).has(size(1)).has(error(
				ClassifyConnectorTaxonomyActionParametersValidator.MUST_SPECIFY_ALL_DEFAULT_VALUES_WHEN_NO_MAPPING_AND_NO_PARENT_FOLDER));
	}

	@Test
	public void givenTaxoAndNoPathPrefixThenError()
			throws Exception {
		when(parameters.getInTaxonomy()).thenReturn("taxo");
		when(parameters.getDefaultCategory()).thenReturn("categoryId");
		when(parameters.getDefaultRetentionRule()).thenReturn("retRuleId");
		when(parameters.getDefaultCopyStatus()).thenReturn(CopyType.PRINCIPAL);
		when(parameters.getDefaultOpenDate()).thenReturn(new LocalDate());

		validator.validate(parameters, schema, configProvider, errors);

		assertThat(errors).has(size(1)).has(error(
				ClassifyConnectorTaxonomyActionParametersValidator.MUST_SPECIFY_PATH_PREFIX_WITH_TAXO));
	}

	@Test
	public void givenNoTaxoAndNoMappingAndMissingDefaultValueThenError()
			throws Exception {
		when(parameters.getPathPrefix()).thenReturn("prefix");
		when(parameters.getDefaultCategory()).thenReturn("categoryId");
		when(parameters.getDefaultRetentionRule()).thenReturn("retRuleId");
		when(parameters.getDefaultCopyStatus()).thenReturn(CopyType.PRINCIPAL);
		when(parameters.getDefaultOpenDate()).thenReturn(new LocalDate());

		validator.validate(parameters, schema, configProvider, errors);

		assertThat(errors).has(size(1)).has(error(
				ClassifyConnectorTaxonomyActionParametersValidator.MUST_SPECIFY_ALL_DEFAULT_VALUES_WHEN_NO_MAPPING_AND_NO_PARENT_FOLDER));
	}

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
