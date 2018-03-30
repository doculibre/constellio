package com.constellio.model.services.records;

import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static java.util.Arrays.asList;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RecordServicesMultilingualAcceptanceTest extends ConstellioTest {

	RecordServicesTestSchemaSetup monolingualCollectionSchemas;
	RecordServicesTestSchemaSetup.ZeSchemaMetadatas monolingualCollectionSchema;

	RecordServicesTestSchemaSetup multilingualCollectionSchemas;
	RecordServicesTestSchemaSetup.ZeSchemaMetadatas multilingualCollectionSchema;

	@Test
	public void name()
			throws Exception {

		givenFrenchSystemOneMonolingualAndOneMultilingualCollection(withAMultilingualStringMetadata);

		//assertThat(monolingualCollectionSchema.type().get

	}

	//-----------------------------------------------------------------------------------------------------------------------

	private SetupAlteration withAMultilingualStringMetadata = new SetupAlteration() {
		@Override
		public void setupMonolingualCollection(MetadataSchemaTypesBuilder schemaTypes) {
			schemaTypes.getSchema("zeSchemaType_default").create("stringMetadata").setType(STRING).setMultiLingual(true);
		}

		@Override
		public void setupMultilingualCollection(MetadataSchemaTypesBuilder schemaTypes) {
			schemaTypes.getSchema("zeSchemaType_default").create("stringMetadata").setType(STRING).setMultiLingual(true);
		}

		@Override
		public void after() {

		}
	};

	private SetupAlteration withAMultilingualListStringMetadata = new SetupAlteration() {
		@Override
		public void setupMonolingualCollection(MetadataSchemaTypesBuilder schemaTypes) {
			schemaTypes.getSchema("zeSchemaType_default").create("stringMetadata").setType(STRING)
					.setMultiLingual(true).setMultivalue(true);
		}

		@Override
		public void setupMultilingualCollection(MetadataSchemaTypesBuilder schemaTypes) {
			schemaTypes.getSchema("zeSchemaType_default").create("stringMetadata").setType(STRING)
					.setMultiLingual(true).setMultivalue(true);
		}

		@Override
		public void after() {

		}
	};

	protected void givenFrenchSystemOneMonolingualAndOneMultilingualCollection(final SetupAlteration setupAlteration) {
		givenSystemLanguageIs("fr");
		givenCollection("monolingual").withAllTestUsers();
		givenCollection("multilingual", asList("fr", "en")).withAllTestUsers();
		defineSchemasManager().using(monolingualCollectionSchemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				setupAlteration.setupMonolingualCollection(schemaTypes);
			}
		}));

		defineSchemasManager().using(multilingualCollectionSchemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				setupAlteration.setupMultilingualCollection(schemaTypes);
			}
		}));
	}

	protected void givenFrenchSystemOneMonolingualAndOneTrilingualCollection(final SetupAlteration setupAlteration) {
		givenSystemLanguageIs("fr");
		givenCollection("monolingual").withAllTestUsers();
		givenCollection("multilingual", asList("fr", "en", "ar")).withAllTestUsers();
		defineSchemasManager().using(monolingualCollectionSchemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				setupAlteration.setupMonolingualCollection(schemaTypes);
			}
		}));

		defineSchemasManager().using(multilingualCollectionSchemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				setupAlteration.setupMultilingualCollection(schemaTypes);
			}
		}));
	}

	protected void givenEnglishSystemOneMonolingualAndOneMultilingualCollection(final SetupAlteration setupAlteration) {
		givenSystemLanguageIs("en");
		givenCollection("monolingual").withAllTestUsers();
		givenCollection("multilingual", asList("fr", "en")).withAllTestUsers();
		defineSchemasManager().using(monolingualCollectionSchemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				setupAlteration.setupMonolingualCollection(schemaTypes);
			}
		}));

		defineSchemasManager().using(multilingualCollectionSchemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				setupAlteration.setupMultilingualCollection(schemaTypes);
			}
		}));
	}

	private interface SetupAlteration {
		void setupMonolingualCollection(MetadataSchemaTypesBuilder schemaTypes);

		void setupMultilingualCollection(MetadataSchemaTypesBuilder schemaTypes);

		void after();
	}
}
