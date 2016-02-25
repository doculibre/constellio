package com.constellio.app.modules.complementary.esRmRobots.validators;

import org.apache.commons.lang.StringUtils;

import com.constellio.app.modules.complementary.esRmRobots.model.ClassifyConnectorFolderInTaxonomyActionParameters;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.ConfigProvider;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.frameworks.validation.ValidationErrors;

public class ClassifyConnectorTaxonomyActionParametersValidator implements RecordValidator {

	public static final String MUST_SPECIFY_TAXO_XOR_DEFAULT_PARENT_FOLDER = "mustSpecifyTaxoXORDefaultParentFolder";
	public static final String MUST_NOT_SPECIFY_DEFAULT_VALUES_WITH_PARENT_FOLDER = "mustNotSpecifyDefaultValuesWithParentFolder";
	public static final String MUST_SPECIFY_DEFAULT_OPENDATE_OR_MAPPING = "mustSpecifyDefaultOpenDateOrMapping";
	public static final String MUST_SPECIFY_PATH_PREFIX_WITH_TAXO = "mustSpecifyPathPrefixWithTaxo";
	public static final String MUST_SPECIFY_ALL_DEFAULT_VALUES_WHEN_NO_MAPPING_AND_NO_PARENT_FOLDER = "mustSpecifyAllDefaultValuesWhenNoMappingAndNoParentFolder";

	@Override
	public void validate(Record record, MetadataSchemaTypes types, MetadataSchema schema, ConfigProvider configProvider,
			ValidationErrors validationErrors) {
		ClassifyConnectorFolderInTaxonomyActionParameters parameters = new ClassifyConnectorFolderInTaxonomyActionParameters(record, types);
		validate(parameters, schema, configProvider, validationErrors);
	}

	void validate(ClassifyConnectorFolderInTaxonomyActionParameters parameters, MetadataSchema schema, ConfigProvider configProvider,
			ValidationErrors validationErrors) {
		if (StringUtils.isNotEmpty(parameters.getDefaultParentFolder())) {
			validateParentFolderUseCase(parameters, validationErrors);
		} else if(StringUtils.isNotEmpty(parameters.getInTaxonomy())) {
			validateClassifyInTaxoUseCase(parameters, validationErrors);
		} else {
			validateNoTaxoAndNoParentFolderUseCase(parameters, validationErrors);
		}
	}

	private void validateNoTaxoAndNoParentFolderUseCase(ClassifyConnectorFolderInTaxonomyActionParameters parameters,
			ValidationErrors validationErrors) {
		if(StringUtils.isEmpty(parameters.getDefaultAdminUnit()) || StringUtils.isEmpty(parameters.getDefaultRetentionRule()) || StringUtils.isEmpty(parameters.getDefaultCategory()) || parameters.getDefaultCopyStatus() == null || parameters.getDefaultOpenDate() == null) {
			validationErrors.add(getClass(),MUST_SPECIFY_ALL_DEFAULT_VALUES_WHEN_NO_MAPPING_AND_NO_PARENT_FOLDER);
		}
	}

	private void validateClassifyInTaxoUseCase(ClassifyConnectorFolderInTaxonomyActionParameters parameters,
			ValidationErrors validationErrors) {
		if(StringUtils.isEmpty(parameters.getPathPrefix())) {
			validationErrors.add(getClass(),MUST_SPECIFY_PATH_PREFIX_WITH_TAXO);
		} else if(parameters.getFolderMapping() == null) {
			if(StringUtils.isEmpty(parameters.getDefaultAdminUnit()) || StringUtils.isEmpty(parameters.getDefaultRetentionRule()) || StringUtils.isEmpty(parameters.getDefaultCategory()) || parameters.getDefaultCopyStatus() == null || parameters.getDefaultOpenDate() == null) {
				validationErrors.add(getClass(),MUST_SPECIFY_ALL_DEFAULT_VALUES_WHEN_NO_MAPPING_AND_NO_PARENT_FOLDER);
			}
		}
	}

	private void validateParentFolderUseCase(ClassifyConnectorFolderInTaxonomyActionParameters parameters,
			ValidationErrors validationErrors) {
		if (StringUtils.isNotEmpty(parameters.getInTaxonomy())) {
			validationErrors.add(getClass(), MUST_SPECIFY_TAXO_XOR_DEFAULT_PARENT_FOLDER);
		} else if(StringUtils.isNotEmpty(parameters.getDefaultAdminUnit()) || StringUtils.isNotEmpty(parameters.getDefaultRetentionRule()) || StringUtils.isNotEmpty(parameters.getDefaultCategory())) {
			validationErrors.add(getClass(),MUST_NOT_SPECIFY_DEFAULT_VALUES_WITH_PARENT_FOLDER);
		} else if(parameters.getFolderMapping() == null && parameters.getDefaultOpenDate() == null) {
			validationErrors.add(getClass(),MUST_SPECIFY_DEFAULT_OPENDATE_OR_MAPPING);
		}
	}
}
