package com.constellio.app.services.migrations;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.sdk.dev.tools.i18n.CombinePropertyFilesServices;
import com.constellio.sdk.tests.AppLayerConfigurationAlteration;
import com.constellio.sdk.tests.ConstellioTest;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeSpec;

public class FastMigrationsGeneratorAcceptanceTest extends ConstellioTest {

	@Test
	public void genereCoreMigrations()
			throws Exception {

		configure(new AppLayerConfigurationAlteration() {
			@Override
			public void alter(AppLayerConfiguration configuration) {
				when(configuration.isFastMigrationsEnabled()).thenReturn(false);
			}
		});

		givenCollection(zeCollection);

		System.out.println(" ------ Migration script ------");

		File migrationsResources = new File(new FoldersLocator().getI18nFolder(), "migrations");

		generateI18n(new File(migrationsResources, "core"));

		MethodSpec constructor = generateConstructor();

		TypeSpec generatedClassSpec = TypeSpec.classBuilder("GeneratedCoreMigrationCombo")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addField(String.class, "collection")
				.addField(AppLayerFactory.class, "appLayerFactory")
				.addField(MigrationResourcesProvider.class, "resourcesProvider")
				.addMethod(generateTypes())
				.addMethod(generateRoles())
				.addMethod(generateConstructor())
				.build();

		JavaFile file = JavaFile.builder("com.constellio.app.services.migrations", generatedClassSpec)
				.addStaticImport(java.util.Arrays.class, "asList")
				.build();

		File dest = new File(
				"/Users/francisbaril/IdeaProjects/constellio-dev/constellio/app/src/com/constellio/app/services/migrations/GeneratedCoreMigrationCombo.java");
		FileUtils.writeStringToFile(dest, file.toString());
	}

	private MethodSpec generateConstructor() {
		MethodSpec constructor = MethodSpec.constructorBuilder().addParameter(String.class, "collection")
				.addParameter(AppLayerFactory.class, "appLayerFactory")
				.addParameter(MigrationResourcesProvider.class, "resourcesProvider")
				.addStatement("this.$N = $N", "collection", "collection")
				.addStatement("this.$N = $N", "appLayerFactory", "appLayerFactory")
				.addStatement("this.$N = $N", "resourcesProvider", "resourcesProvider")
				.build();
		System.out.println(constructor.toString());
		return constructor;
	}

	private void generateI18n(File moduleFolder)
			throws IOException {

		String module = moduleFolder.getName();
		File comboFolder = new File(moduleFolder, "combo");
		FileUtils.deleteQuietly(comboFolder);
		FileUtils.forceMkdir(comboFolder);

		File[] childFiles = moduleFolder.listFiles();
		if (childFiles != null) {

			List<File> properties = new ArrayList<>();
			List<File> frProperties = new ArrayList<>();
			List<File> enProperties = new ArrayList<>();

			for (File version : childFiles) {
				if (version.isDirectory()) {
					File[] propertyFiles = version.listFiles();
					if (propertyFiles != null) {
						for (File file : propertyFiles) {
							if (file.getName().endsWith("_en.properties")) {
								enProperties.add(file);
							} else if (file.getName().endsWith("_fr.properties")) {
								frProperties.add(file);
							} else if (file.getName().endsWith(".properties")) {
								properties.add(file);
							}
						}
					}
				}
			}

			CombinePropertyFilesServices.combine(properties, new File(comboFolder, module + "_combo.properties"));
			CombinePropertyFilesServices.combine(enProperties, new File(comboFolder, module + "_combo_en.properties"));
			CombinePropertyFilesServices.combine(frProperties, new File(comboFolder, module + "_combo_fr.properties"));
		}
	}

	private MethodSpec generateRoles() {
		RolesManager rolesManager = getModelLayerFactory().getRolesManager();
		Builder main = MethodSpec.methodBuilder("applyGeneratedRoles")
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class);

		main.addStatement("RolesManager rolesManager = appLayerFactory.getModelLayerFactory().getRolesManager();");
		for (Role role : rolesManager.getAllRoles(zeCollection)) {

			main.addStatement("rolesManager.addRole(new $T(collection, $S, $S, $L))", Role.class, role.getCode(), role.getTitle(),
					asListLitteral(role.getOperationPermissions()));

		}

		MethodSpec spec = main.build();
		System.out.println(spec.toString());
		return spec;
	}

	private MethodSpec generateTypes() {
		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

		Builder main = MethodSpec.methodBuilder("applyGeneratedSchemaAlteration")
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class)
				.addParameter(MetadataSchemaTypesBuilder.class, "typesBuilder");

		List<MetadataSchemaType> metadataSchemaTypes = sorted(types.getSchemaTypes());

		for (MetadataSchemaType type : metadataSchemaTypes) {

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

		for (MetadataSchemaType type : metadataSchemaTypes) {
			for (MetadataSchema schema : type.getAllSchemas()) {
				for (Metadata metadata : schema.getMetadatas()) {
					if (metadata.getInheritance() == null) {
						if (!Schemas.isGlobalMetadata(metadata.getLocalCode())) {
							main.addStatement(
									"MetadataBuilder $L = $L.create($S).setType(MetadataValueType.$L)$L",
									variableOf(metadata), variableOf(schema), metadata.getLocalCode(), metadata.getType().name(),
									metadataAlterations(metadata));
						} else {
							main.addStatement(
									"MetadataBuilder $L = $L.get($S)$L",
									variableOf(metadata), variableOf(schema), metadata.getLocalCode(),
									metadataAlterations(metadata));
						}
						for (RecordMetadataValidator validator : metadata.getValidators()) {
							main.addStatement("$L.defineValidators().add($T.class)", variableOf(metadata), validator.getClass());
						}
					}
				}
			}

		}

		for (MetadataSchemaType type : metadataSchemaTypes) {
			for (MetadataSchema schema : type.getAllSchemas()) {
				for (Metadata metadata : schema.getMetadatas()) {
					if (metadata.getInheritance() == null) {
						if (metadata.getDataEntry().getType() == DataEntryType.COPIED) {
							CopiedDataEntry dataEntry = (CopiedDataEntry) metadata.getDataEntry();
							main.addStatement("$L.defineDataEntry().asCopied($L, $L)",
									variableOf(metadata),
									variableOfMetadata(dataEntry.getReferenceMetadata()),
									variableOfMetadata(dataEntry.getCopiedMetadata()));
						}
						if (metadata.getDataEntry().getType() == DataEntryType.CALCULATED) {
							CalculatedDataEntry dataEntry = (CalculatedDataEntry) metadata.getDataEntry();
							main.addStatement("$L.defineDataEntry().asCalculated($T.class)",
									variableOf(metadata),
									dataEntry.getCalculator().getClass());
						}
					}
				}
			}

		}

		MethodSpec spec = main.build();
		System.out.println(spec.toString());
		return spec;
	}

	private List<MetadataSchemaType> sorted(List<MetadataSchemaType> schemaTypes) {
		List<MetadataSchemaType> ordered = new ArrayList<>();
		List<String> firstTypes = asList(Collection.SCHEMA_TYPE, Group.SCHEMA_TYPE, User.SCHEMA_TYPE);
		for (MetadataSchemaType type : schemaTypes) {
			if (type.getCode().equals(Collection.SCHEMA_TYPE)) {
				ordered.add(type);
				break;
			}
		}

		for (MetadataSchemaType type : schemaTypes) {
			if (type.getCode().equals(Group.SCHEMA_TYPE)) {
				ordered.add(type);
				break;
			}
		}
		for (MetadataSchemaType type : schemaTypes) {
			if (type.getCode().equals(User.SCHEMA_TYPE)) {
				ordered.add(type);
				break;
			}
		}
		for (MetadataSchemaType type : schemaTypes) {
			if (!firstTypes.contains(type.getCode())) {
				ordered.add(type);
			}
		}

		return ordered;
	}

	private String asListLitteral(List<String> values) {
		if (values.isEmpty()) {
			return "new ArrayList<String>()";
		}
		StringBuilder valuesLiteral = new StringBuilder();
		for (String value : values) {
			if (valuesLiteral.length() > 0) {
				valuesLiteral.append(", ");
			}
			valuesLiteral.append("\"");
			valuesLiteral.append(value);
			valuesLiteral.append("\"");
		}

		return "asList(" + valuesLiteral + ")";
	}

	private String asListLitteralWithoutQuotes(List<String> values) {
		if (values.isEmpty()) {
			return "new ArrayList<String>()";
		}
		StringBuilder valuesLiteral = new StringBuilder();
		for (String value : values) {
			if (valuesLiteral.length() > 0) {
				valuesLiteral.append(", ");
			}
			valuesLiteral.append(value);
		}

		return "asList(" + valuesLiteral + ")";
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
		return variableOfMetadata(metadata.getCode());
	}

	private String variableOfMetadata(String code) {
		if (code.contains("_default_")) {
			return code.split("_")[0] + "_" + code.split("_")[2];
		} else {
			return code;
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

		if (metadata.getDefaultValue() != null) {
			if (metadata.getDefaultValue().getClass().equals(String.class)) {
				stringBuilder.append(".setDefaultValue(\"" + metadata.getDefaultValue() + "\")");

			} else if (metadata.getDefaultValue().getClass().equals(Double.class)
					|| metadata.getDefaultValue().getClass().equals(Integer.class)
					|| metadata.getDefaultValue().getClass().equals(Boolean.class)) {
				stringBuilder.append(".setDefaultValue(" + metadata.getDefaultValue() + ")");

			} else if (metadata.getDefaultValue() instanceof EnumWithSmallCode) {
				stringBuilder.append(".setDefaultValue(" + metadata.getDefaultValue().getClass().getName() + "." +
						((Enum) metadata.getDefaultValue()).name() + ")");

			} else {
				throw new ImpossibleRuntimeException("Unsupported type '" + metadata.getDefaultValue().getClass() + "'");
			}

		}

		if (metadata.getStructureFactory() != null) {
			stringBuilder
					.append(".defineStructureFactory(" + metadata.getStructureFactory().getClass().getName().replace("$", ".")
							+ ".class)");
		}

		if (metadata.getEnumClass() != null) {
			stringBuilder.append(".defineAsEnum(" + metadata.getEnumClass().getName().replace("$", ".") + ".class)");
		}

		if (metadata.getType() == MetadataValueType.REFERENCE && !Schemas.isGlobalMetadata(metadata.getLocalCode())) {
			MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
			if (metadata.getAllowedReferences().getAllowedSchemas().isEmpty()) {
				String referencedType = metadata.getAllowedReferences().getAllowedSchemaType();
				String referencedTypeVariable = variableOf(types.getSchemaType(referencedType));
				if (metadata.isTaxonomyRelationship()) {
					stringBuilder.append(".defineTaxonomyRelationshipToType(" + referencedTypeVariable + ")");

				} else if (metadata.isChildOfRelationship()) {
					stringBuilder.append(".defineChildOfRelationshipToType(" + referencedTypeVariable + ")");

				} else {
					stringBuilder.append(".defineReferencesTo(" + referencedTypeVariable + ")");

				}
			} else {
				Set<String> referencedSchemas = metadata.getAllowedReferences().getAllowedSchemas();
				List<String> referencedSchemasVariables = new ArrayList<>();
				for (String referencedSchema : referencedSchemas) {
					referencedSchemasVariables.add(variableOf(types.getSchema(referencedSchema)));
				}
				String argument = asListLitteralWithoutQuotes(referencedSchemasVariables);
				if (metadata.isTaxonomyRelationship()) {
					stringBuilder.append(".defineTaxonomyRelationshipToType(" + argument + ")");

				} else if (metadata.isChildOfRelationship()) {
					stringBuilder.append(".defineChildOfRelationshipToType(" + argument + ")");

				} else {
					stringBuilder.append(".defineReferencesTo(" + argument + ")");

				}
			}

		}

		return stringBuilder.toString();
	}

}
