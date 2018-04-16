package com.constellio.model.services.records;

import static com.constellio.model.entities.Language.Arabic;
import static com.constellio.model.entities.Language.English;
import static com.constellio.model.entities.Language.French;
import static com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval.PREFERRING;
import static com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval.STRICT;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.Schemas.dummy;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultilingual;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSortable;
import static java.util.Arrays.asList;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.FRENCH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Locale;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
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

		assertThat(multiLingualRecord.getValues(multilingualSchema.stringMetadata())).containsOnly("value3fr");
		assertThat(multiLingualRecord.getValues(multilingualSchema.stringMetadata(), FRENCH, PREFERRING))
				.containsOnly("value3fr");
		assertThat(multiLingualRecord.getValues(multilingualSchema.stringMetadata(), ENGLISH, PREFERRING))
				.containsOnly("value3en");
		assertThat(multiLingualRecord.getValues(multilingualSchema.stringMetadata(), ARABIC, PREFERRING))
				.containsOnly("value3fr");
		assertThat(multiLingualRecord.getValues(multilingualSchema.stringMetadata(), FRENCH, STRICT)).containsOnly("value3fr");
		assertThat(multiLingualRecord.getValues(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).containsOnly("value3en");
		assertThat(multiLingualRecord.getValues(multilingualSchema.stringMetadata(), ARABIC, STRICT)).isEmpty();

		assertThat(multiLingualRecord.get(dummy(multilingualSchema.stringMetadata()))).isEqualTo("value3fr");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.stringMetadata()), FRENCH, PREFERRING)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.stringMetadata()), ENGLISH, PREFERRING)).isEqualTo("value3en");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.stringMetadata()), ARABIC, PREFERRING)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.stringMetadata()), FRENCH, STRICT)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.stringMetadata()), ENGLISH, STRICT)).isEqualTo("value3en");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.stringMetadata()), ARABIC, STRICT)).isNull();

		assertThat(multiLingualRecord.getValues(dummy(multilingualSchema.stringMetadata()))).containsOnly("value3fr");
		assertThat(multiLingualRecord.getValues(dummy(multilingualSchema.stringMetadata()), FRENCH, PREFERRING))
				.containsOnly("value3fr");
		assertThat(multiLingualRecord.getValues(dummy(multilingualSchema.stringMetadata()), ENGLISH, PREFERRING))
				.containsOnly("value3en");
		assertThat(multiLingualRecord.getValues(dummy(multilingualSchema.stringMetadata()), ARABIC, PREFERRING))
				.containsOnly("value3fr");
		assertThat(multiLingualRecord.getValues(dummy(multilingualSchema.stringMetadata()), FRENCH, STRICT))
				.containsOnly("value3fr");
		assertThat(multiLingualRecord.getValues(dummy(multilingualSchema.stringMetadata()), ENGLISH, STRICT))
				.containsOnly("value3en");
		assertThat(multiLingualRecord.getValues(dummy(multilingualSchema.stringMetadata()), ARABIC, STRICT)).isEmpty();

		assertThat(multiLingualRecord.get(multilingualSchema.anotherStringMetadata())).isEqualTo("value4");
		assertThat(multiLingualRecord.get(multilingualSchema.anotherStringMetadata(), FRENCH, PREFERRING)).isEqualTo("value4");
		assertThat(multiLingualRecord.get(multilingualSchema.anotherStringMetadata(), ENGLISH, PREFERRING)).isEqualTo("value4");
		assertThat(multiLingualRecord.get(multilingualSchema.anotherStringMetadata(), ARABIC, PREFERRING)).isEqualTo("value4");
		assertThat(multiLingualRecord.get(multilingualSchema.anotherStringMetadata(), FRENCH, STRICT)).isEqualTo("value4");
		assertThat(multiLingualRecord.get(multilingualSchema.anotherStringMetadata(), ENGLISH, STRICT)).isNull();
		assertThat(multiLingualRecord.get(multilingualSchema.anotherStringMetadata(), ARABIC, STRICT)).isNull();

		assertThat(multiLingualRecord.get(dummy(multilingualSchema.anotherStringMetadata()))).isEqualTo("value4");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.anotherStringMetadata()), FRENCH, PREFERRING))
				.isEqualTo("value4");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.anotherStringMetadata()), ENGLISH, PREFERRING))
				.isEqualTo("value4");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.anotherStringMetadata()), ARABIC, PREFERRING))
				.isEqualTo("value4");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.anotherStringMetadata()), FRENCH, STRICT)).isEqualTo("value4");
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.anotherStringMetadata()), ENGLISH, STRICT)).isNull();
		assertThat(multiLingualRecord.get(dummy(multilingualSchema.anotherStringMetadata()), ARABIC, STRICT)).isNull();
	}

	@Test
	public void givenEnglishSystemWithAMultilingualCollectionsRecordWhenMetadatasHasMultilingualMetadatasAllLanguageValuesThenObtainedValueAccordingToLanguage()
			throws Exception {

		givenEnglishSystemOneMonolingualAndOneTrilingualCollection(withAMultilingualListStringMetadata);

		recordServices.add(multiLingualRecord = new TestRecord(multilingualSchema, "multiLingualRecord")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, asList("value1fr", "value2fr"))
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, asList("value1en", "value2en")));

		multiLingualRecord = record("multiLingualRecord");

		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata())).isEqualTo(asList("value1en", "value2en"));
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), FRENCH, PREFERRING))
				.isEqualTo(asList("value1fr", "value2fr"));
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ENGLISH, PREFERRING))
				.isEqualTo(asList("value1en", "value2en"));
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ARABIC, PREFERRING))
				.isEqualTo(asList("value1en", "value2en"));
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), FRENCH, STRICT))
				.isEqualTo(asList("value1fr", "value2fr"));
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ENGLISH, STRICT))
				.isEqualTo(asList("value1en", "value2en"));
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ARABIC, STRICT)).isEqualTo(new ArrayList<>());

		assertThat(multiLingualRecord.getList(multilingualSchema.stringMetadata())).isEqualTo(asList("value1en", "value2en"));
		assertThat(multiLingualRecord.getList(multilingualSchema.stringMetadata(), FRENCH, PREFERRING))
				.isEqualTo(asList("value1fr", "value2fr"));
		assertThat(multiLingualRecord.getList(multilingualSchema.stringMetadata(), ENGLISH, PREFERRING))
				.isEqualTo(asList("value1en", "value2en"));
		assertThat(multiLingualRecord.getList(multilingualSchema.stringMetadata(), ARABIC, PREFERRING))
				.isEqualTo(asList("value1en", "value2en"));
		assertThat(multiLingualRecord.getList(multilingualSchema.stringMetadata(), FRENCH, STRICT))
				.isEqualTo(asList("value1fr", "value2fr"));
		assertThat(multiLingualRecord.getList(multilingualSchema.stringMetadata(), ENGLISH, STRICT))
				.isEqualTo(asList("value1en", "value2en"));
		assertThat(multiLingualRecord.getList(multilingualSchema.stringMetadata(), ARABIC, STRICT)).isEmpty();

		assertThat(multiLingualRecord.getValues(multilingualSchema.stringMetadata())).isEqualTo(asList("value1en", "value2en"));
		assertThat(multiLingualRecord.getValues(multilingualSchema.stringMetadata(), FRENCH, PREFERRING))
				.isEqualTo(asList("value1fr", "value2fr"));
		assertThat(multiLingualRecord.getValues(multilingualSchema.stringMetadata(), ENGLISH, PREFERRING))
				.isEqualTo(asList("value1en", "value2en"));
		assertThat(multiLingualRecord.getValues(multilingualSchema.stringMetadata(), ARABIC, PREFERRING))
				.isEqualTo(asList("value1en", "value2en"));
		assertThat(multiLingualRecord.getValues(multilingualSchema.stringMetadata(), FRENCH, STRICT))
				.isEqualTo(asList("value1fr", "value2fr"));
		assertThat(multiLingualRecord.getValues(multilingualSchema.stringMetadata(), ENGLISH, STRICT))
				.isEqualTo(asList("value1en", "value2en"));
		assertThat(multiLingualRecord.getValues(multilingualSchema.stringMetadata(), ARABIC, STRICT)).isEmpty();
	}

	@Test
	public void whenModifyingMultilingualMetadataThenModifiedValuesPersistedAndUnmodifiedValuesKept()
			throws Exception {

		givenFrenchSystemOneMonolingualAndOneMultilingualCollection(
				withAMultilingualStringMetadata, andAnotherUnilingualStringMetadata);

		multiLingualRecord = new TestRecord(multilingualSchema, "multiLingualRecord")
				.set(multilingualSchema.stringMetadata(), "value3fr")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "value3en")
				.set(multilingualSchema.anotherStringMetadata(), "value4");

		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("value3en");

		recordServices.add(multiLingualRecord);
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("value3en");

		multiLingualRecord = recordServices.getDocumentById(multiLingualRecord.getId());
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("value3en");

		multiLingualRecord.set(multilingualSchema.stringMetadata(), "newValue");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("newValue");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("value3en");

		multiLingualRecord.set(multilingualSchema.stringMetadata(), Locale.UK, "cupOfTea");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("newValue");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("cupOfTea");

		assertThat(multiLingualRecord.getCopyOfOriginalRecord().get(multilingualSchema.stringMetadata(), FRENCH, STRICT))
				.isEqualTo("value3fr");
		assertThat(multiLingualRecord.getCopyOfOriginalRecord().get(multilingualSchema.stringMetadata(), ENGLISH, STRICT))
				.isEqualTo("value3en");

		Record originalCopy = multiLingualRecord.getCopyOfOriginalRecordKeepingOnly(asList(multilingualSchema.stringMetadata()));
		assertThat(originalCopy.get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("value3fr");
		assertThat(originalCopy.get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("value3en");

		recordServices.update(multiLingualRecord);

		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("newValue");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("cupOfTea");
		multiLingualRecord = recordServices.getDocumentById(multiLingualRecord.getId());
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("newValue");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("cupOfTea");
	}

	@Test
	public void whenModifyingMultilingualMultivalueMetadataThenModifiedValuesPersistedAndUnmodifiedValuesKept()
			throws Exception {

		givenEnglishSystemOneMonolingualAndOneTrilingualCollection(
				withAMultilingualListStringMetadata, andAnotherUnilingualStringMetadata);

		multiLingualRecord = new TestRecord(multilingualSchema, "multiLingualRecord")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, asList("value1en", "value2en"))
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, asList("value1fr", "value2fr"));

		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), FRENCH, STRICT))
				.isEqualTo(asList("value1fr", "value2fr"));
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ENGLISH, STRICT))
				.isEqualTo(asList("value1en", "value2en"));

		recordServices.add(multiLingualRecord);
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), FRENCH, STRICT))
				.isEqualTo(asList("value1fr", "value2fr"));
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ENGLISH, STRICT))
				.isEqualTo(asList("value1en", "value2en"));

		multiLingualRecord = recordServices.getDocumentById(multiLingualRecord.getId());
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), FRENCH, STRICT))
				.isEqualTo(asList("value1fr", "value2fr"));
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ENGLISH, STRICT))
				.isEqualTo(asList("value1en", "value2en"));

		multiLingualRecord.set(multilingualSchema.stringMetadata(), asList("value3en", "value4en"));
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), FRENCH, STRICT))
				.isEqualTo(asList("value1fr", "value2fr"));
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ENGLISH, STRICT))
				.isEqualTo(asList("value3en", "value4en"));

		multiLingualRecord.set(multilingualSchema.stringMetadata(), Locale.CANADA_FRENCH, asList("value3fr", "value4fr"));
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), FRENCH, STRICT))
				.isEqualTo(asList("value3fr", "value4fr"));
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ENGLISH, STRICT))
				.isEqualTo(asList("value3en", "value4en"));

		assertThat(multiLingualRecord.getCopyOfOriginalRecord().get(multilingualSchema.stringMetadata(), FRENCH, STRICT))
				.isEqualTo(asList("value1fr", "value2fr"));
		assertThat(multiLingualRecord.getCopyOfOriginalRecord().get(multilingualSchema.stringMetadata(), ENGLISH, STRICT))
				.isEqualTo(asList("value1en", "value2en"));

	}

	@Test
	public void givenOptimisticLockingProblemWhenUpdatingSameMetadataThenMergedIfValuesOfDifferentLanguages()
			throws Exception {

		givenFrenchSystemOneMonolingualAndOneMultilingualCollection(
				withAMultilingualStringMetadata, andAnotherUnilingualStringMetadata);

		multiLingualRecord = new TestRecord(multilingualSchema, "multiLingualRecord")
				.set(multilingualSchema.stringMetadata(), "value3fr")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "value3en")
				.set(multilingualSchema.anotherStringMetadata(), "value4");
		recordServices.add(multiLingualRecord);

		Record multiLingualRecord2 = multiLingualRecord.getCopyOfOriginalRecord();

		recordServices.update(multiLingualRecord.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "newFRvalue"));
		recordServices.update(multiLingualRecord2.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "newENvalue"));

		multiLingualRecord = recordServices.getDocumentById(multiLingualRecord.getId());
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("newFRvalue");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("newENvalue");

		assertThat(multiLingualRecord2.get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("newFRvalue");
		assertThat(multiLingualRecord2.get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("newENvalue");

		multiLingualRecord2 = multiLingualRecord.getCopyOfOriginalRecord();

		recordServices.update(multiLingualRecord.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "newFRvalue2"));
		Transaction tx = new Transaction();
		tx.add(multiLingualRecord2.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "mouhahahah"));
		tx.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		try {
			recordServices.execute(tx);
			fail("Exception expected");
		} catch (RecordServicesException.OptimisticLocking e) {
			//OK
		}

		multiLingualRecord2 = multiLingualRecord2.getCopyOfOriginalRecord();
		try {
			recordServices.update(multiLingualRecord2.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "mouhahahah"));
			fail("Exception expected");
		} catch (RecordServicesException.UnresolvableOptimisticLockingConflict e) {
			//OK
		}

		multiLingualRecord = recordServices.getDocumentById(multiLingualRecord.getId());
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("newFRvalue2");
		assertThat(multiLingualRecord.get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("newENvalue");
	}

	@Test
	public void givenSortedMultilingualMetadataThenSortFieldCreatedForEachLanguageAndSortUsingQueryLanguage()
			throws Exception {

		givenSystemLanguageIs("fr");
		givenCollection("multilingual", asList("fr", "en")).withAllTestUsers();
		defineSchemasManager().using(multilingualCollectionSchemas
				.withAStringMetadata(whichIsSortable, whichIsMultilingual)
				.withAnotherStringMetadata(whichIsSortable));

		setupServices();
		Transaction tx = new Transaction();

		tx.add(new TestRecord(multilingualSchema, "r1")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "pomme")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "Apple")
				.set(multilingualSchema.anotherStringMetadata(), "Fruit"));

		tx.add(new TestRecord(multilingualSchema, "r2")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "Pêche")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "Peach")
				.set(multilingualSchema.anotherStringMetadata(), "Fruit"));

		tx.add(new TestRecord(multilingualSchema, "r3")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "Poire")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "pear")
				.set(multilingualSchema.anotherStringMetadata(), "Fruit"));

		tx.add(new TestRecord(multilingualSchema, "r4")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "Fraise")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "Strawberry")
				.set(multilingualSchema.anotherStringMetadata(), "Fruit"));

		tx.add(new TestRecord(multilingualSchema, "r5")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "perdrix")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "Partridge")
				.set(multilingualSchema.anotherStringMetadata(), "Oiseau"));

		tx.add(new TestRecord(multilingualSchema, "r6")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "peanut")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "peanut")
				.set(multilingualSchema.anotherStringMetadata(), "Autre"));

		recordServices.execute(tx);

		LogicalSearchQuery query = new LogicalSearchQuery(from(multilingualSchema.type()).returnAll());
		query.sortAsc(multilingualSchema.stringMetadata());
		query.setLanguage(Locale.FRENCH);

		assertThatRecords(searchServices.search(query)).preferring(Locale.FRENCH).extractingMetadata("stringMetadata")
				.isEqualTo(asList("Fraise", "peanut", "Pêche", "perdrix", "Poire", "pomme"));

		query.setLanguage(Locale.ENGLISH);
		assertThatRecords(searchServices.search(query)).preferring(Locale.ENGLISH).extractingMetadata("stringMetadata")
				.isEqualTo(asList("Apple", "Partridge", "Peach", "peanut", "pear", "Strawberry"));

		query = new LogicalSearchQuery(from(multilingualSchema.type()).returnAll());
		query.sortAsc(multilingualSchema.anotherStringMetadata()).sortAsc(multilingualSchema.stringMetadata());
		query.setLanguage(Locale.FRENCH);
		assertThatRecords(searchServices.search(query)).preferring(Locale.FRENCH).extractingMetadata("stringMetadata")
				.isEqualTo(asList("peanut", "Fraise", "Pêche", "Poire", "pomme", "perdrix"));

		query.setLanguage(Locale.ENGLISH);
		assertThatRecords(searchServices.search(query)).preferring(Locale.ENGLISH).extractingMetadata("stringMetadata")
				.isEqualTo(asList("peanut", "Apple", "Peach", "pear", "Strawberry", "Partridge"));

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

	protected void givenFrenchSystemWithMultilingualCollection(final SetupAlteration... setupAlterations) {
		givenSystemLanguageIs("fr");
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
