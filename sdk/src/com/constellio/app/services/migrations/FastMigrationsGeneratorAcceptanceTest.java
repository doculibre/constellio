package com.constellio.app.services.migrations;

import javax.lang.model.element.Modifier;

import org.junit.Test;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;

public class FastMigrationsGeneratorAcceptanceTest extends ConstellioTest {

	@Test
	public void genereCoreMigrations()
			throws Exception {

		givenCollection(zeCollection);

		System.out.println(" ------ Migration script ------");

		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

		Builder main = MethodSpec.methodBuilder("applyGeneratedSchemaAlteration")
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class)
				.addParameter(MetadataSchemaTypesBuilder.class, "typesBuilder");

		for (MetadataSchemaType type : types.getSchemaTypes()) {

			main.addStatement("MetadataSchemaTypeBuilder $LSchemaType = typesBuilder.createNewSchemaType($S)$L",
					type.getCode(), type.getCode(), typeAlterations(type));
			for (MetadataSchema schema : type.getAllSchemas()) {
				if ("default".equals(schema.getLocalCode())) {
					main.addStatement("MetadataSchemaBuilder $L = $LSchemaType.getDefaultSchema()",
							variableOf(schema), type.getCode());
				} else {
					main.addStatement("MetadataSchemaBuilder $L = $LSchemaType.createCustomSchema($S)",
							variableOf(schema), type.getCode(), schema.getLocalCode());
				}
				for (RecordValidator validator : type.getDefaultSchema().getValidators()) {
					main.addStatement("$L.defineValidators().add($T)", variableOf(schema), validator.getClass());
				}
			}
		}
		for (MetadataSchemaType type : types.getSchemaTypes()) {
			for (MetadataSchema schema : type.getAllSchemas()) {
				for (Metadata metadata : schema.getMetadatas()) {
					if (metadata.getInheritance() == null) {
						if (!Schemas.isGlobalMetadata(metadata.getLocalCode())) {
							main.addStatement(
									"MetadataBuilder $L = $L.create($S).setType(MetadataValueType.$L)$L",
									variableOf(metadata), variableOf(schema), metadata.getLocalCode(), metadata.getType().name(),
									metadataAlterations(metadata));
						}
					}
				}
			}

		}

		System.out.println(main.build().toString());

	}

	private String variableOf(MetadataSchemaType schemaType) {
		return schemaType.getCode() + "SchemaType";
	}

	private String variableOf(MetadataSchema schema) {
		if ("default".equals(schema.getLocalCode())) {
			return schema.getCode().split("_")[0] + "Schema";
		} else {
			return schema.getCode() + "Schema";
		}
	}

	private String variableOf(Metadata metadata) {
		if (metadata.getCode().contains("_default_")) {
			return metadata.getCode().split("_")[0] + "_" + metadata.getCode().split("_")[2];
		} else {
			return metadata.getCode();
		}
	}

	private String typeAlterations(MetadataSchemaType type) {
		StringBuilder stringBuilder = new StringBuilder();

		if (!type.hasSecurity()) {
			stringBuilder.append(".setSecurity(false)");
		}
		if (!type.isInTransactionLog()) {
			stringBuilder.append(".setInTransactionLog(false)");
		}
		if (type.isUndeletable()) {
			stringBuilder.append(".setUndeletable(true)");
		}

		return stringBuilder.toString();
	}

	private String metadataAlterations(Metadata metadata) {
		StringBuilder stringBuilder = new StringBuilder();

		if (metadata.isMultivalue()) {
			stringBuilder.append(".setMultivalue(true)");
		}

		if (metadata.isDefaultRequirement()) {
			stringBuilder.append(".setDefaultRequirement(true)");
		}

		if (metadata.isSystemReserved()) {
			stringBuilder.append(".setSystemReserved(true)");
		}

		if (metadata.isUndeletable()) {
			stringBuilder.append(".setUndeletable(true)");
		}

		if (!metadata.isEnabled()) {
			stringBuilder.append(".setEnabled(false)");
		}

		if (metadata.isEssential()) {
			stringBuilder.append(".setEssential(true)");
		}

		if (metadata.isEssentialInSummary()) {
			stringBuilder.append(".setEssentialInSummary(true)");
		}

		if (metadata.isSchemaAutocomplete()) {
			stringBuilder.append(".setSchemaAutocomplete(true)");
		}

		if (metadata.isSearchable()) {
			stringBuilder.append(".setSearchable(true)");
		}

		if (metadata.isSortable()) {
			stringBuilder.append(".setSortable(true)");
		}

		if (metadata.isUniqueValue()) {
			stringBuilder.append(".setUniqueValue(true)");
		}

		if (metadata.isUnmodifiable()) {
			stringBuilder.append(".setUnmodifiable(true)");
		}

		if (metadata.getType() == MetadataValueType.REFERENCE) {
			MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
			String referencedType = metadata.getAllowedReferences().getTypeWithAllowedSchemas();
			String referencedTypeVariable = variableOf(types.getSchemaType(referencedType));
			if (metadata.isTaxonomyRelationship()) {
				stringBuilder.append(".defineTaxonomyRelationshipToType(" + referencedTypeVariable + ")");

			} else if (metadata.isChildOfRelationship()) {
				stringBuilder.append(".defineChildOfRelationshipToType(" + referencedTypeVariable + ")");

			} else {
				stringBuilder.append(".defineReferencesTo(" + referencedTypeVariable + ")");

			}
		}

		return stringBuilder.toString();
	}

}
