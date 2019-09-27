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
import com.constellio.app.services.schemasDisplay.SchemaTypesDisplayTransactionBuilder;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.data.conf.IdGeneratorType;
import com.constellio.data.conf.PropertiesDataLayerConfiguration.InMemoryDataLayerConfiguration;
import com.constellio.data.dao.services.records.DataStore;
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
import com.constellio.model.entities.schemas.MetadataTransiency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.entities.schemas.entries.AggregatedDataEntry;
import com.constellio.model.entities.schemas.entries.AggregationType;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.entities.security.Role;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.extensions.ConstellioModulesManager;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.SchemaUtils;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.util.LangUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.constellio.model.entities.records.wrappers.Collection.SYSTEM_COLLECTION;
import static com.constellio.model.entities.schemas.entries.AggregationType.LOGICAL_AND;
import static com.constellio.model.entities.schemas.entries.AggregationType.LOGICAL_OR;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static java.util.Arrays.asList;

@InDevelopmentTest
public class ComboMigrationsGeneratorAcceptanceTest extends ConstellioTest {

	String collection = zeCollection;
	public Class[] problems = new Class[]{
			ArrayList.class,
			RolesManager.class,
			Map.class,
			List.class,
			HashMap.class,
			MetadataValueType.class,
			MetadataTransiency.class,
			SchemaTypesDisplayConfig.class,
			SchemaTypesDisplayTransactionBuilder.class,
			RecordCacheType.class
	};

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

		givenCollection(collection);

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
				.addMethods(generateTypes(null))
				.addMethod(generateDisplayConfigs(new ArrayList<String>()))
				.addMethod(generateRoles(new ArrayList<Role>()))
				.addMethod(generateConstructor())
				.build();

		JavaFile file = JavaFile.builder("com.constellio.app.services.migrations", generatedClassSpec)
				.addStaticImport(java.util.Arrays.class, "asList")
				.addStaticImport(HashMapBuilder.class, "stringObjectMap")
				.build();

		String fileWithoutProblems = this.resolveProblems(file);
		File dest = new File(
				getFoldersLocator().getAppProject()
				+ "/src/com/constellio/app/services/migrations/GeneratedCoreMigrationCombo.java");
		FileUtils.writeStringToFile(dest, fileWithoutProblems);
	}

	@Test
	public void generateSystemCoreMigrations()
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

		givenCollection(collection);
		collection = "_system_";

		System.out.println(" ------ Migration script ------");

		File migrationsResources = new File(new FoldersLocator().getI18nFolder(), "migrations");

		//generateI18n(new File(migrationsResources, "system"));

		MethodSpec constructor = generateConstructor();

		TypeSpec generatedClassSpec = TypeSpec.classBuilder("GeneratedSystemMigrationCombo")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addField(String.class, "collection")
				.addField(AppLayerFactory.class, "appLayerFactory")
				.addField(MigrationResourcesProvider.class, "resourcesProvider")
				//.addMethod(generateRecords())
				.addMethods(generateTypes(null))
				.addMethod(generateDisplayConfigs(new ArrayList<String>()))
				.addMethod(generateRoles(new ArrayList<Role>()))
				.addMethod(generateConstructor())
				.build();

		JavaFile file = JavaFile.builder("com.constellio.app.services.migrations", generatedClassSpec)
				.addStaticImport(java.util.Arrays.class, "asList")
				.addStaticImport(HashMapBuilder.class, "stringObjectMap")
				.build();

		String fileWithoutProblems = this.resolveProblems(file);
		File dest = new File(
				getFoldersLocator().getAppProject()
				+ "/src/com/constellio/app/services/migrations/GeneratedSystemMigrationCombo.java");
		FileUtils.writeStringToFile(dest, fileWithoutProblems);
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

		givenCollection(collection);
		CollectionsListManager collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		ConstellioModulesManager constellioModulesManager = getAppLayerFactory().getModulesManager();
		constellioModulesManager.installValidModuleAndGetInvalidOnes(new TaskModule(), collectionsListManager);
		constellioModulesManager.enableValidModuleAndGetInvalidOnes(collection, new TaskModule());

		List<Role> rolesBefore = getModelLayerFactory().getRolesManager().getAllRoles(collection);
		List<String> codesBefore = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.rewriteInOrderAndGetCodes(collection);
		MetadataSchemaTypes typesBefore = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);

		constellioModulesManager.installValidModuleAndGetInvalidOnes(new ConstellioRMModule(), collectionsListManager);
		constellioModulesManager.enableValidModuleAndGetInvalidOnes(collection, new ConstellioRMModule());

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
				.addMethods(generateTypes(typesBefore))
				.addMethod(generateDisplayConfigs(codesBefore))
				.addMethod(generateRoles(rolesBefore))
				.addMethod(generateConstructor())
				.build();

		JavaFile file = JavaFile.builder("com.constellio.app.modules.rm.migrations", generatedClassSpec)
				.addStaticImport(java.util.Arrays.class, "asList")
				.addStaticImport(HashMapBuilder.class, "stringObjectMap")
				.build();
		String newFile = resolveProblems(file);
		File dest = new File(getFoldersLocator().getAppProject()
							 + "/src/com/constellio/app/modules/rm/migrations/GeneratedRMMigrationCombo.java");
		FileUtils.writeStringToFile(dest, newFile);
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

		givenCollection(collection);
		CollectionsListManager collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		ConstellioModulesManager constellioModulesManager = getAppLayerFactory().getModulesManager();

		List<Role> rolesBefore = getModelLayerFactory().getRolesManager().getAllRoles(collection);
		List<String> codesBefore = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.rewriteInOrderAndGetCodes(collection);
		MetadataSchemaTypes typesBefore = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);

		constellioModulesManager.installValidModuleAndGetInvalidOnes(new TaskModule(), collectionsListManager);
		constellioModulesManager.enableValidModuleAndGetInvalidOnes(collection, new TaskModule());

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
				.addMethods(generateTypes(typesBefore))
				.addMethod(generateDisplayConfigs(codesBefore))
				.addMethod(generateRoles(rolesBefore))
				.addMethod(generateConstructor())
				.build();

		JavaFile file = JavaFile.builder("com.constellio.app.modules.tasks.migrations", generatedClassSpec)
				.addStaticImport(java.util.Arrays.class, "asList")
				.addStaticImport(HashMapBuilder.class, "stringObjectMap")
				.build();

		String fileWithoutProblems = this.resolveProblems(file);

		File dest = new File(getFoldersLocator().getAppProject()
							 + "/src/com/constellio/app/modules/tasks/migrations/GeneratedTasksMigrationCombo.java");
		FileUtils.writeStringToFile(dest, fileWithoutProblems);
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

		givenCollection(collection);
		CollectionsListManager collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		ConstellioModulesManager constellioModulesManager = getAppLayerFactory().getModulesManager();

		List<Role> rolesBefore = getModelLayerFactory().getRolesManager().getAllRoles(collection);
		List<String> codesBefore = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.rewriteInOrderAndGetCodes(collection);
		MetadataSchemaTypes typesBefore = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);

		constellioModulesManager.installValidModuleAndGetInvalidOnes(new ConstellioRobotsModule(), collectionsListManager);
		constellioModulesManager.enableValidModuleAndGetInvalidOnes(collection, new ConstellioRobotsModule());

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
				.addMethods(generateTypes(typesBefore))
				.addMethod(generateDisplayConfigs(codesBefore))
				.addMethod(generateRoles(rolesBefore))
				.addMethod(generateConstructor())
				.build();

		JavaFile file = JavaFile.builder("com.constellio.app.modules.robots.migrations", generatedClassSpec)
				.addStaticImport(java.util.Arrays.class, "asList")
				.addStaticImport(HashMapBuilder.class, "stringObjectMap")
				.build();

		String fileWithoutProblems = this.resolveProblems(file);
		File dest = new File(getFoldersLocator().getAppProject()
							 + "/src/com/constellio/app/modules/robots/migrations/GeneratedRobotsMigrationCombo.java");
		FileUtils.writeStringToFile(dest, fileWithoutProblems);
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

		givenCollection(collection);
		CollectionsListManager collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		ConstellioModulesManager constellioModulesManager = getAppLayerFactory().getModulesManager();

		List<Role> rolesBefore = getModelLayerFactory().getRolesManager().getAllRoles(collection);
		List<String> codesBefore = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.rewriteInOrderAndGetCodes(collection);
		MetadataSchemaTypes typesBefore = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);

		constellioModulesManager.installValidModuleAndGetInvalidOnes(new ConstellioESModule(), collectionsListManager);
		constellioModulesManager.enableValidModuleAndGetInvalidOnes(collection, new ConstellioESModule());

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
				.addMethods(generateTypes(typesBefore))
				.addMethod(generateDisplayConfigs(codesBefore))
				.addMethod(generateRoles(rolesBefore))
				.addMethod(generateConstructor())
				.build();

		JavaFile file = JavaFile.builder("com.constellio.app.modules.es.migrations", generatedClassSpec)
				.addStaticImport(java.util.Arrays.class, "asList")
				.addStaticImport(HashMapBuilder.class, "stringObjectMap")
				.build();

		String fileWithoutProblems = this.resolveProblems(file);
		File dest = new File(getFoldersLocator().getAppProject()
							 + "/src/com/constellio/app/modules/es/migrations/GeneratedESMigrationCombo.java");
		FileUtils.writeStringToFile(dest, fileWithoutProblems);
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

		givenCollection(collection);
		CollectionsListManager collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		ConstellioModulesManager constellioModulesManager = getAppLayerFactory().getModulesManager();

		((JSPFConstellioPluginManager) getAppLayerFactory().getPluginManager()).unregisterModule(new ESRMRobotsModule());

		constellioModulesManager.installValidModuleAndGetInvalidOnes(new ConstellioESModule(), collectionsListManager);
		constellioModulesManager.enableValidModuleAndGetInvalidOnes(collection, new ConstellioESModule());
		constellioModulesManager.installValidModuleAndGetInvalidOnes(new ConstellioRMModule(), collectionsListManager);
		constellioModulesManager.enableValidModuleAndGetInvalidOnes(collection, new ConstellioRMModule());
		constellioModulesManager.installValidModuleAndGetInvalidOnes(new ConstellioRobotsModule(), collectionsListManager);
		constellioModulesManager.enableValidModuleAndGetInvalidOnes(collection, new ConstellioRobotsModule());

		List<Role> rolesBefore = getModelLayerFactory().getRolesManager().getAllRoles(collection);
		List<String> codesBefore = getAppLayerFactory().getMetadataSchemasDisplayManager()
				.rewriteInOrderAndGetCodes(collection);
		MetadataSchemaTypes typesBefore = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);

		((JSPFConstellioPluginManager) getAppLayerFactory().getPluginManager()).registerModule(new ESRMRobotsModule());
		constellioModulesManager.installValidModuleAndGetInvalidOnes(new ESRMRobotsModule(), collectionsListManager);
		constellioModulesManager.enableValidModuleAndGetInvalidOnes(collection, new ESRMRobotsModule());

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
				.addMethods(generateTypes(typesBefore))
				.addMethod(generateDisplayConfigs(codesBefore))
				.addMethod(generateRoles(rolesBefore))
				.addMethod(generateConstructor())
				.build();

		JavaFile file = JavaFile.builder("com.constellio.app.modules.complementary.esRmRobots.migrations", generatedClassSpec)
				.addStaticImport(java.util.Arrays.class, "asList")
				.addStaticImport(HashMapBuilder.class, "stringObjectMap")
				.build();

		String fileWithoutProblems = this.resolveProblems(file);
		File dest = new File(getFoldersLocator().getAppProject()
							 + "/src/com/constellio/app/modules/complementary/esRmRobots/migrations/GeneratedESRMRobotsMigrationCombo.java");
		FileUtils.writeStringToFile(dest, fileWithoutProblems);
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

	//	private MethodSpec generateSearchBoost() {
	//		Builder main = MethodSpec.methodBuilder("applySearchBoost").addModifiers(Modifier.PUBLIC)
	//				.addParameter(SearchBoostManager.class, "manager")
	//				.returns(void.class);
	//
	//		SearchBoostManager manager = getModelLayerFactory().getSearchBoostManager();
	//		main.beginControlFlow("for(SearchBoost searchBoost : manager.getAllSearchBoosts(collection))");
	//		main.addStatement("manager.delete(collection, searchBoost)");
	//		main.endControlFlow();
	//		main.addStatement("SearchBoost newBoost");
	//		for (SearchBoost searchBoost : manager.getAllSearchBoosts(collection)) {
	//			main.addStatement("newBoost = new SearchBoost(SearchBoost.$N_TYPE, $S, $S, $N)",
	//					searchBoost.getType().toString().toUpperCase(), searchBoost.getKey(), searchBoost.getLabel(), searchBoost.getValue()+"d");
	//			main.addStatement("manager.add(collection, newBoost)");
	//		}
	//
	//		MethodSpec spec = main.build();
	//		System.out.println(spec.toString());
	//		return spec;
	//	}

	protected void generateI18n(File moduleFolder)
			throws IOException {

		generateI18n(moduleFolder, moduleFolder.getName());
	}

	protected void generateI18n(File moduleFolder, String module)
			throws IOException {
		generateI18n(moduleFolder, module, new HashMap<String, String>(), new HashMap<String, String>(), new HashMap<String, String>());
	}

	protected void generateI18n(File moduleFolder, String module, Map<String, String> extraFrenchLabels,
								Map<String, String> extraEnglishLabels, Map<String, String> extraArabicLabels)
			throws IOException {

		File comboFolder = new File(moduleFolder, "combo");
		FileUtils.deleteQuietly(comboFolder);
		FileUtils.forceMkdir(comboFolder);

		List<File> childFiles = Arrays.asList(moduleFolder.listFiles());
		childFiles.sort((f1, f2) -> f1.getName().compareTo(f2.getName()));
		if (childFiles != null) {

			List<File> properties = new ArrayList<>();
			List<File> frProperties = new ArrayList<>();
			List<File> arProperties = new ArrayList<>();
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
							} else if (file.getName().endsWith("_ar.properties")) {
								arProperties.add(file);
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
					.combine(arProperties, new File(comboFolder, module + "_combo_ar.properties"), extraArabicLabels);
			CombinePropertyFilesServices
					.combine(frProperties, new File(comboFolder, module + "_combo_fr.properties"), new HashMap<String, String>());

			for (File resourceFile : resourcesFiles) {
				if (resourceFile.isDirectory()) {
					FileUtils.copyDirectory(resourceFile, new File(comboFolder, resourceFile.getName()));
				} else {
					System.out.println("Copying " + resourceFile.getAbsolutePath());
					FileUtils.copyFile(resourceFile, new File(comboFolder, resourceFile.getName()));
				}
			}
		}
	}

	protected MethodSpec generateDisplayConfigs(List<String> codesBefore) {
		SchemasDisplayManager manager = getAppLayerFactory().getMetadataSchemasDisplayManager();
		Builder main = MethodSpec.methodBuilder("applySchemasDisplay")
				.addModifiers(Modifier.PUBLIC)
				.addParameter(SchemasDisplayManager.class, "manager")
				.returns(void.class);

		List<String> codes = manager.rewriteInOrderAndGetCodes(collection);

		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		SchemaTypesDisplayConfig typesDisplay = manager.getTypes(collection);
		main.addStatement("SchemaTypesDisplayTransactionBuilder transaction = manager.newTransactionBuilderFor(collection)");
		main.addStatement("SchemaTypesDisplayConfig typesConfig = manager.getTypes(collection)");

		if (codes.contains("SchemaTypesDisplayConfig")) {
			main.addStatement("transaction.setModifiedCollectionTypes(manager.getTypes(collection).withFacetMetadataCodes($L))",
					asListLitteral(typesDisplay.getFacetMetadataCodes()));
		}

		for (MetadataSchemaType type : types.getSchemaTypes()) {
			SchemaTypeDisplayConfig typeDisplay = manager.getType(collection, type.getCode());
			if (codes.contains(typeDisplay.getSchemaType()) && !codesBefore.contains(typeDisplay.getSchemaType())) {
				main.addStatement("transaction.add(manager.getType(collection, $S).withSimpleSearchStatus($L)"
								  + ".withAdvancedSearchStatus($L).withManageableStatus($L)"
								  + ".withMetadataGroup(resourcesProvider.getLanguageMap($L)))", type.getCode(),
						typeDisplay.isSimpleSearch(), typeDisplay.isAdvancedSearch(), typeDisplay.isManageable(),
						asListLitteral(new ArrayList<String>(typeDisplay.getMetadataGroup().keySet())));
			}

			for (MetadataSchema schema : type.getAllSchemas()) {
				SchemaDisplayConfig schemaDisplay = manager.getSchema(collection, schema.getCode());
				if (codes.contains(schemaDisplay.getSchemaCode()) && !codesBefore.contains(schemaDisplay.getSchemaCode())) {
					main.addStatement("transaction.add(manager.getSchema(collection, $S).withFormMetadataCodes($L)"
									  + ".withDisplayMetadataCodes($L).withSearchResultsMetadataCodes($L).withTableMetadataCodes($L))"
							, schema.getCode(), asListLitteral(schemaDisplay.getFormMetadataCodes())
							, asListLitteral(schemaDisplay.getDisplayMetadataCodes())
							, asListLitteral(schemaDisplay.getSearchResultsMetadataCodes())
							, asListLitteral(schemaDisplay.getTableMetadataCodes()));
				}

				for (Metadata metadata : schema.getMetadatas()) {
					MetadataDisplayConfig metadataDisplay = manager.getMetadata(collection, metadata.getCode());

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
		for (Role role : rolesManager.getAllRoles(collection)) {

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

		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
		List<Record> records = searchServices.search(new LogicalSearchQuery(fromAllSchemasIn(collection).returnAll()));
		Map<String, Integer> mapping = new HashMap<>();
		int i = 0;
		for (Record record : records) {
			if (!record.getId().contains(collection) && !record.getId().contains(SYSTEM_COLLECTION)) {
				mapping.put(record.getId(), i++);
				main.addStatement("records.add(recordServices.newRecordWithSchema(types.getSchema($S)))", record.getId());
			}
		}
		i = 0;
		for (Record record : records) {
			if (!record.getId().contains(collection) && !record.getId().contains(SYSTEM_COLLECTION)) {
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

	protected List<MethodSpec> generateTypes(MetadataSchemaTypes typesBeforeMigration) {
		MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);

		Builder main = MethodSpec.methodBuilder("applyGeneratedSchemaAlteration")
				.addModifiers(Modifier.PUBLIC)
				.returns(void.class)
				.addParameter(MetadataSchemaTypesBuilder.class, "typesBuilder");

		Map<String, Builder> typeMethadatasMethodsBuilder = new HashMap<>();

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
				main.addStatement("$T $LSchemaType = typesBuilder.createNewSchemaType($S,false)$L",
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

			String methodName = "create" + StringUtils.capitalize(type.getCode()) + "SchemaTypeMetadatas";
			Builder typeMetadatasMethod = MethodSpec.methodBuilder(methodName)
					.addModifiers(Modifier.PRIVATE)
					.returns(void.class)
					.addParameter(MetadataSchemaTypesBuilder.class, "types")
					.addParameter(MetadataSchemaTypeBuilder.class, variableOf(type));
			typeMethadatasMethodsBuilder.put(type.getCode(), typeMetadatasMethod);

			StringBuilder createTypeMetadatasMethodCall = new StringBuilder(methodName);
			createTypeMetadatasMethodCall.append("(typesBuilder,");
			createTypeMetadatasMethodCall.append(variableOf(type));

			for (MetadataSchema schema : type.getAllSchemas()) {
				typeMetadatasMethod.addParameter(MetadataSchemaBuilder.class, variableOf(schema));
				createTypeMetadatasMethodCall.append(", ");
				createTypeMetadatasMethodCall.append(variableOf(schema));
			}

			createTypeMetadatasMethodCall.append(")");
			main.addStatement(createTypeMetadatasMethodCall.toString());

			for (MetadataSchema schema : type.getAllSchemas()) {
				for (Metadata metadata : schema.getMetadatas()) {
					String variable = variableOf(metadata);
					if (metadata.getInheritance() == null && (typesBeforeMigration == null || !typesBeforeMigration
							.hasMetadata(metadata.getCode()))) {
						//if (!Schemas.isGlobalMetadata(metadata.getLocalCode()) || "url".equals(metadata.getLocalCode())) {
						typeMetadatasMethod.addStatement("$T $L = $L.create($S).setType(MetadataValueType.$L)",
								MetadataBuilder.class, variable, variableOf(schema), metadata.getLocalCode(),
								metadata.getType().name());
						configureMetadata(typeMetadatasMethod, variable, metadata);
						//						} else {
						//
						//							main.addStatement("$T $L = $L.get($S)",
						//									MetadataBuilder.class, variable, variableOf(schema), metadata.getLocalCode());
						//							configureMetadata(main, variable, metadata);
						//						}
						for (RecordMetadataValidator validator : metadata.getValidators()) {
							typeMetadatasMethod.addStatement("$L.defineValidators().add($T.class)", variableOf(metadata),
									validator.getClass());
						}
					}
				}
			}
		}

		for (MetadataSchemaType type : metadataSchemaTypes) {
			Builder typeMetadatasMethod = typeMethadatasMethodsBuilder.get(type.getCode());
			for (MetadataSchema schema : type.getAllSchemas()) {
				for (Metadata metadata : schema.getMetadatas()) {
					String variable = variableOf(metadata);
					if (metadata.getInheritance() != null) {
						typeMetadatasMethod.addStatement("$T $L = $L.get($S)",
								MetadataBuilder.class, variable, variableOf(schema), metadata.getLocalCode());
						configureInheritedMetadata(typeMetadatasMethod, variable, metadata);
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

							Metadata referenceMetadata = schema.getMetadata(dataEntry.getReferenceMetadata());
							Metadata copiedMetadata = types.getSchemaType(referenceMetadata.getReferencedSchemaType()).
									getDefaultSchema().getMetadata(dataEntry.getCopiedMetadata());

							main.addStatement("$L.get($S).defineDataEntry().asCopied($L.get($S), typesBuilder.getMetadata($S))",
									variableOf(schema),
									metadata.getLocalCode(),
									variableOf(schema),
									referenceMetadata.getLocalCode(),
									copiedMetadata.getCode());
						}
						if (metadata.getDataEntry().getType() == DataEntryType.CALCULATED) {
							CalculatedDataEntry dataEntry = (CalculatedDataEntry) metadata.getDataEntry();
							main.addStatement("$L.get($S).defineDataEntry().asCalculated($T.class)",
									variableOf(schema),
									metadata.getLocalCode(),
									dataEntry.getCalculator().getClass());
						}
						if (metadata.getDataEntry().getType() == DataEntryType.AGGREGATED) {
							AggregatedDataEntry dataEntry = (AggregatedDataEntry) metadata.getDataEntry();

							if (dataEntry.getAgregationType().equals(AggregationType.REFERENCE_COUNT)) {
								List<Metadata> referenceMetadatas = types.getMetadatas(dataEntry.getReferenceMetadatas());
								Metadata referenceMetadata = !referenceMetadatas.isEmpty() ? referenceMetadatas.get(0) : null;
								main.addStatement("$L.get($S).defineDataEntry().asReferenceCount(typesBuilder.getMetadata($S))",
										variableOf(schema),
										metadata.getLocalCode(),
										referenceMetadata.getCode());
							}
							if (dataEntry.getAgregationType().equals(AggregationType.SUM)) {
								List<Metadata> referenceMetadatas = types.getMetadatas(dataEntry.getReferenceMetadatas());
								Metadata referenceMetadata = !referenceMetadatas.isEmpty() ? referenceMetadatas.get(0) : null;

								List<String> inputMetadatasCalls = new ArrayList<>();
								//types.getMetadata($S)

								for (String inputMetadata : dataEntry.getInputMetadatas()) {
									inputMetadatasCalls.add("typesBuilder.getMetadata(\"" + inputMetadata + "\")");
								}

								main.addStatement("$L.get($S).defineDataEntry().asSum(typesBuilder.getMetadata($S), $L)",
										variableOf(schema),
										metadata.getLocalCode(),
										referenceMetadata.getCode(),
										StringUtils.join(inputMetadatasCalls, ", "));
							}

							if (dataEntry.getAgregationType().equals(AggregationType.VALUES_UNION)) {
								List<Metadata> referenceMetadatas = types.getMetadatas(dataEntry.getReferenceMetadatas());
								Metadata referenceMetadata = !referenceMetadatas.isEmpty() ? referenceMetadatas.get(0) : null;

								List<String> inputMetadatasCalls = new ArrayList<>();
								//types.getMetadata($S)

								for (String inputMetadata : dataEntry.getInputMetadatas()) {
									inputMetadatasCalls.add("typesBuilder.getMetadata(\"" + inputMetadata + "\")");
								}

								main.addStatement("$L.get($S).defineDataEntry().asUnion(typesBuilder.getMetadata($S), $L)",
										variableOf(schema),
										metadata.getLocalCode(),
										referenceMetadata.getCode(),
										StringUtils.join(inputMetadatasCalls, ", "));
							}

							if (dataEntry.getAgregationType().equals(LOGICAL_OR) || dataEntry.getAgregationType().equals(LOGICAL_AND)) {
								List<Metadata> referenceMetadatas = types.getMetadatas(dataEntry.getReferenceMetadatas());
								Metadata referenceMetadata = !referenceMetadatas.isEmpty() ? referenceMetadatas.get(0) : null;

								List<String> inputMetadatasCalls = new ArrayList<>();
								//types.getMetadata($S)

								for (String inputMetadata : dataEntry.getInputMetadatas()) {
									inputMetadatasCalls.add("typesBuilder.getMetadata(\"" + inputMetadata + "\")");
								}

								Map<MetadataBuilder, List<MetadataBuilder>> metadatasBy = new HashMap<>();
								//								metadatasByRefMetadata.put(documentSchema.get(Document.FOLDER), singletonList(documentSchema.get(Document.HAS_CONTENT)));
								//								metadatasByRefMetadata.put(folderSchema.get(Folder.PARENT_FOLDER), singletonList(folderHasContent));
								//								folderHasContent.defineDataEntry().asAggregatedOr(metadatasByRefMetadata);

								main.addStatement("Map<MetadataBuilder, List<MetadataBuilder>> $LRefs = new HashMap<>();", metadata.getCode());

								for (Map.Entry<String, List<String>> entry : dataEntry.getInputMetadatasByReferenceMetadata().entrySet()) {
									String schemaCode = new SchemaUtils().getSchemaCode(entry.getKey());
									String metadataLocalCode = new SchemaUtils().getLocalCode(entry.getKey(), schemaCode);

									StringBuilder valuesInstructions = new StringBuilder();

									for (String value : entry.getValue()) {

										String valueSchemaCode = new SchemaUtils().getSchemaCode(value);
										String valueMetadataLocalCode = new SchemaUtils().getLocalCode(value, valueSchemaCode);

										if (valuesInstructions.length() > 0) {
											valuesInstructions.append(", ");
										}
										valuesInstructions.append(variableOfSchema(valueSchemaCode));
										valuesInstructions.append(".get(\"");
										valuesInstructions.append(valueMetadataLocalCode);
										valuesInstructions.append("\")");
									}

									main.addStatement("$LRefs.put($L.get($S), asList($L));",
											metadata.getCode(),
											variableOfSchema(schemaCode),
											metadataLocalCode,
											valuesInstructions);
								}


								main.addStatement("$L.get($S).defineDataEntry().asAggregated$L($LRefs)",
										variableOf(schema),
										metadata.getLocalCode(),
										dataEntry.getAgregationType() == LOGICAL_OR ? "Or" : "And",
										metadata.getCode()
								);
							}
						}
					}
				}
			}

		}

		List<MethodSpec> specs = new ArrayList<>();
		specs.add(main.build());
		//System.out.println(spec.toString());
		for (Builder builder : typeMethadatasMethodsBuilder.values()) {
			specs.add(builder.build());
		}

		MethodSpec spec = main.build();
		return specs;
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
		return variableOfSchema(schema.getCode());
	}

	protected String variableOfSchema(String schemaCode) {
		if (schemaCode.contains("_default")) {
			return schemaCode.split("_")[0] + "Schema";
		} else {
			return schemaCode + "Schema";
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

		if (type.getCacheType() != RecordCacheType.FULLY_CACHED) {
			stringBuilder.append(".setRecordCacheType(RecordCacheType." + type.getCacheType().name() + ")");
		}

		if (type.getSmallCode() != null) {
			stringBuilder.append(".setSmallCode(\"" + type.getSmallCode() + "\")");
		}


		if (!DataStore.RECORDS.equals(type.getDataStore())) {
			stringBuilder.append(".setDataStore(\"" + type.getDataStore() + "\")");
		}

		return stringBuilder.toString();
	}

	protected void configureMetadata(Builder method, String variable, Metadata metadata) {

		if (MetadataTransiency.TRANSIENT_EAGER.equals(metadata.getTransiency()) || MetadataTransiency.TRANSIENT_LAZY
				.equals(metadata.getTransiency())) {
			method.addStatement("$L.setTransiency(MetadataTransiency.$N)", variable,
					metadata.getTransiency().name().toUpperCase());
		}

		if (metadata.isMultivalue()) {
			method.addStatement("$L.setMultivalue(true)", variable);
		}

		if (metadata.getInputMask() != null) {
			method.addStatement("$L.setInputMask($S)", variable, metadata.getInputMask());
		}

		if (metadata.isMarkedForDeletion()) {
			method.addStatement("$L.setMarkedForDeletion(true)", variable);
		}

		if (metadata.isCacheIndex()) {
			method.addStatement("$L.setCacheIndex(true)", variable);
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

		if (metadata.isMultiLingual() || metadata.isGlobal()) {
			method.addStatement("$L.setMultiLingual(" + metadata.isMultiLingual() + ")", variable);
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

		if (metadata.getType() == MetadataValueType.REFERENCE /*&& !Schemas.isGlobalMetadata(metadata.getLocalCode()) */) {
			MetadataSchemaTypes types = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection);
			if (metadata.getAllowedReferences().getAllowedSchemas().isEmpty()) {
				String referencedType = metadata.getAllowedReferences().getAllowedSchemaType();
				if (metadata.isTaxonomyRelationship()) {
					method.addStatement("$L.defineTaxonomyRelationshipToType(types.getSchemaType($S))", variable, referencedType);

				} else if (metadata.isChildOfRelationship()) {
					method.addStatement("$L.defineChildOfRelationshipToType(types.getSchemaType($S))", variable, referencedType);

				} else {
					method.addStatement("$L.defineReferencesTo(types.getSchemaType($S))", variable, referencedType);

				}
			} else {
				Set<String> referencedSchemas = metadata.getAllowedReferences().getAllowedSchemas();
				List<String> referencedSchemasVariables = new ArrayList<>();
				for (String referencedSchema : referencedSchemas) {
					referencedSchemasVariables.add("types.getSchema(\"" + referencedSchema + "\")");
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

	private String resolveProblems(JavaFile file) {
		List<String> lines = new ArrayList<>(Arrays.asList(file.toString().split("\n")));
		for (Class clazz : problems) {
			lines.add(2, "import " + clazz.getName() + ";");
		}
		return StringUtils.join(lines, "\n");
	}

}
