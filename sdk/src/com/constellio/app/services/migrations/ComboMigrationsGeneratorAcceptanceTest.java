package com.constellio.app.services.migrations;

import com.constellio.app.conf.PropertiesAppLayerConfiguration.InMemoryAppLayerConfiguration;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.schemasDisplay.MetadataDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypeDisplayConfig;
import com.constellio.app.entities.schemasDisplay.SchemaTypesDisplayConfig;
import com.constellio.app.entities.schemasDisplay.enums.MetadataInputType;
import com.constellio.app.modules.complementary.ESRMRobotsModule;
import com.constellio.app.modules.es.ConstellioESModule;
import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.robots.ConstellioRobotsModule;
import com.constellio.app.modules.tasks.TaskModule;
import com.constellio.app.services.extensions.plugins.JSPFConstellioPluginManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.conf.IdGeneratorType;
import com.constellio.data.conf.PropertiesDataLayerConfiguration.InMemoryDataLayerConfiguration;
import com.constellio.data.utils.HashMapBuilder;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.Collection;
import com.constellio.model.entities.records.wrappers.Group;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.extensions.ConstellioModulesManager;
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
import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static java.util.Arrays.asList;

@InDevelopmentTest
public class ComboMigrationsGeneratorAcceptanceTest extends ConstellioTest {

	@Test
	public void generateCoreMigrations()
			throws Exception {

		configure(new DataLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryDataLayerConfiguration configuration) {
				configuration.setSecondaryIdGeneratorType(IdGeneratorType.SEQUENTIAL);
				//when(configuration.createRandomUniqueKey()).thenReturn("123-456-789");
			}
		});
		configure(new AppLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryAppLayerConfiguration configuration) {
				configuration.setFastMigrationsEnabled(false);
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
				.addMethod(generateTypes(null))
				.addMethod(generateDisplayConfigs(new ArrayList<String>()))
				.addMethod(generateRoles(new ArrayList<Role>()))
				.addMethod(generateConstructor())
				.build();

		JavaFile file = JavaFile.builder("com.constellio.app.services.migrations", generatedClassSpec)
				.addStaticImport(java.util.Arrays.class, "asList")
				.addStaticImport(HashMapBuilder.class, "stringObjectMap")
				.build();

		File dest = new File(
				getFoldersLocator().getAppProject()
						+ "/src/com/constellio/app/services/migrations/GeneratedCoreMigrationCombo.java");
		FileUtils.writeStringToFile(dest, file.toString());
	}

	@Test
	public void generateRMMigrations()
			throws Exception {

		configure(new DataLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryDataLayerConfiguration configuration) {
				configuration.setSecondaryIdGeneratorType(IdGeneratorType.SEQUENTIAL);
				//when(configuration.createRandomUniqueKey()).thenReturn("123-456-789");
			}
		});
		configure(new AppLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryAppLayerConfiguration configuration) {
				configuration.setFastMigrationsEnabled(false);
			}
		});

		givenCollection(zeCollection);
		CollectionsListManager collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		ConstellioModulesManager constellioModulesManager = getAppLayerFactory().getModulesManager();
		constellioModulesManager.installValidModuleAndGetInvalidOnes(new TaskModule(), collectionsListManager);
		constellioModulesManager.enableValidModuleAndGetInvalidOnes(zeCollection, new TaskModule());

		List<Role> rolesBefore = getModelLayerFactory().getRolesManager().getAllRoles(zeCollection);
		List<String> codesBefore = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.rewriteInOrderAndGetCodes(zeCollection);
		MetadataSchemaTypes typesBefore = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

		constellioModulesManager.installValidModuleAndGetInvalidOnes(new ConstellioRMModule(), collectionsListManager);
		constellioModulesManager.enableValidModuleAndGetInvalidOnes(zeCollection, new ConstellioRMModule());

		System.out.println(" ------ Migration script ------");

		File migrationsResources = new File(new FoldersLocator().getI18nFolder(), "migrations");

		generateI18n(new File(migrationsResources, "rm"));

		MethodSpec constructor = generateConstructor();

		TypeSpec generatedClassSpec = TypeSpec.classBuilder("GeneratedRMMigrationCombo")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addField(String.class, "collection")
				.addField(AppLayerFactory.class, "appLayerFactory")
				.addField(MigrationResourcesProvider.class, "resourcesProvider")
				//.addMethod(generateRecords())
				.addMethod(generateTypes(typesBefore))
				.addMethod(generateDisplayConfigs(codesBefore))
				.addMethod(generateRoles(rolesBefore))
				.addMethod(generateConstructor())
				.build();

		JavaFile file = JavaFile.builder("com.constellio.app.modules.rm.migrations", generatedClassSpec)
				.addStaticImport(java.util.Arrays.class, "asList")
				.addStaticImport(HashMapBuilder.class, "stringObjectMap")
				.build();

		File dest = new File(getFoldersLocator().getAppProject()
				+ "/src/com/constellio/app/modules/rm/migrations/GeneratedRMMigrationCombo.java");
		FileUtils.writeStringToFile(dest, file.toString());
	}

	@Test
	public void generateTasksMigrations()
			throws Exception {

		configure(new DataLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryDataLayerConfiguration configuration) {
				configuration.setSecondaryIdGeneratorType(IdGeneratorType.SEQUENTIAL);
				//when(configuration.createRandomUniqueKey()).thenReturn("123-456-789");
			}
		});
		configure(new AppLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryAppLayerConfiguration configuration) {
				configuration.setFastMigrationsEnabled(false);
			}
		});

		givenCollection(zeCollection);
		CollectionsListManager collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		ConstellioModulesManager constellioModulesManager = getAppLayerFactory().getModulesManager();

		List<Role> rolesBefore = getModelLayerFactory().getRolesManager().getAllRoles(zeCollection);
		List<String> codesBefore = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.rewriteInOrderAndGetCodes(zeCollection);
		MetadataSchemaTypes typesBefore = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

		constellioModulesManager.installValidModuleAndGetInvalidOnes(new TaskModule(), collectionsListManager);
		constellioModulesManager.enableValidModuleAndGetInvalidOnes(zeCollection, new TaskModule());

		System.out.println(" ------ Migration script ------");

		File migrationsResources = new File(new FoldersLocator().getI18nFolder(), "migrations");

		generateI18n(new File(migrationsResources, "tasks"));

		MethodSpec constructor = generateConstructor();

		TypeSpec generatedClassSpec = TypeSpec.classBuilder("GeneratedTasksMigrationCombo")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addField(String.class, "collection")
				.addField(AppLayerFactory.class, "appLayerFactory")
				.addField(MigrationResourcesProvider.class, "resourcesProvider")
				//.addMethod(generateRecords())
				.addMethod(generateTypes(typesBefore))
				.addMethod(generateDisplayConfigs(codesBefore))
				.addMethod(generateRoles(rolesBefore))
				.addMethod(generateConstructor())
				.build();

		JavaFile file = JavaFile.builder("com.constellio.app.modules.tasks.migrations", generatedClassSpec)
				.addStaticImport(java.util.Arrays.class, "asList")
				.addStaticImport(HashMapBuilder.class, "stringObjectMap")
				.build();

		File dest = new File(getFoldersLocator().getAppProject()
				+ "/src/com/constellio/app/modules/tasks/migrations/GeneratedTasksMigrationCombo.java");
		FileUtils.writeStringToFile(dest, file.toString());
	}

	@Test
	public void generateRobotsMigrations()
			throws Exception {

		configure(new DataLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryDataLayerConfiguration configuration) {
				configuration.setSecondaryIdGeneratorType(IdGeneratorType.SEQUENTIAL);
				//when(configuration.createRandomUniqueKey()).thenReturn("123-456-789");
			}
		});
		configure(new AppLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryAppLayerConfiguration configuration) {
				configuration.setFastMigrationsEnabled(false);
			}
		});

		givenCollection(zeCollection);
		CollectionsListManager collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		ConstellioModulesManager constellioModulesManager = getAppLayerFactory().getModulesManager();

		List<Role> rolesBefore = getModelLayerFactory().getRolesManager().getAllRoles(zeCollection);
		List<String> codesBefore = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.rewriteInOrderAndGetCodes(zeCollection);
		MetadataSchemaTypes typesBefore = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

		constellioModulesManager.installValidModuleAndGetInvalidOnes(new ConstellioRobotsModule(), collectionsListManager);
		constellioModulesManager.enableValidModuleAndGetInvalidOnes(zeCollection, new ConstellioRobotsModule());

		System.out.println(" ------ Migration script ------");

		File migrationsResources = new File(new FoldersLocator().getI18nFolder(), "migrations");

		generateI18n(new File(migrationsResources, "robots"));

		MethodSpec constructor = generateConstructor();

		TypeSpec generatedClassSpec = TypeSpec.classBuilder("GeneratedRobotsMigrationCombo")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addField(String.class, "collection")
				.addField(AppLayerFactory.class, "appLayerFactory")
				.addField(MigrationResourcesProvider.class, "resourcesProvider")
				//.addMethod(generateRecords())
				.addMethod(generateTypes(typesBefore))
				.addMethod(generateDisplayConfigs(codesBefore))
				.addMethod(generateRoles(rolesBefore))
				.addMethod(generateConstructor())
				.build();

		JavaFile file = JavaFile.builder("com.constellio.app.modules.robots.migrations", generatedClassSpec)
				.addStaticImport(java.util.Arrays.class, "asList")
				.addStaticImport(HashMapBuilder.class, "stringObjectMap")
				.build();

		File dest = new File(getFoldersLocator().getAppProject()
				+ "/src/com/constellio/app/modules/robots/migrations/GeneratedRobotsMigrationCombo.java");
		FileUtils.writeStringToFile(dest, file.toString());
	}

	@Test
	public void generateESMigrations()
			throws Exception {

		configure(new DataLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryDataLayerConfiguration configuration) {
				configuration.setSecondaryIdGeneratorType(IdGeneratorType.SEQUENTIAL);
				//when(configuration.createRandomUniqueKey()).thenReturn("123-456-789");
			}
		});
		configure(new AppLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryAppLayerConfiguration configuration) {
				configuration.setFastMigrationsEnabled(false);
			}
		});

		givenCollection(zeCollection);
		CollectionsListManager collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		ConstellioModulesManager constellioModulesManager = getAppLayerFactory().getModulesManager();

		List<Role> rolesBefore = getModelLayerFactory().getRolesManager().getAllRoles(zeCollection);
		List<String> codesBefore = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.rewriteInOrderAndGetCodes(zeCollection);
		MetadataSchemaTypes typesBefore = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

		constellioModulesManager.installValidModuleAndGetInvalidOnes(new ConstellioESModule(), collectionsListManager);
		constellioModulesManager.enableValidModuleAndGetInvalidOnes(zeCollection, new ConstellioESModule());

		System.out.println(" ------ Migration script ------");

		File migrationsResources = new File(new FoldersLocator().getI18nFolder(), "migrations");

		generateI18n(new File(migrationsResources, "es"));

		MethodSpec constructor = generateConstructor();

		TypeSpec generatedClassSpec = TypeSpec.classBuilder("GeneratedESMigrationCombo")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addField(String.class, "collection")
				.addField(AppLayerFactory.class, "appLayerFactory")
				.addField(MigrationResourcesProvider.class, "resourcesProvider")
				//.addMethod(generateRecords())
				.addMethod(generateTypes(typesBefore))
				.addMethod(generateDisplayConfigs(codesBefore))
				.addMethod(generateRoles(rolesBefore))
				.addMethod(generateConstructor())
				.build();

		JavaFile file = JavaFile.builder("com.constellio.app.modules.es.migrations", generatedClassSpec)
				.addStaticImport(java.util.Arrays.class, "asList")
				.addStaticImport(HashMapBuilder.class, "stringObjectMap")
				.build();

		File dest = new File(getFoldersLocator().getAppProject()
				+ "/src/com/constellio/app/modules/es/migrations/GeneratedTasksMigrationCombo.java");
		FileUtils.writeStringToFile(dest, file.toString());
	}

	@Test
	public void generateESRMRobotsMigrations()
			throws Exception {

		configure(new DataLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryDataLayerConfiguration configuration) {
				configuration.setSecondaryIdGeneratorType(IdGeneratorType.SEQUENTIAL);
				//when(configuration.createRandomUniqueKey()).thenReturn("123-456-789");
			}
		});
		configure(new AppLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryAppLayerConfiguration configuration) {
				configuration.setFastMigrationsEnabled(false);
			}
		});

		givenCollection(zeCollection);
		CollectionsListManager collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		ConstellioModulesManager constellioModulesManager = getAppLayerFactory().getModulesManager();

		((JSPFConstellioPluginManager) getAppLayerFactory().getPluginManager()).unregisterModule(new ESRMRobotsModule());

		constellioModulesManager.installValidModuleAndGetInvalidOnes(new ConstellioESModule(), collectionsListManager);
		constellioModulesManager.enableValidModuleAndGetInvalidOnes(zeCollection, new ConstellioESModule());
		constellioModulesManager.installValidModuleAndGetInvalidOnes(new ConstellioRMModule(), collectionsListManager);
		constellioModulesManager.enableValidModuleAndGetInvalidOnes(zeCollection, new ConstellioRMModule());
		constellioModulesManager.installValidModuleAndGetInvalidOnes(new ConstellioRobotsModule(), collectionsListManager);
		constellioModulesManager.enableValidModuleAndGetInvalidOnes(zeCollection, new ConstellioRobotsModule());

		List<Role> rolesBefore = getModelLayerFactory().getRolesManager().getAllRoles(zeCollection);
		List<String> codesBefore = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.rewriteInOrderAndGetCodes(zeCollection);
		MetadataSchemaTypes typesBefore = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

		((JSPFConstellioPluginManager) getAppLayerFactory().getPluginManager()).registerModule(new ESRMRobotsModule());
		constellioModulesManager.installValidModuleAndGetInvalidOnes(new ESRMRobotsModule(), collectionsListManager);
		constellioModulesManager.enableValidModuleAndGetInvalidOnes(zeCollection, new ESRMRobotsModule());

		System.out.println(" ------ Migration script ------");

		File migrationsResources = new File(new FoldersLocator().getI18nFolder(), "migrations");

		generateI18n(new File(migrationsResources, "es_rm_robots"));

		MethodSpec constructor = generateConstructor();

		TypeSpec generatedClassSpec = TypeSpec.classBuilder("GeneratedESRMRobotsMigrationCombo")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addField(String.class, "collection")
				.addField(AppLayerFactory.class, "appLayerFactory")
				.addField(MigrationResourcesProvider.class, "resourcesProvider")
				//.addMethod(generateRecords())
				.addMethod(generateTypes(typesBefore))
				.addMethod(generateDisplayConfigs(codesBefore))
				.addMethod(generateRoles(rolesBefore))
				.addMethod(generateConstructor())
				.build();

		JavaFile file = JavaFile.builder("com.constellio.app.modules.complementary.esRmRobots.migrations", generatedClassSpec)
				.addStaticImport(java.util.Arrays.class, "asList")
				.addStaticImport(HashMapBuilder.class, "stringObjectMap")
				.build();

		File dest = new File(getFoldersLocator().getAppProject()
				+ "/src/com/constellio/app/modules/complementary/esRmRobots/migrations/GeneratedTasksMigrationCombo.java");
		FileUtils.writeStringToFile(dest, file.toString());
	}

	protected MethodSpec generateConstructor() {
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

	protected void generateI18n(File moduleFolder)
			throws IOException {

		generateI18n(moduleFolder, moduleFolder.getName());
	}

	protected void generateI18n(File moduleFolder, String module)
			throws IOException {
		generateI18n(moduleFolder, module, new HashMap<String, String>(), new HashMap<String, String>());
	}

	protected void generateI18n(File moduleFolder, String module, Map<String, String> extraFrenchLabels,
			Map<String, String> extraEnglishLabels)
			throws IOException {

		File comboFolder = new File(moduleFolder, "combo");
		FileUtils.deleteQuietly(comboFolder);
		FileUtils.forceMkdir(comboFolder);

		File[] childFiles = moduleFolder.listFiles();
		if (childFiles != null) {

			List<File> properties = new ArrayList<>();
			List<File> frProperties = new ArrayList<>();
			List<File> enProperties = new ArrayList<>();
			List<File> resourcesFiles = new ArrayList<>();

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
							} else {
								resourcesFiles.add(file);
							}
						}
					}
				}
			}

			CombinePropertyFilesServices
					.combine(properties, new File(comboFolder, module + "_combo.properties"), extraFrenchLabels);
			CombinePropertyFilesServices
					.combine(enProperties, new File(comboFolder, module + "_combo_en.properties"), extraEnglishLabels);
			CombinePropertyFilesServices
					.combine(frProperties, new File(comboFolder, module + "_combo_fr.properties"), new HashMap<String, String>());

			for (File resourceFile : resourcesFiles) {
				FileUtils.copyFile(resourceFile, new File(comboFolder, resourceFile.getName()));
			}
		}
	}

	protected MethodSpec generateDisplayConfigs(List<String> codesBefore) {
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
			if (codes.contains(typeDisplay.getSchemaType()) && !codesBefore.contains(typeDisplay.getSchemaType())) {
				main.addStatement("transaction.add(manager.getType(collection, $S).withSimpleSearchStatus($L)"
								+ ".withAdvancedSearchStatus($L).withManageableStatus($L)"
								+ ".withMetadataGroup(resourcesProvider.getLanguageMap($L)))", type.getCode(),
						typeDisplay.isSimpleSearch(), typeDisplay.isAdvancedSearch(), typeDisplay.isManageable(),
						asListLitteral(new ArrayList<String>(typeDisplay.getMetadataGroup().keySet())));
			}

			for (MetadataSchema schema : type.getAllSchemas()) {
				SchemaDisplayConfig schemaDisplay = manager.getSchema(zeCollection, schema.getCode());
				if (codes.contains(schemaDisplay.getSchemaCode()) && !codesBefore.contains(schemaDisplay.getSchemaCode())) {
					main.addStatement("transaction.add(manager.getSchema(collection, $S).withFormMetadataCodes($L)"
									+ ".withDisplayMetadataCodes($L).withSearchResultsMetadataCodes($L).withTableMetadataCodes($L))"
							, schema.getCode(), asListLitteral(schemaDisplay.getFormMetadataCodes())
							, asListLitteral(schemaDisplay.getDisplayMetadataCodes())
							, asListLitteral(schemaDisplay.getSearchResultsMetadataCodes())
							, asListLitteral(schemaDisplay.getTableMetadataCodes()));
				}

				for (Metadata metadata : schema.getMetadatas()) {
					MetadataDisplayConfig metadataDisplay = manager.getMetadata(zeCollection, metadata.getCode());

					if (codes.contains(metadataDisplay.getMetadataCode()) && !codesBefore
							.contains(metadataDisplay.getMetadataCode())) {
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

	protected MethodSpec generateRoles(List<Role> rolesBefore) {
		RolesManager rolesManager = getModelLayerFactory().getRolesManager();
		Builder main = MethodSpec.methodBuilder("applyGeneratedRoles")
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class);

		main.addStatement("RolesManager rolesManager = appLayerFactory.getModelLayerFactory().getRolesManager();");
		for (Role role : rolesManager.getAllRoles(zeCollection)) {

			boolean roleWithSameCode = false;
			for (Role roleBefore : rolesBefore) {
				roleWithSameCode |= role.getCode().equals(roleBefore.getCode());
			}

			if (!roleWithSameCode) {
				main.addStatement("rolesManager.addRole(new $T(collection, $S, $S, $L))", Role.class, role.getCode(),
						role.getTitle(),
						asListLitteral(role.getOperationPermissions()));
			} else {
				main.addStatement("rolesManager.updateRole(rolesManager.getRole(collection, $S).withNewPermissions($L))",
						role.getCode(), asListLitteral(role.getOperationPermissions()));
			}

		}

		MethodSpec spec = main.build();
		System.out.println(spec.toString());
		return spec;
	}

	protected MethodSpec generateRecords() {
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

	protected String toEntriesMethodCalls(MetadataSchemaTypes types, Record record, Map<String, Integer> mapping) {
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

	protected String toValue(Object value) {
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

		} else if (value.getClass().equals(Float.class)) {
			return value.toString() + "F";

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

	protected MethodSpec generateTypes(MetadataSchemaTypes typesBeforeMigration) {
		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(zeCollection);

		Builder main = MethodSpec.methodBuilder("applyGeneratedSchemaAlteration")
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class)
				.addParameter(MetadataSchemaTypesBuilder.class, "typesBuilder");

		List<MetadataSchemaType> metadataSchemaTypes = sorted(types.getSchemaTypes());

		for (MetadataSchemaType type : metadataSchemaTypes) {
			if (typesBeforeMigration != null && typesBeforeMigration.hasType(type.getCode())) {
				main.addStatement("$T $LSchemaType = typesBuilder.getSchemaType($S)",
						MetadataSchemaTypeBuilder.class, type.getCode(), type.getCode());

				for (MetadataSchema schema : type.getAllSchemas()) {
					if ("default".equals(schema.getLocalCode())) {
						main.addStatement("$T $L = $LSchemaType.getDefaultSchema()",
								MetadataSchemaBuilder.class, variableOf(schema), type.getCode());
					} else {
						if (typesBeforeMigration.hasSchema(schema.getCode())) {
							main.addStatement("$T $L = $LSchemaType.getCustomSchema($S)",
									MetadataSchemaBuilder.class, variableOf(schema), type.getCode(), schema.getLocalCode());
						} else {
							main.addStatement("$T $L = $LSchemaType.createCustomSchema($S)",
									MetadataSchemaBuilder.class, variableOf(schema), type.getCode(), schema.getLocalCode());
							for (RecordValidator validator : schema.getValidators()) {
								main.addStatement("$L.defineValidators().add($T.class)", variableOf(schema),
										validator.getClass());
							}
						}
					}
				}
			}
		}

		for (MetadataSchemaType type : metadataSchemaTypes) {
			if (typesBeforeMigration == null || !typesBeforeMigration.hasType(type.getCode())) {
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
						main.addStatement("$L.defineValidators().add($T.class)", variableOf(schema), validator.getClass());
					}
				}
			}
		}

		for (MetadataSchemaType type : metadataSchemaTypes) {
			for (MetadataSchema schema : type.getAllSchemas()) {
				for (Metadata metadata : schema.getMetadatas()) {
					String variable = variableOf(metadata);
					if (metadata.getInheritance() == null && (typesBeforeMigration == null || !typesBeforeMigration
							.hasMetadata(metadata.getCode()))) {
						if (!Schemas.isGlobalMetadata(metadata.getLocalCode()) || "url".equals(metadata.getLocalCode())) {
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
							main.addStatement("$L.defineValidators().add($T.class)", variableOf(metadata),
									validator.getClass());
						}
					}
				}
			}
		}

		for (MetadataSchemaType type : metadataSchemaTypes) {
			for (MetadataSchema schema : type.getAllSchemas()) {
				for (Metadata metadata : schema.getMetadatas()) {
					String variable = variableOf(metadata);
					if(metadata.getInheritance() != null) {
						main.addStatement("$T $L = $L.get($S)",
								MetadataBuilder.class, variable, variableOf(schema), metadata.getLocalCode());
						configureInheritedMetadata(main, variable, metadata);
					}
				}
			}
		}

		for (MetadataSchemaType type : metadataSchemaTypes) {

			for (MetadataSchema schema : type.getAllSchemas()) {
				for (Metadata metadata : schema.getMetadatas()) {
					if (metadata.getInheritance() == null && (typesBeforeMigration == null || !typesBeforeMigration
							.hasMetadata(metadata.getCode()))) {
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

	protected List<MetadataSchemaType> sorted(List<MetadataSchemaType> schemaTypes) {
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

	protected String asListLitteral(List<String> values) {
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

	protected String asListLitteralWithoutQuotes(List<String> values) {
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

	protected String variableOf(MetadataSchemaType schemaType) {
		return schemaType.getCode() + "SchemaType";
	}

	protected String variableOf(MetadataSchema schema) {
		if ("default".equals(schema.getLocalCode())) {
			return schema.getCode().split("_")[0] + "Schema";
		} else {
			return schema.getCode() + "Schema";
		}
	}

	protected String variableOf(Metadata metadata) {
		return variableOfMetadata(metadata.getCode());
	}

	protected String variableOfMetadata(String code) {
		if (code.contains("_default_")) {
			return code.split("_")[0] + "_" + code.split("_")[2];
		} else {
			return code;
		}
	}

	protected String typeAlterations(MetadataSchemaType type) {
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

	protected void configureMetadata(Builder method, String variable, Metadata metadata) {

		if (metadata.isMultivalue()) {
			method.addStatement("$L.setMultivalue(true)", variable);
		}

		if (metadata.getInputMask() != null) {
			method.addStatement("$L.setInputMask($S)", variable, metadata.getInputMask());
		}

		if (metadata.isMarkedForDeletion()) {
			method.addStatement("$L.setMarkedForDeletion(true)", variable);
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

		if (metadata.isEncrypted()) {
			method.addStatement("$L.setEncrypted(true)", variable);
		}

		if (metadata.isSortable()) {
			method.addStatement("$L.setSortable(true)", variable);
		}

		if (metadata.isDuplicable()) {
			method.addStatement("$L.setDuplicable(true)", variable);
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

		if (!metadata.getPopulateConfigs().getProperties().isEmpty()) {
			method.addStatement("$L.getPopulateConfigsBuilder().setProperties($L)", variable,
					asListLitteral(metadata.getPopulateConfigs().getProperties()));
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

	protected void configureInheritedMetadata(Builder method, String variable, Metadata metadata) {
		if (metadata.getInputMask() != null) {
			method.addStatement("$L.setInputMask($S)", variable, metadata.getInputMask());
		}

		if (metadata.getInheritance() != null && metadata.isDefaultRequirement() != metadata.getInheritance()
				.isDefaultRequirement()) {
			method.addStatement("$L.setDefaultRequirement($L)", variable, String.valueOf(metadata.isDefaultRequirement()));
		}

		if (metadata.getInheritance() != null && metadata.isEnabled() != metadata.getInheritance()
				.isEnabled()) {
			method.addStatement("$L.setEnabled($L)", variable, String.valueOf(metadata.isEnabled()));
		}
	}

}
