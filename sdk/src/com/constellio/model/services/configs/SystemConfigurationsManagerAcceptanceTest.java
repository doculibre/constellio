package com.constellio.model.services.configs;

import com.constellio.app.entities.modules.InstallableModule;
import com.constellio.app.entities.modules.MigrationResourcesProvider;
import com.constellio.app.entities.modules.MigrationScript;
import com.constellio.app.entities.navigation.NavigationConfig;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.Delayed;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.MetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.ConfigDependency;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.configs.SystemConfiguration;
import com.constellio.model.entities.configs.SystemConfigurationGroup;
import com.constellio.model.entities.configs.SystemConfigurationScript;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.configs.SystemConfigurationsManagerRuntimeException.SystemConfigurationsManagerRuntimeException_InvalidConfigValue;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataBuilder_EnumClassTest.AValidEnum;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.constellio.sdk.tests.TestUtils.linkEventBus;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@SlowTest
public class SystemConfigurationsManagerAcceptanceTest extends ConstellioTest {

	static AtomicInteger callCollectionsActionCallCount;

	String anotherCollection = "anotherCollection";
	String aThirdCollection = "aThirdCollection";

	static SystemConfigurationGroup aGroup = new SystemConfigurationGroup("zeModule", "aGroup");

	static SystemConfigurationGroup anOtherGroup = new SystemConfigurationGroup("zeModule", "anotherGroup");

	static SystemConfiguration numberUsedByCalculators = aGroup.createInteger("numberUsedByCalculators").withDefaultValue(42);
	static SystemConfiguration textAlteringSchemas = aGroup.createString("textAlteringSchemas").scriptedBy(
			TextAlteringSchemasScript.class).withDefaultValue("ohHellNo");

	static SystemConfiguration text = aGroup.createString("text");
	static SystemConfiguration textWithDefaultValue = aGroup.createString("textWithDefaultValue").withDefaultValue("bob");
	static SystemConfiguration booleanWithTrueByDefault = aGroup.createBooleanTrueByDefault("booleanWithTrueByDefault");
	static SystemConfiguration booleanWithFalseByDefault = aGroup.createBooleanFalseByDefault("booleanWithFalseByDefault");
	static SystemConfiguration binary = aGroup.createBinary("binary");
	static SystemConfiguration number = aGroup.createInteger("number");
	static SystemConfiguration numberWithDefaultValue = aGroup.createInteger("numberWithDefaultValue").withDefaultValue(42);
	static SystemConfiguration enumValue = anOtherGroup.createEnum("enumValue", AValidEnum.class);
	static SystemConfiguration enumWithDefaultValue = anOtherGroup.createEnum("enumWithDefaultValue", AValidEnum.class)
			.withDefaultValue(AValidEnum.FIRST_VALUE);
	static SystemConfigurationsManager manager, managerOfOtherInstance;

	@Before
	public void setUp()
			throws Exception {

		manager = getModelLayerFactory().getSystemConfigurationsManager();
		managerOfOtherInstance = getModelLayerFactory("other-instance").getSystemConfigurationsManager();
		linkEventBus(getModelLayerFactory(), getModelLayerFactory("other-instance"));

		givenSpecialCollection(zeCollection).withModule(ZeModule.class).withAllTestUsers();
		givenSpecialCollection(anotherCollection).withModule(ZeModule.class).withAllTestUsers();
		givenSpecialCollection(aThirdCollection).withAllTestUsers();

		//withSpiedServices(ConstellioPluginManager.class);

		callCollectionsActionCallCount = new AtomicInteger();
	}

	@Test
	public void whenGetConfigurationGroupsAndConfigurationsThenReturnInstalledModulesConfigurations()
			throws Exception {

		assertThat(manager.getConfigurationGroups()).containsOnlyOnce(
				new SystemConfigurationGroup("zeModule", "aGroup"),
				new SystemConfigurationGroup("zeModule", "anotherGroup"));

		assertThat(manager.getGroupConfigurations(new SystemConfigurationGroup("zeModule", "aGroup"))).containsOnlyOnce(
				text, textWithDefaultValue, booleanWithTrueByDefault, booleanWithFalseByDefault, number, numberWithDefaultValue
		);

		assertThat(manager.getGroupConfigurations(new SystemConfigurationGroup("zeModule", "anotherGroup"))).containsOnlyOnce(
				enumValue, enumWithDefaultValue
		);

	}

	@Test
	public void whenInitializingThenConfigsLoaded()
			throws Exception {

		manager.setValue(text, "dakota");

		SystemConfigurationsManager otherManager = new SystemConfigurationsManager(getModelLayerFactory(),
				getDataLayerFactory().getConfigManager(), new Delayed<>(getAppLayerFactory().getModulesManager()),
				getDataLayerFactory().getLocalCacheManager());
		otherManager.initialize();

		assertThat((Object) otherManager.getValue(text)).isEqualTo("dakota");
		assertThat((Object) otherManager.getValue(textWithDefaultValue)).isEqualTo("bob");
	}

	@Test
	public void givenTextMetadataThenCanRetrieveAndAlterValue()
			throws Exception {

		assertThat((Object) manager.getValue(text)).isNull();
		assertThat((Object) manager.getValue(textWithDefaultValue)).isEqualTo("bob");
		assertThat((Object) managerOfOtherInstance.getValue(text)).isNull();
		assertThat((Object) managerOfOtherInstance.getValue(textWithDefaultValue)).isEqualTo("bob");

		manager.setValue(text, "dakota");

		assertThat((Object) manager.getValue(text)).isEqualTo("dakota");
		assertThat((Object) managerOfOtherInstance.getValue(text)).isEqualTo("dakota");

		manager.setValue(textWithDefaultValue, "lindien");

		assertThat((Object) managerOfOtherInstance.getValue(text)).isEqualTo("dakota");
		assertThat((Object) managerOfOtherInstance.getValue(textWithDefaultValue)).isEqualTo("lindien");
		assertThat((Object) manager.getValue(text)).isEqualTo("dakota");
		assertThat((Object) manager.getValue(textWithDefaultValue)).isEqualTo("lindien");

		manager.setValue(text, "alice");
		managerOfOtherInstance.setValue(textWithDefaultValue, "wonderland");

		assertThat((Object) managerOfOtherInstance.getValue(text)).isEqualTo("alice");
		assertThat((Object) manager.getValue(textWithDefaultValue)).isEqualTo("wonderland");

		manager.reset(text);
		managerOfOtherInstance.reset(textWithDefaultValue);

		assertThat((Object) managerOfOtherInstance.getValue(text)).isNull();
		assertThat((Object) manager.getValue(textWithDefaultValue)).isEqualTo("bob");

	}

	@Test
	public void givenNumberMetadataThenCanRetrieveAndAlterValue()
			throws Exception {

		assertThat((Object) manager.getValue(number)).isNull();
		assertThat((Object) managerOfOtherInstance.getValue(numberWithDefaultValue)).isEqualTo(42);

		managerOfOtherInstance.setValue(number, 12);
		manager.setValue(numberWithDefaultValue, 34);

		assertThat((Object) manager.getValue(number)).isEqualTo(12);
		assertThat((Object) managerOfOtherInstance.getValue(numberWithDefaultValue)).isEqualTo(34);

		managerOfOtherInstance.setValue(number, 56);
		manager.setValue(numberWithDefaultValue, 78);

		assertThat((Object) manager.getValue(number)).isEqualTo(56);
		assertThat((Object) managerOfOtherInstance.getValue(numberWithDefaultValue)).isEqualTo(78);

		managerOfOtherInstance.reset(number);
		manager.reset(numberWithDefaultValue);

		assertThat((Object) manager.getValue(number)).isNull();
		assertThat((Object) managerOfOtherInstance.getValue(numberWithDefaultValue)).isEqualTo(42);

	}

	@Test
	public void givenBinaryMetadataThenCanRetrieveAndAlterValue()
			throws Exception {

		assertThat((Object) manager.getValue(binary)).isNull();

		managerOfOtherInstance.setValue(binary, getTestResourceInputStreamFactory("binary1.png"));
		StreamFactory<InputStream> value = manager.getValue(binary);
		assertThat(value.create(SDK_STREAM)).hasContentEqualTo(getTestResourceInputStream("binary1.png"));

		manager.setValue(binary, getTestResourceInputStreamFactory("binary2.png"));
		value = manager.getValue(binary);
		assertThat(value.create(SDK_STREAM)).hasContentEqualTo(getTestResourceInputStream("binary2.png"));

		managerOfOtherInstance.reset(binary);
		assertThat((Object) manager.getValue(binary)).isNull();

		manager.reset(binary);
		assertThat((Object) manager.getValue(binary)).isNull();

	}

	@Test
	public void givenBooleanMetadataThenCanRetrieveAndAlterValue()
			throws Exception {

		assertThat((Object) managerOfOtherInstance.getValue(booleanWithFalseByDefault)).isEqualTo(Boolean.FALSE);
		assertThat((Object) manager.getValue(booleanWithTrueByDefault)).isEqualTo(Boolean.TRUE);

		manager.setValue(booleanWithFalseByDefault, true);
		managerOfOtherInstance.setValue(booleanWithTrueByDefault, false);

		assertThat((Object) managerOfOtherInstance.getValue(booleanWithFalseByDefault)).isEqualTo(true);
		assertThat((Object) manager.getValue(booleanWithTrueByDefault)).isEqualTo(false);

		manager.reset(booleanWithFalseByDefault);
		managerOfOtherInstance.reset(booleanWithTrueByDefault);

		assertThat((Object) managerOfOtherInstance.getValue(booleanWithFalseByDefault)).isEqualTo(Boolean.FALSE);
		assertThat((Object) manager.getValue(booleanWithTrueByDefault)).isEqualTo(Boolean.TRUE);

	}

	@Test
	public void givenEnumMetadataThenCanRetrieveAndAlterValue()
			throws Exception {

		assertThat((Object) managerOfOtherInstance.getValue(enumValue)).isNull();
		assertThat((Object) manager.getValue(enumValue)).isNull();
		assertThat((Object) managerOfOtherInstance.getValue(enumWithDefaultValue)).isEqualTo(AValidEnum.FIRST_VALUE);
		assertThat((Object) manager.getValue(enumWithDefaultValue)).isEqualTo(AValidEnum.FIRST_VALUE);

		manager.setValue(enumValue, AValidEnum.SECOND_VALUE);
		manager.setValue(enumWithDefaultValue, AValidEnum.FIRST_VALUE);

		assertThat((Object) managerOfOtherInstance.getValue(enumValue)).isEqualTo(AValidEnum.SECOND_VALUE);
		assertThat((Object) manager.getValue(enumValue)).isEqualTo(AValidEnum.SECOND_VALUE);
		assertThat((Object) managerOfOtherInstance.getValue(enumWithDefaultValue)).isEqualTo(AValidEnum.FIRST_VALUE);
		assertThat((Object) manager.getValue(enumWithDefaultValue)).isEqualTo(AValidEnum.FIRST_VALUE);

		manager.reset(enumValue);
		manager.reset(enumWithDefaultValue);

		assertThat((Object) managerOfOtherInstance.getValue(enumValue)).isNull();
		assertThat((Object) manager.getValue(enumValue)).isNull();
		assertThat((Object) managerOfOtherInstance.getValue(enumWithDefaultValue)).isEqualTo(AValidEnum.FIRST_VALUE);
		assertThat((Object) manager.getValue(enumWithDefaultValue)).isEqualTo(AValidEnum.FIRST_VALUE);

	}

	@Test
	public void givenCoreConfigUsedByMetadatasWhenChangedThenRecordsUpdated()
			throws Exception {
		assertThat(findUserByTitleInCollection(zeCollection, "Dakota L'Indien")).isNotNull();
		assertThat(findUserByTitleInCollection(zeCollection, "L'Indien, Dakota")).isNull();
		assertThat(findUserByTitleInCollection(aThirdCollection, "Dakota L'Indien")).isNotNull();
		assertThat(findUserByTitleInCollection(aThirdCollection, "L'Indien, Dakota")).isNull();

		assertThat(getDataLayerFactory().getLeaderElectionService().isCurrentNodeLeader()).isTrue();
		assertThat(getDataLayerFactory("other-instance").getLeaderElectionService().isCurrentNodeLeader()).isFalse();

		manager.setValue(ConstellioEIMConfigs.USER_TITLE_PATTERN, "${lastName}, ${firstName}");
		waitForBatchProcess();

		assertThat(findUserByTitleInCollection(zeCollection, "Dakota L'Indien")).isNull();
		assertThat(findUserByTitleInCollection(zeCollection, "L'Indien, Dakota")).isNotNull();
		assertThat(findUserByTitleInCollection(aThirdCollection, "Dakota L'Indien")).isNull();
		assertThat(findUserByTitleInCollection(aThirdCollection, "L'Indien, Dakota")).isNotNull();

	}

	@Test
	public void givenModuleConfigUsedByACalculatorWhen()
			throws Exception {
		long numberOfUsers = 10L;
		assertThat(countUsersWithFavoriteNumberInCollection(42.0, zeCollection)).isEqualTo(numberOfUsers);
		assertThat(countUsersWithFavoriteNumberInCollection(42.0, anotherCollection)).isEqualTo(numberOfUsers);
		assertThat(countUsersWithFavoriteNumberInCollection(666.0, zeCollection)).isEqualTo(0);
		assertThat(countUsersWithFavoriteNumberInCollection(666.0, anotherCollection)).isEqualTo(0);

		manager.setValue(numberUsedByCalculators, 666);
		waitForBatchProcess();

		assertThat(countUsersWithFavoriteNumberInCollection(42.0, zeCollection)).isEqualTo(0);
		assertThat(countUsersWithFavoriteNumberInCollection(42.0, anotherCollection)).isEqualTo(0);
		assertThat(countUsersWithFavoriteNumberInCollection(666.0, zeCollection)).isEqualTo(numberOfUsers);
		assertThat(countUsersWithFavoriteNumberInCollection(666.0, anotherCollection)).isEqualTo(numberOfUsers);
	}

	@Test
	public void givenModuleConfigEnablingModified()
			throws Exception {
		assertThat(getFirstnameMetadataIn(zeCollection).isUniqueValue()).isFalse();
		assertThat(getFirstnameMetadataIn(anotherCollection).isUniqueValue()).isFalse();
		assertThat(getFirstnameMetadataIn(aThirdCollection).isUniqueValue()).isFalse();

		managerOfOtherInstance.setValue(textAlteringSchemas, "ohHellYeah");
		assertThat(getFirstnameMetadataIn(zeCollection).isUniqueValue()).isTrue();
		assertThat(getFirstnameMetadataIn(anotherCollection).isUniqueValue()).isTrue();
		assertThat(getFirstnameMetadataIn(aThirdCollection).isUniqueValue()).isFalse();
		assertThat(callCollectionsActionCallCount.get()).isEqualTo(1);

		manager.reset(textAlteringSchemas);
		assertThat(getFirstnameMetadataIn(zeCollection).isUniqueValue()).isFalse();
		assertThat(getFirstnameMetadataIn(anotherCollection).isUniqueValue()).isFalse();
		assertThat(getFirstnameMetadataIn(aThirdCollection).isUniqueValue()).isFalse();
		assertThat(callCollectionsActionCallCount.get()).isEqualTo(2);

		ValidationErrors errors = new ValidationErrors();
		manager.validate(textAlteringSchemas, "invalidValue!", errors);
		assertThat(errors.getValidationErrors()).hasSize(1);
		assertThat(errors.getValidationErrors().get(0).getCode()).endsWith("ohBobo");

		try {
			managerOfOtherInstance.setValue(textAlteringSchemas, "invalidValue!");
			fail("SystemConfigurationsManagerRuntimeException_InvalidConfigValue expected");
		} catch (SystemConfigurationsManagerRuntimeException_InvalidConfigValue e) {
			//OK
		}
	}

	private long countUsersWithFavoriteNumberInCollection(double number, String collection) {
		MetadataSchema userSchema = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchema(User.DEFAULT_SCHEMA);
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(userSchema).where(
				userSchema.getMetadata("favoriteNumber")).isEqualTo(number);
		return getModelLayerFactory().newSearchServices().getResultsCount(condition);
	}

	private Metadata getUsernameMetadataIn(String collection) {
		MetadataSchema userSchema = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchema(User.DEFAULT_SCHEMA);
		return userSchema.getMetadata(User.USERNAME);
	}

	private Metadata getFirstnameMetadataIn(String collection) {
		MetadataSchema userSchema = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchema(User.DEFAULT_SCHEMA);
		return userSchema.getMetadata(User.FIRSTNAME);
	}

	private Record findUserByTitleInCollection(String collection, String title) {
		MetadataSchema userSchema = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes(collection)
				.getSchema(User.DEFAULT_SCHEMA);
		LogicalSearchCondition condition = LogicalSearchQueryOperators.from(userSchema).where(Schemas.TITLE).isEqualTo(title);
		return getModelLayerFactory().newSearchServices().searchSingleResult(condition);
	}

	// ------------------------

	public static class ZeModule implements InstallableModule {
		@Override
		public String getId() {
			return "zeModule";
		}

		@Override
		public String getName() {
			return "zeModuleName";
		}

		@Override
		public String getPublisher() {
			return "sdk";
		}

		@Override
		public List<MigrationScript> getMigrationScripts() {
			List<MigrationScript> scripts = new ArrayList<>();
			scripts.add(new MigrationScript() {
				@Override
				public String getVersion() {
					return "5.0.1";
				}

				@Override
				public void migrate(String collection, MigrationResourcesProvider migrationResourcesProvider,
									AppLayerFactory appLayerFactory) {
					ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
					MetadataSchemaTypesBuilder typesBuilder = modelLayerFactory.getMetadataSchemasManager().modify(collection);

					typesBuilder.getSchema(User.DEFAULT_SCHEMA).create("favoriteNumber").setType(MetadataValueType.NUMBER)
							.defineDataEntry().asCalculated(FavoriteNumberCalculator.class);

					try {
						modelLayerFactory.getMetadataSchemasManager().saveUpdateSchemaTypes(typesBuilder);
					} catch (OptimisticLocking optimistickLocking) {
						throw new RuntimeException(optimistickLocking);
					}
				}
			});
			return scripts;
		}

		@Override
		public void configureNavigation(NavigationConfig config) {
		}

		@Override
		public boolean isComplementary() {
			return false;
		}

		@Override
		public List<String> getDependencies() {
			return new ArrayList<>();
		}

		@Override
		public List<SystemConfiguration> getConfigurations() {
			return asList(text, textWithDefaultValue, booleanWithTrueByDefault, booleanWithFalseByDefault, number,
					numberWithDefaultValue, enumValue, enumWithDefaultValue);
		}

		@Override
		public Map<String, List<String>> getPermissions() {
			return new HashMap<>();
		}

		@Override
		public List<String> getRolesForCreator() {
			return new ArrayList<>();
		}

		@Override
		public void start(String collection, AppLayerFactory appLayerFactory) {
		}

		@Override
		public void stop(String collection, AppLayerFactory appLayerFactory) {
		}

		@Override
		public void addDemoData(String collection, AppLayerFactory appLayerFactory) {

		}
	}

	public static class TextAlteringSchemasScript implements SystemConfigurationScript<String> {
		@Override
		public void onNewCollection(String newValue, String collection, ModelLayerFactory modelLayerFactory) {

		}

		@Override
		public void validate(String newValue, ValidationErrors errors) {
			if (!"ohHellYeah".equals(newValue) && !"ohHellNo".equals(newValue)) {
				errors.add(TextAlteringSchemasScript.class, "ohBobo");
			}
		}

		@Override
		public void onValueChanged(String previousValue, String newValue, ModelLayerFactory modelLayerFactory) {
			callCollectionsActionCallCount.incrementAndGet();
		}

		@Override
		public void onValueChanged(String previousValue, String newValue, ModelLayerFactory modelLayerFactory,
								   String collection) {
			MetadataSchemasManager schemasManager = modelLayerFactory.getMetadataSchemasManager();
			MetadataSchemaTypesBuilder typesBuilder = schemasManager.modify(collection);

			typesBuilder.getSchema(User.DEFAULT_SCHEMA).get(User.FIRSTNAME).setUniqueValue("ohHellYeah".equals(newValue));

			try {
				modelLayerFactory.getMetadataSchemasManager().saveUpdateSchemaTypes(typesBuilder);
			} catch (OptimisticLocking optimistickLocking) {
				throw new RuntimeException(optimistickLocking);
			}
		}
	}

	public static class FavoriteNumberCalculator implements MetadataValueCalculator<Double> {
		ConfigDependency<Integer> numberConfig = new ConfigDependency<>(numberUsedByCalculators);

		@Override
		public Double calculate(CalculatorParameters parameters) {
			Integer configValue = parameters.get(numberConfig);
			return configValue.doubleValue();
		}

		@Override
		public Double getDefaultValue() {
			return 0.0;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.NUMBER;
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return asList(numberConfig);
		}
	}
}
