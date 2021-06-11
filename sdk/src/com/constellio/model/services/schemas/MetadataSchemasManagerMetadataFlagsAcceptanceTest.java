package com.constellio.model.services.schemas;

import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.services.DataStoreTypesFactory;
import com.constellio.data.dao.services.solr.SolrDataStoreTypesFactory;
import com.constellio.data.utils.Delayed;
import com.constellio.model.entities.schemas.MetadataTransiency;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.schemas.builders.MetadataBuilderRuntimeException;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.junit.Before;
import org.junit.Test;

import static com.constellio.model.entities.schemas.MetadataTransiency.TRANSIENT_EAGER;
import static com.constellio.model.entities.schemas.MetadataTransiency.TRANSIENT_LAZY;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasCustomAttributes;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasTransiency;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIncreaseDependencyLevel;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsDuplicable;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEncrypted;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEssential;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEssentialInSummary;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMarkedForDeletion;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMarkedForTypeMigrationToString;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMarkedMultivalueMigrationToTrue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsScripted;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class MetadataSchemasManagerMetadataFlagsAcceptanceTest extends ConstellioTest {

	MetadataSchemasManager otherMetadataSchemasManager;

	TestsSchemasSetup schemas = new TestsSchemasSetup();
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();

	@Test
	public void whenAddUpdateSchemasThenSaveEssentialFlag()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichIsEssential).withABooleanMetadata());

		assertThat(zeSchema.stringMetadata().isEssential()).isTrue();
		assertThat(zeSchema.booleanMetadata().isEssential()).isFalse();

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).get(zeSchema.stringMetadata().getLocalCode()).setEssential(false);
				types.getSchema(zeSchema.code()).get(zeSchema.booleanMetadata().getLocalCode()).setEssential(true);
			}
		});

		assertThat(zeSchema.stringMetadata().isEssential()).isFalse();
		assertThat(zeSchema.booleanMetadata().isEssential()).isTrue();
	}

	@Test
	public void whenAddUpdateSchemasThenSaveMarkedForDeletionFlag()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichIsMarkedForDeletion).withABooleanMetadata());

		assertThat(zeSchema.stringMetadata().isMarkedForDeletion()).isTrue();
		assertThat(zeSchema.booleanMetadata().isMarkedForDeletion()).isFalse();

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).get(zeSchema.stringMetadata().getLocalCode()).setMarkedForDeletion(false);
				types.getSchema(zeSchema.code()).get(zeSchema.booleanMetadata().getLocalCode()).setMarkedForDeletion(true);
			}
		});

		assertThat(zeSchema.stringMetadata().isMarkedForDeletion()).isFalse();
		assertThat(zeSchema.booleanMetadata().isMarkedForDeletion()).isTrue();
	}

	@Test
	public void whenAddUpdateSchemasThenSaveMarkedForTypeOrMultivalueMigrationFlag() throws Exception {
		defineSchemasManager().using(schemas.withALargeTextMetadata(whichIsMarkedForTypeMigrationToString, whichIsMarkedMultivalueMigrationToTrue)
				.withAStringMetadata());

		assertThat(zeSchema.largeTextMetadata().getMarkedForMigrationToType()).isEqualTo(MetadataValueType.STRING);
		assertThat(zeSchema.largeTextMetadata().isMarkedForMigrationToMultivalue()).isEqualTo(Boolean.TRUE);
		assertThat(zeSchema.stringMetadata().getMarkedForMigrationToType()).isNull();
		assertThat(zeSchema.stringMetadata().isMarkedForMigrationToMultivalue()).isNull();

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).get(zeSchema.largeTextMetadata().getLocalCode())
						.setMarkedForMigrationToMultivalue(null).setMarkedForMigrationToType(null);
				types.getSchema(zeSchema.code()).get(zeSchema.stringMetadata().getLocalCode())
						.setMarkedForMigrationToMultivalue(Boolean.TRUE);
			}
		});

		assertThat(zeSchema.largeTextMetadata().getMarkedForMigrationToType()).isNull();
		assertThat(zeSchema.largeTextMetadata().isMarkedForMigrationToMultivalue()).isNull();
		assertThat(zeSchema.stringMetadata().getMarkedForMigrationToType()).isNull();
		assertThat(zeSchema.stringMetadata().isMarkedForMigrationToMultivalue()).isEqualTo(Boolean.TRUE);
	}

	@Test
	public void whenAddUpdateSchemasThenSaveEncryptedFlag()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata().withAnotherStringMetadata(whichIsEncrypted));

		assertThat(zeSchema.stringMetadata().isEncrypted()).isFalse();
		assertThat(zeSchema.anotherStringMetadata().isEncrypted()).isTrue();

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).get(zeSchema.stringMetadata().getLocalCode()).setEncrypted(true);
				types.getSchema(zeSchema.code()).get(zeSchema.anotherStringMetadata().getLocalCode()).setEncrypted(false);
			}
		});

		assertThat(zeSchema.stringMetadata().isEncrypted()).isTrue();
		assertThat(zeSchema.anotherStringMetadata().isEncrypted()).isFalse();
	}

	@Test
	public void whenAddUpdateSchemasThenSaveTransientFlag()
			throws Exception {
		defineSchemasManager().using(schemas
				.withAStringMetadata(whichIsScripted("title"))
				.withAnotherStringMetadata(whichIsScripted("title"), whichHasTransiency(TRANSIENT_EAGER))
				.withANumberMetadata(whichIsScripted("title.length"), whichHasTransiency(MetadataTransiency.TRANSIENT_LAZY)));

		assertThat(zeSchema.stringMetadata().getTransiency()).isEqualTo(MetadataTransiency.PERSISTED);
		assertThat(zeSchema.anotherStringMetadata().getTransiency()).isEqualTo(TRANSIENT_EAGER);
		assertThat(zeSchema.numberMetadata().getTransiency()).isEqualTo(MetadataTransiency.TRANSIENT_LAZY);

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).get(zeSchema.stringMetadata().getLocalCode())
						.setTransiency(TRANSIENT_EAGER);
				types.getSchema(zeSchema.code()).get(zeSchema.anotherStringMetadata().getLocalCode()).setTransiency(null);
				types.getSchema(zeSchema.code()).get(zeSchema.numberMetadata().getLocalCode())
						.setTransiency(MetadataTransiency.PERSISTED);
			}
		});

		assertThat(zeSchema.stringMetadata().getTransiency()).isEqualTo(TRANSIENT_EAGER);
		assertThat(zeSchema.anotherStringMetadata().getTransiency()).isEqualTo(MetadataTransiency.PERSISTED);
		assertThat(zeSchema.numberMetadata().getTransiency()).isEqualTo(MetadataTransiency.PERSISTED);
	}

	@Test(expected = MetadataBuilderRuntimeException.ReferenceCannotBeTransient.class)
	public void whenAddTransientLazyAutomaticReferenceMetadataThenException()
			throws Exception {
		defineSchemasManager().using(schemas
				.withAReferenceFromAnotherSchemaToZeSchema(whichIsScripted("title"), whichHasTransiency(TRANSIENT_LAZY)));
	}

	@Test(expected = MetadataBuilderRuntimeException.MetadataEnteredManuallyCannotBeTransient.class)
	public void whenAddManualMetadataWithTransientEagerThenException()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichHasTransiency(TRANSIENT_EAGER)));
	}

	@Test(expected = MetadataBuilderRuntimeException.MetadataEnteredManuallyCannotBeTransient.class)
	public void whenAddManualMetadataWithTransientLazyThenException()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichHasTransiency(MetadataTransiency.TRANSIENT_LAZY)));
	}

	@Test
	public void whenUpdateManualMetadataWithTransientLazyThenException()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichHasTransiency(MetadataTransiency.PERSISTED)));

		try {
			getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
				@Override
				public void alter(MetadataSchemaTypesBuilder types) {
					types.getSchemaType(zeSchema.typeCode()).getDefaultSchema().get("stringMetadata")
							.setTransiency(TRANSIENT_EAGER);
				}
			});
			fail("Exception expected");
		} catch (MetadataBuilderRuntimeException.MetadataEnteredManuallyCannotBeTransient e) {
			//OK
		}

		try {
			getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
				@Override
				public void alter(MetadataSchemaTypesBuilder types) {
					types.getSchemaType(zeSchema.typeCode()).getDefaultSchema().get("stringMetadata")
							.setTransiency(TRANSIENT_LAZY);
				}
			});
			fail("Exception expected");
		} catch (MetadataBuilderRuntimeException.MetadataEnteredManuallyCannotBeTransient e) {
			//OK
		}
	}

	@Test
	public void whenAddUpdateSchemasThenSaveEssentialInSummaryFlag()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata().withAnotherStringMetadata(whichIsEssentialInSummary));

		assertThat(zeSchema.stringMetadata().isEssentialInSummary()).isFalse();
		assertThat(zeSchema.anotherStringMetadata().isEssentialInSummary()).isTrue();

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).get(zeSchema.stringMetadata().getLocalCode()).setEssentialInSummary(true);
				types.getSchema(zeSchema.code()).get(zeSchema.anotherStringMetadata().getLocalCode())
						.setEssentialInSummary(false);
			}
		});

		assertThat(zeSchema.stringMetadata().isEssentialInSummary()).isTrue();
		assertThat(zeSchema.anotherStringMetadata().isEssentialInSummary()).isFalse();
	}

	@Test
	public void whenAddUpdateSchemasThenSaveReversedDependencyInSummaryFlag()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata().withAnotherStringMetadata(whichIncreaseDependencyLevel));

		assertThat(zeSchema.stringMetadata().isIncreasedDependencyLevel()).isFalse();
		assertThat(zeSchema.anotherStringMetadata().isIncreasedDependencyLevel()).isTrue();

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).get(zeSchema.stringMetadata().getLocalCode()).setIncreasedDependencyLevel(true);
				types.getSchema(zeSchema.code()).get(zeSchema.anotherStringMetadata().getLocalCode())
						.setIncreasedDependencyLevel(false);
			}
		});

		assertThat(zeSchema.stringMetadata().isIncreasedDependencyLevel()).isTrue();
		assertThat(zeSchema.anotherStringMetadata().isIncreasedDependencyLevel()).isFalse();
	}

	@Test
	public void whenAddUpdateSchemasThenSaveCustomAttributes()
			throws Exception {
		defineSchemasManager().using(schemas
				.withAStringMetadata(whichHasCustomAttributes("flag1", "flag2"))
				.withANumberMetadata(whichHasCustomAttributes("flag3", "flag4"))
				.withABooleanMetadata());

		assertThat(zeSchema.stringMetadata().getCustomAttributes()).containsOnly("flag1", "flag2");
		assertThat(zeSchema.numberMetadata().getCustomAttributes()).containsOnly("flag3", "flag4");
		assertThat(zeSchema.booleanMetadata().getCustomAttributes()).isEmpty();

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				MetadataSchemaBuilder schema = types.getSchema(zeSchema.code());
				schema.get(zeSchema.stringMetadata()).removeCustomAttribute("flag1").addCustomAttribute("flag5");
				schema.get(zeSchema.numberMetadata()).removeCustomAttribute("flag3").removeCustomAttribute("flag4");
				schema.get(zeSchema.booleanMetadata()).addCustomAttribute("flag6");
			}
		});

		assertThat(zeSchema.stringMetadata().getCustomAttributes()).containsOnly("flag5", "flag2");
		assertThat(zeSchema.numberMetadata().getCustomAttributes()).isEmpty();
		assertThat(zeSchema.booleanMetadata().getCustomAttributes()).containsOnly("flag6");
	}

	@Test

	public void whenAddUpdateSchemasThenSaveDuplicableFlag()
			throws Exception {
		defineSchemasManager().using(schemas.withAStringMetadata(whichIsDuplicable).withABooleanMetadata());

		assertThat(zeSchema.stringMetadata().isDuplicable()).isTrue();
		assertThat(zeSchema.booleanMetadata().isDuplicable()).isFalse();

		schemas.modify(new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchema(zeSchema.code()).get(zeSchema.stringMetadata().getLocalCode()).setDuplicable(false);
				types.getSchema(zeSchema.code()).get(zeSchema.booleanMetadata().getLocalCode()).setDuplicable(true);
			}
		});

		assertThat(zeSchema.stringMetadata().isDuplicable()).isFalse();
		assertThat(zeSchema.booleanMetadata().isDuplicable()).isTrue();
	}

	@Before
	public void setUp()
			throws Exception {

		ConfigManager configManager = getDataLayerFactory().getConfigManager();
		DataStoreTypesFactory typesFactory = new SolrDataStoreTypesFactory();
		TaxonomiesManager taxonomiesManager = getModelLayerFactory().getTaxonomiesManager();
		CollectionsListManager collectionsListManager = getModelLayerFactory().getCollectionsListManager();
		BatchProcessesManager batchProcessesManager = getModelLayerFactory().getBatchProcessesManager();
		SearchServices searchServices = getModelLayerFactory().newSearchServices();

		otherMetadataSchemasManager = new MetadataSchemasManager(getModelLayerFactory(),
				new Delayed<>(getAppLayerFactory().getModulesManager()));

	}

}
