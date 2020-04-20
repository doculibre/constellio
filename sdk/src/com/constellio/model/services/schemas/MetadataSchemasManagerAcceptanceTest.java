package com.constellio.model.services.schemas;

import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.systemSetup.SystemGlobalConfigsManager;
import com.constellio.app.services.systemSetup.SystemLocalConfigsManager;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.utils.Delayed;
import com.constellio.model.api.impl.schemas.validation.impl.CreationDateIsBeforeOrEqualToLastModificationDateValidator;
import com.constellio.model.api.impl.schemas.validation.impl.Maximum50CharsRecordMetadataValidator;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.InitializedMetadataValueCalculator;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.wrappers.TemporaryRecord;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.RegexConfig;
import com.constellio.model.entities.schemas.RegexConfig.RegexConfigType;
import com.constellio.model.entities.schemas.entries.AggregatedDataEntry;
import com.constellio.model.entities.schemas.entries.AggregationType;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.CopiedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.entries.SequenceDataEntry;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.contents.ContentFactory;
import com.constellio.model.services.schemas.MetadataSchemasManagerRuntimeException.MetadataSchemasManagerRuntimeException_NoSuchCollection;
import com.constellio.model.services.schemas.builders.DataEntryBuilderRuntimeException.DataEntryBuilderRuntimeException_InvalidMetadataCode;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException.MultilingualMetadatasNotSupportedWithPermanentSummaryCache;
import com.constellio.model.services.schemas.builders.MetadataBuilder_EnumClassTest;
import com.constellio.model.services.schemas.builders.MetadataPopulateConfigsBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilderRuntimeException;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilderRuntimeException.CannotDeleteSchemaTypeSinceItHasRecords;
import com.constellio.model.services.schemas.testimpl.TestMetadataValidator3;
import com.constellio.model.services.schemas.testimpl.TestRecordMetadataValidator1;
import com.constellio.model.services.schemas.testimpl.TestRecordMetadataValidator2;
import com.constellio.model.services.schemas.testimpl.TestRecordValidator1;
import com.constellio.model.services.schemas.testimpl.TestRecordValidator2;
import com.constellio.model.services.schemas.testimpl.TestRecordValidator3;
import com.constellio.model.services.schemas.testimpl.TestStructureFactory1;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.model.utils.ClassProvider;
import com.constellio.model.utils.DefaultClassProvider;
import com.constellio.model.utils.Parametrized;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.annotations.SlowTest;
import com.constellio.sdk.tests.schemas.DaysBetweenSingleLocalDateAndAnotherSchemaRequiredDateCalculator;
import com.constellio.sdk.tests.schemas.MetadataBuilderConfigurator;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.AnotherSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ThirdSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeCustomSchemaMetadatas;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.NUMBER;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.MetadataValueType.TEXT;
import static com.constellio.model.entities.schemas.RecordCacheType.FULLY_CACHED;
import static com.constellio.model.entities.schemas.RecordCacheType.NOT_CACHED;
import static com.constellio.model.entities.schemas.RecordCacheType.SUMMARY_CACHED_WITHOUT_VOLATILE;
import static com.constellio.model.entities.schemas.RecordCacheType.SUMMARY_CACHED_WITH_VOLATILE;
import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.entities.schemas.entries.DataEntryType.AGGREGATED;
import static com.constellio.model.entities.schemas.entries.DataEntryType.CALCULATED;
import static com.constellio.model.entities.schemas.entries.DataEntryType.COPIED;
import static com.constellio.model.entities.schemas.entries.DataEntryType.MANUAL;
import static com.constellio.model.entities.schemas.entries.DataEntryType.SEQUENCE;
import static com.constellio.model.services.schemas.MetadataSchemasManager.SCHEMAS_CONFIG_PATH;
import static com.constellio.model.services.schemas.builders.MetadataPopulateConfigsBuilder.create;
import static com.constellio.sdk.tests.TestUtils.getElementsClasses;
import static com.constellio.sdk.tests.TestUtils.onlyElementsOfClass;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.ANOTHER_SCHEMA_TYPE_CODE;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZE_SCHEMA_TYPE_CODE;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.limitedTo50Characters;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.limitedTo50CharactersInCustomSchema;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichAllowsAnotherDefaultSchema;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichAllowsThirdSchemaType;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasDefaultRequirement;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasDefaultRequirementInCustomSchema;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasDefaultValue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasInputMask;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasLabel;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasLabelInCustomSchema;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasNoDefaultRequirement;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasNoDefaultRequirementInCustomSchema;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasStructureFactory;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsChildOfRelationship;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsDisabled;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsDisabledInCustomSchema;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEnabled;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEnabledInCustomSchema;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultilingual;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsNotAvailableInSummary;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsProvidingSecurity;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSchemaAutocomplete;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsTaxonomyRelationship;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsUndeletable;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichMaxLengthIs7;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichMeasurementUnitIsCm;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SlowTest
public class MetadataSchemasManagerAcceptanceTest extends ConstellioTest {

	TaxonomiesManager taxonomiesManager;

	MetadataSchemasManager schemasManager;

	ConfigManager configManager;

	TestsSchemasSetup defaultSchema, schemas;
	ZeSchemaMetadatas zeSchema;
	ZeCustomSchemaMetadatas zeCustomSchema;
	AnotherSchemaMetadatas anotherSchema;
	ThirdSchemaMetadatas thirdSchema;

	DataStoreTypesFactory typesFactory;
	CollectionsListManager collectionsListManager;
	BatchProcessesManager batchProcessesManager;
	SearchServices searchServices;

	ClassProvider classProvider;

	@Mock MetadataSchemasManagerListener schemasManagerFirstCollection1Listener, schemasManagerSecondCollection1Listener, otherSchemasManagerFirstCollection1Listener, otherSchemasManagerSecondCollection1Listener, schemasManagerCollection2Listener, otherSchemasManagerCollection2Listener;

	@Before
	public void setUp()
			throws Exception {
		classProvider = new DefaultClassProvider();
		prepareSystem(withZeCollection());

		configManager = getDataLayerFactory().getConfigManager();
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		schemas = defaultSchema = new TestsSchemasSetup();
		zeSchema = defaultSchema.new ZeSchemaMetadatas();
		zeCustomSchema = defaultSchema.new ZeCustomSchemaMetadatas();
		anotherSchema = defaultSchema.new AnotherSchemaMetadatas();
		thirdSchema = defaultSchema.new ThirdSchemaMetadatas();
		taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		searchServices = getModelLayerFactory().newSearchServices();
		typesFactory = getDataLayerFactory().newTypesFactory();
		collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		batchProcessesManager = getModelLayerFactory().getBatchProcessesManager();
	}

	@Test
	public void whenCreatingSchemaAndThenReadingSchemaSettingOnDiskThenIdAreWritten() {

		schemasManager.modify(zeCollection, (MetadataSchemaTypesAlteration) types -> {
			MetadataSchemaTypeBuilder temporaryFolderSchemaType = types.getSchemaType(TemporaryRecord.SCHEMA_TYPE);
			temporaryFolderSchemaType.createCustomSchema("employe");
		});

		int idOfSchemaCreated1 = schemasManager.getSchemaTypes(zeCollection).getSchema(TemporaryRecord.SCHEMA_TYPE + "_" + "employe").getId();


		assertThat(xmlOfConfig(zeCollection + SCHEMAS_CONFIG_PATH))
				.containsOnlyOnce("schema code=\"employe\" id=\"" + idOfSchemaCreated1 + "\"");
	}

	@Test
	public void whenCreatingSchemaTypeAndThenReadingSchemaTypeSettingOnDiskThenIdAreWritten() {

		schemasManager.modify(zeCollection, (MetadataSchemaTypesAlteration) types -> {
			MetadataSchemaTypeBuilder newSchemaTypeToTest = types.createNewSchemaType("newSchemaTypeToTest");
		});

		int idOfSchemaTypeCreated1 = schemasManager.getSchemaTypes(zeCollection).getSchemaType("newSchemaTypeToTest").getId();


		assertThat(xmlOfConfig(zeCollection + SCHEMAS_CONFIG_PATH))
				.containsOnlyOnce("type code=\"newSchemaTypeToTest\" id=\"" + idOfSchemaTypeCreated1 + "\"");
	}

	@Test
	public void whenCreatingMetadataAndThenReadingSettingOnDiskThenIdAreWritten() {

		schemasManager.modify(zeCollection, (MetadataSchemaTypesAlteration) types -> {
			MetadataSchemaBuilder metadataSchemaBuilder = types.getSchema(TemporaryRecord.DEFAULT_SCHEMA);
			metadataSchemaBuilder.createUndeletable("metadataName").setType(STRING);
		});

		int idOfMetadataCreated1 = schemasManager.getSchemaTypes(zeCollection).getSchema(TemporaryRecord.DEFAULT_SCHEMA)
				.getMetadata("metadataName").getId();

		assertThat(xmlOfConfig(zeCollection + SCHEMAS_CONFIG_PATH))
				.containsOnlyOnce("code=\"metadataName\" label_en=\"metadataName\" label_fr=\"metadataName\" id=\"" + idOfMetadataCreated1 + "\"");
	}


	@Test
	public void givenSchemasInMultipleCollectionsThenAllIndependentAndDifferentIdsSequence()
			throws Exception {

		givenCollection("collection1");
		givenCollection("collection2");
		int sizeCollection1 = schemasManager.getSchemaTypes("collection1").getSchemaTypes().size();
		int sizeCollection2 = schemasManager.getSchemaTypes("collection2").getSchemaTypes().size();
		MetadataSchemaTypesBuilder collection1Builder = schemasManager.modify("collection1");
		MetadataSchemaTypesBuilder collection2Builder = schemasManager.modify("collection2");
		collection1Builder.createNewSchemaType("a");
		collection2Builder.createNewSchemaType("b");

		schemasManager.saveUpdateSchemaTypes(collection1Builder);
		schemasManager.saveUpdateSchemaTypes(collection2Builder);

		MetadataSchemaTypes systemTypes = schemasManager.getSchemaTypes("_system_");
		MetadataSchemaTypes typesCollection1 = schemasManager.getSchemaTypes("collection1");
		MetadataSchemaTypes typesCollection2 = schemasManager.getSchemaTypes("collection2");
		MetadataSchemaTypes zeCollectionTypes = schemasManager.getSchemaTypes(zeCollection);
		assertThat(schemasManager.getAllCollectionsSchemaTypes())
				.containsOnly(typesCollection1, typesCollection2, zeCollectionTypes, systemTypes);
		assertThat(typesCollection1.getCollection()).isEqualTo("collection1");
		assertThat(typesCollection1.getSchemaTypes()).hasSize(sizeCollection1 + 1);
		assertThat(typesCollection1.getSchemaType("a")).isNotNull();
		assertThat(typesCollection2.getCollection()).isEqualTo("collection2");
		assertThat(typesCollection2.getSchemaTypes()).hasSize(sizeCollection2 + 1);
		assertThat(typesCollection2.getSchemaType("b")).isNotNull();
		assertThat(typesCollection2.getSchemaType("collection").getDefaultSchema().getId()).isEqualTo((short) 1);

		assertThat(typesCollection1.getSchemaTypes()).extracting("id").contains((short) 2, (short) 4, (short) 6);
		assertThat(typesCollection2.getSchemaTypes()).extracting("id").contains((short) 2, (short) 4, (short) 6);
		assertThat(zeCollectionTypes.getSchemaTypes()).extracting("id").contains((short) 2, (short) 4, (short) 6);

	}

	@Test
	public void whenSavingSchemaTypesAndMetadatasThenIdsPersisted()
			throws Exception {
		MetadataSchemaTypesBuilder collection1Builder = schemasManager.modify(zeCollection);

		//setId should never be used, this test use it to proove that values are saved
		MetadataSchemaTypeBuilder type = collection1Builder.createNewSchemaType("t").setId((short) 405);
		MetadataSchemaBuilder defaultSchema = type.getDefaultSchema().setId((short) 406);
		MetadataSchemaBuilder customSchema = type.createCustomSchema("custom").setId((short) 330);
		MetadataBuilder defaultSchemaMetadata = defaultSchema.create("m1").setType(STRING).setId((short) 230);
		MetadataBuilder customSchemaMetadata = customSchema.create("m2").setType(STRING).setId((short) 50);

		schemasManager.saveUpdateSchemaTypes(collection1Builder);


		assertThat(xmlOfConfig(zeCollection + SCHEMAS_CONFIG_PATH))
				.containsOnlyOnce("\"405\"")
				.containsOnlyOnce("\"406\"")
				.containsOnlyOnce("\"330\"")
				.containsOnlyOnce("\"230\"")
				.containsOnlyOnce("\"50\"")
				.doesNotContain("\"-2\"")
				.doesNotContain("\"-7\"");

		MetadataSchemaTypes zeCollectionTypes = schemasManager.getSchemaTypes(zeCollection);
		assertThat(zeCollectionTypes.getSchemaType("t").getId()).isEqualTo((short) 405);
		assertThat(zeCollectionTypes.getSchemaType("t").getDefaultSchema().getId()).isEqualTo((short) 406);
		assertThat(zeCollectionTypes.getSchemaType("t").getSchema("custom").getId()).isEqualTo((short) 330);
		assertThat(zeCollectionTypes.getSchemaType("t").getDefaultSchema().get("m1").getId()).isEqualTo((short) 230);
		assertThat(zeCollectionTypes.getSchemaType("t").getSchema("custom").get("m1").getId()).isEqualTo((short) 230);
		assertThat(zeCollectionTypes.getSchemaType("t").getSchema("custom").get("m2").getId()).isEqualTo((short) 50);
		assertThat(zeCollectionTypes.getSchemaType("t").getDefaultSchema().get("title").getId()).isEqualTo((short) -7);
		assertThat(zeCollectionTypes.getSchemaType("t").getSchema("custom").get("title").getId()).isEqualTo((short) -7);

	}

	private String xmlOfConfig(String path) {
		XMLOutputter xmlOutput = new XMLOutputter(Format.getPrettyFormat());
		return xmlOutput.outputString(getDataLayerFactory().getConfigManager().getXML(path).getDocument());
	}

	@Test
	public void whenSavingSchemaTypeThenSaveCacheType()
			throws Exception {

		MetadataSchemaTypesBuilder collection1Builder = schemasManager.modify(zeCollection);
		collection1Builder.createNewSchemaType("a").setRecordCacheType(FULLY_CACHED);
		collection1Builder.createNewSchemaType("b").setRecordCacheType(NOT_CACHED);
		collection1Builder.createNewSchemaType("c").setRecordCacheType(SUMMARY_CACHED_WITH_VOLATILE);
		collection1Builder.createNewSchemaType("d").setRecordCacheType(SUMMARY_CACHED_WITHOUT_VOLATILE);

		schemasManager.saveUpdateSchemaTypes(collection1Builder);

		MetadataSchemaTypes zeCollectionTypes = schemasManager.getSchemaTypes(zeCollection);

		assertThat(zeCollectionTypes.getSchemaType("a").getCacheType()).isEqualTo(FULLY_CACHED);
		assertThat(zeCollectionTypes.getSchemaType("b").getCacheType()).isEqualTo(NOT_CACHED);
		assertThat(zeCollectionTypes.getSchemaType("c").getCacheType()).isEqualTo(SUMMARY_CACHED_WITH_VOLATILE);
		assertThat(zeCollectionTypes.getSchemaType("d").getCacheType()).isEqualTo(SUMMARY_CACHED_WITHOUT_VOLATILE);
	}

	@Test
	public void whenSavingSchemaTypeThenSaveTransactionLogStatus()
			throws Exception {

		MetadataSchemaTypesBuilder collection1Builder = schemasManager.modify(zeCollection);
		collection1Builder.createNewSchemaType("a");
		collection1Builder.createNewSchemaType("b").setInTransactionLog(true);
		collection1Builder.createNewSchemaType("c").setInTransactionLog(false);

		schemasManager.saveUpdateSchemaTypes(collection1Builder);

		MetadataSchemaTypes zeCollectionTypes = schemasManager.getSchemaTypes(zeCollection);

		assertThat(zeCollectionTypes.getSchemaType("a").isInTransactionLog()).isTrue();
		assertThat(zeCollectionTypes.getSchemaType("b").isInTransactionLog()).isTrue();
		assertThat(zeCollectionTypes.getSchemaType("c").isInTransactionLog()).isFalse();
	}

	@Test
	public void givenSchemasManagerIsInstanciatedThenLoadTypesAndListenThem()
			throws Exception {

		givenCollection("collection1");
		givenCollection("collection2");
		int sizeCollection1 = schemasManager.getSchemaTypes("collection1").getSchemaTypes().size();
		int sizeCollection2 = schemasManager.getSchemaTypes("collection2").getSchemaTypes().size();
		MetadataSchemaTypesBuilder collection1Builder = schemasManager.modify("collection1");
		MetadataSchemaTypesBuilder collection2Builder = schemasManager.modify("collection2");
		collection1Builder.createNewSchemaType("a");
		collection2Builder.createNewSchemaType("b");

		MetadataSchemasManager otherManager = new MetadataSchemasManager(getModelLayerFactory(),
				new Delayed<>(getAppLayerFactory().getModulesManager()));
		otherManager.initialize();

		schemasManager.saveUpdateSchemaTypes(collection1Builder);
		schemasManager.saveUpdateSchemaTypes(collection2Builder);

		MetadataSchemaTypes typesCollection1 = otherManager.getSchemaTypes("collection1");
		MetadataSchemaTypes typesCollection2 = otherManager.getSchemaTypes("collection2");
		assertThat(typesCollection1.getCollection()).isEqualTo("collection1");
		assertThat(typesCollection1.getSchemaTypes()).hasSize(sizeCollection1 + 1);
		assertThat(typesCollection1.getSchemaType("a")).isNotNull();
		assertThat(typesCollection2.getCollection()).isEqualTo("collection2");
		assertThat(typesCollection2.getSchemaTypes()).hasSize(sizeCollection2 + 1);
		assertThat(typesCollection2.getSchemaType("b")).isNotNull();
	}

	@Test
	public void givenSchemasInMultipleCollectionsModifiedThenOtherManagerNotified()
			throws Exception {

		MetadataSchemasManager otherManager = new MetadataSchemasManager(getModelLayerFactory(),
				new Delayed<>(getAppLayerFactory().getModulesManager()));
		otherManager.initialize();

		givenCollection("collection1");
		givenCollection("collection2");
		int sizeCollection1 = schemasManager.getSchemaTypes("collection1").getSchemaTypes().size();
		int sizeCollection2 = schemasManager.getSchemaTypes("collection2").getSchemaTypes().size();
		MetadataSchemaTypesBuilder collection1Builder = schemasManager.modify("collection1");
		MetadataSchemaTypesBuilder collection2Builder = schemasManager.modify("collection2");
		collection1Builder.createNewSchemaType("a");
		collection2Builder.createNewSchemaType("b");

		schemasManager.saveUpdateSchemaTypes(collection1Builder);
		schemasManager.saveUpdateSchemaTypes(collection2Builder);

		MetadataSchemaTypes typesCollection1 = otherManager.getSchemaTypes("collection1");
		MetadataSchemaTypes typesCollection2 = otherManager.getSchemaTypes("collection2");
		assertThat(typesCollection1.getCollection()).isEqualTo("collection1");
		assertThat(typesCollection1.getSchemaTypes()).hasSize(sizeCollection1 + 1);
		assertThat(typesCollection1.getSchemaType("a")).isNotNull();
		assertThat(typesCollection2.getCollection()).isEqualTo("collection2");
		assertThat(typesCollection2.getSchemaTypes()).hasSize(sizeCollection2 + 1);
		assertThat(typesCollection2.getSchemaType("b")).isNotNull();
	}

	@Test
	public void givenSchemasInMultipleCollectionsModifiedThenAllListenersNotified()
			throws Exception {

		MetadataSchemasManager otherManager = new MetadataSchemasManager(getModelLayerFactory(),
				new Delayed<>(getAppLayerFactory().getModulesManager()));
		otherManager.initialize();

		givenCollection("collection1");
		givenCollection("collection2");

		schemasManager.registerListener(schemasManagerFirstCollection1Listener);
		schemasManager.registerListener(schemasManagerSecondCollection1Listener);
		schemasManager.registerListener(schemasManagerCollection2Listener);
		otherManager.registerListener(otherSchemasManagerFirstCollection1Listener);
		otherManager.registerListener(otherSchemasManagerSecondCollection1Listener);
		otherManager.registerListener(otherSchemasManagerCollection2Listener);

		MetadataSchemaTypesBuilder collection1Builder = MetadataSchemaTypesBuilder
				.modify(schemasManager.getSchemaTypes("collection1"), new DefaultClassProvider());
		collection1Builder.createNewSchemaType("a");
		schemasManager.saveUpdateSchemaTypes(collection1Builder);

		verify(schemasManagerFirstCollection1Listener).onCollectionSchemasModified("collection1");
		verify(schemasManagerSecondCollection1Listener).onCollectionSchemasModified("collection1");
		verify(schemasManagerCollection2Listener).onCollectionSchemasModified("collection1");
		verify(otherSchemasManagerFirstCollection1Listener).onCollectionSchemasModified("collection1");
		verify(otherSchemasManagerSecondCollection1Listener).onCollectionSchemasModified("collection1");
		verify(otherSchemasManagerCollection2Listener).onCollectionSchemasModified("collection1");
	}

	@Test
	public void whenSavingMetadataWithAccessRestrictionsThenSaved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.defineAccessRestrictions().withRequiredReadRole("read").withRequiredWriteRole("write")
						.withRequiredModificationRole("modification").withRequiredDeleteRole("delete");
			}
		}));

		assertThat(zeSchema.stringMetadata().getAccessRestrictions().getRequiredReadRoles()).containsOnly("read");
		assertThat(zeSchema.stringMetadata().getAccessRestrictions().getRequiredWriteRoles()).containsOnly("write");
		assertThat(zeSchema.stringMetadata().getAccessRestrictions().getRequiredModificationRoles())
				.containsOnly("modification");
		assertThat(zeSchema.stringMetadata().getAccessRestrictions().getRequiredDeleteRoles()).containsOnly("delete");

	}

	@Test
	public void whenSavingMetadataWithPopulatedConfigsThenSaved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(new MetadataBuilderConfigurator() {

			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.definePopulateConfigsBuilder(createPopulateConfigsBuilder(1));
			}
		}));

		assertThat(zeSchema.stringMetadata().getPopulateConfigs().getStyles()).containsOnly("style1");
		assertThat(zeSchema.stringMetadata().getPopulateConfigs().getProperties()).containsOnly("property1");
		assertThat(zeSchema.stringMetadata().getPopulateConfigs().getRegexes())
				.extracting("inputMetadata", "regex.pattern", "value")
				.containsOnly(tuple("inputMetadata1", "regex1", "value1"));
	}

	@Test
	public void whenModifyingPopulateConfigsOfAMetadataWithInheritanceThenModifyValuesOfCustomSchemasWithSameValue()
			throws Exception {
		defineSchemasManager().using(defaultSchema);
		schemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType("zeSchemaType").getDefaultSchema().create("ze").setType(STRING)
						.getPopulateConfigsBuilder().setStyles(asList("style1"));
				types.getSchemaType("zeSchemaType").createCustomSchema("custom1");
				types.getSchemaType("zeSchemaType").createCustomSchema("custom2");
			}
		});

		MetadataSchemaTypes types = schemasManager.getSchemaTypes(zeCollection);
		assertThat(types.getMetadata("zeSchemaType_default_ze").getPopulateConfigs())
				.isEqualTo(create().setStyles(asList("style1")).build());
		assertThat(types.getMetadata("zeSchemaType_custom1_ze").getPopulateConfigs())
				.isEqualTo(create().setStyles(asList("style1")).build());
		assertThat(types.getMetadata("zeSchemaType_custom2_ze").getPopulateConfigs())
				.isEqualTo(create().setStyles(asList("style1")).build());

		schemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getMetadata("zeSchemaType_custom2_ze").getPopulateConfigsBuilder().setProperties(asList("property1"));
			}
		});

		types = schemasManager.getSchemaTypes(zeCollection);
		assertThat(types.getMetadata("zeSchemaType_default_ze").getPopulateConfigs())
				.isEqualTo(create().setStyles(asList("style1")).build());
		assertThat(types.getMetadata("zeSchemaType_custom1_ze").getPopulateConfigs())
				.isEqualTo(create().setStyles(asList("style1")).build());
		assertThat(types.getMetadata("zeSchemaType_custom2_ze").getPopulateConfigs()).isEqualTo(
				create().setStyles(asList("style1")).setProperties(asList("property1")).build());

		schemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getMetadata("zeSchemaType_default_ze").getPopulateConfigsBuilder().setStyles(asList("style2"));
			}
		});

		types = schemasManager.getSchemaTypes(zeCollection);
		assertThat(types.getMetadata("zeSchemaType_default_ze").getPopulateConfigs())
				.isEqualTo(create().setStyles(asList("style2")).build());
		assertThat(types.getMetadata("zeSchemaType_custom1_ze").getPopulateConfigs())
				.isEqualTo(create().setStyles(asList("style2")).build());
		assertThat(types.getMetadata("zeSchemaType_custom2_ze").getPopulateConfigs()).isEqualTo(
				create().setStyles(asList("style1")).setProperties(asList("property1")).build());

	}

	private MetadataPopulateConfigsBuilder createPopulateConfigsBuilder(int i) {
		MetadataPopulateConfigsBuilder metadataPopulateConfigsBuilder = create();
		metadataPopulateConfigsBuilder.setProperties(Arrays.asList("property" + i));
		metadataPopulateConfigsBuilder.setStyles(Arrays.asList("style" + i));
		metadataPopulateConfigsBuilder.setRegexes(Arrays.asList(createRegexConfig(i)));
		return metadataPopulateConfigsBuilder;
	}

	private RegexConfig createRegexConfig(int i) {
		Pattern regex = Pattern.compile("regex" + i);
		return new RegexConfig("inputMetadata" + i, regex, "value" + i, RegexConfigType.SUBSTITUTION);
	}

	@Test
	public void givenSchemaWithCodeContainingDefaultThenWorkNormally()
			throws Exception {
		defineSchemasManager().using(defaultSchema.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				MetadataSchemaBuilder builder = schemaTypes.getSchemaType("zeSchemaType").createCustomSchema("USRdefaults");
				builder.create("zeMeta").setType(STRING);
			}
		}));

	}

	@Test
	public void whenSavingDefaultSchemaMetadataThenCodeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata());

		assertThat(zeSchema.stringMetadata().getLocalCode()).isEqualTo("stringMetadata");
	}

	@Test
	public void whenSavingDefaultSchemaMetadataThenCompleteCodeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata());

		assertThat(zeSchema.stringMetadata().getCode()).isEqualTo("zeSchemaType_default_stringMetadata");
	}

	@Test
	public void whenSavingDefaultSchemaMetadataThenLabelConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichHasLabel("aLabel")));

		assertThat(zeSchema.stringMetadata().getLabel(Language.French)).isEqualTo("aLabel");
	}

	@Test
	public void whenSavingDefaultSchemaMetadataWithDefaultRequirementThenDefaultRequirementFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichHasDefaultRequirement));

		assertThat(zeSchema.stringMetadata().isDefaultRequirement()).isTrue();
	}

	@Test
	public void whenSavingDefaultSchemaMetadataWithoutDefaultRequirementThenDefaultRequirementFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichHasNoDefaultRequirement));

		assertThat(zeSchema.stringMetadata().isDefaultRequirement()).isFalse();
	}

	@Test
	public void whenSavingDefaultSchemaEnabledMetadataThenDefaultRequirementFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichIsEnabled));

		assertThat(zeSchema.stringMetadata().isEnabled()).isTrue();
	}

	@Test
	public void whenSavingDefaultSchemaDisabledMetadataWithoutDefaultRequirementThenDefaultRequirementFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichIsDisabled));

		assertThat(zeSchema.stringMetadata().isEnabled()).isFalse();
	}

	@Test
	public void whenHavingCustomParameterValueInTemporaryRecordEmployeThenIsSavedWithValue() {
		final Map<String, Object> customParameter = new HashMap();

		customParameter.put("key", "value");

		schemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaTypeBuilder temporaryFolderSchemaType = types.getSchemaType(TemporaryRecord.SCHEMA_TYPE);
				temporaryFolderSchemaType.createCustomSchema("employe").getMetadata("title").setCustomParameter(customParameter);
			}
		});

		Map<String, Object> customParameterFromMetadata = schemasManager.getSchemaTypes(zeCollection).getSchemaType(TemporaryRecord.SCHEMA_TYPE).getSchema("employe").getMetadata("title").getCustomParameter();

		assertThat(customParameterFromMetadata.get("key")).isEqualTo("value");
		assertThat(customParameterFromMetadata.size()).isEqualTo(1);
	}

	@Test
	public void whenSavingSchemaSingleMetadatThenFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withAStringMetadata());

		assertThat(zeSchema.stringMetadata().isMultivalue()).isFalse();
		assertThat(schemas.getMetadata("zeSchemaType_custom_stringMetadata").isMultivalue()).isFalse();
	}

	@Test
	public void whenSavingSchemaWithEnumMetadataThenConserved()
			throws Exception {
		defineSchemasManager()
				.using(defaultSchema.andCustomSchema().withAnEnumMetadata(MetadataBuilder_EnumClassTest.AValidEnum.class)
						.withAStringMetadata());

		assertThat(zeSchema.enumMetadata().getType()).isEqualTo(MetadataValueType.ENUM);
		assertThat(zeSchema.enumMetadata().getEnumClass()).isEqualTo(MetadataBuilder_EnumClassTest.AValidEnum.class);
		assertThat(zeCustomSchema.enumMetadata().getType()).isEqualTo(MetadataValueType.ENUM);
		assertThat(zeCustomSchema.enumMetadata().getEnumClass()).isEqualTo(MetadataBuilder_EnumClassTest.AValidEnum.class);
	}

	@Test
	public void whenSavingSchemaMultivalueMetadatThenFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withAStringMetadata(whichIsMultivalue));

		assertThat(zeSchema.stringMetadata().isMultivalue()).isTrue();
		assertThat(schemas.getMetadata("zeSchemaType_custom_stringMetadata").isMultivalue()).isTrue();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenSavingDefaultSchemaMetadataWithValidationThenValidatorConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(limitedTo50Characters));

		assertThat(zeSchema.stringMetadata().getValidators()).has(
				onlyElementsOfClass(Maximum50CharsRecordMetadataValidator.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenSavingDefaultSchemaWithValidationThenValidatorConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withCreationAndModificationDateInZeSchema());

		assertThat(zeSchema.instance().getValidators()).has(
				onlyElementsOfClass(CreationDateIsBeforeOrEqualToLastModificationDateValidator.class));
		assertThat(zeCustomSchema.instance().getValidators()).has(
				onlyElementsOfClass(CreationDateIsBeforeOrEqualToLastModificationDateValidator.class));
		assertThat(zeSchema.instance().getValidators()).isNotEmpty();
		assertThat(zeCustomSchema.instance().getValidators()).isNotEmpty();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenSavingCustomSchemaWithValidationThenDefaultSchemaHasNoValidator()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withCreationAndModificationDateInZeCustomSchema());

		assertThat(zeSchema.instance().getValidators()).isEmpty();
		assertThat(zeCustomSchema.instance().getValidators()).isNotEmpty();
		assertThat(zeCustomSchema.instance().getValidators()).has(
				onlyElementsOfClass(CreationDateIsBeforeOrEqualToLastModificationDateValidator.class));
	}

	@Test
	public void whenSavingDeletableMetadataThenUndeletableFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata());

		assertThat(zeSchema.stringMetadata().isUndeletable()).isFalse();
	}

	@Test
	public void whenSavingUndeletableMetadataThenUndeletableFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichIsUndeletable));

		assertThat(zeSchema.stringMetadata().isUndeletable()).isTrue();
	}

	@Test
	public void whenCacheIndexTrueThenIsPersistedInXmlFile() {
		final Map<String, Object> customParameter = new HashMap();

		customParameter.put("key", "value");

		schemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaTypeBuilder testschemaSchemaType = types.createNewSchemaType("testschema");
				testschemaSchemaType.createMetadata("string").setType(STRING).setCacheIndex(true);

			}
		});

		boolean isCacheIndexSchemasManager = schemasManager.getSchemaTypes(zeCollection).getSchemaType("testschema").getMetadata("testschema_default_string").isCacheIndex();

		assertThat(isCacheIndexSchemasManager).isTrue();

		// Create a new instance to read and get the xml values
		MetadataSchemasManager otherManager = new MetadataSchemasManager(getModelLayerFactory(),
				new Delayed<>(getAppLayerFactory().getModulesManager()));
		otherManager.initialize();

		boolean isCacheIndex = otherManager.getSchemaTypes(zeCollection).getSchemaType("testschema").getMetadata("testschema_default_string").isCacheIndex();

		assertThat(isCacheIndex).isTrue();
	}

	@Test
	public void whenCacheIndexTrueOnMetadataInDefaultSchemaThenIsPersistedInXmlFile() {
		final Map<String, Object> customParameter = new HashMap();

		customParameter.put("key", "value");

		schemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaTypeBuilder testschemaSchemaType = types.createNewSchemaType("testschema");
				testschemaSchemaType.createMetadata("string").setType(STRING).setCacheIndex(true);
				testschemaSchemaType.createCustomSchema("test2");

			}
		});

		boolean isCacheIndexSchemasManager = schemasManager.getSchemaTypes(zeCollection).getSchemaType("testschema").getMetadata("testschema_test2_string").isCacheIndex();

		assertThat(isCacheIndexSchemasManager).isTrue();

		// Create a new instance to read and get the xml values
		MetadataSchemasManager otherManager = new MetadataSchemasManager(getModelLayerFactory(),
				new Delayed<>(getAppLayerFactory().getModulesManager()));
		otherManager.initialize();

		boolean isCacheIndex = otherManager.getSchemaTypes(zeCollection).getSchemaType("testschema").getMetadata("testschema_test2_string").isCacheIndex();

		assertThat(isCacheIndex).isTrue();
	}

	@Test
	public void whenSavingSchemaAutocompleteMetadataThenUndeletableFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichIsSchemaAutocomplete));

		assertThat(zeSchema.stringMetadata().isSchemaAutocomplete()).isTrue();
	}

	@Test
	public void whenSavingSchemaMultilingualMetadataThenUndeletableFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichIsMultilingual));

		assertThat(zeSchema.stringMetadata().isMultiLingual()).isTrue();

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(defaultSchema.zeDefaultSchemaCode()).get("stringMetadata").setMultiLingual(false);
			}
		});
		assertThat(zeSchema.stringMetadata().isMultiLingual()).isFalse();
	}

	@Test(expected = MultilingualMetadatasNotSupportedWithPermanentSummaryCache.class)
	public void whenSavingSchemaMultilingualAndSummaryRecordCacheThenException()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichIsMultilingual));

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType("zeSchemaType").setRecordCacheType(SUMMARY_CACHED_WITHOUT_VOLATILE);
			}
		});

	}

	@Test
	public void whenSavingSingleValueMetadataThenMultivalueFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata());

		assertThat(zeSchema.stringMetadata().isMultivalue()).isFalse();
	}

	@Test
	public void whenSavingMultiValueMetadataThenMultivalueFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichIsMultivalue));

		assertThat(zeSchema.stringMetadata().isMultivalue()).isTrue();
	}

	@Test
	public void whenSavingNumberMetadataThenTypeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withANumberMetadata());

		assertThat(zeSchema.numberMetadata().getType()).isEqualTo(NUMBER);
	}

	@Test
	public void whenSavingTextMetadataThenTypeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata());

		assertThat(zeSchema.stringMetadata().getType()).isEqualTo(STRING);
	}

	@Test
	public void whenSavingLargeTextMetadataThenTypeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withALargeTextMetadata());

		assertThat(zeSchema.largeTextMetadata().getType()).isEqualTo(TEXT);
	}

	@Test
	public void whenSavingContentMetadataThenTypeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAContentMetadata());

		assertThat(zeSchema.contentMetadata().getType()).isEqualTo(CONTENT);
		assertThat(zeSchema.contentMetadata().getStructureFactory().getClass()).isEqualTo(ContentFactory.class);
	}

	@Test
	public void whenSavingTextMetadataWithStructureFactoryThenSettingConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichHasStructureFactory));

		assertThat(zeSchema.stringMetadata().getStructureFactory().getClass()).isEqualTo(TestStructureFactory1.class);
	}

	@Test
	public void whenSavingTextMetadataWithStructureFactoryThenSettingConservedInCustomSchema()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withAStringMetadata(whichHasStructureFactory));

		assertThat(zeCustomSchema.stringMetadata().getStructureFactory().getClass()).isEqualTo(TestStructureFactory1.class);
	}

	@Test
	public void whenSavingDoubleMetadataThenHasCorrectDataStoreType()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withANumberMetadata());

		assertThat(zeSchema.numberMetadata().getDataStoreType()).isEqualTo("d");
	}

	@Test
	public void whenSavingMultivalueStringMetadataThenHasCorrectDataStoreType()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichIsMultivalue));

		assertThat(zeSchema.stringMetadata().getDataStoreType()).isEqualTo("ss");
	}

	@Test
	public void whenSavingDoubleMetadataThenHasCorrectDataStoreTypeInCustomSchema()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withANumberMetadata());

		assertThat(zeCustomSchema.numberMetadata().getDataStoreType()).isEqualTo("d");
	}

	@Test
	public void whenSavingMultivalueStringMetadataThenHasCorrectDataStoreTypeInCustomSchema()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withAStringMetadata(whichIsMultivalue));

		assertThat(zeCustomSchema.stringMetadata().getDataStoreType()).isEqualTo("ss");
	}

	@Test
	public void whenSavingCustomSchemaDateMetadataThenHasCorrectDataStoreType()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withADateTimeMetadataInCustomSchema());

		assertThat(zeCustomSchema.customDateMetadata().getDataStoreType()).isEqualTo("dt");
	}

	@Test
	public void whenSavingCustomSchemaStringMetadataThenHasCorrectDataStoreType()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withAStringMetadataInCustomSchema(whichIsMultivalue));

		assertThat(zeCustomSchema.customStringMetadata().getDataStoreType()).isEqualTo("ss");
	}

	@Test
	public void whenSavingReferencedMetadataThenAllowedSchemasConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAReferenceMetadata(whichAllowsThirdSchemaType));

		assertThat(zeSchema.referenceMetadata().getAllowedReferences().getAllowedSchemaType()).isEqualTo(
				schemas.aThirdSchemaTypeCode());
		assertThat(zeSchema.referenceMetadata().isChildOfRelationship()).isFalse();
	}

	@Test
	public void whenSavingChildOfReferencedMetadataThenFlagConserved()
			throws Exception {
		defineSchemasManager().using(
				defaultSchema.withAReferenceMetadata(whichAllowsThirdSchemaType, whichIsChildOfRelationship));

		assertThat(zeSchema.referenceMetadata().isChildOfRelationship()).isTrue();
	}

	@Test
	public void whenSavingTaxonomyReferencedMetadataThenFlagConserved()
			throws Exception {
		defineSchemasManager().using(
				defaultSchema.withAReferenceMetadata(whichAllowsThirdSchemaType, whichIsTaxonomyRelationship));

		assertThat(zeSchema.referenceMetadata().isTaxonomyRelationship()).isTrue();
	}

	@Test
	public void whenSavingStringMetadataThenMaxLengthConserved() throws Exception {
		defineSchemasManager().using(
				defaultSchema.withAStringMetadata(whichMaxLengthIs7)
		);

		assertThat(zeSchema.stringMetadata().getMaxLength()).isEqualTo(7);
	}

	@Test
	public void whenSavingLargeTextMetadataThenMaxLengthConserved() throws Exception {
		defineSchemasManager().using(
				defaultSchema.withALargeTextMetadata(whichMaxLengthIs7)
		);

		assertThat(zeSchema.largeTextMetadata().getMaxLength()).isEqualTo(7);
	}

	@Test
	public void whenSavingIntegerMetadataThenMeasurementUnitConserved() throws Exception {
		defineSchemasManager().using(
				defaultSchema.withAnIntegerMetadata(whichMeasurementUnitIsCm)
		);

		assertThat(zeSchema.integerMetadata().getMeasurementUnit()).isEqualTo("cm");
	}

	@Test
	public void whenSavingNumberMetadataThenMeasurementUnitConserved() throws Exception {
		defineSchemasManager().using(
				defaultSchema.withANumberMetadata(whichMeasurementUnitIsCm)
		);

		assertThat(zeSchema.numberMetadata().getMeasurementUnit()).isEqualTo("cm");
	}

	@Test
	public void whenSavingReferenceProvidingSecurityMetadataThenFlagConserved()
			throws Exception {
		defineSchemasManager().using(
				defaultSchema.withAReferenceMetadata(whichAllowsThirdSchemaType, whichIsProvidingSecurity));

		assertThat(zeSchema.referenceMetadata().isRelationshipProvidingSecurity()).isTrue();

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).get(zeSchema.referenceMetadata().getLocalCode())
						.setRelationshipProvidingSecurity(false);
			}
		});

		assertThat(zeSchema.referenceMetadata().isRelationshipProvidingSecurity()).isFalse();

	}

	@Test
	public void whenSavingReferencedMetadataThenAllowedSchemaTypeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAReferenceMetadata(whichAllowsAnotherDefaultSchema));

		assertThat(zeSchema.referenceMetadata().getAllowedReferences().getAllowedSchemas()).containsOnly(
				schemas.anotherDefaultSchemaCode());
	}

	@Test
	public void whenSavingManualEntryMetadataThenDataTypeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata());

		assertThat(zeSchema.stringMetadata().getDataEntry().getType()).isEqualTo(MANUAL);
	}

	@Test
	public void whenSavingCopiedMetadataThenDataTypeConserved()
			throws Exception {
		defineSchemasManager()
				.using(defaultSchema.withTwoMetadatasCopyingAnotherSchemaValuesUsingTwoDifferentReferenceMetadata(false, false,
						false));

		DataEntry dataEntry = zeSchema.stringCopiedFromFirstReferenceStringMeta().getDataEntry();

		assertThat(dataEntry.getType()).isEqualTo(COPIED);
		assertThat(((CopiedDataEntry) dataEntry).getCopiedMetadata()).isEqualTo(anotherSchema.stringMetadata().getCode());
		assertThat(((CopiedDataEntry) dataEntry).getReferenceMetadata()).isEqualTo(
				zeSchema.firstReferenceToAnotherSchema().getCode());
	}

	@Test
	public void whenSavingAgregatedMetadatasThenDataTypeAndParametersConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {

				MetadataSchemaBuilder anotherSchemaBuilder = schemaTypes.getSchema("anotherSchemaType_default");

				schemaTypes.getSchema(anotherSchema.code()).create("ref")
						.defineReferencesTo(schemaTypes.getSchemaType("zeSchemaType"));
				schemaTypes.getSchema(anotherSchema.code()).create("number").setType(MetadataValueType.NUMBER);
				schemaTypes.getSchema(zeSchema.code()).create("sum").setType(STRING).defineDataEntry()
						.asSum(anotherSchemaBuilder.get("ref"), anotherSchemaBuilder.get("number"));
			}
		}));

		AggregatedDataEntry dataEntry = (AggregatedDataEntry) zeSchema.metadata("sum").getDataEntry();
		assertThat(dataEntry.getType()).isEqualTo(AGGREGATED);
		assertThat(dataEntry.getInputMetadatas()).containsOnly("anotherSchemaType_default_number");
		assertThat(dataEntry.getReferenceMetadatas()).containsOnly("anotherSchemaType_default_ref");
		assertThat(dataEntry.getAgregationType()).isEqualTo(AggregationType.SUM);
	}

	@Test
	public void whenCreateAgregatedMetadataWithIncompleteMetadataCodesOrCodeOfMetadataInCustomSchema()
			throws Exception {
		defineSchemasManager().using(defaultSchema.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				schemaTypes.getSchema(anotherSchema.code()).create("ref")
						.defineReferencesTo(schemaTypes.getSchemaType("zeSchemaType"));
				schemaTypes.getSchema(anotherSchema.code()).create("number").setType(MetadataValueType.NUMBER);
				schemaTypes.getSchema(thirdSchema.code()).create("number").setType(MetadataValueType.NUMBER);
				schemaTypes.getSchemaType(anotherSchema.typeCode()).createCustomSchema("custom1");

				schemaTypes.getSchemaType(zeSchema.typeCode()).createCustomSchema("custom2");
			}
		}));

		MetadataSchemaTypesBuilder builder = schemasManager.modify(zeCollection);

		MetadataBuilder metadataBuilder = builder.getSchema("zeSchemaType_default").create("zeMeta");
		MetadataBuilder numberMetadata = builder.getSchema("anotherSchemaType_default").get("number");
		MetadataBuilder refMetadata = builder.getSchema("anotherSchemaType_default").get("ref");

		try {
			metadataBuilder.defineDataEntry().asSum(numberMetadata, builder.getSchema("anotherSchemaType_custom1").get("ref"));
			fail("exception expected");
		} catch (DataEntryBuilderRuntimeException_InvalidMetadataCode e) {
			//OK
		}

		try {
			metadataBuilder.defineDataEntry().asSum(builder.getSchema("anotherSchemaType_custom1").get("number"), refMetadata);
			fail("exception expected");
		} catch (DataEntryBuilderRuntimeException_InvalidMetadataCode e) {
			//OK
		}

		try {
			metadataBuilder.defineDataEntry().asSum(refMetadata, refMetadata);
			fail("exception expected");
		} catch (DataEntryBuilderRuntimeException_InvalidMetadataCode e) {
			//OK
		}

		try {
			metadataBuilder.defineDataEntry().asSum(numberMetadata, numberMetadata);
			fail("exception expected");
		} catch (DataEntryBuilderRuntimeException_InvalidMetadataCode e) {
			//OK
		}

		try {
			metadataBuilder.defineDataEntry().asSum(builder.getSchema(thirdSchema.code()).get("number"), refMetadata);
			fail("exception expected");
		} catch (DataEntryBuilderRuntimeException_InvalidMetadataCode e) {
			//OK
		}

		metadataBuilder.defineDataEntry().asSum(refMetadata, numberMetadata);
	}

	@Test
	public void whenSavingParameterizedCalculatedMetadataThenDataTypeAndParametersConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				schemaTypes.getSchema(zeSchema.code()).create("calculatedString").setType(STRING).defineDataEntry()
						.asCalculated(new TestParametrizedMetadataValueCalculator("value1", 42));
			}
		}));

		CalculatedDataEntry dataEntry = (CalculatedDataEntry) zeSchema.metadata("calculatedString").getDataEntry();

		TestParametrizedMetadataValueCalculator calculator = (TestParametrizedMetadataValueCalculator) dataEntry.getCalculator();
		assertThat(dataEntry.getType()).isEqualTo(CALCULATED);
		assertThat(calculator.parameter1).isEqualTo("value1");
		assertThat(calculator.parameter2).isEqualTo(42);
	}

	@Test
	public void whenSavingInitializedCalculatedMetadataThenInitializedWhenBuilt()
			throws Exception {
		defineSchemasManager().using(defaultSchema.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				schemaTypes.getSchema(zeSchema.code()).create("calculatedString").setType(STRING).defineDataEntry()
						.asCalculated(TestInitializedMetadataValueCalculator.class);
			}
		}));

		//TODO : initialize(List<Metadata> schemaMetadatas, Metadata calculatedMetadata) is called twice

		CalculatedDataEntry dataEntry = (CalculatedDataEntry) zeSchema.metadata("calculatedString").getDataEntry();
		TestInitializedMetadataValueCalculator calculator = (TestInitializedMetadataValueCalculator) dataEntry.getCalculator();
		assertThat(calculator.initializationCounter1).isEqualTo(2);
		assertThat(calculator.initializationCounter2).isEqualTo(1);

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).get("title").setDefaultRequirement(true);
			}
		});

		dataEntry = (CalculatedDataEntry) zeSchema.metadata("calculatedString").getDataEntry();
		calculator = (TestInitializedMetadataValueCalculator) dataEntry.getCalculator();
		assertThat(calculator.initializationCounter1).isEqualTo(2);
		assertThat(calculator.initializationCounter2).isEqualTo(1);
	}

	@Test
	public void whenSavingSequenceMetadataThenDataTypeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAFixedSequence().withADynamicSequence());

		SequenceDataEntry fixedSeqDataEntry = (SequenceDataEntry) zeSchema.fixedSequenceMetadata().getDataEntry();
		SequenceDataEntry dynamicSeqDataEntry = (SequenceDataEntry) zeSchema.dynamicSequenceMetadata().getDataEntry();

		assertThat(fixedSeqDataEntry.getType()).isEqualTo(SEQUENCE);
		assertThat(fixedSeqDataEntry.getFixedSequenceCode()).isEqualTo("zeSequence");
		assertThat(fixedSeqDataEntry.getMetadataProvidingSequenceCode()).isNull();

		assertThat(dynamicSeqDataEntry.getType()).isEqualTo(SEQUENCE);
		assertThat(dynamicSeqDataEntry.getFixedSequenceCode()).isNull();
		assertThat(dynamicSeqDataEntry.getMetadataProvidingSequenceCode()).isEqualTo("metadataDefiningSequenceNumber");
	}

	@Test
	public void whenSavingCalculatedMetadataThenDataTypeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withCalculatedDaysBetweenLocalDateAndAnotherSchemaRequiredDate(false));

		DataEntry dataEntry = zeSchema.calculatedDaysBetween().getDataEntry();

		assertThat(dataEntry.getType()).isEqualTo(CALCULATED);
		assertThat(((CalculatedDataEntry) dataEntry).getCalculator()).isInstanceOf(
				DaysBetweenSingleLocalDateAndAnotherSchemaRequiredDateCalculator.class);
	}

	@Test
	public void whenSavingCustomSchemaMetadataThenCodeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withAStringMetadata());

		assertThat(zeCustomSchema.stringMetadata().getLocalCode()).isEqualTo("stringMetadata");
	}

	@Test
	public void whenSavingCustomSchemaMetadataThenCompleteCodeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withAStringMetadata());

		assertThat(zeCustomSchema.stringMetadata().getCode()).isEqualTo("zeSchemaType_custom_stringMetadata");
	}

	@Test
	public void whenSavingCustomSchemaMetadataThenLabelConserved()
			throws Exception {
		defineSchemasManager().using(
				defaultSchema.andCustomSchema().withAStringMetadata(whichHasLabelInCustomSchema("customLabel")));

		assertThat(zeSchema.stringMetadata().getLabel(Language.French)).isNotEqualTo("customLabel");
		assertThat(zeCustomSchema.stringMetadata().getLabel(Language.French)).isEqualTo("customLabel");
	}

	@Test
	public void whenSavingCustomSchemaMetadataWithDefaultRequirementThenDefaultRequirementFlagConserved()
			throws Exception {
		defineSchemasManager().using(
				defaultSchema.andCustomSchema().withAStringMetadata(whichHasDefaultRequirementInCustomSchema));

		assertThat(zeSchema.stringMetadata().isDefaultRequirement()).isFalse();
		assertThat(zeCustomSchema.stringMetadata().isDefaultRequirement()).isTrue();
	}

	@Test
	public void whenSavingCustomSchemaMetadataWithoutDefaultRequirementThenDefaultRequirementFlagConserved()
			throws Exception {
		defineSchemasManager().using(
				defaultSchema.andCustomSchema().withAStringMetadata(whichHasDefaultRequirement,
						whichHasNoDefaultRequirementInCustomSchema));

		assertThat(zeSchema.stringMetadata().isDefaultRequirement()).isTrue();
		assertThat(zeCustomSchema.stringMetadata().isDefaultRequirement()).isFalse();
	}

	@Test
	public void whenSavingCustomSchemaEnabledMetadataThenEnabledFlagConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withAStringMetadata(whichIsDisabledInCustomSchema));

		assertThat(zeSchema.stringMetadata().isEnabled()).isTrue();
		assertThat(zeCustomSchema.stringMetadata().isEnabled()).isFalse();
	}

	@Test
	public void whenSavingCustomSchemaDisabledMetadataThenEnabledFlagConserved()
			throws Exception {
		defineSchemasManager().using(
				defaultSchema.andCustomSchema().withAStringMetadata(whichIsDisabled, whichIsEnabledInCustomSchema));

		assertThat(zeSchema.stringMetadata().isEnabled()).isFalse();
		assertThat(zeCustomSchema.stringMetadata().isEnabled()).isTrue();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenSavingCustomSchemaWithInheritedValidatorThenValidatorConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withAStringMetadata(limitedTo50Characters));

		assertThat(zeSchema.stringMetadata().getValidators()).has(
				onlyElementsOfClass(Maximum50CharsRecordMetadataValidator.class))
				.hasSize(1);
		assertThat(zeCustomSchema.stringMetadata().getValidators())
				.has(onlyElementsOfClass(Maximum50CharsRecordMetadataValidator.class))
				.hasSize(1);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void whenSavingCustomSchemaWithCustomValidatorThenValidatorConserved()
			throws Exception {
		defineSchemasManager()
				.using(defaultSchema.andCustomSchema().withAStringMetadata(limitedTo50CharactersInCustomSchema));

		assertThat(zeSchema.stringMetadata().getValidators()).isEmpty();
		assertThat(zeCustomSchema.stringMetadata().getValidators()).has(
				onlyElementsOfClass(Maximum50CharsRecordMetadataValidator.class));
	}

	@Test
	public void whenSavingDefaultSchemaThenDefaultCodeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema);

		assertThat(zeSchema.instance().getLocalCode()).isEqualTo("default");
	}

	@Test
	public void whenSavingDefaultSchemaThenCodeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema);

		assertThat(zeSchema.instance().getCode()).isEqualTo("zeSchemaType_default");
	}

	@Test
	public void whenSavingDefaultSchemaThenLabelConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withSchemaFrenchLabel("zeLabel"));

		assertThat(zeSchema.instance().getLabel(Language.French)).isEqualTo("zeLabel");
	}

	@Test
	public void whenSavingCustomSchemaThenCodeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema());

		assertThat(zeCustomSchema.instance().getLocalCode()).isEqualTo("custom");
	}

	@Test
	public void whenSavingCustomSchemaWithThenCodeConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema());

		assertThat(zeCustomSchema.instance().getCode()).isEqualTo("zeSchemaType_custom");
	}

	@Test
	public void whenSavingCustomSchemaThenLabelConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withCustomSchemaFrenchLabel("zeLabel"));

		assertThat(zeCustomSchema.instance().getLabel(Language.French)).isEqualTo("zeLabel");
	}

	@Test
	public void whenSavingCustomUndeletableSchemaThenItIsUndeletable()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().whichCustomSchemaIsUndeletable());

		assertThat(zeCustomSchema.instance().isUndeletable()).isTrue();
	}

	@Test
	public void whenSavingCustomDeletableSchemaThenUndeletableIsConserved()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().whichCustomSchemaIsDeletable());

		assertThat(zeCustomSchema.instance().isUndeletable()).isFalse();
	}

	@Test()
	public void whenSavingSchemaTypesThenCanRetreiveThemWithManager()
			throws Exception {
		MetadataSchemaTypesBuilder typesBuilder = modifySchemaTypesAddingTwoNewTypes();

		MetadataSchemaTypes types = saveAndLoadSavedSchemaTypes(typesBuilder);
		MetadataSchemaTypes builtTypes = typesBuilder.build(getDataLayerFactory().newTypesFactory(), getModelLayerFactory());
		assertThat(types.getSchema(UserDocument.DEFAULT_SCHEMA).getMetadata(UserDocument.USER))
				.isEqualTo(builtTypes.getSchema(UserDocument.DEFAULT_SCHEMA).getMetadata(UserDocument.USER));

		Metadata content = types.getSchema(UserDocument.DEFAULT_SCHEMA).getMetadata(UserDocument.CONTENT);
		Metadata builtContent = builtTypes.getSchema(UserDocument.DEFAULT_SCHEMA).getMetadata(UserDocument.CONTENT);

		assertThat(content).isEqualTo(builtContent);
		assertThat(types.getSchema(UserDocument.DEFAULT_SCHEMA)).isEqualTo(
				builtTypes.getSchema(UserDocument.DEFAULT_SCHEMA));

		for (Metadata metadata : types.getAllMetadatasIncludingThoseWithInheritance()) {
			Metadata builtMetadata = builtTypes.getMetadata(metadata.getCode());
			assertThat(metadata).describedAs(metadata.getCode()).isEqualTo(builtMetadata);
		}

		for (MetadataSchemaType type : types.getSchemaTypes()) {
			MetadataSchemaType builtType = builtTypes.getSchemaType(type.getCode());
			for (MetadataSchema schema : type.getAllSchemas()) {
				MetadataSchema builtSchema = builtTypes.getSchema(schema.getCode());

				for (int i = 0; i < schema.getMetadatas().size(); i++) {
					Metadata metadata = schema.getMetadatas().get(i);
					Metadata builtMetadata = builtSchema.getMetadatas().get(i);
					assertThat(metadata).describedAs(metadata.getCode()).isEqualTo(builtMetadata);
				}

				assertThat(schema.getMetadatas()).describedAs(schema.getCode()).isEqualTo(builtSchema.getMetadatas());
				assertThat(schema).describedAs(schema.getCode()).isEqualTo(builtSchema);
			}
			assertThat(type.getAllMetadatas()).describedAs(type.getCode()).isEqualTo(builtType.getAllMetadatas());
			assertThat(type.getAllSchemas()).describedAs(type.getCode()).isEqualTo(builtType.getAllSchemas());
			assertThat(type).describedAs(type.getCode()).isEqualTo(builtType);
		}

		assertThat(types).isEqualTo(builtTypes);

	}

	@Test
	public void whenSavingDefaultSchemaWithAMetadataThenCustomSchemaHasThisMetadata()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata().andCustomSchema());

		assertThat(zeCustomSchema.stringMetadata().getLocalCode()).isEqualTo(zeSchema.stringMetadata().getLocalCode());
	}

	@Test
	public void whenModifyingDefaultMetadataThenCustomMetadataIsModified()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAModifiedStringMetadata().andCustomSchema());

		assertThat(zeSchema.stringMetadata().getLabel(Language.French))
				.isEqualTo(zeCustomSchema.stringMetadata().getLabel(Language.French));
	}

	@Test(expected = MetadataSchemasRuntimeException.NoSuchMetadata.class)
	public void whenSavingCustomSchemaWithAMetadataThenDefaultSchemaHasNotThisMetadata()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema().withADateTimeMetadataInCustomSchema());

		assertThat(zeCustomSchema.customStringMetadata()).isNotNull();
	}

	@Test()
	public void whenSavingMetadataWithInheritedValuesThenInheritDefaultValuesAfterSave()
			throws Exception {
		MetadataSchemaTypes types = createTwoSchemas();

		assertThat(types.getMetadata("folder_default_rule").isEnabled()).isEqualTo(
				types.getMetadata("folder_employee_rule").isEnabled()).isTrue();
		assertThat(types.getMetadata("folder_default_rule").getLabel(Language.French)).isEqualTo(
				types.getMetadata("folder_employee_rule").getLabel(Language.French)).isEqualTo("Rule");
		assertThat(types.getMetadata("folder_default_rule").getAllowedReferences()).isEqualTo(
				types.getMetadata("folder_employee_rule").getAllowedReferences());
		assertThat(types.getMetadata("folder_default_rule").isDefaultRequirement()).isEqualTo(
				types.getMetadata("folder_employee_rule").isDefaultRequirement()).isFalse();
		assertThat(types.getMetadata("folder_default_rule").isUndeletable()).isFalse();
		assertThat(types.getMetadata("folder_default_rule").isMultivalue()).isFalse();
	}

	@Test()
	public void whenUpdateDefaultMetadataThenCustomMetadataInheritingItHaveNew()
			throws Exception {
		MetadataSchemaTypes types = createTwoSchemas();

		types = modifyDefaultMetadata(types);

		assertThat(types.getMetadata("folder_default_rule").isEnabled()).isEqualTo(
				types.getMetadata("folder_employee_rule").isEnabled()).isFalse();
		assertThat(types.getMetadata("folder_default_rule").getLabel(Language.French)).isEqualTo(
				types.getMetadata("folder_employee_rule").getLabel(Language.French)).isEqualTo("Ze Rule");
		assertThat(types.getMetadata("folder_default_rule").getAllowedReferences()).isEqualTo(
				types.getMetadata("folder_employee_rule").getAllowedReferences());
		assertThat(types.getMetadata("folder_default_rule").isDefaultRequirement()).isEqualTo(
				types.getMetadata("folder_employee_rule").isDefaultRequirement()).isTrue();
		assertThat(types.getMetadata("folder_default_rule").isUndeletable()).isEqualTo(
				types.getMetadata("folder_employee_rule").isUndeletable()).isFalse();
		assertThat(types.getMetadata("folder_default_rule").isMultivalue()).isEqualTo(
				types.getMetadata("folder_employee_rule").isMultivalue()).isFalse();
	}

	@Test()
	public void givenInheritingMetadataWhenChangeEnabledThenStopInheritingDefaultValueUntilResetToNull()
			throws Exception {
		MetadataSchemaTypes types = createTwoSchemas();

		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types, classProvider);
		typesBuilder.getMetadata("folder_employee_rule").setEnabled(false);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(types.getMetadata("folder_default_rule").isEnabled()).isTrue();
		assertThat(types.getMetadata("folder_employee_rule").isEnabled()).isFalse();

		typesBuilder = MetadataSchemaTypesBuilder.modify(types, classProvider);
		typesBuilder.getMetadata("folder_employee_rule").setEnabled(null);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(types.getMetadata("folder_default_rule").isEnabled()).isTrue();
		assertThat(types.getMetadata("folder_employee_rule").isEnabled()).isTrue();
	}

	@Test()
	public void givenInheritingMetadataWhenChangeDefaultRequirementThenStopInheritingDefaultValueUntilResetToNull()
			throws Exception {
		MetadataSchemaTypes types = createTwoSchemas();

		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types, classProvider);
		typesBuilder.getMetadata("folder_employee_rule").setDefaultRequirement(true);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(types.getMetadata("folder_default_rule").isDefaultRequirement()).isFalse();
		assertThat(types.getMetadata("folder_employee_rule").isDefaultRequirement()).isTrue();

		typesBuilder = MetadataSchemaTypesBuilder.modify(types, classProvider);
		typesBuilder.getMetadata("folder_employee_rule").setDefaultRequirement(null);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(types.getMetadata("folder_default_rule").isDefaultRequirement()).isFalse();
		assertThat(types.getMetadata("folder_employee_rule").isDefaultRequirement()).isFalse();
	}

	@Test()
	public void givenInheritingMetadataWhenChangeLabelThenStopInheritingDefaultValueUntilResetToNull()
			throws Exception {
		MetadataSchemaTypes types = createTwoSchemas();

		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types, classProvider);
		typesBuilder.getMetadata("folder_employee_rule").addLabel(Language.French, "a custom rule");
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(types.getMetadata("folder_default_rule").getLabel(Language.French)).isEqualTo("Rule");
		assertThat(types.getMetadata("folder_employee_rule").getLabel(Language.French)).isEqualTo("a custom rule");

		typesBuilder = MetadataSchemaTypesBuilder.modify(types, classProvider);
		typesBuilder.getMetadata("folder_default_rule").addLabel(Language.French, "Ze Rule");
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(types.getMetadata("folder_default_rule").getLabel(Language.French)).isEqualTo("Ze Rule");
		assertThat(types.getMetadata("folder_employee_rule").getLabel(Language.French)).isEqualTo("a custom rule");

		typesBuilder = MetadataSchemaTypesBuilder.modify(types, classProvider);
		typesBuilder.getMetadata("folder_employee_rule").addLabel(Language.French, null);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(types.getMetadata("folder_default_rule").getLabel(Language.French)).isEqualTo("Ze Rule");
		assertThat(types.getMetadata("folder_employee_rule").getLabel(Language.French)).isEqualTo("Ze Rule");
	}

	@Test(expected = MetadataSchemasManagerRuntimeException_NoSuchCollection.class)
	public void whenGetTypesOfInvalidCollectionThenException()
			throws Exception {
		schemasManager.getSchemaTypes("invalidCollection");
	}

	@Test(expected = MetadataSchemaBuilderRuntimeException.CannotModifyAttributeOfInheritingMetadata.class)
	public void whenModifyUndeletableOnInheritingMetadataThenUnmodifiableAttributeException()
			throws Exception {
		MetadataSchemaTypes types = createTwoSchemas();
		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types, classProvider);

		typesBuilder.getMetadata("folder_employee_rule").setUndeletable(false);
		typesBuilder.getSchema("folder_employee").getMetadata("rule").setUndeletable(false);
	}

	@Test(expected = MetadataSchemaBuilderRuntimeException.CannotModifyAttributeOfInheritingMetadata.class)
	public void whenModifyReferencesOnInheritingMetadataThenUnmodifiableAttributeException()
			throws Exception {
		MetadataSchemaTypes types = createTwoSchemas();
		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types, classProvider);

		typesBuilder.getMetadata("folder_employee_rule").defineReferences();
	}

	@Test()
	public void givenInheritingMetadataThenHasItsValidatorsAndItsInheritedRecordMetadataValidators()
			throws Exception {
		MetadataSchemaTypes types = createTwoSchemas();

		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types, classProvider);
		typesBuilder.getMetadata("folder_default_rule").defineValidators().add(TestRecordMetadataValidator1.class)
				.add(TestRecordMetadataValidator2.class);
		typesBuilder.getMetadata("folder_employee_rule").defineValidators().add(TestMetadataValidator3.class);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);
		//
		assertThat(getElementsClasses(types.getMetadata("folder_default_rule").getValidators())).containsOnly(
				TestRecordMetadataValidator1.class, TestRecordMetadataValidator2.class);
		assertThat(getElementsClasses(types.getMetadata("folder_employee_rule").getValidators())).containsOnly(
				TestRecordMetadataValidator1.class, TestRecordMetadataValidator2.class, TestMetadataValidator3.class);

		typesBuilder = MetadataSchemaTypesBuilder.modify(types, new DefaultClassProvider());
		typesBuilder.getMetadata("folder_default_rule").defineValidators().remove(TestRecordMetadataValidator2.class);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(getElementsClasses(types.getMetadata("folder_default_rule").getValidators())).containsOnly(
				TestRecordMetadataValidator1.class);
		assertThat(getElementsClasses(types.getMetadata("folder_employee_rule").getValidators())).containsOnly(
				TestRecordMetadataValidator1.class, TestMetadataValidator3.class);

		typesBuilder = MetadataSchemaTypesBuilder.modify(types, classProvider);
		typesBuilder.getMetadata("folder_default_rule").defineValidators().add(TestRecordMetadataValidator2.class);
		typesBuilder.getMetadata("folder_employee_rule").defineValidators().remove(TestMetadataValidator3.class);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(getElementsClasses(types.getMetadata("folder_default_rule").getValidators())).containsOnly(
				TestRecordMetadataValidator1.class, TestRecordMetadataValidator2.class);
		assertThat(getElementsClasses(types.getMetadata("folder_employee_rule").getValidators())).containsOnly(
				TestRecordMetadataValidator1.class, TestRecordMetadataValidator2.class);
	}

	@Test()
	public void givenInheritingSchemaThenHasItsValidatorsAndItsInheritedSchemaValidators()
			throws Exception {
		MetadataSchemaTypes types = createTwoSchemas();

		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types, classProvider);
		typesBuilder.getSchema("folder_default").defineValidators().add(TestRecordValidator1.class)
				.add(TestRecordValidator2.class);
		typesBuilder.getSchema("folder_employee").defineValidators().add(TestRecordValidator3.class);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(getElementsClasses(types.getSchema("folder_default").getValidators())).containsOnly(
				TestRecordValidator1.class, TestRecordValidator2.class);
		assertThat(getElementsClasses(types.getSchema("folder_employee").getValidators())).containsOnly(
				TestRecordValidator1.class, TestRecordValidator2.class, TestRecordValidator3.class);

		typesBuilder = MetadataSchemaTypesBuilder.modify(types, classProvider);
		typesBuilder.getSchema("folder_default").defineValidators().remove(TestRecordValidator2.class);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(getElementsClasses(types.getSchema("folder_default").getValidators())).containsOnly(
				TestRecordValidator1.class);
		assertThat(getElementsClasses(types.getSchema("folder_employee").getValidators())).containsOnly(
				TestRecordValidator1.class, TestRecordValidator3.class);

		typesBuilder = MetadataSchemaTypesBuilder.modify(types, classProvider);
		typesBuilder.getSchema("folder_default").defineValidators().add(TestRecordValidator2.class);
		typesBuilder.getSchema("folder_employee").defineValidators().remove(TestRecordValidator3.class);
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(getElementsClasses(types.getSchema("folder_default").getValidators())).containsOnly(
				TestRecordValidator1.class, TestRecordValidator2.class);
		assertThat(getElementsClasses(types.getSchema("folder_employee").getValidators())).containsOnly(
				TestRecordValidator1.class, TestRecordValidator2.class);
	}

	@Test
	public void givenSecurityFlagsWhenSavingAndModifyingThenFlagsConserved()
			throws Exception {

		MetadataSchemaTypes types = createTwoSchemas();
		assertThat(types.getSchemaType("folder").hasSecurity()).isTrue();
		assertThat(types.getSchemaType("rule").hasSecurity()).isFalse();

		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types, new DefaultClassProvider());
		types = saveAndLoadSavedSchemaTypes(typesBuilder);

		assertThat(types.getSchemaType("folder").hasSecurity()).isTrue();
		assertThat(types.getSchemaType("rule").hasSecurity()).isFalse();
	}

	@Test
	public void givenReadOnlyLockedFlagsWhenSavingAndModifyingThenReadOnlyLockedConserved()
			throws Exception {

		MetadataSchemaTypes types = createTwoSchemas();
		assertThat(types.getSchemaType("folder").isReadOnlyLocked()).isFalse();
		assertThat(types.getSchemaType("rule").isReadOnlyLocked()).isFalse();

		schemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType("folder").setReadOnlyLocked(true);
			}
		});
		types = schemasManager.getSchemaTypes("zeCollection");

		assertThat(types.getSchemaType("folder").isReadOnlyLocked()).isTrue();
		assertThat(types.getSchemaType("rule").isReadOnlyLocked()).isFalse();

		schemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType("folder").setReadOnlyLocked(false);
				types.getSchemaType("rule").setReadOnlyLocked(true);
			}
		});
		types = schemasManager.getSchemaTypes("zeCollection");

		assertThat(types.getSchemaType("folder").isReadOnlyLocked()).isFalse();
		assertThat(types.getSchemaType("rule").isReadOnlyLocked()).isTrue();
	}

	@Test
	public void givenNewSchemaTypesThenSchemaHasCommonMetadatas()
			throws Exception {

		MetadataSchema aSchema = createTwoSchemas().getSchemaType(Folder.SCHEMA_TYPE).getDefaultSchema();

		Metadata idMetadata = aSchema.getMetadata("id");
		assertThat(idMetadata.isUndeletable()).isTrue();
		assertThat(idMetadata.isSystemReserved()).isTrue();
		assertThat(idMetadata.isUnmodifiable()).isTrue();
		assertThat(idMetadata.isMultivalue()).isFalse();
		assertThat(idMetadata.isSortable()).isTrue();
		assertThat(idMetadata.isSearchable()).isTrue();
		assertThat(idMetadata.getDataEntry().getType()).isSameAs(DataEntryType.MANUAL);
		assertThat(idMetadata.getType()).isSameAs(STRING);
		assertThat(idMetadata.getLocalCode()).isEqualTo("id");
		assertThat(idMetadata.getCode()).isEqualTo("folder_default_id");
		assertThat(idMetadata.isEnabled()).isTrue();
		assertThat(idMetadata.isDefaultRequirement()).isTrue();
		assertThat(idMetadata.getDataStoreCode()).isEqualTo("id");

		Metadata schemaMetadata = aSchema.getMetadata("schema");
		assertThat(schemaMetadata.isUndeletable()).isTrue();
		assertThat(schemaMetadata.isSystemReserved()).isTrue();
		assertThat(schemaMetadata.isUnmodifiable()).isFalse();
		assertThat(schemaMetadata.isMultivalue()).isFalse();
		assertThat(schemaMetadata.isSortable()).isFalse();
		assertThat(schemaMetadata.isSearchable()).isFalse();
		assertThat(schemaMetadata.getDataEntry().getType()).isSameAs(DataEntryType.MANUAL);
		assertThat(schemaMetadata.getType()).isSameAs(STRING);
		assertThat(schemaMetadata.getLocalCode()).isEqualTo("schema");
		assertThat(schemaMetadata.getCode()).isEqualTo("folder_default_schema");
		assertThat(schemaMetadata.isEnabled()).isTrue();
		assertThat(schemaMetadata.isDefaultRequirement()).isTrue();
		assertThat(schemaMetadata.getDataStoreCode()).isEqualTo("schema_s");

		Metadata titleMetadata = aSchema.getMetadata("title");
		assertThat(titleMetadata.isUndeletable()).isTrue();
		assertThat(titleMetadata.isSystemReserved()).isFalse();
		assertThat(titleMetadata.isUnmodifiable()).isFalse();
		assertThat(titleMetadata.isMultivalue()).isFalse();
		assertThat(titleMetadata.isSortable()).isFalse();
		assertThat(titleMetadata.isSearchable()).isTrue();
		assertThat(titleMetadata.getDataEntry().getType()).isSameAs(DataEntryType.MANUAL);
		assertThat(titleMetadata.getType()).isSameAs(STRING);
		assertThat(titleMetadata.getLocalCode()).isEqualTo("title");
		assertThat(titleMetadata.getCode()).isEqualTo("folder_default_title");
		assertThat(titleMetadata.isEnabled()).isTrue();
		assertThat(titleMetadata.isDefaultRequirement()).isFalse();
		assertThat(titleMetadata.getDataStoreCode()).isEqualTo("title_s");
	}

	@Test
	public void givenSchemaTypeHasRecordsWhenDeletingThenException()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata());

		getModelLayerFactory().newRecordServices().add(new TestRecord(anotherSchema, "2").set(TITLE, "2"));

		try {
			schemasManager.deleteSchemaTypes(asList(zeSchema.type(), anotherSchema.type()));
			fail("exception expected");
		} catch (CannotDeleteSchemaTypeSinceItHasRecords e) {
			//OK
		}
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(zeCollection);
		assertThat(types.hasType(zeSchema.typeCode())).isTrue();
		assertThat(types.hasType(anotherSchema.typeCode())).isTrue();
	}

	@Test
	public void givenSchemaTypesHasNoRecordsWhenDeletingSchemaTypesThenDeleted()
			throws Exception {
		defineSchemasManager().using(defaultSchema.withAStringMetadata());

		schemasManager.deleteSchemaTypes(asList(zeSchema.type(), anotherSchema.type()));

		MetadataSchemaTypes types = schemasManager.getSchemaTypes(zeCollection);
		assertThat(types.hasType(zeSchema.typeCode())).isFalse();
		assertThat(types.hasType(anotherSchema.typeCode())).isFalse();
	}

	@Test
	public void givenCustomSchemaHasNoRecordsWhenDeletingSchemaThenDeleted()
			throws Exception {
		defineSchemasManager().using(defaultSchema.andCustomSchema());

		schemasManager.deleteCustomSchemas(asList(zeCustomSchema.instance()));

		MetadataSchemaType type = schemasManager.getSchemaTypes(zeCollection).getSchemaType(zeSchema.typeCode());
		assertThat(type.getCustomSchemas()).doesNotContain(zeCustomSchema.instance());
	}

	@Test
	public void givenACustomSchemaWithAMetadataWhenCreatingAMetadataWithSameCodeAndTypeThenCorrectlyCreated()
			throws Exception {
		defineSchemasManager()
				.using(defaultSchema.andCustomSchema().withAStringMetadataInCustomSchema(whichHasLabel("zeUltimateCustom")));

		assertThat(zeSchema.instance().hasMetadataWithCode("customString")).isFalse();
		assertThat(zeCustomSchema.instance().hasMetadataWithCode("customString")).isTrue();
		assertThat(zeCustomSchema.instance().get("customString").getType()).isEqualTo(MetadataValueType.STRING);

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).create("customString").setType(MetadataValueType.STRING)
						.addLabel(Language.French, "zeUltimate");
			}
		});

		assertThat(zeSchema.instance().hasMetadataWithCode("customString")).isTrue();
		assertThat(zeSchema.instance().get("customString").getType()).isEqualTo(MetadataValueType.STRING);
		assertThat(zeSchema.instance().get("customString").getLabel(Language.French)).isEqualTo("zeUltimate");

		assertThat(zeCustomSchema.instance().hasMetadataWithCode("customString")).isTrue();
		assertThat(zeCustomSchema.instance().get("customString").getType()).isEqualTo(MetadataValueType.STRING);
		assertThat(zeCustomSchema.instance().get("customString").getInheritance())
				.isEqualTo(zeSchema.instance().get("customString"));
	}

	@Test
	public void whenSavingSchemaTypesThenPersistPropertiesOfGlobalMetadatas()
			throws Exception {

		defineSchemasManager().using(schemas);
		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaBuilder zeSchemaType = types.getSchemaType(ZE_SCHEMA_TYPE_CODE).getDefaultSchema();
				MetadataSchemaBuilder anotherSchema = types.getSchemaType(ANOTHER_SCHEMA_TYPE_CODE).getDefaultSchema();
				zeSchemaType.get(TITLE.getCode()).addLabel(Language.French, "ze title label").setSortable(true)
						.setEssentialInSummary(true).setAvailableInSummary(true).setEssential(true).setDefaultRequirement(true).setDefaultValue("toto")
						.setEnabled(true).setSchemaAutocomplete(true).setSearchable(true).setSystemReserved(true)
						.setUniqueValue(true).setUnmodifiable(true);

				anotherSchema.get(TITLE.getCode()).addLabel(Language.French, "another title label").setSortable(false)
						.setEssentialInSummary(false).setAvailableInSummary(false).setEssential(false).setDefaultRequirement(false).setDefaultValue("tata")
						.setEnabled(false).setSchemaAutocomplete(false).setSearchable(false).setSystemReserved(false)
						.setUniqueValue(false).setUnmodifiable(false);
			}
		});

		Metadata zeSchemaLabel = schemas.getTypes().getDefaultSchema(ZE_SCHEMA_TYPE_CODE).get(TITLE.getCode());
		assertThat(zeSchemaLabel.getLabel(Language.French)).isEqualTo("ze title label");
		assertThat(zeSchemaLabel.isSortable()).isTrue();
		assertThat(zeSchemaLabel.isAvailableInSummary()).isTrue();
		assertThat(zeSchemaLabel.isEssentialInSummary()).isTrue();
		assertThat(zeSchemaLabel.isEssential()).isTrue();
		assertThat(zeSchemaLabel.isDefaultRequirement()).isTrue();
		assertThat(zeSchemaLabel.getDefaultValue()).isEqualTo("toto");
		assertThat(zeSchemaLabel.isEnabled()).isTrue();
		assertThat(zeSchemaLabel.isSchemaAutocomplete()).isTrue();
		assertThat(zeSchemaLabel.isSearchable()).isTrue();
		assertThat(zeSchemaLabel.isSystemReserved()).isTrue();
		assertThat(zeSchemaLabel.isUniqueValue()).isTrue();
		assertThat(zeSchemaLabel.isUnmodifiable()).isTrue();

		Metadata anotherSchemaLabel = schemas.getTypes().getDefaultSchema(ANOTHER_SCHEMA_TYPE_CODE).get(TITLE.getCode());
		assertThat(anotherSchemaLabel.getLabel(Language.French)).isEqualTo("another title label");
		assertThat(anotherSchemaLabel.isSortable()).isFalse();
		assertThat(anotherSchemaLabel.isAvailableInSummary()).isFalse();
		assertThat(anotherSchemaLabel.isEssentialInSummary()).isFalse();
		assertThat(anotherSchemaLabel.isEssential()).isFalse();
		assertThat(anotherSchemaLabel.isDefaultRequirement()).isFalse();
		assertThat(anotherSchemaLabel.getDefaultValue()).isEqualTo("tata");
		assertThat(anotherSchemaLabel.isEnabled()).isFalse();
		assertThat(anotherSchemaLabel.isSchemaAutocomplete()).isFalse();
		assertThat(anotherSchemaLabel.isSearchable()).isFalse();
		assertThat(anotherSchemaLabel.isSystemReserved()).isFalse();
		assertThat(anotherSchemaLabel.isUniqueValue()).isFalse();
		assertThat(anotherSchemaLabel.isUnmodifiable()).isFalse();

	}

	@Test
	public void givenStringMetadataWithInputMaskThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.andCustomSchema().withAStringMetadata(whichHasInputMask("(###) ###-####")));

		assertThat(zeSchema.stringMetadata().getInputMask()).isEqualTo("(###) ###-####");
		assertThat(zeCustomSchema.stringMetadata().getInputMask()).isEqualTo("(###) ###-####");

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getMetadata(zeSchema.stringMetadata().getCode()).setInputMask("###.###.####");
			}
		});

		assertThat(zeSchema.stringMetadata().getInputMask()).isEqualTo("###.###.####");
		assertThat(zeCustomSchema.stringMetadata().getInputMask()).isEqualTo("###.###.####");

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeCustomSchema.code()).get(zeSchema.stringMetadata().getLocalCode())
						.setInputMask("(###) ### ####");
			}
		});

		assertThat(zeSchema.stringMetadata().getInputMask()).isEqualTo("###.###.####");
		assertThat(zeCustomSchema.stringMetadata().getInputMask()).isEqualTo("(###) ### ####");
	}

	@Test
	public void givenStringMetadataWithDefaultValueThenValueSaved()
			throws Exception {
		defineSchemasManager().using(schemas.andCustomSchema().withAStringMetadata(whichHasDefaultValue("value1")));

		assertThat(zeSchema.stringMetadata().getDefaultValue()).isEqualTo("value1");
		assertThat(zeCustomSchema.stringMetadata().getDefaultValue()).isEqualTo("value1");

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getMetadata(zeSchema.stringMetadata().getCode()).setDefaultValue("value2");
			}
		});

		assertThat(zeSchema.stringMetadata().getDefaultValue()).isEqualTo("value2");
		assertThat(zeCustomSchema.stringMetadata().getDefaultValue()).isEqualTo("value2");

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeCustomSchema.code()).get(zeSchema.stringMetadata().getLocalCode())
						.setDefaultValue("value3");
			}
		});

		assertThat(zeSchema.stringMetadata().getDefaultValue()).isEqualTo("value2");
		assertThat(zeCustomSchema.stringMetadata().getDefaultValue()).isEqualTo("value3");
	}

	@Test
	public void givenDifferentDataStoreIsUsedToPersistRecordsThenSaved()
			throws Exception {
		defineSchemasManager().using(schemas.whichIsIsStoredInDataStore("events"));

		assertThat(zeSchema.type().getDataStore()).isEqualTo("events");
		assertThat(anotherSchema.type().getDataStore()).isEqualTo("records");

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeSchema.typeCode()).setDataStore(null);
				types.getSchemaType(anotherSchema.typeCode()).setDataStore("events");
			}
		});

		assertThat(zeSchema.type().getDataStore()).isEqualTo("records");
		assertThat(anotherSchema.type().getDataStore()).isEqualTo("events");
	}

	@Test
	public void givenNonEssentialStatusModifiedWhenModifyingSchemaThenIsNotMarkedForCacheRebuild() throws Exception {
		SystemGlobalConfigsManager configsManager = getAppLayerFactory().getSystemGlobalConfigsManager();
		assertThat(configsManager.isReindexingRequired()).isFalse();
		defineSchemasManager().using(defaultSchema.withAStringMetadata());
		schemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getMetadata(zeSchema.stringMetadata().getCode()).setMaxLength(20);
			}
		});
		assertThat(configsManager.isReindexingRequired()).isFalse();
	}

	@Test
	public void givenStringMetadataWhenModifyingAvailableInSummaryStatusThenSchemaMarkedForCacheRebuild()
			throws Exception {
		SystemLocalConfigsManager localConfigsManager = getAppLayerFactory().getSystemLocalConfigsManager();
		assertThat(localConfigsManager.isMarkedForCacheRebuild()).isFalse();
		defineSchemasManager().using(defaultSchema.withAStringMetadata(whichIsNotAvailableInSummary));
		schemasManager.modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getMetadata(zeSchema.stringMetadata().getCode()).setAvailableInSummary(true);
			}
		});
		assertThat(localConfigsManager.isMarkedForCacheRebuild()).isTrue();
	}


	private MetadataSchemaTypes createTwoSchemas()
			throws Exception {
		MetadataSchemaTypesBuilder typesBuilder = modifySchemaTypesAddingTwoNewTypes();
		assertThat(typesBuilder.getMetadata("folder_default_rule").getInheritance()).isNull();
		assertThat(typesBuilder.getMetadata("folder_default_rule").isMultivalue()).isFalse();
		assertThat(typesBuilder.getMetadata("folder_default_path").isMultivalue()).isTrue();
		assertThat(typesBuilder.getMetadata("folder_employee_path").isMultivalue()).isTrue();
		MetadataSchemaTypes types = saveAndLoadSavedSchemaTypes(typesBuilder);
		assertThat(types.getMetadata("folder_default_rule").inheritDefaultSchema()).isFalse();
		assertThat(types.getMetadata("folder_default_rule").isMultivalue()).isFalse();
		return types;
	}

	private MetadataSchemaTypes modifyDefaultMetadata(MetadataSchemaTypes types)
			throws MetadataSchemasManagerException {
		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types, new DefaultClassProvider());
		MetadataBuilder rule = typesBuilder.getMetadata("folder_default_rule");
		rule.setEnabled(false).addLabel(Language.French, "Ze Rule").setDefaultRequirement(true);
		// rule.defineReferences().add(types.getDefaultSchema("rule"));
		types = saveAndLoadSavedSchemaTypes(typesBuilder);
		return types;
	}

	private MetadataSchemaTypesBuilder modifySchemaTypesAddingTwoNewTypes()
			throws Exception {
		MetadataSchemaTypes types = schemasManager.getSchemaTypes("zeCollection");
		MetadataSchemaTypesBuilder typesBuilder = MetadataSchemaTypesBuilder.modify(types, new DefaultClassProvider());
		MetadataSchemaTypeBuilder folderType = addFolderSchemaTypeBuilderWithoutRuleMetadatas(typesBuilder);
		MetadataSchemaTypeBuilder ruleType = addRuleSchemaTypeBuilder(typesBuilder);

		MetadataBuilder rule = folderType.getDefaultSchema().create("rule").addLabel(Language.French, "Rule").setType(REFERENCE)
				.setMultivalue(false);
		rule.defineReferences().set(ruleType);

		folderType.getDefaultSchema().create("ruleCode").addLabel(Language.French, "Rule code").setType(STRING).defineDataEntry()
				.asCopied(rule, ruleType.getDefaultSchema().getMetadata("code"));

		return typesBuilder;
	}

	private MetadataSchemaTypeBuilder addFolderSchemaTypeBuilderWithoutRuleMetadatas(MetadataSchemaTypesBuilder types)
			throws Exception {

		MetadataSchemaTypeBuilder folderBuilder = types.createNewSchemaType("folder").addLabel(Language.French, "Folder")
				.setSecurity(true);
		folderBuilder.getDefaultSchema().create("zetitle").setType(STRING).addLabel(Language.French, "Title").setUndeletable(true)
				.setUnmodifiable(true);
		folderBuilder.createCustomSchema("employee").create("employeeName").setType(STRING)
				.addLabel(Language.French, "Name of employee")
				.setUndeletable(true).setSystemReserved(true).setMultivalue(false).setUniqueValue(true);

		return folderBuilder;
	}

	private MetadataSchemaTypeBuilder addRuleSchemaTypeBuilder(MetadataSchemaTypesBuilder types) {

		MetadataSchemaTypeBuilder ruleBuilder = types.createNewSchemaType("rule").addLabel(Language.French, "Rule")
				.setSecurity(false);
		ruleBuilder.getDefaultSchema().create("zetitle").setType(STRING).addLabel(Language.French, "Title").setUndeletable(true);
		ruleBuilder.getDefaultSchema().create("code").setType(STRING).addLabel(Language.French, "Code").setUndeletable(true);

		return ruleBuilder;

	}

	private MetadataSchemaTypes saveAndLoadSavedSchemaTypes(MetadataSchemaTypesBuilder typesBuilder)
			throws MetadataSchemasManagerException {
		schemasManager.saveUpdateSchemaTypes(typesBuilder);

		MetadataSchemaTypes newTypes = schemasManager.getSchemaTypes("zeCollection");
		return newTypes;
	}

	public static class TestInitializedMetadataValueCalculator extends AbstractMetadataValueCalculator<String> implements InitializedMetadataValueCalculator<String> {

		int initializationCounter1 = 0;
		int initializationCounter2 = 0;

		MetadataSchemaTypes types;
		MetadataSchema schema;
		LocalDependency<String> titleParam = LocalDependency.toAString("title");

		@Override
		public String calculate(CalculatorParameters parameters) {
			return parameters.get(titleParam);
		}

		@Override
		public String getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.STRING;
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return Arrays.asList(titleParam);
		}

		@Override
		public void initialize(List<Metadata> schemaMetadatas, Metadata calculatedMetadata) {
			initializationCounter1++;
		}

		@Override
		public void initialize(MetadataSchemaTypes types, MetadataSchema schema, Metadata metadata) {
			initializationCounter2++;
		}
	}

	public static class TestParametrizedMetadataValueCalculator extends AbstractMetadataValueCalculator<String> implements Parametrized {

		String parameter1;
		int parameter2;
		LocalDependency<String> titleParam = LocalDependency.toAString("title");

		public TestParametrizedMetadataValueCalculator(String parameter1, Integer parameter2) {
			this.parameter1 = parameter1;
			this.parameter2 = parameter2;
		}

		@Override
		public String calculate(CalculatorParameters parameters) {
			return parameter1 + ":" + parameter2 + parameters.get(titleParam);
		}

		@Override
		public String getDefaultValue() {
			return null;
		}

		@Override
		public MetadataValueType getReturnType() {
			return MetadataValueType.STRING;
		}

		@Override
		public boolean isMultiValue() {
			return false;
		}

		@Override
		public List<? extends Dependency> getDependencies() {
			return Arrays.asList(titleParam);
		}

		@Override
		public Object[] getInstanceParameters() {
			return new Object[]{parameter1, parameter2};
		}
	}
}
