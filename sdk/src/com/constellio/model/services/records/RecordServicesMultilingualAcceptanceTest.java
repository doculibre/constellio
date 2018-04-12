package com.constellio.model.services.records;

import static com.constellio.model.entities.Language.Arabic;
import static com.constellio.model.entities.Language.English;
import static com.constellio.model.entities.Language.French;
import static com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval.PREFERRING;
import static com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval.STRICT;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.Schemas.dummy;
import static java.util.Arrays.asList;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.FRENCH;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.constellio.model.entities.records.Record;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RecordServicesMultilingualAcceptanceTest extends ConstellioTest {

	private static Locale ARABIC = new Locale("ar");

	RecordServicesTestSchemaSetup monolingualCollectionSchemas = new RecordServicesTestSchemaSetup("monolingual");
	RecordServicesTestSchemaSetup.ZeSchemaMetadatas monolingualSchema
			= monolingualCollectionSchemas.new ZeSchemaMetadatas();

	RecordServicesTestSchemaSetup
			multilingualCollectionSchemas = new RecordServicesTestSchemaSetup("multilingual");
	RecordServicesTestSchemaSetup.ZeSchemaMetadatas multilingualSchema
			= multilingualCollectionSchemas.new ZeSchemaMetadatas();

	RecordServices recordServices;
	SearchServices searchServices;
	Record monoLingualRecord, multiLingualRecord;

	@Test
	public void givenFrenchSystemWithAMultilingualCollectionsThenSchemasHaveValidLanguages()
			throws Exception {

		givenFrenchSystemOneMonolingualAndOneMultilingualCollection();
		assertThat(monolingualSchema.instance().getCollectionInfo().getMainSystemLanguage()).isEqualTo(French);
		assertThat(monolingualSchema.instance().getCollectionInfo().getCollectionLanguages())
				.containsOnly(French);

		assertThat(monolingualSchema.type().getCollectionInfo().getMainSystemLanguage()).isEqualTo(French);
		assertThat(monolingualSchema.type().getCollectionInfo().getCollectionLanguages())
				.containsOnly(French);

		assertThat(multilingualSchema.instance().getCollectionInfo().getMainSystemLanguage()).isEqualTo(French);
		assertThat(multilingualSchema.instance().getCollectionInfo().getCollectionLanguages())
				.containsOnly(French, English);

		assertThat(multilingualSchema.type().getCollectionInfo().getMainSystemLanguage()).isEqualTo(French);
		assertThat(multilingualSchema.type().getCollectionInfo().getCollectionLanguages())
				.containsOnly(French, English);

	}

	@Test
	public void givenEnglishSystemWithAMultilingualCollectionsThenSchemasHaveValidLanguages()
			throws Exception {

		givenEnglishSystemOneMonolingualAndOneTrilingualCollection();
		assertThat(monolingualSchema.instance().getCollectionInfo().getMainSystemLanguage()).isEqualTo(English);
		assertThat(monolingualSchema.instance().getCollectionInfo().getCollectionLanguages())
				.containsOnly(English);

		assertThat(monolingualSchema.type().getCollectionInfo().getMainSystemLanguage()).isEqualTo(English);
		assertThat(monolingualSchema.type().getCollectionInfo().getCollectionLanguages())
				.containsOnly(English);

		assertThat(multilingualSchema.instance().getCollectionInfo().getMainSystemLanguage()).isEqualTo(English);
		assertThat(multilingualSchema.instance().getCollectionInfo().getCollectionLanguages())
				.containsOnly(French, English, Arabic);

		assertThat(multilingualSchema.type().getCollectionInfo().getMainSystemLanguage()).isEqualTo(English);
		assertThat(multilingualSchema.type().getCollectionInfo().getCollectionLanguages())
				.containsOnly(French, English, Arabic);

	}

	@Test
	public void givenFrenchSystemWithAMultilingualCollectionsRecordWhenMetadatasHasMultilingualMetadatasAndOnlyMainLanguageValuesThenObtainedNo()
			throws Exception {

		givenFrenchSystemOneMonolingualAndOneMultilingualCollection(
				withAMultilingualStringMetadata, andAnotherUnilingualStringMetadata);

		recordServices.add(monoLingualRecord = new TestRecord(monolingualSchema, "monoLingualRecord")
				.set(monolingualSchema.stringMetadata(), "value1")
				.set(monolingualSchema.anotherStringMetadata(), "value2"));

		recordServices.add(multiLingualRecord = new TestRecord(multilingualSchema, "multiLingualRecord")
				.set(multilingualSchema.stringMetadata(), "value3")
				.set(multilingualSchema.anotherStringMetadata(), "value4"));

		monoLingualRecord = record("monoLingualRecord");
		multiLingualRecord = record("multiLingualRecord");

		assertThat(monoLingualRecord.get(monolingualSchema.stringMetadata())).isEqualTo("value1");
		assertThat(monoLingualRecord.get(monolingualSchema.stringMetadata(), FRENCH, PREFERRING)).isEqualTo("value1");
		assertThat(monoLingualRecord.get(monolingualSchema.stringMetadata(), ENGLISH, PREFERRING)).isEqualTo("value1");
		assertThat(monoLingualRecord.get(monolingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("value1");
		assertThat(monoLingualRecord.get(monolingualSchema.stringMetadata(), ENGLISH, STRICT)).isNull();

		assertThat(monoLingualRecord.get(dummy(monolingualSchema.stringMetadata()))).isEqualTo("value1");
		assertThat(monoLingualRecord.get(dummy(monolingualSchema.stringMetadata()), FRENCH, PREFERRING)).isEqualTo("value1");
		assertThat(monoLingualRecord.get(dummy(monolingualSchema.stringMetadata()), ENGLISH, PREFERRING)).isEqualTo("value1");
		assertThat(monoLingualRecord.get(dummy(monolingualSchema.stringMetadata()), FRENCH, STRICT)).isEqualTo("value1");
		assertThat(monoLingualRecord.get(dummy(monolingualSchema.stringMetadata()), ENGLISH, STRICT)).isNull();

		assertThat(monoLingualRecord.get(monolingualSchema.anotherStringMetadata())).isEqualTo("value2");
		assertThat(monoLingualRecord.get(monolingualSchema.anotherStringMetadata(), FRENCH, PREFERRING)).isEqualTo("value2");
		assertThat(monoLingualRecord.get(monolingualSchema.anotherStringMetadata(), ENGLISH, PREFERRING)).isEqualTo("value2");
		assertThat(monoLingualRecord.get(monolingualSchema.anotherStringMetadata(), FRENCH, STRICT)).isEqualTo("value2");
		assertThat(monoLingualRecord.get(monolingualSchema.anotherStringMetadata(), ENGLISH, STRICT)).isNull();

		assertThat(monoLingualRecord.get(monolingualSchema.anotherStringMetadata())).isEqualTo("value2");
		assertThat(monoLingualRecord.get(monolingualSchema.anotherStringMetadata(), FRENCH, PREFERRING)).isEqualTo("value2");
		assertThat(monoLingualRecord.get(monolingualSchema.anotherStringMetadata(), ENGLISH, PREFERRING)).isEqualTo("value2");
		assertThat(monoLingualRecord.get(monolingualSchema.anotherStringMetadata(), FRENCH, STRICT)).isEqualTo("value2");
		assertThat(monoLingualRecord.get(monolingualSchema.anotherStringMetadata(), ENGLISH, STRICT)).isNull();

		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata())).isEqualTo("value3");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), FRENCH, PREFERRING)).isEqualTo("value3");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ENGLISH, PREFERRING)).isEqualTo("value3");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("value3");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isNull();

		assertThat(multiLingualRecord.get(dummy(multilingualSchema.stringMetadata()))).isEqualTo("value3");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.stringMetadata()), FRENCH, PREFERRING)).isEqualTo("value3");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.stringMetadata()), ENGLISH, PREFERRING)).isEqualTo("value3");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.stringMetadata()), FRENCH, STRICT)).isEqualTo("value3");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.stringMetadata()), ENGLISH, STRICT)).isNull();

		assertThat(multiLingualRecord.get(multilingualSchema.anotherStringMetadata())).isEqualTo("value4");
		assertThat(multiLingualRecord.get(multilingualSchema.anotherStringMetadata(), FRENCH, PREFERRING)).isEqualTo("value4");
		assertThat(multiLingualRecord.get(multilingualSchema.anotherStringMetadata(), ENGLISH, PREFERRING)).isEqualTo("value4");
		assertThat(multiLingualRecord.get(multilingualSchema.anotherStringMetadata(), FRENCH, STRICT)).isEqualTo("value4");
		assertThat(multiLingualRecord.get(multilingualSchema.anotherStringMetadata(), ENGLISH, STRICT)).isNull();
	}

	@Test
	public void givenFrenchSystemWithAMultilingualCollectionsRecordWhenMetadatasHasMultilingualMetadatasAllLanguageValuesThenObtainedValueAccordingToLanguage()
			throws Exception {

		givenFrenchSystemOneMonolingualAndOneMultilingualCollection(
				withAMultilingualStringMetadata, andAnotherUnilingualStringMetadata);

		recordServices.add(multiLingualRecord = new TestRecord(multilingualSchema, "multiLingualRecord")
				.set(multilingualSchema.stringMetadata(), "value3fr")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "value3en")
				.set(multilingualSchema.anotherStringMetadata(), "value4"));

		multiLingualRecord = record("multiLingualRecord");

		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata())).isEqualTo("value3fr");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), FRENCH, PREFERRING)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ENGLISH, PREFERRING)).isEqualTo("value3en");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ARABIC, PREFERRING)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("value3en");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ARABIC, STRICT)).isNull();

		assertThat(multiLingualRecord.get(dummy(multilingualSchema.stringMetadata()))).isEqualTo("value3fr");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.stringMetadata()), FRENCH, PREFERRING)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.stringMetadata()), ENGLISH, PREFERRING)).isEqualTo("value3en");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.stringMetadata()), ARABIC, PREFERRING)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.stringMetadata()), FRENCH, STRICT)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.stringMetadata()), ENGLISH, STRICT)).isEqualTo("value3en");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.stringMetadata()), ARABIC, STRICT)).isNull();

		assertThat(multiLingualRecord.get(multilingualSchema.anotherStringMetadata())).isEqualTo("value4");
		assertThat(multiLingualRecord.get(multilingualSchema.anotherStringMetadata(), FRENCH, PREFERRING)).isEqualTo("value4");
		assertThat(multiLingualRecord.get(multilingualSchema.anotherStringMetadata(), ENGLISH, PREFERRING)).isEqualTo("value4");
		assertThat(multiLingualRecord.get(multilingualSchema.anotherStringMetadata(), ARABIC, PREFERRING)).isEqualTo("value4");
		assertThat(multiLingualRecord.get(multilingualSchema.anotherStringMetadata(), FRENCH, STRICT)).isEqualTo("value4");
		assertThat(multiLingualRecord.get(multilingualSchema.anotherStringMetadata(), ENGLISH, STRICT)).isNull();
		assertThat(multiLingualRecord.get(multilingualSchema.anotherStringMetadata(), ARABIC, STRICT)).isNull();
	}

	//-----------------------------------------------------------------------------------------------------------------------

	private SetupAlteration andAnotherUnilingualStringMetadata = new SetupAlteration() {
		@Override
		public void setupMonolingualCollection(MetadataSchemaTypesBuilder schemaTypes) {
			schemaTypes.getSchema("zeSchemaType_default").create("anotherStringMetadata").setType(STRING).setMultiLingual(false);
		}

		@Override
		public void setupMultilingualCollection(MetadataSchemaTypesBuilder schemaTypes) {
			schemaTypes.getSchema("zeSchemaType_default").create("anotherStringMetadata").setType(STRING).setMultiLingual(false);
		}

		@Override
		public void after() {

		}
	};

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

	protected void givenFrenchSystemOneMonolingualAndOneMultilingualCollection(final SetupAlteration... setupAlterations) {
		givenSystemLanguageIs("fr");
		givenCollection("monolingual", asList("fr")).withAllTestUsers();
		givenCollection("multilingual", asList("fr", "en")).withAllTestUsers();
		defineSchemasManager().using(monolingualCollectionSchemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				for (SetupAlteration setupAlteration : setupAlterations) {
					setupAlteration.setupMonolingualCollection(schemaTypes);
				}
			}
		}));

		defineSchemasManager().using(multilingualCollectionSchemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				for (SetupAlteration setupAlteration : setupAlterations) {
					setupAlteration.setupMultilingualCollection(schemaTypes);
				}
			}
		}));

		setupServices();
	}

	protected void givenFrenchSystemOneMonolingualAndOneTrilingualCollection(final SetupAlteration... setupAlterations) {
		givenSystemLanguageIs("fr");
		givenCollection("monolingual", asList("fr")).withAllTestUsers();
		givenCollection("multilingual", asList("fr", "en", "ar")).withAllTestUsers();
		defineSchemasManager().using(monolingualCollectionSchemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				for (SetupAlteration setupAlteration : setupAlterations) {
					setupAlteration.setupMonolingualCollection(schemaTypes);
				}
			}
		}));

		defineSchemasManager().using(multilingualCollectionSchemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {

				for (SetupAlteration setupAlteration : setupAlterations) {
					setupAlteration.setupMultilingualCollection(schemaTypes);
				}
			}
		}));

		setupServices();
	}

	protected void givenEnglishSystemOneMonolingualAndOneTrilingualCollection(final SetupAlteration... setupAlterations) {
		givenSystemLanguageIs("en");
		givenCollection("monolingual", asList("en")).withAllTestUsers();
		givenCollection("multilingual", asList("fr", "en", "ar")).withAllTestUsers();
		defineSchemasManager().using(monolingualCollectionSchemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				for (SetupAlteration setupAlteration : setupAlterations) {
					setupAlteration.setupMonolingualCollection(schemaTypes);
				}
			}
		}));

		defineSchemasManager().using(multilingualCollectionSchemas.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				for (SetupAlteration setupAlteration : setupAlterations) {
					setupAlteration.setupMultilingualCollection(schemaTypes);
				}
			}
		}));

		setupServices();
	}

	private void setupServices() {
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
	}

	private interface SetupAlteration {
		void setupMonolingualCollection(MetadataSchemaTypesBuilder schemaTypes);

		void setupMultilingualCollection(MetadataSchemaTypesBuilder schemaTypes);

		void after();
	}
}
