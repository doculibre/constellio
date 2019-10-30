package com.constellio.model.services.records;

import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.params.GetCaptionForRecordParams;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.records.cache.RecordsCaches;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.constellio.model.entities.Language.Arabic;
import static com.constellio.model.entities.Language.English;
import static com.constellio.model.entities.Language.French;
import static com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval.PREFERRING;
import static com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval.STRICT;
import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static com.constellio.model.entities.schemas.MetadataValueType.STRING;
import static com.constellio.model.entities.schemas.Schemas.dummy;
import static com.constellio.model.entities.schemas.Schemas.getSortMetadata;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory.autocompleteFieldMatching;
import static com.constellio.sdk.tests.TestUtils.assertThatRecords;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultilingual;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsMultivalue;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSchemaAutocomplete;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSortable;
import static java.util.Arrays.asList;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.FRENCH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.Assert.fail;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RecordServicesMultilingualAcceptanceTest extends ConstellioTest {

	private static Locale ARABIC = new Locale("ar");

	RecordServicesTestSchemaSetup monolingualCollectionSchemas;
	RecordServicesTestSchemaSetup.ZeSchemaMetadatas monolingualSchema;
	RecordServicesTestSchemaSetup multilingualCollectionSchemas;
	RecordServicesTestSchemaSetup.ZeSchemaMetadatas multilingualSchema;
	RecordServicesTestSchemaSetup.AnotherSchemaMetadatas referencingMultilingualSchema;

	RecordServices recordServices;
	SearchServices searchServices;
	Record monoLingualRecord, multiLingualRecord;

	@Test
	public void givenFrenchSystemWithAMultilingualCollectionsThenSchemasHaveValidLanguages()
			throws Exception {

		givenFrenchSystemWithOneMonolingualAndOneMultilingualCollectionAndSchemas();
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

		givenEnglishSystemWithOneMonolingualAndOneTrilingualCollectionAndSchemas();
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

		givenFrenchSystemWithOneMonolingualAndOneMultilingualCollectionAndSchemas(
				withAMultilingualStringMetadata, andAnotherUnilingualStringMetadata);

		recordServices.add(monoLingualRecord = new TestRecord(monolingualSchema, "monoLingualRecord")
				.set(monolingualSchema.stringMetadata(), "value1")
				.set(monolingualSchema.anotherStringMetadata(), "value2"));

		recordServices.add(multiLingualRecord = new TestRecord(multilingualSchema, "multiLingualRecord")
				.set(multilingualSchema.stringMetadata(), "value3")
				.set(multilingualSchema.anotherStringMetadata(), "value4"));

		monoLingualRecord = record("monoLingualRecord");
		multiLingualRecord = record("multiLingualRecord");

		assertThat(monoLingualRecord.<String>get(monolingualSchema.stringMetadata())).isEqualTo("value1");
		assertThat(monoLingualRecord.<String>get(monolingualSchema.stringMetadata(), FRENCH, PREFERRING)).isEqualTo("value1");
		assertThat(monoLingualRecord.<String>get(monolingualSchema.stringMetadata(), ENGLISH, PREFERRING)).isEqualTo("value1");
		assertThat(monoLingualRecord.<String>get(monolingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("value1");
		assertThat(monoLingualRecord.<String>get(monolingualSchema.stringMetadata(), ENGLISH, STRICT)).isNull();

		assertThat(monoLingualRecord.<String>get(dummy(monolingualSchema.stringMetadata()))).isEqualTo("value1");
		assertThat(monoLingualRecord.<String>get(dummy(monolingualSchema.stringMetadata()), FRENCH, PREFERRING)).isEqualTo("value1");
		assertThat(monoLingualRecord.<String>get(dummy(monolingualSchema.stringMetadata()), ENGLISH, PREFERRING)).isEqualTo("value1");
		assertThat(monoLingualRecord.<String>get(dummy(monolingualSchema.stringMetadata()), FRENCH, STRICT)).isEqualTo("value1");
		assertThat(monoLingualRecord.<String>get(dummy(monolingualSchema.stringMetadata()), ENGLISH, STRICT)).isNull();

		assertThat(monoLingualRecord.<String>get(monolingualSchema.anotherStringMetadata())).isEqualTo("value2");
		assertThat(monoLingualRecord.<String>get(monolingualSchema.anotherStringMetadata(), FRENCH, PREFERRING)).isEqualTo("value2");
		assertThat(monoLingualRecord.<String>get(monolingualSchema.anotherStringMetadata(), ENGLISH, PREFERRING)).isEqualTo("value2");
		assertThat(monoLingualRecord.<String>get(monolingualSchema.anotherStringMetadata(), FRENCH, STRICT)).isEqualTo("value2");
		assertThat(monoLingualRecord.<String>get(monolingualSchema.anotherStringMetadata(), ENGLISH, STRICT)).isNull();

		assertThat(monoLingualRecord.<String>get(monolingualSchema.anotherStringMetadata())).isEqualTo("value2");
		assertThat(monoLingualRecord.<String>get(monolingualSchema.anotherStringMetadata(), FRENCH, PREFERRING)).isEqualTo("value2");
		assertThat(monoLingualRecord.<String>get(monolingualSchema.anotherStringMetadata(), ENGLISH, PREFERRING)).isEqualTo("value2");
		assertThat(monoLingualRecord.<String>get(monolingualSchema.anotherStringMetadata(), FRENCH, STRICT)).isEqualTo("value2");
		assertThat(monoLingualRecord.<String>get(monolingualSchema.anotherStringMetadata(), ENGLISH, STRICT)).isNull();

		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata())).isEqualTo("value3");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), FRENCH, PREFERRING)).isEqualTo("value3");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), ENGLISH, PREFERRING)).isEqualTo("value3");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("value3");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isNull();

		assertThat(multiLingualRecord.<String>get(dummy(multilingualSchema.stringMetadata()))).isEqualTo("value3");
		assertThat(multiLingualRecord.<String>get(dummy(multilingualSchema.stringMetadata()), FRENCH, PREFERRING)).isEqualTo("value3");
		assertThat(multiLingualRecord.<String>get(dummy(multilingualSchema.stringMetadata()), ENGLISH, PREFERRING)).isEqualTo("value3");
		assertThat(multiLingualRecord.<String>get(dummy(multilingualSchema.stringMetadata()), FRENCH, STRICT)).isEqualTo("value3");
		assertThat(multiLingualRecord.<String>get(dummy(multilingualSchema.stringMetadata()), ENGLISH, STRICT)).isNull();

		assertThat(multiLingualRecord.<String>get(multilingualSchema.anotherStringMetadata())).isEqualTo("value4");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.anotherStringMetadata(), FRENCH, PREFERRING)).isEqualTo("value4");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.anotherStringMetadata(), ENGLISH, PREFERRING)).isEqualTo("value4");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.anotherStringMetadata(), FRENCH, STRICT)).isEqualTo("value4");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.anotherStringMetadata(), ENGLISH, STRICT)).isNull();
	}

	@Test
	public void givenFrenchSystemWithAMultilingualCollectionsRecordWhenMetadatasHasMultilingualMetadatasAllLanguageValuesThenObtainedValueAccordingToLanguage()
			throws Exception {

		givenFrenchSystemWithOneMonolingualAndOneMultilingualCollectionAndSchemas(
				withAMultilingualStringMetadata, andAnotherUnilingualStringMetadata);

		recordServices.add(multiLingualRecord = new TestRecord(multilingualSchema, "multiLingualRecord")
				.set(multilingualSchema.stringMetadata(), "value3fr")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "value3en")
				.set(multilingualSchema.anotherStringMetadata(), "value4"));

		multiLingualRecord = record("multiLingualRecord");

		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata())).isEqualTo("value3fr");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), FRENCH, PREFERRING)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), ENGLISH, PREFERRING)).isEqualTo("value3en");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), ARABIC, PREFERRING)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("value3en");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), ARABIC, STRICT)).isNull();

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

		assertThat(multiLingualRecord.<String>get(dummy(multilingualSchema.stringMetadata()))).isEqualTo("value3fr");
		assertThat(multiLingualRecord.<String>get(dummy(multilingualSchema.stringMetadata()), FRENCH, PREFERRING)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.<String>get(dummy(multilingualSchema.stringMetadata()), ENGLISH, PREFERRING)).isEqualTo("value3en");
		assertThat(multiLingualRecord.<String>get(dummy(multilingualSchema.stringMetadata()), ARABIC, PREFERRING)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.<String>get(dummy(multilingualSchema.stringMetadata()), FRENCH, STRICT)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.<String>get(dummy(multilingualSchema.stringMetadata()), ENGLISH, STRICT)).isEqualTo("value3en");
		assertThat(multiLingualRecord.<String>get(dummy(multilingualSchema.stringMetadata()), ARABIC, STRICT)).isNull();

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

		assertThat(multiLingualRecord.<String>get(multilingualSchema.anotherStringMetadata())).isEqualTo("value4");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.anotherStringMetadata(), FRENCH, PREFERRING)).isEqualTo("value4");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.anotherStringMetadata(), ENGLISH, PREFERRING)).isEqualTo("value4");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.anotherStringMetadata(), ARABIC, PREFERRING)).isEqualTo("value4");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.anotherStringMetadata(), FRENCH, STRICT)).isEqualTo("value4");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.anotherStringMetadata(), ENGLISH, STRICT)).isNull();
		assertThat(multiLingualRecord.<String>get(multilingualSchema.anotherStringMetadata(), ARABIC, STRICT)).isNull();

		assertThat(multiLingualRecord.<String>get(dummy(multilingualSchema.anotherStringMetadata()))).isEqualTo("value4");
		assertThat(multiLingualRecord.<String>get(dummy(multilingualSchema.anotherStringMetadata()), FRENCH, PREFERRING))
				.isEqualTo("value4");
		assertThat(multiLingualRecord.<String>get(dummy(multilingualSchema.anotherStringMetadata()), ENGLISH, PREFERRING))
				.isEqualTo("value4");
		assertThat(multiLingualRecord.<String>get(dummy(multilingualSchema.anotherStringMetadata()), ARABIC, PREFERRING))
				.isEqualTo("value4");
		assertThat(multiLingualRecord.<String>get(dummy(multilingualSchema.anotherStringMetadata()), FRENCH, STRICT)).isEqualTo("value4");
		assertThat(multiLingualRecord.<String>get(dummy(multilingualSchema.anotherStringMetadata()), ENGLISH, STRICT)).isNull();
		assertThat(multiLingualRecord.<String>get(dummy(multilingualSchema.anotherStringMetadata()), ARABIC, STRICT)).isNull();
	}

	@Test
	public void givenEnglishSystemWithAMultilingualCollectionsRecordWhenMetadatasHasMultilingualMetadatasAllLanguageValuesThenObtainedValueAccordingToLanguage()
			throws Exception {

		givenEnglishSystemWithOneMonolingualAndOneTrilingualCollectionAndSchemas(withAMultilingualListStringMetadata);

		recordServices.add(multiLingualRecord = new TestRecord(multilingualSchema, "multiLingualRecord")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, asList("value1fr", "value2fr"))
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, asList("value1en", "value2en")));

		multiLingualRecord = record("multiLingualRecord");

		assertThat(multiLingualRecord.<List<String>>get(multilingualSchema.stringMetadata())).isEqualTo(asList("value1en", "value2en"));
		assertThat(multiLingualRecord.<List<String>>get(multilingualSchema.stringMetadata(), FRENCH, PREFERRING))
				.isEqualTo(asList("value1fr", "value2fr"));
		assertThat(multiLingualRecord.<List<String>>get(multilingualSchema.stringMetadata(), ENGLISH, PREFERRING))
				.isEqualTo(asList("value1en", "value2en"));
		assertThat(multiLingualRecord.<List<String>>get(multilingualSchema.stringMetadata(), ARABIC, PREFERRING))
				.isEqualTo(asList("value1en", "value2en"));
		assertThat(multiLingualRecord.<List<String>>get(multilingualSchema.stringMetadata(), FRENCH, STRICT))
				.isEqualTo(asList("value1fr", "value2fr"));
		assertThat(multiLingualRecord.<List<String>>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT))
				.isEqualTo(asList("value1en", "value2en"));
		assertThat(multiLingualRecord.<List<String>>get(multilingualSchema.stringMetadata(), ARABIC, STRICT)).isEqualTo(new ArrayList<>());

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

		givenFrenchSystemWithOneMonolingualAndOneMultilingualCollectionAndSchemas(
				withAMultilingualStringMetadata, andAnotherUnilingualStringMetadata);

		multiLingualRecord = new TestRecord(multilingualSchema, "multiLingualRecord")
				.set(multilingualSchema.stringMetadata(), "value3fr")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "value3en")
				.set(multilingualSchema.anotherStringMetadata(), "value4");

		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("value3en");

		recordServices.add(multiLingualRecord);
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("value3en");

		multiLingualRecord = recordServices.getDocumentById(multiLingualRecord.getId());
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("value3fr");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("value3en");

		multiLingualRecord.set(multilingualSchema.stringMetadata(), "newValue");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("newValue");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("value3en");

		multiLingualRecord.set(multilingualSchema.stringMetadata(), Locale.UK, "cupOfTea");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("newValue");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("cupOfTea");

		assertThat(multiLingualRecord.getCopyOfOriginalRecord().<String>get(multilingualSchema.stringMetadata(), FRENCH, STRICT))
				.isEqualTo("value3fr");
		assertThat(multiLingualRecord.getCopyOfOriginalRecord().<String>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT))
				.isEqualTo("value3en");

		Record originalCopy = multiLingualRecord.getCopyOfOriginalRecordKeepingOnly(asList(multilingualSchema.stringMetadata()));
		assertThat(originalCopy.<String>get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("value3fr");
		assertThat(originalCopy.<String>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("value3en");

		recordServices.update(multiLingualRecord);

		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("newValue");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("cupOfTea");
		multiLingualRecord = recordServices.getDocumentById(multiLingualRecord.getId());
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("newValue");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("cupOfTea");
	}

	@Test
	public void whenModifyingMultilingualMultivalueMetadataThenModifiedValuesPersistedAndUnmodifiedValuesKept()
			throws Exception {

		givenEnglishSystemWithOneMonolingualAndOneTrilingualCollectionAndSchemas(
				withAMultilingualListStringMetadata, andAnotherUnilingualStringMetadata);

		multiLingualRecord = new TestRecord(multilingualSchema, "multiLingualRecord")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, asList("value1en", "value2en"))
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, asList("value1fr", "value2fr"));

		assertThat(multiLingualRecord.<List<String>>get(multilingualSchema.stringMetadata(), FRENCH, STRICT))
				.isEqualTo(asList("value1fr", "value2fr"));
		assertThat(multiLingualRecord.<List<String>>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT))
				.isEqualTo(asList("value1en", "value2en"));

		recordServices.add(multiLingualRecord);
		assertThat(multiLingualRecord.<List<String>>get(multilingualSchema.stringMetadata(), FRENCH, STRICT))
				.isEqualTo(asList("value1fr", "value2fr"));
		assertThat(multiLingualRecord.<List<String>>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT))
				.isEqualTo(asList("value1en", "value2en"));

		multiLingualRecord = recordServices.getDocumentById(multiLingualRecord.getId());
		assertThat(multiLingualRecord.<List<String>>get(multilingualSchema.stringMetadata(), FRENCH, STRICT))
				.isEqualTo(asList("value1fr", "value2fr"));
		assertThat(multiLingualRecord.<List<String>>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT))
				.isEqualTo(asList("value1en", "value2en"));

		multiLingualRecord.set(multilingualSchema.stringMetadata(), asList("value3en", "value4en"));
		assertThat(multiLingualRecord.<List<String>>get(multilingualSchema.stringMetadata(), FRENCH, STRICT))
				.isEqualTo(asList("value1fr", "value2fr"));
		assertThat(multiLingualRecord.<List<String>>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT))
				.isEqualTo(asList("value3en", "value4en"));

		multiLingualRecord.set(multilingualSchema.stringMetadata(), Locale.CANADA_FRENCH, asList("value3fr", "value4fr"));
		assertThat(multiLingualRecord.<List<String>>get(multilingualSchema.stringMetadata(), FRENCH, STRICT))
				.isEqualTo(asList("value3fr", "value4fr"));
		assertThat(multiLingualRecord.<List<String>>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT))
				.isEqualTo(asList("value3en", "value4en"));

		assertThat(multiLingualRecord.getCopyOfOriginalRecord().<List<String>>get(multilingualSchema.stringMetadata(), FRENCH, STRICT))
				.isEqualTo(asList("value1fr", "value2fr"));
		assertThat(multiLingualRecord.getCopyOfOriginalRecord().<List<String>>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT))
				.isEqualTo(asList("value1en", "value2en"));

	}

	@Test
	public void givenOptimisticLockingProblemWhenUpdatingSameMetadataThenMergedIfValuesOfDifferentLanguages()
			throws Exception {

		givenFrenchSystemWithOneMonolingualAndOneMultilingualCollectionAndSchemas(
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
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("newFRvalue");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("newENvalue");

		assertThat(multiLingualRecord2.<String>get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("newFRvalue");
		assertThat(multiLingualRecord2.<String>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("newENvalue");

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
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), FRENCH, STRICT)).isEqualTo("newFRvalue2");
		assertThat(multiLingualRecord.<String>get(multilingualSchema.stringMetadata(), ENGLISH, STRICT)).isEqualTo("newENvalue");
	}

	@Test
	public void givenSortedMultilingualMetadataThenSortFieldCreatedForEachLanguageAndSortUsingQueryLanguage()
			throws Exception {

		givenFrenchSystemWithOneMonolingualAndOneMultilingualCollection();
		defineSchemasManager().using(multilingualCollectionSchemas
				.withAStringMetadata(whichIsSortable, whichIsMultilingual)
				.withAnotherStringMetadata(whichIsSortable)
				.withAReferenceFromAnotherSchemaToZeSchema(whichIsSortable));
		setupServices();
		getModelLayerFactory().getExtensions().forCollection("multilingual").recordExtensions.add(new RecordExtension() {
			@Override
			public String getCaptionForRecord(GetCaptionForRecordParams params) {
				return params.getRecord().get(multilingualSchema.stringMetadata(), params.getLocale());
			}
		});

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

		tx.add(new TestRecord(referencingMultilingualSchema, "rr1")
				.set(referencingMultilingualSchema.referenceFromAnotherSchemaToZeSchema(), "r1"));

		tx.add(new TestRecord(referencingMultilingualSchema, "rr2")
				.set(referencingMultilingualSchema.referenceFromAnotherSchemaToZeSchema(), "r2"));

		tx.add(new TestRecord(referencingMultilingualSchema, "rr3")
				.set(referencingMultilingualSchema.referenceFromAnotherSchemaToZeSchema(), "r3"));

		tx.add(new TestRecord(referencingMultilingualSchema, "rr4")
				.set(referencingMultilingualSchema.referenceFromAnotherSchemaToZeSchema(), "r4"));

		tx.add(new TestRecord(referencingMultilingualSchema, "rr5")
				.set(referencingMultilingualSchema.referenceFromAnotherSchemaToZeSchema(), "r5"));

		tx.add(new TestRecord(referencingMultilingualSchema, "rr6")
				.set(referencingMultilingualSchema.referenceFromAnotherSchemaToZeSchema(), "r6"));

		recordServices.execute(tx);

		Metadata metadata = referencingMultilingualSchema.referenceFromAnotherSchemaToZeSchema();
		assertThat(metadata.getSortField().getDataStoreCode()).isEqualTo("referenceFromAnotherSchemaToZeSchema_sort_s");
		assertThat(getSortMetadata(metadata).getDataStoreCode()).isEqualTo("referenceFromAnotherSchemaToZeSchema_sort_s");

		assertThat(Schemas.getSecondaryLanguageMetadata(metadata.getSortField(), "en").getDataStoreCode())
				.isEqualTo("referenceFromAnotherSchemaToZeSchema.en_sort_s");
		assertThat(Schemas.getSecondaryLanguageMetadata(getSortMetadata(metadata), "en").getDataStoreCode())
				.isEqualTo("referenceFromAnotherSchemaToZeSchema.en_sort_s");

		/*****
		 * Validating multilingual text sort
		 */

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

		/*****
		 * Validating multilingual reference sort
		 */

		query = new LogicalSearchQuery(from(referencingMultilingualSchema.type()).returnAll());
		query.sortAsc(referencingMultilingualSchema.referenceFromAnotherSchemaToZeSchema());
		query.setLanguage(Locale.FRENCH);

		assertThatRecords(searchServices.search(query)).preferring(Locale.FRENCH)
				.extractingMetadata("referenceFromAnotherSchemaToZeSchema.stringMetadata")
				.isEqualTo(asList("Fraise", "peanut", "Pêche", "perdrix", "Poire", "pomme"));

		query.setLanguage(Locale.ENGLISH);
		assertThatRecords(searchServices.search(query)).preferring(Locale.ENGLISH)
				.extractingMetadata("referenceFromAnotherSchemaToZeSchema.stringMetadata")
				.isEqualTo(asList("Apple", "Partridge", "Peach", "peanut", "pear", "Strawberry"));

	}

	@Test
	public void givenFullyCachedSchemaAutocompleteMultilingualMetadataThenAutocompletionBasedOnLanguage()
			throws Exception {

		givenFrenchSystemWithOneMonolingualAndOneMultilingualCollection();
		defineSchemasManager().using(multilingualCollectionSchemas
				.withAStringMetadata(whichIsSchemaAutocomplete, whichIsMultilingual)
				.withAnotherStringMetadata(whichIsSchemaAutocomplete));

		setupServices();
		Transaction tx = new Transaction();

		tx.add(new TestRecord(multilingualSchema, "r1")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "pomme")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "Apple")
				.set(multilingualSchema.anotherStringMetadata(), "Fruit"));

		tx.add(new TestRecord(multilingualSchema, "r2")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "Pêche molle")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "Peach")
				.set(multilingualSchema.anotherStringMetadata(), "Fruit"));

		tx.add(new TestRecord(multilingualSchema, "r3")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "Poire")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "pear")
				.set(multilingualSchema.anotherStringMetadata(), "Fruit"));

		tx.add(new TestRecord(multilingualSchema, "r4")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "Quick aux fraises")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "Strawberry quick")
				.set(multilingualSchema.anotherStringMetadata(), "Fruit"));

		tx.add(new TestRecord(multilingualSchema, "r5")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "perdrix")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "Partridge")
				.set(multilingualSchema.anotherStringMetadata(), "Oiseau"));

		tx.add(new TestRecord(multilingualSchema, "r6")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "Arachide")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "peanut")
				.set(multilingualSchema.anotherStringMetadata(), "Autre"));

		recordServices.execute(tx);

		RecordsCaches recordsCaches = getModelLayerFactory().getRecordsCaches();
		assertThat(recordsCaches.getRecord("r1").<String>get(multilingualSchema.stringMetadata(), Locale.FRENCH))
				.isEqualTo("pomme");

		assertThat(recordsCaches.getRecord("r1").<String>get(multilingualSchema.stringMetadata(), ENGLISH))
				.isEqualTo("Apple");

		assertThat(recordsCaches.getRecord("r1").<String>get(multilingualSchema.anotherStringMetadata()))
				.isEqualTo("Fruit");

		assertThat(recordsCaches.getRecord("r4").<String>get(multilingualSchema.stringMetadata(), Locale.FRENCH))
				.isEqualTo("Quick aux fraises");

		assertThat(recordsCaches.getRecord("r4").<String>get(multilingualSchema.stringMetadata(), ENGLISH))
				.isEqualTo("Strawberry quick");

		assertThat(recordsCaches.getRecord("r4").<String>get(multilingualSchema.anotherStringMetadata()))
				.isEqualTo("Fruit");

		LogicalSearchQuery query = new LogicalSearchQuery(from(multilingualSchema.type()).returnAll());
		query.sortAsc(multilingualSchema.stringMetadata());
		query.setLanguage(Locale.FRENCH);

		assertThatRecords(searchServices.search(query)).preferring(Locale.FRENCH).extractingMetadatas("id", "autocomplete")
				.containsOnly(
						tuple("r1", asList("fruit", "pomme")),
						tuple("r2", asList("fruit", "molle", "peche")),
						tuple("r3", asList("fruit", "poire")),
						tuple("r4", asList("aux", "fraises", "fruit", "quick")),
						tuple("r5", asList("oiseau", "perdrix")),
						tuple("r6", asList("arachide", "autre")));

		assertThatRecords(searchServices.search(query)).preferring(Locale.ENGLISH).extractingMetadatas("id", "autocomplete")
				.containsOnly(
						tuple("r1", asList("apple", "fruit")),
						tuple("r2", asList("fruit", "peach")),
						tuple("r3", asList("fruit", "pear")),
						tuple("r4", asList("fruit", "quick", "strawberry")),
						tuple("r5", asList("oiseau", "partridge")),
						tuple("r6", asList("autre", "peanut")));

		assertThatAutoCompleteSearch(Locale.FRENCH, "fr").containsOnly("r1", "r2", "r3", "r4");
		assertThatAutoCompleteSearch(Locale.ENGLISH, "fr").containsOnly("r1", "r2", "r3", "r4");

		assertThatAutoCompleteSearch(Locale.FRENCH, "fra").containsOnly("r4");
		assertThatAutoCompleteSearch(Locale.ENGLISH, "fra").isEmpty();

		assertThatAutoCompleteSearch(Locale.FRENCH, "str").isEmpty();
		assertThatAutoCompleteSearch(Locale.ENGLISH, "str").containsOnly("r4");

		assertThatAutoCompleteSearch(Locale.FRENCH, "fr").containsOnly("r1", "r2", "r3", "r4");
		assertThatAutoCompleteSearch(Locale.ENGLISH, "fr").containsOnly("r1", "r2", "r3", "r4");

		assertThatAutoCompleteSearch(Locale.FRENCH, "p").containsOnly("r1", "r2", "r3", "r5");
		assertThatAutoCompleteSearch(Locale.ENGLISH, "p").containsOnly("r2", "r3", "r5", "r6");

		assertThatAutoCompleteSearch(Locale.FRENCH, "pe").containsOnly("r2", "r5");
		assertThatAutoCompleteSearch(Locale.ENGLISH, "pe").containsOnly("r2", "r3", "r6");

		assertThatAutoCompleteFruitSearch(Locale.FRENCH, "p").containsOnly("r1", "r2", "r3");
		assertThatAutoCompleteFruitSearch(Locale.ENGLISH, "p").containsOnly("r2", "r3");

		assertThatAutoCompleteFruitSearch(Locale.FRENCH, "pe").containsOnly("r2");
		assertThatAutoCompleteFruitSearch(Locale.ENGLISH, "pe").containsOnly("r2", "r3");

	}

	@Test
	public void whenFilteringReturnedMetadatasThenReturnedMetadatasOfAllLocales()
			throws Exception {

		givenFrenchSystemWithOneMonolingualAndOneMultilingualCollection();
		defineSchemasManager().using(multilingualCollectionSchemas
				.withAStringMetadata(whichIsSchemaAutocomplete, whichIsMultilingual)
				.withAnotherStringMetadata(whichIsSchemaAutocomplete));

		setupServices();
		Transaction tx = new Transaction();

		tx.add(new TestRecord(multilingualSchema, "r1")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "pomme")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "Apple")
				.set(multilingualSchema.anotherStringMetadata(), "Fruit"));

		tx.add(new TestRecord(multilingualSchema, "r2")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "Pêche molle")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "Peach")
				.set(multilingualSchema.anotherStringMetadata(), "Fruit"));

		tx.add(new TestRecord(multilingualSchema, "r3")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "Poire")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "pear")
				.set(multilingualSchema.anotherStringMetadata(), "Fruit"));

		tx.add(new TestRecord(multilingualSchema, "r4")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "Quick aux fraises")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "Strawberry quick")
				.set(multilingualSchema.anotherStringMetadata(), "Fruit"));

		tx.add(new TestRecord(multilingualSchema, "r5")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "perdrix")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "Partridge")
				.set(multilingualSchema.anotherStringMetadata(), "Oiseau"));

		tx.add(new TestRecord(multilingualSchema, "r6")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "Arachide")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "peanut")
				.set(multilingualSchema.anotherStringMetadata(), "Autre"));

		recordServices.execute(tx);

		LogicalSearchQuery query = new LogicalSearchQuery(from(multilingualSchema.type()).returnAll());
		query.setLanguage(Locale.FRENCH);
		query.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(multilingualSchema.stringMetadata()));

		assertThatRecords(searchServices.search(query)).preferring(Locale.FRENCH).extractingMetadatas("id", "stringMetadata")
				.containsOnly(
						tuple("r1", "pomme"),
						tuple("r2", "Pêche molle"),
						tuple("r3", "Poire"),
						tuple("r4", "Quick aux fraises"),
						tuple("r5", "perdrix"),
						tuple("r6", "Arachide"));

		assertThatRecords(searchServices.search(query)).preferring(Locale.ENGLISH).extractingMetadatas("id", "stringMetadata")
				.containsOnly(
						tuple("r1", "Apple"),
						tuple("r2", "Peach"),
						tuple("r3", "pear"),
						tuple("r4", "Strawberry quick"),
						tuple("r5", "Partridge"),
						tuple("r6", "peanut"));

		query.setLanguage(Locale.ENGLISH);
		assertThatRecords(searchServices.search(query)).preferring(Locale.FRENCH).extractingMetadatas("id", "stringMetadata")
				.containsOnly(
						tuple("r1", "pomme"),
						tuple("r2", "Pêche molle"),
						tuple("r3", "Poire"),
						tuple("r4", "Quick aux fraises"),
						tuple("r5", "perdrix"),
						tuple("r6", "Arachide"));

		assertThatRecords(searchServices.search(query)).preferring(Locale.ENGLISH).extractingMetadatas("id", "stringMetadata")
				.containsOnly(
						tuple("r1", "Apple"),
						tuple("r2", "Peach"),
						tuple("r3", "pear"),
						tuple("r4", "Strawberry quick"),
						tuple("r5", "Partridge"),
						tuple("r6", "peanut"));
	}

	@Test
	public void givenSearchableMultilingualStringMetadataThenSearchBasedOnLanguage()
			throws Exception {

		givenFrenchSystemWithOneMonolingualAndOneMultilingualCollection();
		defineSchemasManager().using(multilingualCollectionSchemas
				.withAStringMetadata(whichIsSearchable, whichIsMultilingual)
				.withAnotherStringMetadata(whichIsSearchable));
		setupServices();
		Transaction tx = new Transaction();

		tx.add(new TestRecord(multilingualSchema, "r1")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "pomme")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "Apple")
				.set(multilingualSchema.anotherStringMetadata(), "Fruit"));

		tx.add(new TestRecord(multilingualSchema, "r2")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "Pêche molle")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "Peach")
				.set(multilingualSchema.anotherStringMetadata(), "Fruit"));

		tx.add(new TestRecord(multilingualSchema, "r3")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "Poire")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "pear")
				.set(multilingualSchema.anotherStringMetadata(), "Fruit"));

		tx.add(new TestRecord(multilingualSchema, "r4")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "Quick aux fraises")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "Strawberry quick")
				.set(multilingualSchema.anotherStringMetadata(), "Fruit"));

		tx.add(new TestRecord(multilingualSchema, "r5")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "perdrix")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "Partridge")
				.set(multilingualSchema.anotherStringMetadata(), "Oiseau"));

		tx.add(new TestRecord(multilingualSchema, "r6")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, "Arachide")
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, "peanut")
				.set(multilingualSchema.anotherStringMetadata(), "Autre"));

		recordServices.execute(tx);

		assertThatSearch(Locale.FRENCH, "fruit").containsOnly("r1", "r2", "r3", "r4");
		assertThatSearch(Locale.ENGLISH, "fruit").containsOnly("r1", "r2", "r3", "r4");

		assertThatSearch(Locale.FRENCH, "fraise").containsOnly("r4");
		assertThatSearch(Locale.FRENCH, "fraisé").containsOnly("r4");
		assertThatSearch(Locale.ENGLISH, "fraise").isEmpty();

		assertThatSearch(Locale.FRENCH, "strawberry").isEmpty();
		assertThatSearch(Locale.ENGLISH, "strawberry").containsOnly("r4");
		assertThatSearch(Locale.ENGLISH, "strawbèrry").isEmpty();
		assertThatSearch(Locale.ENGLISH, "strawberries").containsOnly("r4");

	}

	@Test
	public void givenSearchableMultilingualMultivalueStringMetadataThenSearchBasedOnLanguage()
			throws Exception {

		givenFrenchSystemWithOneMonolingualAndOneMultilingualCollection();
		defineSchemasManager().using(multilingualCollectionSchemas
				.withAStringMetadata(whichIsSearchable, whichIsMultilingual, whichIsMultivalue)
				.withAnotherStringMetadata(whichIsSearchable, whichIsMultivalue));
		setupServices();
		Transaction tx = new Transaction();

		tx.add(new TestRecord(multilingualSchema, "r1")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, asList("Cortland", "pomme"))
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, asList("Cortland", "Apple"))
				.set(multilingualSchema.anotherStringMetadata(), asList("Fruit")));

		tx.add(new TestRecord(multilingualSchema, "r2")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, asList("Pêche", "molle"))
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, asList("Peach"))
				.set(multilingualSchema.anotherStringMetadata(), asList("Fruit")));

		tx.add(new TestRecord(multilingualSchema, "r3")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, asList("Poire"))
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, asList("pear"))
				.set(multilingualSchema.anotherStringMetadata(), asList("Fruit")));

		tx.add(new TestRecord(multilingualSchema, "r4")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, asList("Quick aux fraises"))
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, asList("Strawberry quick"))
				.set(multilingualSchema.anotherStringMetadata(), asList("Fruit")));

		tx.add(new TestRecord(multilingualSchema, "r5")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, asList("perdrix"))
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, asList("Partridge"))
				.set(multilingualSchema.anotherStringMetadata(), asList("Oiseau")));

		tx.add(new TestRecord(multilingualSchema, "r6")
				.set(multilingualSchema.stringMetadata(), Locale.FRENCH, asList("Arachide"))
				.set(multilingualSchema.stringMetadata(), Locale.ENGLISH, asList("peanut"))
				.set(multilingualSchema.anotherStringMetadata(), asList("Autre")));

		recordServices.execute(tx);

		assertThatSearch(Locale.FRENCH, "fruit").containsOnly("r1", "r2", "r3", "r4");
		assertThatSearch(Locale.ENGLISH, "fruit").containsOnly("r1", "r2", "r3", "r4");

		assertThatSearch(Locale.FRENCH, "fraise").containsOnly("r4");
		assertThatSearch(Locale.FRENCH, "fraisé").containsOnly("r4");
		assertThatSearch(Locale.ENGLISH, "fraise").isEmpty();

		assertThatSearch(Locale.FRENCH, "strawberry").isEmpty();
		assertThatSearch(Locale.ENGLISH, "strawberry").containsOnly("r4");
		assertThatSearch(Locale.ENGLISH, "strawbèrry").isEmpty();
		assertThatSearch(Locale.ENGLISH, "strawberries").containsOnly("r4");

	}

	@Test
	public void givenMetadataSometimeHasMultilingualAndSometimeNotThenAlwaysIncludeDataStoreLanguage1()
			throws Exception {

		givenFrenchSystemWithOneMonolingualAndOneMultilingualCollection();
		defineSchemasManager().using(multilingualCollectionSchemas
				.with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						schemaTypes.getSchema("zeSchemaType_default").get("title").setMultiLingual(true).setSearchable(true);
						schemaTypes.getSchema("anotherSchemaType_default").get("title").setSearchable(true);
					}
				}));
		setupServices();

		Transaction tx = new Transaction();

		tx.add(new TestRecord(multilingualSchema, "r1")
				.set(Schemas.TITLE, Locale.FRENCH, "Pomme")
				.set(Schemas.TITLE, Locale.ENGLISH, "Apple"));

		tx.add(new TestRecord(multilingualSchema, "r2")
				.set(Schemas.TITLE, Locale.FRENCH, "Pêche")
				.set(Schemas.TITLE, Locale.ENGLISH, "Pear"));

		tx.add(new TestRecord(multilingualSchema, "r3")
				.set(Schemas.TITLE, Locale.FRENCH, "Fraise")
				.set(Schemas.TITLE, Locale.ENGLISH, "Strawberry"));

		tx.add(new TestRecord(multilingualSchema, "r4")
				.set(Schemas.TITLE, Locale.FRENCH, "Banane")
				.set(Schemas.TITLE, Locale.ENGLISH, "Banana"));

		tx.add(new TestRecord(referencingMultilingualSchema, "r5")
				.set(Schemas.TITLE, "Pomme de terre"));

		tx.add(new TestRecord(referencingMultilingualSchema, "r6")
				.set(Schemas.TITLE, "Apple computer"));

		recordServices.execute(tx);

		assertThatEveryTypesSearch(Locale.FRENCH, "pomme").containsOnly("r1", "r5");
		assertThatEveryTypesSearch(Locale.ENGLISH, "pomme").containsOnly("r1", "r5");
		assertThatEveryTypesSearch(Locale.FRENCH, "fraise").containsOnly("r3");
		assertThatEveryTypesSearch(Locale.ENGLISH, "fraise").containsOnly("r3");
		assertThatEveryTypesSearch(Locale.FRENCH, "apple").containsOnly("r6");
		assertThatEveryTypesSearch(Locale.ENGLISH, "apple").containsOnly("r1", "r6");
		assertThatEveryTypesSearch(Locale.FRENCH, "pear").isEmpty();
		assertThatEveryTypesSearch(Locale.ENGLISH, "pear").containsOnly("r2");

		assertThatTypeSearch(multilingualSchema.type(), Locale.FRENCH, "pomme").containsOnly("r1");
		assertThatTypeSearch(multilingualSchema.type(), Locale.ENGLISH, "pomme").isEmpty();
		assertThatTypeSearch(multilingualSchema.type(), Locale.FRENCH, "fraise").containsOnly("r3");
		assertThatTypeSearch(multilingualSchema.type(), Locale.ENGLISH, "fraise").isEmpty();
		assertThatTypeSearch(multilingualSchema.type(), Locale.FRENCH, "apple").isEmpty();
		assertThatTypeSearch(multilingualSchema.type(), Locale.ENGLISH, "apple").containsOnly("r1");
		assertThatTypeSearch(multilingualSchema.type(), Locale.FRENCH, "pear").isEmpty();
		assertThatTypeSearch(multilingualSchema.type(), Locale.ENGLISH, "pear").containsOnly("r2");

		assertThatTypeSearch(referencingMultilingualSchema.type(), Locale.FRENCH, "pomme").containsOnly("r5");
		assertThatTypeSearch(referencingMultilingualSchema.type(), Locale.ENGLISH, "pomme").containsOnly("r5");
		assertThatTypeSearch(referencingMultilingualSchema.type(), Locale.FRENCH, "apple").containsOnly("r6");
		assertThatTypeSearch(referencingMultilingualSchema.type(), Locale.ENGLISH, "apple").containsOnly("r6");

	}

	@Test
	public void givenMetadataSometimeHasMultilingualAndSometimeNotThenAlwaysIncludeDataStoreLanguage2()
			throws Exception {

		givenFrenchSystemWithOneMonolingualAndOneMultilingualCollection();
		defineSchemasManager().using(multilingualCollectionSchemas
				.with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						schemaTypes.getSchema("anotherSchemaType_default").get("title").setMultiLingual(true).setSearchable(true);
						schemaTypes.getSchema("zeSchemaType_default").get("title").setSearchable(true);
					}
				}));
		setupServices();
		Transaction tx = new Transaction();

		tx.add(new TestRecord(referencingMultilingualSchema, "r1")
				.set(Schemas.TITLE, Locale.FRENCH, "Pomme")
				.set(Schemas.TITLE, Locale.ENGLISH, "Apple"));

		tx.add(new TestRecord(referencingMultilingualSchema, "r2")
				.set(Schemas.TITLE, Locale.FRENCH, "Pêche")
				.set(Schemas.TITLE, Locale.ENGLISH, "Pear"));

		tx.add(new TestRecord(referencingMultilingualSchema, "r3")
				.set(Schemas.TITLE, Locale.FRENCH, "Fraise")
				.set(Schemas.TITLE, Locale.ENGLISH, "Strawberry"));

		tx.add(new TestRecord(referencingMultilingualSchema, "r4")
				.set(Schemas.TITLE, Locale.FRENCH, "Banane")
				.set(Schemas.TITLE, Locale.ENGLISH, "Banana"));

		tx.add(new TestRecord(multilingualSchema, "r5")
				.set(Schemas.TITLE, "Pomme de terre"));

		tx.add(new TestRecord(multilingualSchema, "r6")
				.set(Schemas.TITLE, "Apple computer"));

		recordServices.execute(tx);

		assertThatEveryTypesSearch(Locale.FRENCH, "pomme").containsOnly("r1", "r5");
		assertThatEveryTypesSearch(Locale.ENGLISH, "pomme").containsOnly("r1", "r5");
		assertThatEveryTypesSearch(Locale.FRENCH, "fraise").containsOnly("r3");
		assertThatEveryTypesSearch(Locale.ENGLISH, "fraise").containsOnly("r3");
		assertThatEveryTypesSearch(Locale.FRENCH, "apple").containsOnly("r6");
		assertThatEveryTypesSearch(Locale.ENGLISH, "apple").containsOnly("r1", "r6");
		assertThatEveryTypesSearch(Locale.FRENCH, "pear").isEmpty();
		assertThatEveryTypesSearch(Locale.ENGLISH, "pear").containsOnly("r2");

		assertThatTypeSearch(referencingMultilingualSchema.type(), Locale.FRENCH, "pomme").containsOnly("r1");
		assertThatTypeSearch(referencingMultilingualSchema.type(), Locale.ENGLISH, "pomme").isEmpty();
		assertThatTypeSearch(referencingMultilingualSchema.type(), Locale.FRENCH, "fraise").containsOnly("r3");
		assertThatTypeSearch(referencingMultilingualSchema.type(), Locale.ENGLISH, "fraise").isEmpty();
		assertThatTypeSearch(referencingMultilingualSchema.type(), Locale.FRENCH, "apple").isEmpty();
		assertThatTypeSearch(referencingMultilingualSchema.type(), Locale.ENGLISH, "apple").containsOnly("r1");
		assertThatTypeSearch(referencingMultilingualSchema.type(), Locale.FRENCH, "pear").isEmpty();
		assertThatTypeSearch(referencingMultilingualSchema.type(), Locale.ENGLISH, "pear").containsOnly("r2");

		assertThatTypeSearch(multilingualSchema.type(), Locale.FRENCH, "pomme").containsOnly("r5");
		assertThatTypeSearch(multilingualSchema.type(), Locale.ENGLISH, "pomme").containsOnly("r5");
		assertThatTypeSearch(multilingualSchema.type(), Locale.FRENCH, "apple").containsOnly("r6");
		assertThatTypeSearch(multilingualSchema.type(), Locale.ENGLISH, "apple").containsOnly("r6");

	}


	@Test
	public void givenContentMetadataThenTextIsSearchableNoMatterTheLanguage()
			throws Exception {

		givenEnglishSystemWithOneMonolingualAndOneTrilingualCollection();
		defineSchemasManager().using(multilingualCollectionSchemas
				.with(new MetadataSchemaTypesConfigurator() {
					@Override
					public void configure(MetadataSchemaTypesBuilder schemaTypes) {
						schemaTypes.getSchema("zeSchemaType_default").create("contentMetadata").setType(CONTENT).setSearchable(true);
					}
				}));
		setupServices();


		Transaction tx = new Transaction();


		tx.add(new TestRecord(multilingualSchema, "r1")
				.set(multilingualSchema.contentMetadata(), toUploadedContent("englishText.txt")));

		tx.add(new TestRecord(multilingualSchema, "r2")
				.set(multilingualSchema.contentMetadata(), toUploadedContent("frenchText.txt")));

		tx.add(new TestRecord(multilingualSchema, "r3")
				.set(multilingualSchema.contentMetadata(), toUploadedContent("arabicText.txt")));

		tx.add(new TestRecord(multilingualSchema, "r4")
				.set(multilingualSchema.contentMetadata(), toUploadedContent("portugueseText.txt")));


		recordServices.execute(tx);


		//Content contains the word secret, but not secrets
		assertThatEveryTypesSearch(Locale.FRENCH, "secret").containsOnly("r1");
		assertThatEveryTypesSearch(Locale.FRENCH, "secrets").containsOnly("r1");
		assertThatEveryTypesSearch(Locale.ENGLISH, "secret").containsOnly("r1");
		assertThatEveryTypesSearch(Locale.ENGLISH, "secrets").containsOnly("r1");
		assertThatEveryTypesSearch(ARABIC, "secret").containsOnly("r1");
		assertThatEveryTypesSearch(ARABIC, "secrets").containsOnly("r1");

		//Content contains the word voisinage, but not voisin
		assertThatEveryTypesSearch(Locale.FRENCH, "voisinage").containsOnly("r2");
		assertThatEveryTypesSearch(Locale.FRENCH, "voisinages").containsOnly("r2");
		assertThatEveryTypesSearch(Locale.ENGLISH, "voisinage").containsOnly("r2");
		assertThatEveryTypesSearch(Locale.ENGLISH, "voisinages").containsOnly("r2");
		assertThatEveryTypesSearch(ARABIC, "voisinage").containsOnly("r2");
		assertThatEveryTypesSearch(ARABIC, "voisinages").containsOnly("r2");

		assertThatEveryTypesSearch(Locale.FRENCH, "سامع").containsOnly("r3");
		assertThatEveryTypesSearch(Locale.FRENCH, "كاتب").containsOnly("r3");
		assertThatEveryTypesSearch(Locale.ENGLISH, "سامع").containsOnly("r3");
		assertThatEveryTypesSearch(Locale.ENGLISH, "كاتب").containsOnly("r3");
		assertThatEveryTypesSearch(ARABIC, "سامع").containsOnly("r3");
		assertThatEveryTypesSearch(ARABIC, "كاتب").containsOnly("r3");

		assertThatEveryTypesSearch(Locale.FRENCH, "importante").containsOnly("r4");
		assertThatEveryTypesSearch(Locale.FRENCH, "importantes").containsOnly("r4");
		assertThatEveryTypesSearch(Locale.ENGLISH, "importante").containsOnly("r4");
		assertThatEveryTypesSearch(Locale.ENGLISH, "importantes").containsOnly("r4");
		assertThatEveryTypesSearch(ARABIC, "importante").containsOnly("r4");
		assertThatEveryTypesSearch(ARABIC, "importantes").containsOnly("r4");


	}

	private Content toUploadedContent(String filename) {
		ContentManager contentManager = getModelLayerFactory().getContentManager();
		try {
			return contentManager.createSystemContent(filename, contentManager.upload(getTestResourceFile(filename)));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private org.assertj.core.api.ListAssert<String> assertThatAutoCompleteSearch(Locale locale, String text) {
		MetadataSchemaType type = getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes("multilingual").getSchemaType(multilingualSchema.type().getCode());
		LogicalSearchQuery query = new LogicalSearchQuery().setCondition(from(type).where(autocompleteFieldMatching(text)));
		query.setLanguage(locale);
		return assertThat(searchServices.searchRecordIds(query));
	}

	private org.assertj.core.api.ListAssert<String> assertThatSearch(Locale locale, String text) {
		MetadataSchemaType type = getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes("multilingual").getSchemaType(multilingualSchema.type().getCode());
		LogicalSearchQuery query = new LogicalSearchQuery().setCondition(from(type).returnAll());
		query.setFreeTextQuery(text);
		query.setLanguage(locale);
		return assertThat(searchServices.searchRecordIds(query));
	}

	private org.assertj.core.api.ListAssert<String> assertThatEveryTypesSearch(Locale locale, String text) {
		LogicalSearchQuery query = new LogicalSearchQuery().setCondition(fromAllSchemasIn("multilingual").returnAll());
		query.setFreeTextQuery(text);
		query.setLanguage(locale);
		return assertThat(searchServices.searchRecordIds(query));
	}

	private org.assertj.core.api.ListAssert<String> assertThatTypeSearch(MetadataSchemaType type, Locale locale,
																		 String text) {
		LogicalSearchQuery query = new LogicalSearchQuery().setCondition(from(type).returnAll());
		query.setFreeTextQuery(text);
		query.setLanguage(locale);
		return assertThat(searchServices.searchRecordIds(query));
	}

	private org.assertj.core.api.ListAssert<String> assertThatAutoCompleteFruitSearch(Locale locale, String text) {
		MetadataSchemaType type = getModelLayerFactory().getMetadataSchemasManager()
				.getSchemaTypes("multilingual").getSchemaType(multilingualSchema.type().getCode());
		LogicalSearchQuery query = new LogicalSearchQuery().setCondition(from(type).where(autocompleteFieldMatching(text))
				.andWhere(multilingualSchema.anotherStringMetadata()).isEqualTo("Fruit"));
		query.setLanguage(locale);
		return assertThat(searchServices.searchRecordIds(query));
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


	protected void givenFrenchSystemWithOneMonolingualAndOneMultilingualCollection() {

		monolingualCollectionSchemas = new RecordServicesTestSchemaSetup("monolingual", asList("fr"));
		monolingualSchema = monolingualCollectionSchemas.new ZeSchemaMetadatas();
		multilingualCollectionSchemas = new RecordServicesTestSchemaSetup("multilingual", asList("fr", "en"));
		multilingualSchema = multilingualCollectionSchemas.new ZeSchemaMetadatas();
		referencingMultilingualSchema = multilingualCollectionSchemas.new AnotherSchemaMetadatas();

		givenSystemLanguageIs("fr");
		givenCollection("monolingual", asList("fr")).withAllTestUsers();
		givenCollection("multilingual", asList("fr", "en")).withAllTestUsers();

	}

	protected void givenFrenchSystemWithOneMonolingualAndOneMultilingualCollectionAndSchemas(
			final SetupAlteration... setupAlterations) {

		givenFrenchSystemWithOneMonolingualAndOneMultilingualCollection();

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

	protected void givenEnglishSystemWithOneMonolingualAndOneTrilingualCollection() {

		monolingualCollectionSchemas = new RecordServicesTestSchemaSetup("monolingual", asList("en"));
		monolingualSchema = monolingualCollectionSchemas.new ZeSchemaMetadatas();
		multilingualCollectionSchemas = new RecordServicesTestSchemaSetup("multilingual", asList("fr", "en", "ar"));
		multilingualSchema = multilingualCollectionSchemas.new ZeSchemaMetadatas();
		referencingMultilingualSchema = multilingualCollectionSchemas.new AnotherSchemaMetadatas();

		givenSystemLanguageIs("en");
		givenCollection("monolingual", asList("en")).withAllTestUsers();
		givenCollection("multilingual", asList("fr", "en", "ar")).withAllTestUsers();

	}

	protected void givenEnglishSystemWithOneMonolingualAndOneTrilingualCollectionAndSchemas(
			final SetupAlteration... setupAlterations) {

		givenEnglishSystemWithOneMonolingualAndOneTrilingualCollection();
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
