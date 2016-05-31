package com.constellio.app.services.migrations;

import static com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Modifier;

import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import com.constellio.app.conf.AppLayerConfiguration;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.conf.IdGeneratorType;
import com.constellio.data.utils.HashMapBuilder;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.roles.RolesManager;
import com.constellio.sdk.dev.tools.i18n.CombinePropertyFilesServices;
import com.constellio.sdk.tests.AppLayerConfigurationAlteration;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.DataLayerConfigurationAlteration;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeSpec;
import com.steadystate.css.util.LangUtils;

public class FastMigrationsGeneratorAcceptanceTest extends ConstellioTest {

	@InDevelopmentTest
	@Test
	public void genereCoreMigrations()
			throws Exception {

		configure(new DataLayerConfigurationAlteration() {
			@Override
			public void alter(DataLayerConfiguration configuration) {
				when(configuration.getSecondaryIdGeneratorType()).thenReturn(IdGeneratorType.SEQUENTIAL);
				when(configuration.createRandomUniqueKey()).thenReturn("123-456-789");
			}
		});
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
				//.addMethod(generateRecords())
				.addMethod(generateTypes())
				.addMethod(generateDisplayConfigs())
				.addMethod(generateRoles())
				.addMethod(generateConstructor())
				.build();

		JavaFile file = JavaFile.builder("com.constellio.app.services.migrations", generatedClassSpec)
				.addStaticImport(java.util.Arrays.class, "asList")
				.addStaticImport(HashMapBuilder.class, "stringObjectMap")
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

	private MethodSpec generateDisplayConfigs() {
		SchemasDisplayManager manager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		Builder main = MethodSpec.methodBuilder("applySchemasDisplay")
				.addModifiers(Modifier.PUBLIC)
				.addParameter(SchemasDisplayManager.class, "manager")
				.returns(void.class);

		List<String> codes = manager.rewriteInOrderAndGetCodes(zeCollection);

		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		SchemaTypesDisplayConfig typesDisplay = manager.getTypes(zeCollection);
		main.addStatement("SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection)");
		main.addStatement("SchemaTypesDisplayConfig typesConfig = manager.getTypes(collection)");

		if (codes.contains("SchemaTypesDisplayConfig")) {
			main.addStatement("transaction.setModifiedCollectionTypes(manager.getTypes(collection).withFacetMetadataCodes($L))",
					asListLitteral(typesDisplay.getFacetMetadataCodes()));
		}

		for (MetadataSchemaType type : types.getSchemaTypes()) {
			SchemaTypeDisplayConfig typeDisplay = manager.getType(zeCollection, type.getCode());
			if (codes.contains(typeDisplay.getSchemaType())) {
				main.addStatement("transaction.add(manager.getType(collection, $S).withSimpleSearchStatus($L)"
								+ ".withAdvancedSearchStatus($L).withManageableStatus($L)"
								+ ".withMetadataGroup(resourcesProvider.getLanguageMapWithKeys($L)))", type.getCode(),
						typeDisplay.isSimpleSearch(), typeDisplay.isAdvancedSearch(), typeDisplay.isManageable(),
						asListLitteral(new ArrayList<String>(typeDisplay.getMetadataGroup().keySet())));
			}

			for (MetadataSchema schema : type.getAllSchemas()) {
				SchemaDisplayConfig schemaDisplay = manager.getSchema(zeCollection, schema.getCode());
				if (codes.contains(schemaDisplay.getSchemaCode())) {
					main.addStatement("transaction.add(manager.getSchema(collection, $S).withFormMetadataCodes($L)"
									+ ".withDisplayMetadataCodes($L).withSearchResultsMetadataCodes($L).withTableMetadataCodes($L))"
							, schema.getCode(), asListLitteral(schemaDisplay.getFormMetadataCodes())
							, asListLitteral(schemaDisplay.getDisplayMetadataCodes())
							, asListLitteral(schemaDisplay.getSearchResultsMetadataCodes())
							, asListLitteral(schemaDisplay.getTableMetadataCodes()));
				}

				for (Metadata metadata : schema.getMetadatas()) {
					MetadataDisplayConfig metadataDisplay = manager.getMetadata(zeCollection, metadata.getCode());

					if (codes.contains(metadataDisplay.getMetadataCode())) {
						main.addStatement("transaction.add(manager.getMetadata(collection, $S).withMetadataGroup($S)"
										+ ".withInputType($T.$L).withHighlightStatus($L).withVisibleInAdvancedSearchStatus($L))",
								metadata.getCode(), metadataDisplay.getMetadataGroupCode(), MetadataInputType.class,
								metadataDisplay.getInputType().name(), metadataDisplay.isHighlight(),
								metadataDisplay.isVisibleInAdvancedSearch());
					}
				}

			}

		}

		main.addStatement("manager.execute(transaction.build())");

		MethodSpec spec = main.build();
		System.out.println(spec.toString());
		return spec;
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

	private MethodSpec generateRecords() {
		Builder main = MethodSpec.methodBuilder("createRecordTransaction")
				.addModifiers(Modifier.PUBLIC)
				.returns(Transaction.class)
				.addParameter(RecordServices.class, "recordServices")
				.addParameter(MetadataSchemaTypes.class, "types");

		main.addStatement("List<Record> records = new ArrayList<>()");

		SearchServices searchServices = getModelLayerFactory().newSearchServices();

		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
		List<Record> records = searchServices.search(new LogicalSearchQuery(fromAllSchemasIn(zeCollection).returnAll()));
		Map<String, Integer> mapping = new HashMap<>();
		int i = 0;
		for (Record record : records) {
			if (!record.getId().contains(zeCollection) && !record.getId().contains(SYSTEM_COLLECTION)) {
				mapping.put(record.getId(), i++);
				main.addStatement("records.add(recordServices.newRecordWithSchema(types.getSchema($S)))", record.getId());
			}
		}
		i = 0;
		for (Record record : records) {
			if (!record.getId().contains(zeCollection) && !record.getId().contains(SYSTEM_COLLECTION)) {
				main.addStatement("records.get($L)$L", i, toEntriesMethodCalls(types, record, mapping));
			}
		}

		main.addStatement("return new Transaction(records)");
		MethodSpec spec = main.build();
		System.out.println(spec.toString());
		return spec;
	}

	private String toEntriesMethodCalls(MetadataSchemaTypes types, Record record, Map<String, Integer> mapping) {
		StringBuilder stringBuilder = new StringBuilder();

		for (Metadata metadata : types.getSchema(record.getSchemaCode()).getMetadatas().onlyManuals()) {
			if (!metadata.getDataStoreCode().equals("modifiedOn_dt") && !metadata.getDataStoreCode().equals("createdOn_dt")) {

				if (metadata.isMultivalue()) {
					if (!LangUtils.equals(record.getList(metadata), metadata.getDefaultValue())) {

						String value = toValue(record.get(metadata));

						stringBuilder.append(
								".set(types.getMetadata(\"" + metadata.getCode() + "\"), " + value + ")");
					}

				} else {

					if (!LangUtils.equals(record.get(metadata), metadata.getDefaultValue())) {

						String value;
						if (metadata.getType() == MetadataValueType.REFERENCE) {
							value = "records.get(" + mapping.get((String) record.get(metadata)) + ")";

						} else if (metadata.getType() == MetadataValueType.STRUCTURE) {
							String strStructure = metadata.getStructureFactory()
									.toString((ModifiableStructure) record.get(metadata))
									.replace("\"", "\\\"");
							value = "new " + metadata.getStructureFactory().getClass().getName() + "().build(\"" + strStructure
									+ "\")";
						} else {
							value = toValue(record.get(metadata));
						}

						stringBuilder.append(
								".set(types.getMetadata(\"" + metadata.getCode() + "\"), " + value + ")");
					}
				}
			}
		}

		return stringBuilder.toString();
	}

	private String toValue(Object value) {
		if (value == null) {
			return null;
		}
		if (List.class.isAssignableFrom(value.getClass())) {
			List<Object> values = (List) value;
			List<String> convertedValues = new ArrayList<>();
			for (Object item : values) {
				convertedValues.add(toValue(item));
			}
			return asListLitteralWithoutQuotes(convertedValues);
		}
		if (value.getClass().equals(String.class)) {
			return "\"" + value.toString().replace("\"", "\\\"") + "\"";

		} else if (value.getClass().equals(Long.class)) {
			return value.toString() + "L";

		} else if (value instanceof Enum) {
			return value.getClass().getName().replace("$", ".") + "." + ((Enum) value).name();

		} else if (value.getClass().equals(Double.class)
				|| value.getClass().equals(Integer.class)
				|| value.getClass().equals(Boolean.class)) {
			return value.toString();

		} else if (value instanceof LocalDate) {
			LocalDate date = (LocalDate) value;
			return "new LocalDate(" + date.getYear() + ", " + date.getMonthOfYear() + ", " + date.getDayOfMonth() + ")";

		} else if (value instanceof LocalDateTime) {
			LocalDateTime date = (LocalDateTime) value;
			return "new LocalDateTime(" + date.getYear() + ", " + date.getMonthOfYear() + ", " + date.getDayOfMonth() + ", " +
					date.getHourOfDay() + ", " + date.getMinuteOfHour() + ", " + date.getSecondOfMinute() + ", " +
					date.getMillisOfSecond() + ")";

		} else {
			throw new ImpossibleRuntimeException("Unsupported type '" + value.getClass() + "'");
		}
	}

	private MethodSpec generateTypes() {
		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

		Builder main = MethodSpec.methodBuilder("applyGeneratedSchemaAlteration")
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class)
				.addParameter(MetadataSchemaTypesBuilder.class, "typesBuilder");

		List<MetadataSchemaType> metadataSchemaTypes = sorted(types.getSchemaTypes());

		for (MetadataSchemaType type : metadataSchemaTypes) {

			main.addStatement("$T $LSchemaType = typesBuilder.createNewSchemaType($S)$L",
					MetadataSchemaTypeBuilder.class, type.getCode(), type.getCode(), typeAlterations(type));
			for (MetadataSchema schema : type.getAllSchemas()) {
				if ("default".equals(schema.getLocalCode())) {
					main.addStatement("$T $L = $LSchemaType.getDefaultSchema()",
							MetadataSchemaBuilder.class, variableOf(schema), type.getCode());
				} else {
					main.addStatement("$T $L = $LSchemaType.createCustomSchema($S)",
							MetadataSchemaBuilder.class, variableOf(schema), type.getCode(), schema.getLocalCode());
				}
				for (RecordValidator validator : type.getDefaultSchema().getValidators()) {
					main.addStatement("$L.defineValidators().add($T)", variableOf(schema), validator.getClass());
				}
			}
		}

		for (MetadataSchemaType type : metadataSchemaTypes) {
			for (MetadataSchema schema : type.getAllSchemas()) {
				for (Metadata metadata : schema.getMetadatas()) {
					String variable = variableOf(metadata);
					if (metadata.getInheritance() == null) {
						if (!Schemas.isGlobalMetadata(metadata.getLocalCode())) {
							main.addStatement("$T $L = $L.create($S).setType(MetadataValueType.$L)",
									MetadataBuilder.class, variable, variableOf(schema), metadata.getLocalCode(),
									metadata.getType().name());
							configureMetadata(main, variable, metadata);
						} else {

							main.addStatement("$T $L = $L.get($S)",
									MetadataBuilder.class, variable, variableOf(schema), metadata.getLocalCode());
							configureMetadata(main, variable, metadata);
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

	private void configureMetadata(Builder method, String variable, Metadata metadata) {

		if (metadata.isMultivalue()) {
			method.addStatement("$L.setMultivalue(true)", variable);
		}

		if (metadata.isDefaultRequirement()) {
			method.addStatement("$L.setDefaultRequirement(true)", variable);
		}

		if (metadata.isSystemReserved()) {
			method.addStatement("$L.setSystemReserved(true)", variable);
		}

		if (metadata.isUndeletable()) {
			method.addStatement("$L.setUndeletable(true)", variable);
		}

		if (!metadata.isEnabled()) {
			method.addStatement("$L.setEnabled(false)", variable);
		}

		if (metadata.isEssential()) {
			method.addStatement("$L.setEssential(true)", variable);
		}

		if (metadata.isEssentialInSummary()) {
			method.addStatement("$L.setEssentialInSummary(true)", variable);
		}

		if (metadata.isSchemaAutocomplete()) {
			method.addStatement("$L.setSchemaAutocomplete(true)", variable);
		}

		if (metadata.isSearchable()) {
			method.addStatement("$L.setSearchable(true)", variable);
		}

		if (metadata.isSortable()) {
			method.addStatement("$L.setSortable(true)", variable);
		}

		if (metadata.isUniqueValue()) {

			method.addStatement("$L.setUniqueValue(true)", variable);
		}

		if (metadata.isUnmodifiable()) {
			method.addStatement("$L.setUnmodifiable(true)", variable);
		}

		Object defaultValue = metadata.getDefaultValue();
		if (defaultValue != null) {
			if (metadata.getDefaultValue() instanceof EnumWithSmallCode) {
				method.addStatement("$L.setDefaultValue($T.$L)", variable, defaultValue.getClass(), ((Enum) defaultValue).name());
			} else {
				method.addStatement("$L.setDefaultValue($L)", variable, toValue(defaultValue));
			}

		}

		if (metadata.getStructureFactory() != null) {
			method.addStatement("$L.defineStructureFactory($T.class)", variable, metadata.getStructureFactory().getClass());
		}

		if (metadata.getEnumClass() != null) {
			method.addStatement("$L.defineAsEnum($T.class)", variable, metadata.getEnumClass());
		}

		if (metadata.getType() == MetadataValueType.REFERENCE && !Schemas.isGlobalMetadata(metadata.getLocalCode())) {
			MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);
			if (metadata.getAllowedReferences().getAllowedSchemas().isEmpty()) {
				String referencedType = metadata.getAllowedReferences().getAllowedSchemaType();
				String referencedTypeVariable = variableOf(types.getSchemaType(referencedType));
				if (metadata.isTaxonomyRelationship()) {
					method.addStatement("$L.defineTaxonomyRelationshipToType($L)", variable, referencedTypeVariable);

				} else if (metadata.isChildOfRelationship()) {
					method.addStatement("$L.defineChildOfRelationshipToType($L)", variable, referencedTypeVariable);

				} else {
					method.addStatement("$L.defineReferencesTo($L)", variable, referencedTypeVariable);

				}
			} else {
				Set<String> referencedSchemas = metadata.getAllowedReferences().getAllowedSchemas();
				List<String> referencedSchemasVariables = new ArrayList<>();
				for (String referencedSchema : referencedSchemas) {
					referencedSchemasVariables.add(variableOf(types.getSchema(referencedSchema)));
				}
				String argument = asListLitteralWithoutQuotes(referencedSchemasVariables);
				if (metadata.isTaxonomyRelationship()) {
					method.addStatement("$L.defineTaxonomyRelationshipToType($L)", variable, argument);

				} else if (metadata.isChildOfRelationship()) {
					method.addStatement("$L.defineChildOfRelationshipToType($L)", variable, argument);

				} else {
					method.addStatement("$L.defineReferencesTo($L)", variable, argument);

				}
			}

		}

	}

}
