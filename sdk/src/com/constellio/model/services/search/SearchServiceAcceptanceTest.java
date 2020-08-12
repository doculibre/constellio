package com.constellio.model.services.search;

import com.constellio.app.modules.tasks.model.wrappers.Task;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.dao.dto.records.OptimisticLockingResolution;
import com.constellio.data.dao.services.bigVault.solr.BigVaultRuntimeException.BadRequest;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.calculators.AbstractMetadataValueCalculator;
import com.constellio.model.entities.calculators.CalculatorParameters;
import com.constellio.model.entities.calculators.dependencies.Dependency;
import com.constellio.model.entities.calculators.dependencies.LocalDependency;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataAccessRestriction;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.RecordCacheType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesException;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.builders.MetadataAccessRestrictionBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataBuilder_EnumClassTest.AValidEnum;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.search.entities.SearchBoost;
import com.constellio.model.services.search.moreLikeThis.MoreLikeThisClustering;
import com.constellio.model.services.search.query.ReturnedMetadatasFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.QueryExecutionMethod;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.criteria.MeasuringUnitTime;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchConditionWithDataStoreFields;
import com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.TestUtils;
import com.constellio.sdk.tests.annotations.SlowTest;
import com.constellio.sdk.tests.schemas.MetadataBuilderConfigurator;
import com.constellio.sdk.tests.schemas.MetadataSchemaTypesConfigurator;
import com.constellio.sdk.tests.setups.Users;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import static com.constellio.data.dao.services.records.DataStore.EVENTS;
import static com.constellio.data.dao.services.records.DataStore.RECORDS;
import static com.constellio.model.entities.schemas.MetadataTransiency.TRANSIENT_EAGER;
import static com.constellio.model.entities.schemas.MetadataTransiency.TRANSIENT_LAZY;
import static com.constellio.model.entities.schemas.Schemas.TITLE;
import static com.constellio.model.services.search.VisibilityStatusFilter.ALL;
import static com.constellio.model.services.search.VisibilityStatusFilter.HIDDENS;
import static com.constellio.model.services.search.VisibilityStatusFilter.VISIBLES;
import static com.constellio.model.services.search.entities.SearchBoost.createRegexBoost;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.allConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.anyConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.containingText;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromEveryTypesOfEveryCollection;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromEveryTypesOfEveryCollectionInDataStore;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.startingWithText;
import static com.constellio.sdk.tests.TestUtils.ids;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.ANOTHER_SCHEMA_TYPE_CODE;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZE_SCHEMA_TYPE_CODE;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichHasTransiency;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsCalculatedUsing;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsEncrypted;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@SlowTest
public class SearchServiceAcceptanceTest extends ConstellioTest {
	private static final LocalDateTime DATE_TIME4 = new LocalDateTime(2003, 7, 15, 22, 40);
	private static final LocalDateTime DATE_TIME3 = new LocalDateTime(2002, 8, 15, 22, 40);
	private static final LocalDateTime DATE_TIME2 = new LocalDateTime(2001, 9, 15, 22, 40);
	private static final LocalDateTime DATE_TIME1 = new LocalDateTime(2000, 10, 15, 22, 40);

	private static final LocalDate DATE4 = new LocalDate(2003, 7, 15);
	private static final LocalDate DATE3 = new LocalDate(2002, 8, 15);
	private static final LocalDate DATE2 = new LocalDate(2001, 9, 15);
	private static final LocalDate DATE1 = new LocalDate(2000, 10, 15);
	LocalDateTime NOW = TimeProvider.getLocalDateTime();

	RecordServices recordServices;
	SearchServices searchServices;
	RecordDao recordDao;

	SearchServiceAcceptanceTestSchemas schema = new SearchServiceAcceptanceTestSchemas(zeCollection);
	SearchServiceAcceptanceTestSchemas.ZeSchemaMetadatas zeSchema = schema.new ZeSchemaMetadatas();
	SearchServiceAcceptanceTestSchemas.ZeCustomSchemaMetadatas zeCustomSchema = schema.new ZeCustomSchemaMetadatas();
	SearchServiceAcceptanceTestSchemas.AnotherSchemaMetadatas anotherSchema = schema.new AnotherSchemaMetadatas();
	SearchServiceAcceptanceTestSchemas otherSchema = new SearchServiceAcceptanceTestSchemas("collection2");
	SearchServiceAcceptanceTestSchemas.OtherSchemaMetadatasInCollection2 otherSchemaInCollection2 = otherSchema.new OtherSchemaMetadatasInCollection2();
	Users users = new Users();
	LogicalSearchCondition condition;

	ConditionTemplateFactory factory;

	Transaction transaction;

	Record expectedRecord, expectedRecord2, expectedRecord3, expectedRecord4, expectedRecord5;
	Record zeSchemaRecord1, zeSchemaRecord2, zeSchemaRecord3, zeSchemaRecord4, anotherSchemaRecord1, anotherSchemaRecord2,
			otherSchemaRecord1InCollection2, otherSchemaRecord2InCollection2, record1, record2, record3, record4, record5;

	@Before
	public void setUp() {

		prepareSystem(
				withZeCollection().withAllTestUsers().withTasksModule(),
				withCollection("collection2")
		);
		//givenCollection(zeCollection, Arrays.asList(Language.French.getCode(), Language.English.getCode()));
		recordServices = getModelLayerFactory().newRecordServices();
		recordDao = spy(getDataLayerFactory().newRecordDao());
		searchServices = new SearchServices(recordDao, getModelLayerFactory());

		transaction = new Transaction();
		factory = new ConditionTemplateFactory(getModelLayerFactory(), zeCollection);

		UserServices userServices = getModelLayerFactory().newUserServices();
		users.setUp(userServices);
	}

	@Test
	public void givenAListOfDocumentsWhenModifyingOneOfThemAndSearchItThenTheOldVersionIsReturned()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata(whichIsSearchable));

		Record documentA, documentB, documentC, documentCNewVersion;

		transaction.addUpdate(documentA = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "This is a document A"));
		transaction.addUpdate(documentB = newRecordOfZeSchema()
				.set(zeSchema.stringMetadata(), "This is an another document without misspelling"));
		transaction.addUpdate(documentC = newRecordOfZeSchema()
				.set(zeSchema.stringMetadata(), "Document with misspelling: this is a frist version of this document."));
		transaction.addUpdate(documentCNewVersion = newRecordOfZeSchema()
				.set(zeSchema.stringMetadata(), "Document without misspelling: this is a second version of this document."));

		recordServices.execute(transaction);

		//when
		LogicalSearchQuery query = new LogicalSearchQuery(fromAllSchemasIn(zeCollection).returnAll());
		query.setMoreLikeThisRecordId(documentCNewVersion.getId()).addMoreLikeThisField(zeSchema.stringMetadata());

		assertThat(searchServices.searchWithMoreLikeThis(query)).extracting("record.id", "identical").containsOnly(
				tuple(documentA.getId(), false),
				tuple(documentB.getId(), false),
				tuple(documentC.getId(), false));

	}

	private Random random = new Random();

	public String stringWithRandom(String[] words) {

		final int WORD_DOC_CNT = 10;
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < WORD_DOC_CNT; i++) {
			if (sb.length() != 0) {
				sb.append(" ");
			}
			sb.append(words[random.nextInt(words.length)]);
		}
		return sb.toString();
	}

	@Test
	public void givenTwoTopicsWhenSearchingForADocumentThenItsTopicIsAutomaticallyIdentifiedFromSearchResult()
			throws Exception {
		//given
		defineSchemasManager().using(schema.withAStringMetadata(whichIsSearchable).withAnotherStringMetadata());
		String[] politicsWords = new String[]{"party", "democrat", "president", "election", "vote"};
		String[] sportWords = new String[]{"hockey", "team", "game", "play", "league"};

		for (int i = 0; i < 10; i++) {
			transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata()
					, stringWithRandom(politicsWords)).set(zeSchema.anotherStringMetadata(), "POLITICS"));
		}

		for (int i = 0; i < 10; i++) {
			transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata()
					, stringWithRandom(sportWords)).set(zeSchema.anotherStringMetadata(), "SPORT"));
		}

		transaction.add(newRecordOfZeSchema("newSportDoc").set(zeSchema.stringMetadata(), stringWithRandom(sportWords)));
		transaction.add(newRecordOfZeSchema("newPoliticsDoc").set(zeSchema.stringMetadata(), stringWithRandom(politicsWords)));
		recordServices.execute(transaction);

		//when

		LogicalSearchQuery query = new LogicalSearchQuery(fromAllSchemasIn(zeCollection).returnAll());
		query.setMoreLikeThisRecordId("newSportDoc").addMoreLikeThisField(zeSchema.stringMetadata());

		MoreLikeThisClustering facet = new MoreLikeThisClustering(searchServices.searchWithMoreLikeThis(query),
				new MoreLikeThisClustering.StringConverter<Record>() {

					@Override
					public String converToString(Record record) {
						return record.get(zeSchema.anotherStringMetadata());
					}
				});

		assertThat(facet.getClusterScore().entrySet().iterator().next().getKey()).isEqualTo("SPORT");

		query = new LogicalSearchQuery(fromAllSchemasIn(zeCollection).returnAll());
		query.setMoreLikeThisRecordId("newPoliticsDoc").addMoreLikeThisField(zeSchema.stringMetadata());

		facet = new MoreLikeThisClustering(searchServices.searchWithMoreLikeThis(query),
				new MoreLikeThisClustering.StringConverter<Record>() {

					@Override
					public String converToString(Record record) {
						return record.get(zeSchema.anotherStringMetadata());
					}
				});

		assertThat(facet.getClusterScore().entrySet().iterator().next().getKey()).isEqualTo("POLITICS");
	}

	@Test
	public void whenSearchingUsingIdsInFreeTextTheFindRecordWithOrWithoutPaddingZeros()
			throws Exception {
		defineSchemasManager().using(schema);

		transaction.addUpdate(recordServices.newRecordWithSchema(zeSchema.instance(), "00000001042"));
		transaction.addUpdate(recordServices.newRecordWithSchema(zeSchema.instance(), "00000001043"));
		transaction.addUpdate(recordServices.newRecordWithSchema(zeSchema.instance(), "00000011043"));
		recordServices.execute(transaction);

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(zeSchema.type()).returnAll());

		query.setFreeTextQuery("00000001042");
		assertThat(searchServices.searchRecordIds(query)).containsOnly("00000001042");

		query.setFreeTextQuery("00000001043");
		assertThat(searchServices.searchRecordIds(query)).containsOnly("00000001043");

		query.setFreeTextQuery("00000011043");
		assertThat(searchServices.searchRecordIds(query)).containsOnly("00000011043");

		query.setFreeTextQuery("1042");
		assertThat(searchServices.searchRecordIds(query)).containsOnly("00000001042");

		query.setFreeTextQuery("1043");
		assertThat(searchServices.searchRecordIds(query)).containsOnly("00000001043");

		query.setFreeTextQuery("11043");
		assertThat(searchServices.searchRecordIds(query)).containsOnly("00000011043");

	}

	@Test
	public void whenIteratingInAscendingOrDescendingOrderThenOK()
			throws Exception {

		defineSchemasManager().using(schema.withAStringMetadata().withABooleanMetadata());

		Transaction tx = new Transaction();
		for (int i = 10; i < 42; i++) {
			tx.add(recordServices.newRecordWithSchema(zeSchema.instance(), "record" + i)
					.set(zeSchema.stringMetadata(), "V" + i));
		}

		recordServices.execute(tx);

		List<String> stringMetadatasOfRecordsWhenIteratingAsc = new ArrayList<>();
		Iterator<Record> recordIterator = searchServices.recordsIterator(new LogicalSearchQuery(from(zeSchema.type()).returnAll())
				.setQueryExecutionMethod(QueryExecutionMethod.USE_SOLR), 5);
		while (recordIterator.hasNext()) {
			Record record = recordIterator.next();
			stringMetadatasOfRecordsWhenIteratingAsc.add(record.<String>get(zeSchema.stringMetadata()));
		}
		assertThat(stringMetadatasOfRecordsWhenIteratingAsc).isEqualTo(
				asList("V10", "V11", "V12", "V13", "V14", "V15", "V16", "V17", "V18", "V19", "V20", "V21", "V22", "V23", "V24",
						"V25", "V26", "V27", "V28", "V29", "V30", "V31", "V32", "V33", "V34", "V35", "V36", "V37", "V38", "V39",
						"V40", "V41"));

		List<String> stringMetadatasOfRecordsWhenIteratingDesc = new ArrayList<>();
		recordIterator = searchServices.reverseRecordsIterator(new LogicalSearchQuery(from(zeSchema.type()).returnAll())
				.setQueryExecutionMethod(QueryExecutionMethod.USE_SOLR), 5);
		while (recordIterator.hasNext()) {
			Record record = recordIterator.next();
			stringMetadatasOfRecordsWhenIteratingDesc.add(record.<String>get(zeSchema.stringMetadata()));
		}
		assertThat(stringMetadatasOfRecordsWhenIteratingDesc).isEqualTo(
				asList("V41", "V40", "V39", "V38", "V37", "V36", "V35", "V34", "V33", "V32", "V31", "V30", "V29", "V28", "V27",
						"V26", "V25", "V24", "V23", "V22", "V21", "V20", "V19", "V18", "V17", "V16", "V15", "V14", "V13", "V12",
						"V11", "V10"));
	}

	@Test
	public void whenSearchingRecordsReturningWithFullValueContainingSpacesAsteriskAndQuestionMarkThenFindResult()
			throws Exception {

		defineSchemasManager().using(schema.withAStringMetadata().withABooleanMetadata());
		Record withSpace, withSlash, withBackSlash, withSemicolon, withQuestionMark;

		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck \\\"\\?Norris*:"));
		transaction.addUpdate(withSpace = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Norris"));
		transaction.addUpdate(withSlash = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck/Norris"));
		transaction.addUpdate(withBackSlash = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck\\Norris"));
		transaction.addUpdate(withSemicolon = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck:Norris"));
		transaction.addUpdate(withQuestionMark = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck?Norris"));

		recordServices.execute(transaction);

		OngoingLogicalSearchConditionWithDataStoreFields whereMetadata = from(zeSchema.instance())
				.where(zeSchema.stringMetadata());

		assertThat(findRecords(whereMetadata.isEqualTo("Chuck Norris"))).containsOnly(withSpace);
		assertThat(findRecords(whereMetadata.isEqualTo("Chuck/Norris"))).containsOnly(withSlash);
		assertThat(findRecords(whereMetadata.isEqualTo("Chuck\\Norris"))).containsOnly(withBackSlash);
		assertThat(findRecords(whereMetadata.isEqualTo("Chuck:Norris"))).containsOnly(withSemicolon);
		assertThat(findRecords(whereMetadata.isEqualTo("Chuck?Norris"))).containsOnly(withQuestionMark);

		assertThat(findRecords(whereMetadata.isIn(asList("Chuck Norris")))).containsOnly(withSpace);
		assertThat(findRecords(whereMetadata.isIn(asList("Chuck/Norris")))).containsOnly(withSlash);
		assertThat(findRecords(whereMetadata.isIn(asList("Chuck\\Norris")))).containsOnly(withBackSlash);
		assertThat(findRecords(whereMetadata.isIn(asList("Chuck:Norris")))).containsOnly(withSemicolon);
		assertThat(findRecords(whereMetadata.isIn(asList("Chuck?Norris")))).containsOnly(withQuestionMark);

	}

	@Test
	public void whenSearchingRecordsReturningWithFullValueContainingSpecialCharactersThenFindResult()
			throws Exception {

		defineSchemasManager().using(schema.withAStringMetadata().withABooleanMetadata());
		Record special;

		transaction.addUpdate(
				newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Norris + - && || ! ( ) { } [ ] ^ \" ~ * ? : \\"));
		transaction.addUpdate(
				special = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "+ - && || ! ( ) { } [ ] ^ \" ~ * ? : \\"));

		recordServices.execute(transaction);

		OngoingLogicalSearchConditionWithDataStoreFields whereMetadata = from(zeSchema.instance())
				.where(zeSchema.stringMetadata());

		assertThat(findRecords(whereMetadata.isEqualTo("+ - && || ! ( ) { } [ ] ^ \" ~ * ? : \\"))).containsOnly(special);

		assertThat(findRecords(whereMetadata.isIn(asList("+ - && || ! ( ) { } [ ] ^ \" ~ * ? : \\")))).containsOnly(special);

	}

	@Test
	public void whenSearchingByEnumValueThenFindRecords()
			throws Exception {

		defineSchemasManager().using(schema.withAnEnumMetadata(AValidEnum.class));

		transaction.addUpdate(record1 = givenARecord("record1").set(zeSchema.enumMetadata(), AValidEnum.FIRST_VALUE));
		transaction.addUpdate(record2 = givenARecord("record2").set(zeSchema.enumMetadata(), AValidEnum.SECOND_VALUE));
		transaction.addUpdate(record3 = givenARecord("record3").set(zeSchema.enumMetadata(), AValidEnum.FIRST_VALUE));
		transaction.addUpdate(record4 = givenARecord("record4").set(zeSchema.enumMetadata(), AValidEnum.FIRST_VALUE));
		recordServices.execute(transaction);

		OngoingLogicalSearchConditionWithDataStoreFields whereMetadata = from(zeSchema.instance())
				.where(zeSchema.enumMetadata());

		assertThat(findRecords(whereMetadata.isEqualTo(AValidEnum.FIRST_VALUE))).containsOnly(record1, record3, record4);
		assertThat(findRecords(whereMetadata.isEqualTo(AValidEnum.SECOND_VALUE))).containsOnly(record2);
		assertThat(findRecords(whereMetadata.isNotNull())).containsOnly(record1, record2, record3, record4);
		assertThat(findRecords(whereMetadata.isNull())).isEmpty();
		assertThat(findRecords(whereMetadata.isIn(asList(AValidEnum.SECOND_VALUE)))).containsOnly(record2);
		assertThat(findRecords(whereMetadata.isIn(asList(AValidEnum.FIRST_VALUE, AValidEnum.SECOND_VALUE)))).containsOnly(
				record1, record2, record3, record4);
		assertThat(findRecords(whereMetadata.isIn(new ArrayList<>()))).isEmpty();
		assertThat(findRecords(whereMetadata.isNotIn(asList(AValidEnum.FIRST_VALUE)))).containsOnly(record2);
		assertThat(findRecords(whereMetadata.isNotIn(asList(AValidEnum.FIRST_VALUE, AValidEnum.SECOND_VALUE)))).isEmpty();
		assertThat(findRecords(whereMetadata.isNotIn(new ArrayList<>()))).containsOnly(record1, record2, record3, record4);

	}

	@Test
	public void whenSearchingStatsForCollectionThenFindResults()
			throws Exception {
		//recordServices.execute(transaction);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);

	}

	@Test
	public void whenSearchingStatsForNumberMetadataThenFindResults()
			throws Exception {
		defineSchemasManager().using(schema.withANumberMetadata(whichIsSearchable));
		Metadata statsMetadata = zeSchema.numberMetadata();
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema()
				.set(statsMetadata, 12.0));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema()
				.set(statsMetadata, 13.5));
		//transaction.addUpdate(expectedRecord3 = newRecordOfZeSchema());
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).returnAll();
		//when
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.computeStatsOnField(statsMetadata);
		//then
		SPEQueryResponse response = searchServices.query(query);
		Map<String, Object> values = response.getStatValues(statsMetadata);
		assertThat(values.get("min")).isEqualTo(12.0);
		assertThat(values.get("max")).isEqualTo(13.5);
		assertThat(values.get("sum")).isEqualTo(25.5);
		assertThat(values.get("count")).isEqualTo(2L);
		assertThat(values.get("missing")).isEqualTo(0L);
	}

	@Test
	public void whenSearchingStatsForNumberMetadataWithMissingValueThenFindResults()
			throws Exception {
		defineSchemasManager().using(schema.withANumberMetadata(whichIsSearchable));
		Metadata statsMetadata = zeSchema.numberMetadata();
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema()
				.set(statsMetadata, 12.0));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema()
				.set(statsMetadata, 13.5));
		transaction.addUpdate(expectedRecord3 = newRecordOfZeSchema());
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).returnAll();
		//when
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.computeStatsOnField(statsMetadata);
		//then
		SPEQueryResponse response = searchServices.query(query);
		Map<String, Object> values = response.getStatValues(statsMetadata);
		assertThat(values.get("missing")).isEqualTo(1L);
		assertThat(values.get("count")).isEqualTo(2L);
		assertThat(values.get("min")).isEqualTo(12.0);
		assertThat(values.get("max")).isEqualTo(13.5);
		assertThat(values.get("sum")).isEqualTo(25.5);
	}

	@Test
	public void whenSearchingStatsForAllContainersOfACollectionThenFindResults()
			throws Exception {
	}

	@Test
	public void whenSearchingStatsForContainerWithAtLeastOneFolderWithoutLinearSizeThenReturnFolderWithoutLinearSizeException()
			throws Exception {
	}

	@Test
	public void whenSearchingStatsForCollectionWithAtLeastOneFolderWithoutLinearSizeThenReturnFolderWithoutLinearSizeException()
			throws Exception {
	}

	@Test
	public void whenSearchingStatsForContainerWithInvalidCapacityThenReturnContainerCapacityNotFoundException()
			throws Exception {
	}

	@Test
	public void whenSearchingBySingleFacetValueThenFindRecordsWithGivenValue()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata().withANumberMetadata());

		transaction.addUpdate(record1 = givenARecord("record1")
				.set(zeSchema.stringMetadata(), "Chuck Norris").set(zeSchema.numberMetadata(), 1.0));
		transaction.addUpdate(record2 = givenARecord("record2")
				.set(zeSchema.stringMetadata(), "Chuck Norris").set(zeSchema.numberMetadata(), 2.0));
		transaction.addUpdate(record3 = givenARecord("record3")
				.set(zeSchema.stringMetadata(), "Chuck Norris").set(zeSchema.numberMetadata(), 3.0));
		transaction.addUpdate(record4 = givenARecord("record4")
				.set(zeSchema.stringMetadata(), "Gandalf le Blanc").set(zeSchema.numberMetadata(), 1.0));
		transaction.addUpdate(record5 = givenARecord("record5")
				.set(zeSchema.stringMetadata(), "Gandalf le Blanc").set(zeSchema.numberMetadata(), 2.0));
		recordServices.execute(transaction);

		LogicalSearchCondition condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isEqualTo("Chuck Norris");
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.getFacetFilters().selectedFieldFacetValues(zeSchema.numberMetadata().getDataStoreCode(), Arrays.asList("1.0"));

		assertThat(searchServices.search(query)).containsOnly(record1);
	}

	@Test
	public void whenSearchingAndFilteringUsingQueryValueThenFindRecordsWithGivenValue()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata().withANumberMetadata().withADateMetadata());

		transaction.addUpdate(record1 = givenARecord("record1")
				.set(zeSchema.stringMetadata(), "Chuck Norris")
				.set(zeSchema.numberMetadata(), 1.0)
				.set(zeSchema.dateMetadata(), new LocalDate(2012, 5, 12)));

		transaction.addUpdate(record2 = givenARecord("record2")
				.set(zeSchema.stringMetadata(), "Chuck Norris")
				.set(zeSchema.numberMetadata(), 2.0)
				.set(zeSchema.dateMetadata(), new LocalDate(2012, 5, 15)));

		transaction.addUpdate(record3 = givenARecord("record3")
				.set(zeSchema.stringMetadata(), "Chuck Norris")
				.set(zeSchema.numberMetadata(), 3.0)
				.set(zeSchema.dateMetadata(), new LocalDate(2012, 5, 18)));

		transaction.addUpdate(record4 = givenARecord("record4")
				.set(zeSchema.stringMetadata(), "Gandalf le Blanc")
				.set(zeSchema.numberMetadata(), 1.0));

		transaction.addUpdate(record5 = givenARecord("record5")
				.set(zeSchema.stringMetadata(), "Gandalf le Blanc")
				.set(zeSchema.numberMetadata(), 2.0));
		recordServices.execute(transaction);

		LogicalSearchCondition condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isEqualTo("Chuck Norris");
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.getFacetFilters().selectedQueryFacetValue("numberFacet", "numberMetadata_d:[2.0 TO 3.0]");
		assertThat(searchServices.search(query)).containsOnly(record2, record3);

		query.getFacetFilters().selectedQueryFacetValue("numberFacet", "numberMetadata_d:[1.0 TO 2.0]");
		assertThat(searchServices.search(query)).containsOnly(record1, record2, record3);

		query.getFacetFilters()
				.selectedQueryFacetValue("dateFacet", "dateMetadata_da:[2012-05-14T00:00:00Z TO 2012-05-18T00:00:00Z]");
		assertThat(searchServices.search(query)).containsOnly(record2, record3);
	}

	@Test
	public void whenSearchingByMultipleFacetValuesThenFindRecordsWithAnyValue()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata().withANumberMetadata());

		transaction.addUpdate(record1 = givenARecord("record1")
				.set(zeSchema.stringMetadata(), "Chuck Norris").set(zeSchema.numberMetadata(), 1.0));
		transaction.addUpdate(record2 = givenARecord("record2")
				.set(zeSchema.stringMetadata(), "Chuck Norris").set(zeSchema.numberMetadata(), 2.0));
		transaction.addUpdate(record3 = givenARecord("record3")
				.set(zeSchema.stringMetadata(), "Chuck Norris").set(zeSchema.numberMetadata(), 3.0));
		transaction.addUpdate(record4 = givenARecord("record4")
				.set(zeSchema.stringMetadata(), "Gandalf le Blanc").set(zeSchema.numberMetadata(), 1.0));
		transaction.addUpdate(record5 = givenARecord("record5")
				.set(zeSchema.stringMetadata(), "Gandalf le Blanc").set(zeSchema.numberMetadata(), 2.0));
		recordServices.execute(transaction);

		LogicalSearchCondition condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isEqualTo("Chuck Norris");
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.getFacetFilters().selectedFieldFacetValues(zeSchema.numberMetadata().getDataStoreCode(),
				Arrays.asList("1.0", "2.0"));

		assertThat(searchServices.search(query)).containsOnly(record1, record2);
	}

	@Test
	public void whenSearchingByMultipleFacetsThenFindRecordsWithAllValues()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata().withAnotherStringMetadata().withANumberMetadata());

		transaction.addUpdate(record1 = givenARecord("record1")
				.set(zeSchema.stringMetadata(), "Chuck Norris")
				.set(zeSchema.anotherStringMetadata(), "A")
				.set(zeSchema.numberMetadata(), 1.0));
		transaction.addUpdate(record2 = givenARecord("record2")
				.set(zeSchema.stringMetadata(), "Chuck Norris")
				.set(zeSchema.anotherStringMetadata(), "B")
				.set(zeSchema.numberMetadata(), 2.0));
		transaction.addUpdate(record3 = givenARecord("record3")
				.set(zeSchema.stringMetadata(), "Chuck Norris")
				.set(zeSchema.anotherStringMetadata(), "A")
				.set(zeSchema.numberMetadata(), 3.0));
		transaction.addUpdate(record4 = givenARecord("record4")
				.set(zeSchema.stringMetadata(), "Gandalf le Blanc")
				.set(zeSchema.anotherStringMetadata(), "A")
				.set(zeSchema.numberMetadata(), 1.0));
		transaction.addUpdate(record5 = givenARecord("record5")
				.set(zeSchema.stringMetadata(), "Gandalf le Blanc")
				.set(zeSchema.anotherStringMetadata(), "A")
				.set(zeSchema.numberMetadata(), 2.0));
		recordServices.execute(transaction);

		LogicalSearchCondition condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isEqualTo("Chuck Norris");
		LogicalSearchQuery query = new LogicalSearchQuery(condition);
		query.getFacetFilters()
				.selectedFieldFacetValues(zeSchema.numberMetadata().getDataStoreCode(), Arrays.asList("1.0", "2.0"))
				.selectedFieldFacetValue(zeSchema.anotherStringMetadata().getDataStoreCode(), "A");

		assertThat(searchServices.search(query)).containsOnly(record1);
	}

	@Test
	public void whenAskingForSingleFacetThenReturnFacetValues()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata().withAnotherStringMetadata().withANumberMetadata());

		transaction.addUpdate(record1 = givenARecord("record1")
				.set(zeSchema.stringMetadata(), "Chuck Norris")
				.set(zeSchema.anotherStringMetadata(), "A")
				.set(zeSchema.numberMetadata(), 1.0));
		transaction.addUpdate(record2 = givenARecord("record2")
				.set(zeSchema.stringMetadata(), "Chuck Norris")
				.set(zeSchema.anotherStringMetadata(), "B")
				.set(zeSchema.numberMetadata(), 2.0));
		transaction.addUpdate(record3 = givenARecord("record3")
				.set(zeSchema.stringMetadata(), "Chuck Norris")
				.set(zeSchema.anotherStringMetadata(), "A")
				.set(zeSchema.numberMetadata(), 3.0));
		transaction.addUpdate(record4 = givenARecord("record4")
				.set(zeSchema.stringMetadata(), "Gandalf le Blanc")
				.set(zeSchema.anotherStringMetadata(), "A")
				.set(zeSchema.numberMetadata(), 1.0));
		transaction.addUpdate(record5 = givenARecord("record5")
				.set(zeSchema.stringMetadata(), "Gandalf le Blanc")
				.set(zeSchema.anotherStringMetadata(), "A")
				.set(zeSchema.numberMetadata(), 2.0));
		recordServices.execute(transaction);

		LogicalSearchQuery query = new LogicalSearchQuery(from(zeSchema.instance()).returnAll())
				.addFieldFacet(zeSchema.numberMetadata().getDataStoreCode());

		Map<String, List<FacetValue>> facets = searchServices.query(query).getFieldFacetValues();
		assertThat(facets.keySet()).containsOnly(zeSchema.numberMetadata().getDataStoreCode());
		assertThat(facets.get(zeSchema.numberMetadata().getDataStoreCode())).hasSize(3);
	}

	@Test
	public void whenAskingForMultipleFacetsThenReturnFacetValues()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata().withAnotherStringMetadata().withANumberMetadata());

		transaction.addUpdate(record1 = givenARecord("record1")
				.set(zeSchema.stringMetadata(), "Chuck Norris")
				.set(zeSchema.anotherStringMetadata(), "A")
				.set(zeSchema.numberMetadata(), 1.0));
		transaction.addUpdate(record2 = givenARecord("record2")
				.set(zeSchema.stringMetadata(), "Chuck Norris")
				.set(zeSchema.anotherStringMetadata(), "B")
				.set(zeSchema.numberMetadata(), 2.0));
		transaction.addUpdate(record3 = givenARecord("record3")
				.set(zeSchema.stringMetadata(), "Chuck Norris")
				.set(zeSchema.anotherStringMetadata(), "A")
				.set(zeSchema.numberMetadata(), 3.0));
		transaction.addUpdate(record4 = givenARecord("record4")
				.set(zeSchema.stringMetadata(), "Gandalf le Blanc")
				.set(zeSchema.anotherStringMetadata(), "A")
				.set(zeSchema.numberMetadata(), 1.0));
		transaction.addUpdate(record5 = givenARecord("record5")
				.set(zeSchema.stringMetadata(), "Gandalf le Blanc")
				.set(zeSchema.anotherStringMetadata(), "A")
				.set(zeSchema.numberMetadata(), 2.0));
		recordServices.execute(transaction);

		LogicalSearchQuery query = new LogicalSearchQuery(from(zeSchema.instance()).returnAll())
				.addFieldFacet(zeSchema.numberMetadata().getDataStoreCode())
				.addFieldFacet(zeSchema.anotherStringMetadata().getDataStoreCode());

		Map<String, List<FacetValue>> facets = searchServices.query(query).getFieldFacetValues();
		assertThat(facets.keySet()).containsOnly(
				zeSchema.numberMetadata().getDataStoreCode(),
				zeSchema.anotherStringMetadata().getDataStoreCode());
		assertThat(facets.get(zeSchema.numberMetadata().getDataStoreCode())).hasSize(3);
		assertThat(facets.get(zeSchema.anotherStringMetadata().getDataStoreCode())).hasSize(2);
	}

	@Test
	public void whenSearchingRecordsReturningOnlySomeFieldsThenFindResultsEventIfFieldNotReturned()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata().withABooleanMetadata());
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Norris").set(
				zeSchema.booleanMetadata(), true));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Lechat Norris")
				.set(zeSchema.booleanMetadata(), true));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "chuck Norris").set(
				zeSchema.booleanMetadata(), true));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Edouard Chuck Lechat").set(
				zeSchema.booleanMetadata(), true));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isStartingWithText("Chuck");
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		query.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(zeSchema.booleanMetadata()));
		query.setQueryExecutionMethod(QueryExecutionMethod.USE_SOLR);
		List<Record> records = searchServices.search(query);

		assertThat(ids(records)).containsOnly(TestUtils.idsArray(expectedRecord, expectedRecord2));
		assertThat(records.get(0).<String>get(zeSchema.stringMetadata())).isNull();
		assertThat((boolean) records.get(0).get(zeSchema.booleanMetadata())).isTrue();
		assertThat(records.get(1).<String>get(zeSchema.stringMetadata())).isNull();
		assertThat((boolean) records.get(1).get(zeSchema.booleanMetadata())).isTrue();

	}

	@Test
	public void whenSearchingStartingWithTextRecordsReturningExactValueFieldThenValueReturned()
			throws Exception {
		defineSchemasManager().using(schema.withAMultivaluedLargeTextMetadata(multiValueConfigurator()));
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.multivaluedLargeTextMetadata(), asList("00052124500", "52124500")));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.multivaluedLargeTextMetadata(), asList("00052124500", "52124500432")));
		transaction.addUpdate(expectedRecord3 = newRecordOfZeSchema().set(zeSchema.multivaluedLargeTextMetadata(), asList("00052124500", "52124")));
		ConstellioFactories.getInstance().getDataLayerFactory().getDataLayerLogger().setPrintAllQueriesLongerThanMS(0);
		recordServices.execute(transaction);


		condition = from(zeSchema.instance()).where(zeSchema.multivaluedLargeTextMetadata()).isStartingWithText("52124500");
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		List<Record> records = searchServices.search(query);

		assertThat(ids(records)).containsOnly(TestUtils.idsArray(expectedRecord, expectedRecord2));
		List<String> textValue1 = records.get(0).get(zeSchema.multivaluedLargeTextMetadata());
		assertThat(textValue1).contains("52124500");

		List<String> textValue2 = records.get(1).get(zeSchema.multivaluedLargeTextMetadata());
		assertThat(textValue2).contains("52124500432");
	}

	@Test
	public void whenSearchingRecordsReturningOnlySomeFieldsThenReturnOnlyAskedFieldsVersionIdAndSchema()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata().withABooleanMetadata());
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Norris").set(
				zeSchema.booleanMetadata(), true));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Lechat Norris")
				.set(zeSchema.booleanMetadata(), true));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "chuck Norris").set(
				zeSchema.booleanMetadata(), true));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Edouard Chuck Lechat").set(
				zeSchema.booleanMetadata(), true));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isStartingWithText("Chuck");
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		query.setReturnedMetadatas(ReturnedMetadatasFilter.onlyMetadatas(zeSchema.stringMetadata()));
		query.setQueryExecutionMethod(QueryExecutionMethod.USE_SOLR);
		List<Record> records = searchServices.search(query);

		assertThat(ids(records)).containsOnly(TestUtils.idsArray(expectedRecord, expectedRecord2));
		assertThat(records.get(0).<String>get(zeSchema.stringMetadata())).isNotNull();
		assertThat(records.get(0).getId()).isNotNull();
		assertThat(records.get(0).getVersion()).isNotNull();
		assertThat(records.get(0).getSchemaCode()).isNotNull();
		assertThat(records.get(0).<Boolean>get(zeSchema.booleanMetadata())).isNull();
		assertThat(records.get(1).<String>get(zeSchema.stringMetadata())).isNotNull();
		assertThat(records.get(1).getId()).isNotNull();
		assertThat(records.get(1).getVersion()).isNotNull();
		assertThat(records.get(1).getSchemaCode()).isNotNull();
		assertThat(records.get(1).<Boolean>get(zeSchema.booleanMetadata())).isNull();
	}

	@Test
	public void whenSearchingWithPrefereAnalyzedFlagThenUseAnalyzedFields()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata(whichIsSearchable).withAnotherStringMetadata());
		transaction.addUpdate(record1 = givenARecord("record1")
				.set(zeSchema.stringMetadata(), "Rien ne sert de jouer aux échecs avec Chuck Norris, il ne connait pas l'échec"));
		transaction.addUpdate(record2 = givenARecord("record2")
				.set(zeSchema.stringMetadata(),
						"Chuck Norris et Superman ont fait un bras de fer, le perdant devait mettre son slip par dessus son pantalon."));
		transaction.addUpdate(record3 = givenARecord("record3")
				.set(zeSchema.stringMetadata(), "Chuck Norris a déjà compté jusqu'à l'infini. Deux fois."));
		transaction.addUpdate(record4 = givenARecord("record4")
				.set(zeSchema.stringMetadata(),
						"Certaines personnes portent un pyjama superman. Superman porte un pyjama Chuck Norris.")
				.set(zeSchema.anotherStringMetadata(),
						"Dakota l'indien est l'idole de Chuck Norris"));
		transaction.addUpdate(record5 = givenARecord("record5").set(zeSchema.stringMetadata(), "Chuck Norris"));
		recordServices.execute(transaction);

		OngoingLogicalSearchConditionWithDataStoreFields whereStringMetadata = from(zeSchema.instance())
				.where(zeSchema.stringMetadata());

		assertThat(searchServices.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("Chuck Norris"))))
				.containsOnly(record5);
		assertThat(searchServices.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("Superman"))))
				.isEmpty();
		assertThat(searchServices.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("superman"))))
				.isEmpty();
		assertThat(searchServices.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("echec"))))
				.isEmpty();
		assertThat(searchServices.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("personne"))))
				.isEmpty();
		assertThat(searchServices.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("idole"))))
				.isEmpty();
		assertThat(searchServices.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("Chuck Norris")
				.andWhere(zeSchema.anotherStringMetadata()).isEqualTo("Dakota"))))
				.isEmpty();

		assertThat(searchServices
				.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("Chuck Norris")).setPreferAnalyzedFields(true)))
				.containsOnly(record1, record2, record3, record4, record5);
		assertThat(searchServices
				.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("Superman")).setPreferAnalyzedFields(true)))
				.containsOnly(record2, record4);
		assertThat(searchServices
				.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("superman")).setPreferAnalyzedFields(true)))
				.containsOnly(record2, record4);
		assertThat(searchServices
				.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("echec")).setPreferAnalyzedFields(true)))
				.containsOnly(record1);
		assertThat(searchServices
				.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("persone")).setPreferAnalyzedFields(true)))
				.containsOnly(record4);
		assertThat(searchServices
				.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("idole")).setPreferAnalyzedFields(true)))
				.isEmpty();
		assertThat(searchServices.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("Chuck Norris")
				.andWhere(zeSchema.anotherStringMetadata()).isEqualTo("Dakota")).setPreferAnalyzedFields(true)))
				.isEmpty();
	}

	@Test
	public void givenMetadataWithRoleRestrictionThenSearchDoesNotIncludeThatBoost() throws OptimisticLocking {
		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypes metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);
		List<MetadataSchemaType> metadataSchemaTypeList = metadataSchemaTypes.getSchemaTypes();

		User user = users.adminIn(zeCollection);

		SearchBoost searchBoost = createRegexBoost("description_t", "Kiwi", "My boost", 12.0);

		String qfResult1 = searchServices.getQfFor(asList(Language.French.getCode()), Language.French.getCode(), asList(searchBoost), metadataSchemaTypeList, user);

		assertThat(qfResult1.contains("description_t:Kiwi^12.0")).isTrue();

		addRoleToTaskDefaultTitleMetadata("M");

		metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);
		metadataSchemaTypeList = metadataSchemaTypes.getSchemaTypes();

		String qfResult2 = searchServices.getQfFor(asList(Language.French.getCode()), Language.French.getCode(), asList(searchBoost), metadataSchemaTypeList, user);

		assertThat(qfResult2.contains("description_t:Kiwi^12.0")).isFalse();
	}

	@Test
	public void givenMetadataWithRoleRestrictionWithAllSchemaThatDoesNotIncludeThatMetadataThenSearchIncludeThatField()
			throws OptimisticLocking {
		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypes metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);
		List<MetadataSchemaType> metadataSchemaTypeList = metadataSchemaTypes.getSchemaTypes();

		User user = users.adminIn(zeCollection);


		String qfResult1 = searchServices.getQfFor(asList(Language.French.getCode()), Language.French.getCode(), new ArrayList<SearchBoost>(), metadataSchemaTypeList, user);

		assertThat(qfResult1.contains("description_t")).isTrue();

		addRoleToTaskDefaultTitleMetadata("M");

		metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);
		metadataSchemaTypeList = metadataSchemaTypes.getSchemaTypes();

		metadataSchemaTypeList = removeSchemaTypeFromSchemaTypeList(metadataSchemaTypeList, Task.SCHEMA_TYPE);

		String qfResult2 = searchServices.getQfFor(asList(Language.French.getCode()), Language.French.getCode(), new ArrayList<SearchBoost>(), metadataSchemaTypeList, user);

		assertThat(qfResult2.contains("description_t")).isTrue();
	}

	@Test
	public void givenMetadataWithRoleRestrictionAndSchemaThatDoesNotIncludeThatMetadataThenSearchIncludeThatBoost()
			throws OptimisticLocking {
		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypes metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);
		List<MetadataSchemaType> metadataSchemaTypeList = metadataSchemaTypes.getSchemaTypes();

		User user = users.adminIn(zeCollection);

		SearchBoost searchBoost = createRegexBoost("description_t", "Kiwi", "My boost", 12.0);

		String qfResult1 = searchServices.getQfFor(asList(Language.French.getCode()), Language.French.getCode(), asList(searchBoost), metadataSchemaTypeList, user);

		assertThat(qfResult1.contains("description_t:Kiwi^12.0")).isTrue();

		addRoleToTaskDefaultTitleMetadata("M");

		metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);
		metadataSchemaTypeList = metadataSchemaTypes.getSchemaTypes();

		metadataSchemaTypeList = removeSchemaTypeFromSchemaTypeList(metadataSchemaTypeList, Task.SCHEMA_TYPE);

		String qfResult2 = searchServices.getQfFor(asList(Language.French.getCode()), Language.French.getCode(), asList(searchBoost), metadataSchemaTypeList, user);

		assertThat(qfResult2.contains("description_t:Kiwi^12.0")).isTrue();
	}

	private List<MetadataSchemaType> removeSchemaTypeFromSchemaTypeList(List<MetadataSchemaType> metadataSchemaTypes,
																		String code) {
		Iterator iterator = metadataSchemaTypes.iterator();
		List<MetadataSchemaType> metadataSchemaTypeList = new ArrayList<>();

		while (iterator.hasNext()) {
			MetadataSchemaType metadataSchemaType = (MetadataSchemaType) iterator.next();

			if (!metadataSchemaType.getCode().equals(code)) {
				metadataSchemaTypeList.add(metadataSchemaType);
			}
		}

		return metadataSchemaTypeList;
	}

	@Test
	public void givenBoostWithCorrespondingMetadataWithRoleRestrictionThenSearchDoesNotIncludeThatField()
			throws OptimisticLocking {
		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		MetadataSchemaTypes metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);
		List<MetadataSchemaType> metadataSchemaTypeList = metadataSchemaTypes.getSchemaTypes();

		String descriptionCode = metadataSchemaTypes.getSchemaType(Task.SCHEMA_TYPE).getDefaultSchema().getMetadata(Task.DESCRIPTION).getDataStoreCode();
		User user = users.adminIn(zeCollection);
		String qfResult1 = searchServices.getQfFor(asList(Language.French.getCode()), Language.French.getCode(), new ArrayList<SearchBoost>(), metadataSchemaTypeList, user);

		assertThat(qfResult1.contains(descriptionCode)).isTrue();

		addRoleToTaskDefaultTitleMetadata("M");

		metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(zeCollection);
		metadataSchemaTypeList = metadataSchemaTypes.getSchemaTypes();

		String qfResult2 = searchServices.getQfFor(asList(Language.French.getCode()), Language.French.getCode(), new ArrayList<SearchBoost>(), metadataSchemaTypeList, user);

		assertThat(qfResult2.contains(descriptionCode)).isFalse();
	}

	private void addRoleToTaskDefaultTitleMetadata(String role)
			throws OptimisticLocking {
		final MetadataSchemasManager schemasManager = getModelLayerFactory().getMetadataSchemasManager();
		final MetadataSchemaTypesBuilder types = schemasManager.modify(zeCollection);
		final MetadataAccessRestrictionBuilder metadataAccessRestrictionBuilder;
		final MetadataBuilder builder;

		builder = types.getSchema(Task.DEFAULT_SCHEMA).get(Task.DESCRIPTION);

		MetadataAccessRestriction metadataAccessRestriction = new MetadataAccessRestriction(Arrays.asList(role), new ArrayList<String>(),
				new ArrayList<String>(), new ArrayList<String>());

		metadataAccessRestrictionBuilder = MetadataAccessRestrictionBuilder.modify(metadataAccessRestriction);
		builder.setAccessRestrictionBuilder(metadataAccessRestrictionBuilder);

		schemasManager.saveUpdateSchemaTypes(types);
	}

	//Broken multilingual @Test
	public void givenSearchableStringMetadataWhenSearchingRecordsUsingFrenchDefaultSearchFieldThenFindValidFrenchRecods()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata(whichIsSearchable));
		transaction.addUpdate(
				expectedRecord = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Édouard Lechat est le meilleur"));
		transaction
				.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "J'entend un bruit étrange"));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "It is snowing outside. Meilleur"));
		recordServices.execute(transaction);

		assertThat(
				findRecords(from(zeSchema.instance()).where(zeSchema.stringMetadata().getAnalyzedField("fr")).query("meilleur")))
				.containsOnly(expectedRecord);
	}

	//Multilinguage broken @Test
	public void givenSearchableStringMetadataWhenSearchingWithLanguageAnalysisThenFindValue()
			throws Exception {

		ConditionTemplateFactory factory = new ConditionTemplateFactory(getModelLayerFactory(), zeCollection);
		defineSchemasManager().using(schema.withAStringMetadata(whichIsSearchable));
		transaction.addUpdate(
				record1 = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Édouard Lechat est le meilleur"));
		transaction
				.addUpdate(record2 = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "J'entend un bruit étrange"));
		transaction.addUpdate(record3 = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "It is snowing outside. Meilleur"));
		recordServices.execute(transaction);

		assertThat(findRecords(from(zeSchema.instance())
				.where(factory.metadatasHasAnalyzedValue("snowing", zeSchema.stringMetadata())))).containsOnly(record3);

		assertThat(findRecords(from(zeSchema.instance())
				.where(factory.metadatasHasAnalyzedValue("meilleur", zeSchema.stringMetadata())))).containsOnly(record1);

	}

	//Broken multilingual @Test
	public void givenSearchableStringMetadataWhenSearchingRecordsUsingEnglishDefaultSearchFieldThenFindValidEnglishRecords()
			throws Exception {

		defineSchemasManager().using(schema.withAStringMetadata(whichIsSearchable));
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema()
				.set(zeSchema.stringMetadata(), "This is some amazing text in document number 42"));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema()
				.set(zeSchema.stringMetadata(), "My favorite numbers are 42 and 666."));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Ceci est un number en francais : 42"));
		recordServices.execute(transaction);

	}

	//Multilinguage broken @Test
	public void givenSearchableTextMetadataWhenSearchingRecordsUsingFrenchDefaultSearchFieldThenFindValidFrenchRecods()
			throws Exception {
		defineSchemasManager().using(schema.withALargeTextMetadata(whichIsSearchable));
		transaction.addUpdate(
				expectedRecord = newRecordOfZeSchema().set(zeSchema.largeTextMetadata(), "Édouard Lechat est le meilleur"));
		transaction
				.addUpdate(
						expectedRecord2 = newRecordOfZeSchema().set(zeSchema.largeTextMetadata(), "J'entend un bruit étrange"));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.largeTextMetadata(), "It is snowing outside. Meilleur"));
		recordServices.execute(transaction);

	}

	@Test
	public void givenSimpleSearchWhenAskingForHighlightsThenReturnTheHighlights()
			throws Exception {
		//given

		defineSchemasManager().using(schema.withAStringMetadata(whichIsSearchable));
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema()
				.set(zeSchema.stringMetadata(), "This is some amazing text in document number 42"));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema()
				.set(zeSchema.stringMetadata(), "My favorite numbers are 42 and 666, but I like text as well."));
		recordServices.execute(transaction);

		String text = "text";
		condition = fromAllSchemasIn(zeCollection).returnAll();

		//when
		LogicalSearchQuery query = new LogicalSearchQuery(condition).setFreeTextQuery(text).setHighlighting(true);
		SPEQueryResponse response = searchServices.query(query);

		//then
		Map<String, Map<String, List<String>>> highlights = response.getHighlights();

		String highlightText = "<em>" + text + "</em>";
		for (Record record : new Record[]{expectedRecord, expectedRecord2}) {
			assertThat(highlights).containsKey(record.getId());
			assertThat(highlights.get(record.getId()).size()).isGreaterThan(0);
			for (List<String> snippets : highlights.get(record.getId()).values()) {
				for (String snippet : snippets) {
					assertThat(snippet).contains(highlightText);
					assertThat(record.get(zeSchema.stringMetadata()).toString()).contains(snippet.replaceAll("</?em>", ""));
				}
			}
		}
	}

	@Test
	public void givenAdvancedSearchWhenAskingForHighlightsThenReturnTheHighlights()
			throws Exception {
		//given

		defineSchemasManager().using(schema.withAStringMetadata(whichIsSearchable));
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema()
				.set(zeSchema.stringMetadata(), "This is some amazing text in document number 42"));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema()
				.set(zeSchema.stringMetadata(), "My favorite numbers are 42 and 666, but I like text as well."));
		recordServices.execute(transaction);

		String text = "text";
		condition = from(zeSchema.instance()).returnAll();

		//when
		LogicalSearchQuery query = new LogicalSearchQuery(condition).setFreeTextQuery(text).setHighlighting(true);
		SPEQueryResponse response = searchServices.query(query);

		//then
		Map<String, Map<String, List<String>>> highlights = response.getHighlights();

		String highlightText = "<em>" + text + "</em>";
		for (Record record : new Record[]{expectedRecord, expectedRecord2}) {
			assertThat(highlights).containsKey(record.getId());
			assertThat(highlights.get(record.getId()).size()).isGreaterThan(0);
			for (Entry<String, List<String>> snippets : highlights.get(record.getId()).entrySet()) {
				if (!snippets.getKey().equals("stringMetadata_t_en")) {
					continue;
				}
				for (String snippet : snippets.getValue()) {
					assertThat(snippet).contains(highlightText);
					assertThat(record.get(zeSchema.stringMetadata()).toString()).contains(snippet.replaceAll("</?em>", ""));
				}
			}
		}
	}

	@Test
	public void whenSearchingRecordsWithEncryptedMetadatasThenDecryptedInResultedRecords()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata(whichIsEncrypted));
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema("1").set(zeSchema.stringMetadata(), "Chuck Norris"));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema("2").set(zeSchema.stringMetadata(), "Chuck Lechat Norris"));
		recordServices.execute(transaction);

		LogicalSearchQuery query = new LogicalSearchQuery(from(zeSchema.instance())
				.where(zeSchema.stringMetadata()).isNot(containingText("Chuck")));
		query.sortAsc(Schemas.IDENTIFIER);
		List<Record> records = searchServices.search(query);

		assertThat(records).hasSize(2);
		assertThat(records.get(0).<String>get(zeSchema.stringMetadata())).isEqualTo("Chuck Norris");
		assertThat(records.get(1).<String>get(zeSchema.stringMetadata())).isEqualTo("Chuck Lechat Norris");
	}

	@Test
	public void whenSearchingRecordsStartingWithTextThenFindThem()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata());
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Norris"));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Lechat Norris"));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "chuck Norris"));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Edouard Chuck Lechat"));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isStartingWithText("Chuck");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void whenSearchingRecordsEndingWithTextThenFindThem()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata());
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Norris"));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Lechat Norris"));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Lechat"));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Edouard Chuck Lechat"));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isEndingWithText("Norris");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void whenSearchingRecordsContainingTextThenFindThem()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata());
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Norris"));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Lechat Norris"));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "chuck Norris"));
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Edouard Chuck Lechat"));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isContainingText("Lechat");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void whenSearchingRecordsContainingTextWithSpecialCharactersThenFindThem()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata());
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema()
				.set(zeSchema.stringMetadata(), "Chuck:h=T+4zq4cGP/tXkdJp/qz1WVWYhoQ=:Norris -1.-03"));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata())
				.isContainingText("Chuck:h=T+4zq4cGP/tXkdJp/qz1WVWYhoQ=:Norris -1.-03");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord);
	}

	@Test
	public void whenSearchingRecordsInAnySchemaContainingTextThenFindThem()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata().withAnotherSchemaStringMetadata());
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Norris"));
		transaction.addUpdate(expectedRecord2 = newRecordOfAnotherSchema().set(anotherSchema.stringMetadata(),
				"Chuck Lechat Norris"));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Lechat"));
		transaction.addUpdate(newRecordOfAnotherSchema().set(anotherSchema.stringMetadata(), "Edouard Chuck Lechat"));
		recordServices.execute(transaction);

		condition = fromAllSchemasIn(zeCollection).where(zeSchema.stringMetadata()).isContainingText("Norris");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);

	}

	@Test
	public void whenSearchingRecordsIsTrueThenFindThem()
			throws Exception {
		defineSchemasManager().using(schema.withABooleanMetadata());
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.booleanMetadata(), true));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.booleanMetadata(), true));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.booleanMetadata(), false));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.booleanMetadata(), false));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.booleanMetadata()).isTrue();
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void whenSearchingRecordsIsTrueOrNullThenFindThem()
			throws Exception {
		defineSchemasManager().using(schema.withABooleanMetadata());
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.booleanMetadata(), true));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.booleanMetadata(), null));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.booleanMetadata(), false));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.booleanMetadata(), false));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.booleanMetadata()).isTrueOrNull();
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void whenSearchingRecordsIsFalseThenFindThem()
			throws Exception {
		defineSchemasManager().using(schema.withABooleanMetadata());
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.booleanMetadata(), false));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.booleanMetadata(), false));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.booleanMetadata(), true));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.booleanMetadata(), true));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.booleanMetadata()).isFalse();
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void whenSearchingRecordsIsFalseOrNullThenFindThem()
			throws Exception {
		defineSchemasManager().using(schema.withABooleanMetadata());
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.booleanMetadata(), false));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.booleanMetadata(), null));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.booleanMetadata(), true));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.booleanMetadata(), true));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.booleanMetadata()).isFalseOrNull();
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void whenSearchingRecordsOnBooleanMetadataIsNullThenFindThem()
			throws Exception {
		defineSchemasManager().using(schema.withABooleanMetadata());
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.booleanMetadata(), null));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.booleanMetadata(), null));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.booleanMetadata(), true));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.booleanMetadata(), false));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.booleanMetadata()).isNull();
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void whenSearchingRecordsOnBooleanMetadataIsNotNullThenFindThem()
			throws Exception {
		defineSchemasManager().using(schema.withABooleanMetadata());
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.booleanMetadata(), null));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.booleanMetadata(), null));
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.booleanMetadata(), true));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.booleanMetadata(), false));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.booleanMetadata()).isNotNull();
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void whenSearchingRecordsOnTextMetadataIsNullThenFindThem()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata());
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.stringMetadata(), null));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.stringMetadata(), null));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Norris"));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Nuck Chorris"));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isNull();
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void whenSearchingRecordsOnTextMetadataIsNotNullThenFindThem()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata());
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), null));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), null));
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Norris"));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Nuck Chorris"));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isNotNull();
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void whenSearchingRecordsContainingTextANDIsStartingWithTextThenFindThem()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata());
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Norris"));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Lechat"));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Edouard Norris"));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Edouard Chuck Lechat"));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isContainingText("Norris")
				.and(asList(startingWithText("Chuck")));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord);
	}

	@Test
	public void whenSearchingRecordsContainingTextORIsStartingWithTextThenFindThem()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata());
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Norris"));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Lechat"));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Edouard Lechat"));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Edouard Chuck Lechat"));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isContainingText("Norris")
				.or(asList(startingWithText("Chuck")));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void whenSearchingRecordsContainingTextANDIsFalseThenFindThem()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata().withABooleanMetadata());
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Norris"));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Lechat"));
		expectedRecord.set(zeSchema.booleanMetadata(), false);
		expectedRecord2.set(zeSchema.booleanMetadata(), false);
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Edouard Lechat"));
		transaction.addUpdate(expectedRecord3 = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Edouard Norris"));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isContainingText("Chuck")
				.andWhere(zeSchema.booleanMetadata()).isFalse().orWhere(zeSchema.stringMetadata())
				.isNot(containingText("Lechat"));

		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2, expectedRecord3);
	}

	@Test
	public void whenSearchingRecordsContainingTextANDIsFalseOrNotContainingTextThenFindThem()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata().withABooleanMetadata());
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Norris"));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Lechat"));
		expectedRecord.set(zeSchema.booleanMetadata(), false);
		expectedRecord2.set(zeSchema.booleanMetadata(), false);
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Edouard Lechat"));
		transaction.addUpdate(expectedRecord3 = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Edouard Norris"));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isContainingText("Chuck")
				.andWhere(zeSchema.booleanMetadata()).isFalse().orWhere(zeSchema.stringMetadata())
				.isNot(containingText("Lechat"));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2, expectedRecord3);
	}

	@Test
	public void whenSearchingRecordsContainingTextORIsTrueThenFindThem()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata().withABooleanMetadata());
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Edouard Norris"));
		expectedRecord.set(zeSchema.booleanMetadata(), true);
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Lechat"));
		expectedRecord2.set(zeSchema.booleanMetadata(), false);
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Edouard Lechat"));
		transaction.addUpdate(expectedRecord3 = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Norris"));
		expectedRecord.set(zeSchema.booleanMetadata(), true);
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isContainingText("Chuck")
				.orWhere(zeSchema.booleanMetadata()).isTrue();
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2, expectedRecord3);
	}

	@Test
	public void givenNumberValuesWhenIsValueInRange10NegativeTo2ThenReturnTwoRecords()
			throws Exception {
		givenFiveNumberValuesIncludingNegativeNumberAndMinIntegerValue();

		condition = from(zeSchema.instance()).where(zeSchema.numberMetadata()).isValueInRange(-10, 2);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2, expectedRecord3);
	}

	@Test
	public void givenLettersValuesWhenIsValueInRangeBToCThenReturnTwoRecords()
			throws Exception {
		givenFourLettersValues();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isValueInRange("B", "C");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2, expectedRecord3);
	}

	@Test
	public void givenTextValuesWhenIsValueInRangeThenReturnTwoRecords()
			throws Exception {
		givenFiveTextValues();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isValueInRange("Chuck Norris", "Edouard Lechat");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2, expectedRecord3);
	}

	@Test
	public void givenTextValuesIncludingNULLWhenIsValueInRangeFromThenDoNotReturnNullValue()
			throws Exception {
		givenFourTextValuesIncludingNull();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isValueInRange("__A__", "__T__");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord3);
	}

	@Test
	public void givenDateValuesWhenIsValueInRangeThenReturnTwoRecords()
			throws Exception {
		givenFiveDateValuesIncludingNullDate();

		condition = from(zeSchema.instance()).where(zeSchema.dateMetadata()).isValueInRange(DATE2, DATE3);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2, expectedRecord3);
	}

	@Test
	public void givenDateTimeValuesWhenIsValueInRangeThenReturnTwoRecords()
			throws Exception {
		givenFiveDateTimeValuesIncludingNullDateTime();

		condition = from(zeSchema.instance()).where(zeSchema.dateTimeMetadata()).isValueInRange(DATE_TIME2, DATE_TIME3);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2, expectedRecord3);
	}

	@Test
	public void givenNumberValuesIncludingNegativeNumberAndMinIntegerValueWhenIsLessThanThenReturnThreeRecords()
			throws Exception {
		givenFiveNumberValuesIncludingNegativeNumberAndMinIntegerValue();

		List<Record> records = findRecords(from(zeSchema.instance()).where(zeSchema.numberMetadata()).isLessThan(10));
		assertThat(records).containsOnly(expectedRecord, expectedRecord2, expectedRecord3);

		records = findRecords(from(zeSchema.instance()).where(zeSchema.numberMetadata()).isLessThan(11));
		assertThat(records).containsOnly(expectedRecord, expectedRecord2, expectedRecord3, expectedRecord4);

		records = findRecords(from(zeSchema.instance()).where(zeSchema.numberMetadata()).isLessThan(0));
		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void givenLettersValuesWhenIsLessThanThenReturnTwoRecords()
			throws Exception {
		givenFourLettersValues();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isLessThan("C");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void givenTextValuesWhenIsLessThanThenReturnTwoRecords()
			throws Exception {
		givenFiveTextValues();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isLessThan("Edouard Lechat");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void givenTextValuesIncludingNULLWhenIsLessThanThenDoNotReturnNullValue()
			throws Exception {
		givenFourTextValuesIncludingNull();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isLessThan("__Z__");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord3);
	}

	@Test
	public void givenDateTimeIncludingNullDateTimeValuesWhenIsLessThanThenDoNotReturnNullValue()
			throws Exception {
		givenFiveDateTimeValuesIncludingNullDateTime();

		condition = from(zeSchema.instance()).where(zeSchema.dateTimeMetadata()).isLessThan(DATE_TIME3);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void givenDateIncludingNullDateValuesWhenIsLessThanThenDoNotReturnNullValue()
			throws Exception {
		givenFiveDateValuesIncludingNullDate();

		condition = from(zeSchema.instance()).where(zeSchema.dateMetadata()).isLessThan(DATE3);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void givenDateIncludingNullDateValuesWhenIsNotNullThenDoNotReturnNullValue()
			throws Exception {
		givenFiveDateValuesIncludingNullDate();

		condition = from(zeSchema.instance()).where(zeSchema.dateMetadata()).isNotNull();
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2, expectedRecord3, expectedRecord4);
	}

	@Test
	public void givenDateIncludingNullDateValuesWhenIsNullThenReturnNullValueOnly()
			throws Exception {
		givenFiveDateValuesIncludingNullDate();

		condition = from(zeSchema.instance()).where(zeSchema.dateMetadata()).isNull();
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord5);
	}

	@Test
	public void givenNumberValuesIncludingMinIntegerValueWhenIsLessOrEqualThan10ThenReturnTwoRecords()
			throws Exception {
		givenFiveNumberValuesIncludingNegativeNumberAndMinIntegerValue();

		condition = from(zeSchema.instance()).where(zeSchema.numberMetadata()).isLessOrEqualThan(-10);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void givenLettersValuesWhenIsLessOrEqualThanCThenReturnThreeRecords()
			throws Exception {
		givenFourLettersValues();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isLessOrEqualThan("C");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2, expectedRecord3);
	}

	@Test
	public void givenTextValuesWhenIsLessOrEqualThanThirdRecordThenReturnThreeRecords()
			throws Exception {
		givenFiveTextValues();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isLessOrEqualThan("Edouard Lechat");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2, expectedRecord3);
	}

	@Test
	public void givenFourTextValuesIncludingNULLWhenIsLessOrEqualThanZThenReturnThreeRecords()
			throws Exception {
		givenFourTextValuesIncludingNull();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isLessOrEqualThan("__Z__");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord3, expectedRecord4);
	}

	@Test
	public void givenFourDateTimeAndNULLDateTimeValuesWhenIsLessOrEqualThanThirdThenReturnThreeRecords()
			throws Exception {
		givenFiveDateTimeValuesIncludingNullDateTime();

		condition = from(zeSchema.instance()).where(zeSchema.dateTimeMetadata()).isLessOrEqualThan(DATE_TIME3);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2, expectedRecord3);
	}

	@Test
	public void givenFourDateAndNULLDateValuesWhenIsLessOrEqualThanThirdThenReturnThreeRecords()
			throws Exception {
		givenFiveDateValuesIncludingNullDate();

		condition = from(zeSchema.instance()).where(zeSchema.dateMetadata()).isLessOrEqualThan(DATE3);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2, expectedRecord3);
	}

	//
	@Test
	public void givenFourRelativesDatesAndNullDateValuesWhenIsNewerThanThenReturnThreeRecords()
			throws Exception {
		givenTimeIs(NOW);
		givenFiveRelativeDatesDateValuesIncludingNullDate();

		condition = from(zeSchema.instance()).where(zeSchema.dateMetadata()).isNewerThan(2.0, MeasuringUnitTime.YEARS);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord4);
	}

	@Test
	public void givenFourRelativesDatesAndNullDateValuesWhenIsOlderThanThenReturnOneRecords()
			throws Exception {
		givenTimeIs(NOW);
		givenFiveRelativeDatesDateValuesIncludingNullDate();

		condition = from(zeSchema.instance()).where(zeSchema.dateMetadata()).isOlderThan(2.0, MeasuringUnitTime.YEARS);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord3);
	}

	@Test
	public void givenFourRelativesDatesAndNullDateValuesWhenIsOlderLikeThenReturnOneRecords()
			throws Exception {
		givenTimeIs(NOW);
		givenFiveRelativeDatesDateValuesIncludingNullDate();

		condition = from(zeSchema.instance()).where(zeSchema.dateMetadata()).isOldLike(2.0, MeasuringUnitTime.YEARS);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2);
	}

	//

	@Test
	public void givenNumberValuesIncludingMinIntegerValueWhenIsGreaterThanThenReturnThreeRecords()
			throws Exception {
		givenFiveNumberValuesIncludingNegativeNumberAndMinIntegerValue();

		condition = from(zeSchema.instance()).where(zeSchema.numberMetadata()).isGreaterThan(-20);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2, expectedRecord3, expectedRecord4);
	}

	@Test
	public void givenLettresValuesWhenIsGreaterThanThenReturnTwoRecords()
			throws Exception {
		givenFourLettersValues();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isGreaterThan("B");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord3, expectedRecord4);
	}

	@Test
	public void givenTextValuesWhenIsGreaterThanThenReturnTwoRecords()
			throws Exception {
		givenFiveTextValues();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isGreaterThan("Chuck Norris");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord3, expectedRecord4, expectedRecord5);
	}

	@Test
	public void givenTextValuesIncludingNULLWhenIsGreaterThanThenDoNotReturnNull()
			throws Exception {
		givenFourTextValuesIncludingNull();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isGreaterThan("__A__");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord3, expectedRecord4);
	}

	@Test
	public void givenDateValuesWhenIsGreaterThanThenReturnTwoRecords()
			throws Exception {
		givenFiveDateValuesIncludingNullDate();

		condition = from(zeSchema.instance()).where(zeSchema.dateMetadata()).isGreaterThan(DATE2);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord3, expectedRecord4);
	}

	@Test
	public void givenDateTimeValuesWhenIsGreaterThanThenReturnTwoRecords()
			throws Exception {
		givenFiveDateTimeValuesIncludingNullDateTime();

		condition = from(zeSchema.instance()).where(zeSchema.dateTimeMetadata()).isGreaterThan(DATE_TIME2);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord3, expectedRecord4);
	}

	@Test
	public void givenNumberValuesIncludingMinIntegerValueWhenIsGreaterOrEqualThan10NegativeThenReturnThreeRecords()
			throws Exception {
		givenFiveNumberValuesIncludingNegativeNumberAndMinIntegerValue();

		condition = from(zeSchema.instance()).where(zeSchema.numberMetadata()).isGreaterOrEqualThan(-10);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2, expectedRecord3, expectedRecord4);
	}

	@Test
	public void givenLettersValuesWhenIsGreaterOrEqualThanBThenReturnThreeRecords()
			throws Exception {
		givenFourLettersValues();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isGreaterOrEqualThan("B");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2, expectedRecord3, expectedRecord4);
	}

	@Test
	public void givenTextValuesWhenIsGreaterOrEqualThanSecondRecordThenReturnThreeRecords()
			throws Exception {
		givenFiveTextValues();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isGreaterOrEqualThan("Chuck Norris");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2, expectedRecord3, expectedRecord4, expectedRecord5);
	}

	@Test
	public void givenTextValuesIncludingNULLWhenIsGreaterOrEqualThanAThenReturnThreeRecords()
			throws Exception {
		givenFourTextValuesIncludingNull();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isGreaterOrEqualThan("__A__");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord3, expectedRecord4);
	}

	@Test
	public void givenDateAndNULLDateValuesWhenIsGreaterOrEqualThanDateThenReturnThreeRecords()
			throws Exception {
		givenFiveDateValuesIncludingNullDate();

		condition = from(zeSchema.instance()).where(zeSchema.dateMetadata()).isGreaterOrEqualThan(DATE2);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2, expectedRecord3, expectedRecord4);
	}

	@Test
	public void givenDateTimeAndNULLDateValuesWhenIsGreaterOrEqualThanDateThenReturnThreeRecords()
			throws Exception {
		givenFiveDateTimeValuesIncludingNullDateTime();

		condition = from(zeSchema.instance()).where(zeSchema.dateTimeMetadata()).isGreaterOrEqualThan(DATE_TIME2);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2, expectedRecord3, expectedRecord4);
	}

	@Test
	public void givenNumberValuesIncludingMinIntegerValueWhenIsEqualThenReturnTheRecord()
			throws Exception {
		givenFiveNumberValuesIncludingNegativeNumberAndMinIntegerValue();

		condition = from(zeSchema.instance()).where(zeSchema.numberMetadata()).isEqualTo(-10);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2);
	}

	@Test
	public void givenTextValuesWhenIsEqualThenReturnTheRecord()
			throws Exception {
		givenFiveTextValues();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isEqualTo("Chuck Norris");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2);
	}

	@Test
	public void givenTextValuesIncludingNULLWhenIsEqualNullThenReturnNullRecord()
			throws Exception {
		givenFourTextValuesIncludingNull();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isNull();
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2);
	}

	@Test
	public void givenDateAndNULLDateValuesWhenIsEqualThenReturnTheRecord()
			throws Exception {
		givenFiveDateValuesIncludingNullDate();

		condition = from(zeSchema.instance()).where(zeSchema.dateMetadata()).isEqualTo(DATE2);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2);
	}

	@Test
	public void givenDateTimeAndNULLDateTimeValuesWhenIsEqualThenReturnTheRecord()
			throws Exception {
		givenFiveDateTimeValuesIncludingNullDateTime();

		condition = from(zeSchema.instance()).where(zeSchema.dateTimeMetadata()).isEqualTo(DATE_TIME2);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2);
	}

	@Test
	public void givenDateAndNULLDateValuesWhenIsEqualNullDateThenReturnTheNullDateRecord()
			throws Exception {
		givenFiveDateValuesIncludingNullDate();

		condition = from(zeSchema.instance()).where(zeSchema.dateMetadata()).isEqualTo(new LocalDateTime(Integer.MIN_VALUE));
		List<Record> records = findRecords(condition);
		assertThat(records).isEmpty();

		condition = from(zeSchema.instance()).where(zeSchema.dateMetadata()).isNull();
		records = findRecords(condition);
		assertThat(records).containsOnly(expectedRecord5);
	}

	@Test
	public void givenDateTimeAndNULLDateTimeValuesWhenIsEqualNullDateTimeThenReturnTheNullDateTimeRecord()
			throws Exception {
		givenFiveDateTimeValuesIncludingNullDateTime();

		condition = from(zeSchema.instance()).where(zeSchema.dateTimeMetadata()).isEqualTo(new LocalDateTime(Integer.MIN_VALUE));
		List<Record> records = findRecords(condition);
		assertThat(records).isEmpty();

		condition = from(zeSchema.instance()).where(zeSchema.dateTimeMetadata()).isNull();
		records = findRecords(condition);
		assertThat(records).containsOnly(expectedRecord5);
	}

	@Test
	public void givenTwoEqualsValuesWhenIsEqualValueThenReturnTheTwoRecordsWithTheValue()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata());
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Norris"));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Norris"));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isEqualTo("Chuck Norris");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void givenBooleanValuesWhenIsEqualThenReturnTheRecord()
			throws Exception {
		defineSchemasManager().using(schema.withABooleanMetadata());
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.booleanMetadata(), true));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.booleanMetadata(), false));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.booleanMetadata()).isEqualTo(true);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord);
	}

	@Test
	public void givenNumberValuesWhenIsContainingThenReturnTheRecord()
			throws Exception {
		givenNumberMultiValueMetadataWithThreeNumbers();

		condition = from(zeSchema.instance()).where(zeSchema.numberMetadata()).isContaining(Arrays.asList(-10, 0, 1));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord);
	}

	@Test
	public void givenNumberValuesWhenIsContainingAnotherNumberThenDoNotFindRecord()
			throws Exception {
		givenNumberMultiValueMetadataWithThreeNumbers();

		condition = from(zeSchema.instance()).where(zeSchema.numberMetadata()).isContaining(Arrays.asList(-10, 0, 2));
		List<Record> records = findRecords(condition);

		assertThat(records).isEmpty();
	}

	@Test
	public void givenTextValuesWhenIsContainingThenReturnTheRecord()
			throws Exception {
		givenTextMultiValueMetadataWithThreeTexts();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isContaining(
				Arrays.asList("Edouard Lechat", "Chuck Norris"));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord);
	}

	@Test
	public void givenTextValuesWhenIsContainingAnotherTextThenDoNotFindRecord()
			throws Exception {
		givenTextMultiValueMetadataWithThreeTexts();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isContaining(
				Arrays.asList("Edouard Lechat", "Chuck Norris", "a a"));
		List<Record> records = findRecords(condition);

		assertThat(records).isEmpty();
	}

	@Test
	public void givenDateTimeValuesWhenIsContainingThenReturnTheRecord()
			throws Exception {
		givenDateTimeMultiValueMetadataWithThreeDateTimes();

		condition = from(zeSchema.instance()).where(zeSchema.dateTimeMetadata())
				.isContaining(Arrays.asList(DATE_TIME1, DATE_TIME2));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord);
	}

	@Test
	public void givenDateValuesWhenIsContainingThenReturnTheRecord()
			throws Exception {
		givenDateMultiValueMetadataWithThreeDates();

		condition = from(zeSchema.instance()).where(zeSchema.dateMetadata())
				.isContaining(Arrays.asList(DATE1, DATE2));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord);
	}

	@Test
	public void givenDateTimeValuesWhenIsContainingAnotherDateTimeThenDoNotFindRecord()
			throws Exception {
		givenDateTimeMultiValueMetadataWithThreeDateTimes();

		condition = from(zeSchema.instance()).where(zeSchema.dateTimeMetadata())
				.isContaining(Arrays.asList(DATE_TIME1, DATE_TIME4));
		List<Record> records = findRecords(condition);

		assertThat(records).isEmpty();
	}

	@Test
	public void givenDateValuesWhenIsContainingAnotherDateThenDoNotFindRecord()
			throws Exception {
		givenDateMultiValueMetadataWithThreeDates();

		condition = from(zeSchema.instance()).where(zeSchema.dateMetadata())
				.isContaining(Arrays.asList(DATE1, DATE4));
		List<Record> records = findRecords(condition);

		assertThat(records).isEmpty();
	}

	@Test
	public void givenNumberValuesWhenIsIn10NegativeTo2ThenReturnTwoRecords()
			throws Exception {
		givenFiveNumberValuesIncludingNegativeNumberAndMinIntegerValue();

		condition = from(zeSchema.instance()).where(zeSchema.numberMetadata()).isIn(Arrays.asList(-10, 2, 3, 4));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2, expectedRecord3);
	}

	@Test
	public void givenNumberFloatValuesWhenIsIn10NegativeTo2ThenReturnTwoRecords()
			throws Exception {
		givenFiveNumberValuesIncludingNegativeNumberAndMinIntegerValue();

		condition = from(zeSchema.instance()).where(zeSchema.numberMetadata()).isIn(Arrays.asList(-10.0f, 2.0f, 3.0f, 4.0f));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2, expectedRecord3);
	}

	@Test
	public void givenNumberDoubleValuesWhenIsIn10NegativeTo2ThenReturnTwoRecords()
			throws Exception {
		givenFiveNumberValuesIncludingNegativeNumberAndMinIntegerValue();

		condition = from(zeSchema.instance()).where(zeSchema.numberMetadata()).isIn(Arrays.asList(-10.0d, 2.0d, 3.0d, 4.0d));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2, expectedRecord3);
	}

	@Test
	public void givenNumberLongValuesWhenIsIn10NegativeTo2ThenReturnTwoRecords()
			throws Exception {
		givenFiveNumberValuesIncludingNegativeNumberAndMinIntegerValue();

		condition = from(zeSchema.instance()).where(zeSchema.numberMetadata()).isIn(
				Arrays.asList(Long.valueOf(-10l), Long.valueOf(2l), Long.valueOf(3l), Long.valueOf(4l)));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2, expectedRecord3);
	}

	@Test
	public void givenLettersValuesWhenIsInThenReturnTwoRecords()
			throws Exception {
		givenFourLettersValues();
		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isIn(Arrays.asList("B", "C", "F"));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2, expectedRecord3);
	}

	@Test
	public void givenTextValuesWhenIsInThenReturnTwoRecords()
			throws Exception {
		givenFiveTextValues();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isIn(
				Arrays.asList("Chuck Norris", "Edouard Lechat", "a a"));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2, expectedRecord3);
	}

	@Test
	public void givenDateValuesWhenIsInThenReturnTwoRecords()
			throws Exception {
		givenFiveDateValuesIncludingNullDate();

		condition = from(zeSchema.instance()).where(zeSchema.dateMetadata()).isIn(
				Arrays.asList(DATE3, DATE4, new LocalDateTime()));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord3, expectedRecord4);
	}

	@Test
	public void givenDateTimeValuesWhenIsInThenReturnTwoRecords()
			throws Exception {
		givenFiveDateTimeValuesIncludingNullDateTime();

		condition = from(zeSchema.instance()).where(zeSchema.dateTimeMetadata()).isIn(
				Arrays.asList(DATE_TIME3, DATE_TIME4, new LocalDateTime()));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord3, expectedRecord4);
	}

	@Test
	public void givenNumberValuesIncludingMinIntegerValueWhenIsNotEqualThenReturnThreeRecords()
			throws Exception {
		givenFiveNumberValuesIncludingNegativeNumberAndMinIntegerValue();

		condition = from(zeSchema.instance()).where(zeSchema.numberMetadata()).isNotEqual(-10);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord3, expectedRecord4);
	}

	@Test
	public void givenTextValuesWhenIsNotEqualThenReturnThreeRecords()
			throws Exception {
		givenFiveTextValues();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isNotEqual("Chuck Norris");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord3, expectedRecord4, expectedRecord5);
	}

	@Test
	public void givenTextValuesIncludingNULLWhenIsNotEqualThenReturnTwoRecordsAndNotNull()
			throws Exception {
		givenFourTextValuesIncludingNull();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isNotEqual("__A__");
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2, expectedRecord3, expectedRecord4);
	}

	@Test
	public void givenDateAndNULLDateValuesWhenIsNotEqualThenReturnTheRecord()
			throws Exception {
		givenFiveDateValuesIncludingNullDate();

		condition = from(zeSchema.instance()).where(zeSchema.dateMetadata()).isNotEqual(DATE2);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord3, expectedRecord4, expectedRecord5);
	}

	@Test
	public void givenDateTimeAndNULLDateTimeValuesWhenIsNotEqualThenReturnTheRecord()
			throws Exception {
		givenFiveDateTimeValuesIncludingNullDateTime();

		condition = from(zeSchema.instance()).where(zeSchema.dateTimeMetadata()).isNotEqual(DATE_TIME2);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord3, expectedRecord4, expectedRecord5);
	}

	@Test
	public void givenTwoBooleanValuesWhenIsNotEqualThenReturnTheOtherRecord()
			throws Exception {
		defineSchemasManager().using(schema.withABooleanMetadata());
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.booleanMetadata(), true));
		transaction.addUpdate(expectedRecord2 = newRecordOfZeSchema().set(zeSchema.booleanMetadata(), false));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.booleanMetadata()).isNotEqual(true);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord2);
	}

	@Test
	public void givenNumberValuesWhenIsNotContainingThenDoNotReturnTheRecord()
			throws Exception {
		givenNumberMultiValueMetadataWithThreeNumbers();

		condition = from(zeSchema.instance()).where(zeSchema.numberMetadata()).isNotContainingElements(Arrays.asList(-10, 0));
		List<Record> records = findRecords(condition);

		assertThat(records).isEmpty();
	}

	@Test
	public void givenNumberValuesWhenIsNotContainingThenReturnTheRecord()
			throws Exception {
		givenNumberMultiValueMetadataWithThreeNumbers();

		condition = from(zeSchema.instance()).where(zeSchema.numberMetadata())
				.isNotContainingElements(Arrays.asList(-10, 0, 5));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord);
	}

	@Test
	public void givenNumberValuesWhenIsNotContainingThemThenDoNotReturnTheRecord()
			throws Exception {
		givenNumberMultiValueMetadataWithThreeNumbers();

		condition = from(zeSchema.instance()).where(zeSchema.numberMetadata())
				.isNotContainingElements(Arrays.asList(-10, 0, 1));
		List<Record> records = findRecords(condition);

		assertThat(records).isEmpty();
	}

	@Test
	public void givenTextValuesWhenIsNotContainingThenDoNotReturnTheRecord()
			throws Exception {
		givenTextMultiValueMetadataWithThreeTexts();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isNotContainingElements(
				Arrays.asList("Edouard Lechat", "Chuck Norris"));
		List<Record> records = findRecords(condition);

		assertThat(records).isEmpty();
	}

	@Test
	public void givenTextValuesWhenIsNotContainingAnotherTextThenReturnTheRecord()
			throws Exception {
		givenTextMultiValueMetadataWithThreeTexts();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isNotContainingElements(
				Arrays.asList("Edouard Lechat", "Chuck Norris", "a a"));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord);
	}

	@Test
	public void givenDateValuesWhenIsNotContainingThenDoNotReturnTheRecord()
			throws Exception {
		givenDateMultiValueMetadataWithThreeDates();

		condition = from(zeSchema.instance()).where(zeSchema.dateMetadata())
				.isNotContainingElements(Arrays.asList(DATE1));
		List<Record> records = findRecords(condition);

		assertThat(records).isEmpty();
	}

	@Test
	public void givenDateTimeValuesWhenIsNotContainingThenDoNotReturnTheRecord()
			throws Exception {
		givenDateTimeMultiValueMetadataWithThreeDateTimes();

		condition = from(zeSchema.instance()).where(zeSchema.dateTimeMetadata())
				.isNotContainingElements(Arrays.asList(DATE_TIME1));
		List<Record> records = findRecords(condition);

		assertThat(records).isEmpty();
	}

	@Test
	public void givenDateValuesWhenIsNotContainingAnotherDateThenReturnRecord()
			throws Exception {
		givenDateMultiValueMetadataWithThreeDates();

		condition = from(zeSchema.instance()).where(zeSchema.dateMetadata()).isNotContainingElements(
				Arrays.asList(DATE1, DATE4));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord);
	}

	@Test
	public void givenDateTimeValuesWhenIsNotContainingAnotherDateTimeThenReturnRecord()
			throws Exception {
		givenDateTimeMultiValueMetadataWithThreeDateTimes();

		condition = from(zeSchema.instance()).where(zeSchema.dateTimeMetadata()).isNotContainingElements(
				Arrays.asList(DATE_TIME1, DATE_TIME4));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord);
	}

	@Test
	public void whenSearchingByDateTimeTheNullValuesAreNeverReturned()
			throws Exception {

		LocalDateTime time0 = new LocalDateTime(1000, 1, 1, 0, 0, 0, 0);
		LocalDateTime time1 = new LocalDateTime(1000, 2, 1, 0, 0, 0, 0);
		LocalDateTime time2 = new LocalDateTime(2000, 1, 1, 1, 1, 1, 1);
		LocalDateTime time3 = new LocalDateTime(3000, 1, 1, 1, 1, 1, 1);

		defineSchemasManager().using(schema.withADateTimeMetadata());
		transaction.addUpdate(record1 = givenARecord("record1").set(zeSchema.dateTimeMetadata(), time1));
		transaction.addUpdate(record2 = givenARecord("record2").set(zeSchema.dateTimeMetadata(), time2));
		transaction.addUpdate(record3 = givenARecord("record3").set(zeSchema.dateTimeMetadata(), time3));
		transaction.addUpdate(record4 = givenARecord("recordNull").set(zeSchema.dateTimeMetadata(), null));
		recordServices.execute(transaction);

		assertThat(findRecords(from(zeSchema.instance()).where(zeSchema.dateTimeMetadata()).isValueInRange(time0, time3)))
				.containsOnly(record1, record2, record3);

		assertThat(findRecords(from(zeSchema.instance()).where(zeSchema.dateTimeMetadata()).isNull())).containsOnly(record4);
	}

	@Test
	public void whenSearchingByDateTheNullValuesAreNeverReturned()
			throws Exception {

		LocalDate time0 = new LocalDate(1, 1, 1);
		LocalDate time1 = new LocalDate(1, 1, 12);
		LocalDate time2 = new LocalDate(1000, 1, 1);
		LocalDate time3 = new LocalDate(3000, 1, 1);

		defineSchemasManager().using(schema.withADateMetadata());
		transaction.addUpdate(record1 = givenARecord("record1").set(zeSchema.dateMetadata(), time1));
		transaction.addUpdate(record2 = givenARecord("record2").set(zeSchema.dateMetadata(), time2));
		transaction.addUpdate(record3 = givenARecord("record3").set(zeSchema.dateMetadata(), time3));
		transaction.addUpdate(record4 = givenARecord("recordNull").set(zeSchema.dateMetadata(), null));
		recordServices.execute(transaction);

		assertThat(findRecords(from(zeSchema.instance()).where(zeSchema.dateMetadata()).isValueInRange(time0, time3)))
				.containsOnly(record1, record2, record3);

		assertThat(findRecords(from(zeSchema.instance()).where(zeSchema.dateMetadata()).isNull())).containsOnly(record4);

	}

	@Test
	public void whenSearchingByDateTimeThenHaveMillisecondsPrecision()
			throws Exception {

		LocalDateTime time1 = new LocalDateTime().minusHours(42);
		LocalDateTime time2 = time1.plusMillis(1);
		LocalDateTime time3 = time2.plusMillis(1);
		LocalDateTime time4 = time3.plusMillis(1);
		LocalDateTime time5 = time4.plusMillis(1);

		defineSchemasManager().using(schema.withADateTimeMetadata());
		transaction.addUpdate(record1 = givenARecord("record1").set(zeSchema.dateTimeMetadata(), time1));
		transaction.addUpdate(record2 = givenARecord("record2").set(zeSchema.dateTimeMetadata(), time2));
		transaction.addUpdate(record3 = givenARecord("record3").set(zeSchema.dateTimeMetadata(), time3));
		transaction.addUpdate(record4 = givenARecord("record4").set(zeSchema.dateTimeMetadata(), time4));
		transaction.addUpdate(record5 = givenARecord("record5").set(zeSchema.dateTimeMetadata(), time5));
		recordServices.execute(transaction);

		OngoingLogicalSearchConditionWithDataStoreFields whereDateTime = from(zeSchema.instance())
				.where(zeSchema.dateTimeMetadata());

		assertThat(findRecords(whereDateTime.isNotIn(asList(time2, time4)))).containsOnly(record1, record3, record5);
		assertThat(findRecords(whereDateTime.isIn(asList(time2, time4)))).containsOnly(record2, record4);
		assertThat(findRecords(whereDateTime.isGreaterThan(time3))).containsOnly(record4, record5);
		assertThat(findRecords(whereDateTime.isGreaterOrEqualThan(time3))).containsOnly(record3, record4, record5);
		assertThat(findRecords(whereDateTime.isLessThan(time3))).containsOnly(record1, record2);
		assertThat(findRecords(whereDateTime.isLessOrEqualThan(time3))).containsOnly(record1, record2, record3);
		assertThat(findRecords(whereDateTime.isValueInRange(time2, time4))).containsOnly(record2, record3, record4);

	}

	@Test
	public void whenSearchingByDateThenHaveDaysPrecision()
			throws Exception {

		LocalDate time1 = new LocalDate().minusDays(42);
		LocalDate time2 = time1.plusDays(1);
		LocalDate time3 = time2.plusDays(1);
		LocalDate time4 = time3.plusDays(1);
		LocalDate time5 = time4.plusDays(1);

		defineSchemasManager().using(schema.withADateMetadata());
		transaction.addUpdate(record1 = givenARecord("record1").set(zeSchema.dateMetadata(), time1));
		transaction.addUpdate(record2 = givenARecord("record2").set(zeSchema.dateMetadata(), time2));
		transaction.addUpdate(record3 = givenARecord("record3").set(zeSchema.dateMetadata(), time3));
		transaction.addUpdate(record4 = givenARecord("record4").set(zeSchema.dateMetadata(), time4));
		transaction.addUpdate(record5 = givenARecord("record5").set(zeSchema.dateMetadata(), time5));
		recordServices.execute(transaction);

		OngoingLogicalSearchConditionWithDataStoreFields whereDate = from(zeSchema.instance()).where(zeSchema.dateMetadata());

		assertThat(findRecords(whereDate.isNotIn(asList(time2, time4)))).containsOnly(record1, record3, record5);
		assertThat(findRecords(whereDate.isIn(asList(time2, time4)))).containsOnly(record2, record4);
		assertThat(findRecords(whereDate.isGreaterThan(time3))).containsOnly(record4, record5);
		assertThat(findRecords(whereDate.isGreaterOrEqualThan(time3))).containsOnly(record3, record4, record5);
		assertThat(findRecords(whereDate.isLessThan(time3))).containsOnly(record1, record2);
		assertThat(findRecords(whereDate.isLessOrEqualThan(time3))).containsOnly(record1, record2, record3);
		assertThat(findRecords(whereDate.isValueInRange(time2, time4))).containsOnly(record2, record3, record4);

	}

	@Test
	public void givenNumberValuesWhenIsNotInThenReturnTwoRecords()
			throws Exception {
		givenFiveNumberValuesIncludingNegativeNumberAndMinIntegerValue();

		condition = from(zeSchema.instance()).where(zeSchema.numberMetadata()).isNotIn(Arrays.asList(-10, 2, 3));
		List<Record> records = findRecords(condition);

		for (Record record : records) {
			record.get(zeSchema.numberMetadata());
		}

		assertThat(records).containsOnly(expectedRecord, expectedRecord4);
	}

	@Test
	public void givenLettersValuesWhenIsNotInThenReturnTwoRecords()
			throws Exception {
		givenFourLettersValues();
		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isNotIn(Arrays.asList("B", "C", "F"));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord4);
	}

	@Test
	public void givenTextValuesWhenIsNotInThenReturnTwoRecords()
			throws Exception {
		givenFiveTextValues();

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isNotIn(
				Arrays.asList("Chuck Norris", "Edouard Lechat", "a a"));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord4, expectedRecord5);
	}

	@Test
	public void givenDateValuesWhenIsNotInThenReturnTwoRecords()
			throws Exception {
		givenFiveDateValuesIncludingNullDate();

		condition = from(zeSchema.instance()).where(zeSchema.dateMetadata()).isNotIn(
				Arrays.asList(DATE3, DATE4, new LocalDateTime()));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void givenDateTimeValuesWhenIsNotInThenReturnTwoRecords()
			throws Exception {
		givenFiveDateTimeValuesIncludingNullDateTime();

		condition = from(zeSchema.instance()).where(zeSchema.dateTimeMetadata()).isNotIn(
				Arrays.asList(DATE_TIME3, DATE_TIME4, new LocalDateTime()));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void givenFourLettersAndQueryWhenQueryThenItQueriesInSolrl()
			throws Exception {
		givenFourLettersValues();

		String query = "[A TO B]";
		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).query(query);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void givenTextsAndQueryStartingWithChuckWhenQueryThenItQueriesInSolrl()
			throws Exception {
		givenFiveTextValues();

		String query = "Chuck *";
		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).query(query);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);

	}

	@Test
	public void givenFiveTextAndQueryWhenQueryThenItQueriesInSolrl()
			throws Exception {
		givenFiveTextValues();

		String query = "zeEdouard \\*";
		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).query(query);
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord5);

	}

	@Test(expected = BadRequest.class)
	public void givenFourLettersAndWrongQueryWhenQueryThenItQueriesInSolrl()
			throws Exception {
		givenFourLettersValues();

		String wrongQuery = "[A TO B";
		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).query(wrongQuery);
		List<Record> records = findRecords(condition);

		assertThat(records).isEmpty();
	}

	@Test
	public void whenSearchingJustIdsThenUseFlParameters()
			throws Exception {
		givenFourLettersValues();
		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).query("*");

		searchServices = Mockito.spy(searchServices);
		doReturn(recordDao).when(searchServices).dataStoreDao("records");
		List<String> ids = findRecordIds(condition);

		assertThat(ids).hasSize(4);
		assertThat(ids.get(0)).isNotNull();

		ArgumentCaptor<SolrParams> params = ArgumentCaptor.forClass(SolrParams.class);
		verify(recordDao).query(anyString(), params.capture());
		assertThat(params.getValue().get("fl")).isEqualTo("id,schema_s,_version_,collection_s");
	}

	@Test
	public void whenGetResultsCountThenUseFlAndRowsParameters()
			throws Exception {
		givenFourLettersValues();
		searchServices = Mockito.spy(searchServices);
		doReturn(recordDao).when(searchServices).dataStoreDao("records");
		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).query("*");

		long count = searchServices.getResultsCount(condition);

		assertThat(count).isEqualTo(4);

		ArgumentCaptor<SolrParams> params = ArgumentCaptor.forClass(SolrParams.class);
		verify(recordDao).query(anyString(), params.capture());
		assertThat(params.getValue().get("rows")).isEqualTo("0");
	}

	@Test
	public void whenSearchingWithIdentifierThenFindResults()
			throws Exception {
		givenFiveTextValues();

		condition = from(zeSchema.instance()).where(Schemas.IDENTIFIER).isIn(
				Arrays.asList(expectedRecord.getId(), expectedRecord2.getId()));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void whenSearchingWithVersionThenFindResults()
			throws Exception {
		givenFiveTextValues();

		condition = from(zeSchema.instance()).where(Schemas.VERSION).isIn(
				Arrays.asList(expectedRecord.getVersion(), expectedRecord2.getVersion()));
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);
	}

	@Test
	public void whenSearchingWithIdAndVersionsThenObtainCorrectResults()
			throws Exception {

		givenFiveTextValues();

		Record unexpectedRecord = expectedRecord4;

		LogicalSearchCondition condition = LogicalSearchQueryOperators.fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
				.isIn(Arrays.asList(expectedRecord.getId(), expectedRecord2.getId(), unexpectedRecord.getId()))
				.andWhere(Schemas.VERSION).isNotIn(Arrays.asList(1l, 2l, unexpectedRecord.getVersion()));

		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2);

	}

	@Test
	public void whenSearchingWithIsNotInThenSupport1000Elements()
			throws Exception {

		givenTwoZeSchemaRecords();
		Record unexpectedRecord = zeSchemaRecord1;
		Record expectedRecord = zeSchemaRecord2;
		List<String> notInCriterion = new ArrayList<>();
		for (int i = 0; i < 999; i++) {
			notInCriterion.add("anInexistendId_" + anInteger());
		}
		//1000
		notInCriterion.add(unexpectedRecord.getId());

		LogicalSearchCondition condition = LogicalSearchQueryOperators.fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
				.isNotIn(notInCriterion).andWhere(Schemas.IDENTIFIER).isNotIn(
						notInCriterion).andWhere(Schemas.IDENTIFIER).isNotIn(notInCriterion)
				.andWhere(Schemas.IDENTIFIER).isNotIn(notInCriterion).andWhere(Schemas.IDENTIFIER).isNotIn(notInCriterion);

		List<Record> records = findRecords(condition);

		assertThat(records).doesNotContain(unexpectedRecord).contains(expectedRecord);

	}

	@Test
	public void whenSearchingWithIsInThenSupport1000Elements()
			throws Exception {

		givenTwoZeSchemaRecords();
		Record unexpectedRecord = zeSchemaRecord1;
		Record expectedRecord = zeSchemaRecord2;
		List<String> inCriterion = new ArrayList<>();
		for (int i = 0; i < 999; i++) {
			inCriterion.add("anInexistendId_" + anInteger());
		}
		//1000
		inCriterion.add(expectedRecord.getId());

		LogicalSearchCondition condition = LogicalSearchQueryOperators.fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
				.isIn(inCriterion).andWhere(Schemas.IDENTIFIER).isIn(inCriterion).andWhere(Schemas.IDENTIFIER).isIn(inCriterion)
				.andWhere(Schemas.IDENTIFIER).isIn(inCriterion).andWhere(Schemas.IDENTIFIER).isIn(inCriterion);

		List<Record> records = findRecords(condition);

		assertThat(records).doesNotContain(unexpectedRecord).contains(expectedRecord);

	}

	@Test(expected = SearchServicesRuntimeException.TooManyElementsInCriterion.class)
	public void whenSearchingWithIsInWith1001ElementsThenException()
			throws Exception {

		givenTwoZeSchemaRecords();
		List<String> inCriterion = new ArrayList<>();
		for (int i = 0; i < 1001; i++) {
			inCriterion.add("anInexistendId_" + anInteger());
		}

		LogicalSearchCondition condition = LogicalSearchQueryOperators.fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
				.isIn(inCriterion);
		searchServices.hasResults(condition);

	}

	@Test(expected = SearchServicesRuntimeException.TooManyElementsInCriterion.class)
	public void whenSearchingWithIsNotInWith1001ElementsThenException()
			throws Exception {

		givenTwoZeSchemaRecords();
		List<String> notInCriteria = new ArrayList<>();
		for (int i = 0; i < 1001; i++) {
			notInCriteria.add("anInexistendId_" + anInteger());
		}

		LogicalSearchCondition condition = LogicalSearchQueryOperators.fromAllSchemasIn(zeCollection).where(Schemas.IDENTIFIER)
				.isNotIn(notInCriteria);
		searchServices.hasResults(condition);

	}

	@Test
	public void givenRecordsFromTwoSchemaTypesWhenSearchingForEverythingThenReturnTwo()
			throws Exception {
		configureFolderAndRule();

		TestRecord expectedRecord1 = new TestRecord(zeSchema);
		TestRecord expectedRecord2 = new TestRecord(zeSchema);
		recordServices.execute(new Transaction(expectedRecord1, expectedRecord2));

		condition = fromAllSchemasIn(zeCollection).returnAll();
		List<Record> records = findRecords(condition);

		assertThat(records).contains(expectedRecord2, expectedRecord1);
	}

	@Test
	public void givenRecordsFromTwoSchemaTypesWhenReturnAllThenReturnAllRecordsFromSchema()
			throws Exception {

		givenRecordsFromTwoSchemasTypesWithReferences();

		condition = from(zeSchema.instance()).returnAll();
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(expectedRecord, expectedRecord2, expectedRecord3, expectedRecord4);
	}

	@Test
	public void givenRecordsFromTwoCollectionsWhenSearchFromAllSchemasInCollectionThenReturnTwo()
			throws Exception {

		givenSchemasInDiferentsCollections();

		LogicalSearchCondition condition = fromAllSchemasIn(zeCollection).returnAll();
		List<Record> records = findRecords(condition);

		assertThat(records)
				.contains(zeSchemaRecord1, zeSchemaRecord2, recordServices.getDocumentById(zeCollection));
	}

	@Test
	public void givenRecordsFromTwoCollectionsWhenSearchFromSchemaThenReturnTwo()
			throws Exception {

		givenSchemasInDiferentsCollections();

		LogicalSearchCondition condition = from(schema.getSchema(zeSchema.code())).returnAll();
		List<Record> records = findRecords(condition);

		assertThat(records).containsOnly(zeSchemaRecord1, zeSchemaRecord2);
	}

	@Test
	public void givenRecordsInCustomAndDefaultSchemaWhenSearchingInCustomDefaultAndTypeThenGetCorrectRecords()
			throws Exception {

		Record defaultSchemaRecord1, defaultSchemaRecord2, customSchemaRecord1, customSchemaRecord2;

		defineSchemasManager().using(schema.andCustomSchema().withAStringMetadata());
		transaction.addUpdate(defaultSchemaRecord1 = new TestRecord(zeSchema, "defaultSchemaRecord1").set(
				zeSchema.stringMetadata(), "Chuck Lechat"));
		transaction.addUpdate(defaultSchemaRecord2 = new TestRecord(zeSchema, "defaultSchemaRecord2").set(
				zeSchema.stringMetadata(), "Chuck Norris"));
		transaction.addUpdate(customSchemaRecord1 = new TestRecord(zeCustomSchema, "customSchemaRecord1").set(
				zeCustomSchema.stringMetadata(), "Chuck Lechat"));
		transaction.addUpdate(customSchemaRecord2 = new TestRecord(zeCustomSchema, "customSchemaRecord2").set(
				zeCustomSchema.stringMetadata(), "Chuck Norris"));
		recordServices.execute(transaction);

		MetadataSchemaType type = getModelLayerFactory().getMetadataSchemasManager().getSchemaTypes("zeCollection")
				.getSchemaType("zeSchemaType");

		assertThat(findRecords(from(zeSchema.instance()).returnAll()))
				.containsOnly(defaultSchemaRecord1, defaultSchemaRecord2);
		assertThat(findRecords(from(zeSchema.instance()).where(zeSchema.stringMetadata()).isContainingText("Lechat")))
				.containsOnly(defaultSchemaRecord1);

		assertThat(findRecords(from(zeCustomSchema.instance()).returnAll())).containsOnly(customSchemaRecord1,
				customSchemaRecord2);
		assertThat(findRecords(from(zeCustomSchema.instance()).where(zeSchema.stringMetadata()).isContainingText("Lechat")))
				.containsOnly(customSchemaRecord1);

		assertThat(findRecords(from(type).returnAll())).containsOnly(defaultSchemaRecord1, defaultSchemaRecord2,
				customSchemaRecord1, customSchemaRecord2);
		assertThat(findRecords(from(type).where(zeSchema.stringMetadata()).isContainingText("Lechat"))).containsOnly(
				defaultSchemaRecord1, customSchemaRecord1);
	}

	@Test
	public void givenRecordWithTitleWhenSearchSingleResultThenReturnTheRecord()
			throws Exception {

		givenSchemasInDiferentsCollections();

		LogicalSearchCondition condition = from(schema.getSchema(zeSchema.code())).where(Schemas.TITLE).isEqualTo("Folder 1");

		Record record = searchServices.searchSingleResult(condition);
		assertThat(record).isEqualTo(zeSchemaRecord1);
	}

	@Test
	public void givenNoRecordWithTitleWhenSearchSingleResultThenReturnNull()
			throws Exception {

		givenSchemasInDiferentsCollections();

		LogicalSearchCondition condition = from(schema.getSchema(zeSchema.code())).where(Schemas.TITLE).isEqualTo("Folder 3");
		Record record = searchServices.searchSingleResult(condition);

		assertThat(record).isNull();
	}

	@Test(expected = SearchServicesRuntimeException.TooManyRecordsInSingleSearchResult.class)
	public void whenSearchSingleResultReturnMoreThan1RecordThenException()
			throws Exception {

		givenSchemasInDiferentsCollections();

		LogicalSearchCondition condition = from(schema.getSchema(zeSchema.code())).where(Schemas.TITLE).isStartingWithText(
				"Folder");
		Record record = searchServices.searchSingleResult(condition);

		assertThat(record.<String>get(Schemas.TITLE)).isEqualTo("Folder 1");
	}

	@Test
	public void whenNegatingSearchForRecordsContainingTextANDIsStartingWithTextThenDontFindThem()
			throws Exception {
		Record unexpectedRecord;

		defineSchemasManager().using(schema.withAStringMetadata());
		transaction.addUpdate(unexpectedRecord = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Norris"));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Chuck Lechat"));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Edouard Norris"));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "Edouard Chuck Lechat"));
		recordServices.execute(transaction);

		condition = from(zeSchema.instance()).where(zeSchema.stringMetadata()).isContainingText("Norris")
				.and(asList(startingWithText("Chuck"))).negate();
		List<Record> records = findRecords(condition);

		assertThat(records).hasSize(3).doesNotContain(unexpectedRecord);
	}

	@Test
	public void whenSearchingForRecordsContainingTextAndNotHavingUnwantedTitleThenDontFindExcludedDocument()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata());

		Record expected = newRecordOfZeSchema();
		expected.set(zeSchema.metadata("title"), "Wanted");
		expected.set(zeSchema.stringMetadata(), "Chuck Norris");

		Record unexpected = newRecordOfZeSchema();
		unexpected.set(zeSchema.metadata("title"), "Unwanted");
		unexpected.set(zeSchema.stringMetadata(), "Chuck Norris");

		transaction.addUpdate(expected, unexpected);
		recordServices.execute(transaction);

		condition = allConditions(
				from(zeSchema.instance()).where(zeSchema.stringMetadata()).isContainingText("Chuck"),
				from(zeSchema.instance()).where(zeSchema.metadata("title")).isEqualTo("Unwanted").negate());
		List<Record> records = findRecords(condition);

		assertThat(records).containsExactly(expected);
	}

	@Test
	public void whenSearchingForRecordsContainingTextAndNotHavingUnwantedTitleOrTextThenDontFindExcludedDocuments()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata());

		Record expected = newRecordOfZeSchema();
		expected.set(zeSchema.metadata("title"), "Wanted");
		expected.set(zeSchema.stringMetadata(), "Chuck Norris");

		Record unexpected1 = newRecordOfZeSchema();
		unexpected1.set(zeSchema.metadata("title"), "Unwanted");
		unexpected1.set(zeSchema.stringMetadata(), "Chuck Norris");

		Record unexpected2 = newRecordOfZeSchema();
		unexpected2.set(zeSchema.metadata("title"), "Not excluded by the title");
		unexpected2.set(zeSchema.stringMetadata(), "Chuck Lechat");

		transaction.addUpdate(expected, unexpected1, unexpected2);
		recordServices.execute(transaction);

		condition = allConditions(
				from(zeSchema.instance()).where(zeSchema.stringMetadata()).isContainingText("Chuck"),
				anyConditions(
						from(zeSchema.instance()).where(zeSchema.metadata("title")).isEqualTo("Unwanted"),
						from(zeSchema.instance()).where(zeSchema.stringMetadata()).isContainingText("Lechat")).negate());
		List<Record> records = findRecords(condition);

		assertThat(records).containsExactly(expected);
	}

	@Test
	public void whenSearchingWithMultipleNegationsThenDontFindExcludedDocuments()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata());

		Record expected = newRecordOfZeSchema();
		expected.set(zeSchema.metadata("title"), "Wanted");
		expected.set(zeSchema.stringMetadata(), "Chuck Norris");

		Record unexpected1 = newRecordOfZeSchema();
		unexpected1.set(zeSchema.metadata("title"), "Unwanted");
		unexpected1.set(zeSchema.stringMetadata(), "Chuck Norris");

		Record unexpected2 = newRecordOfZeSchema();
		unexpected2.set(zeSchema.metadata("title"), "Not excluded by the title");
		unexpected2.set(zeSchema.stringMetadata(), "Chuck Lechat");

		transaction.addUpdate(expected, unexpected1, unexpected2);
		recordServices.execute(transaction);

		condition = allConditions(
				from(zeSchema.instance()).where(zeSchema.stringMetadata()).isContainingText("Chuck"),
				allConditions(
						from(zeSchema.instance()).where(zeSchema.metadata("title")).isEqualTo("Unwanted").negate(),
						from(zeSchema.instance()).where(zeSchema.stringMetadata()).isContainingText("Lechat").negate()));
		List<Record> records = findRecords(condition);

		assertThat(records).containsExactly(expected);
	}

	@Test
	@SlowTest
	public void givenRecordsModifiedWhenIteratingOverSearchResultsThenDoesNotAffectIteration()
			throws RecordServicesException {
		defineSchemasManager().using(schema);

		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		for (int i = 0; i < 10000; i++) {
			Record record = new TestRecord(schema.zeDefaultSchema(), "" + i);
			record.set(Schemas.TITLE, "zeTitleInitial");
			transaction.addUpdate(record);
		}
		recordServices.execute(transaction);

		Set<String> ids = new HashSet<>();

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(schema.zeDefaultSchema()).where(Schemas.TITLE).isStartingWithText("zeTitle"));
		Iterator<Record> records = searchServices.recordsIterator(query);

		int i = 0;
		while (records.hasNext()) {

			Record record = records.next();
			ids.add(record.getId());
			if (i % 10 == 0) {

				record.set(Schemas.TITLE, "zeTitleModified");
				recordServices.update(record);
			}
			System.out.println(++i);
		}

		assertThat(ids).hasSize(10000);

	}

	@Test
	@SlowTest
	public void givenRecordsModifiedWhenIteratingOverSearchResultsWithSortOnOtherFieldThenDoesNotAffectIteration()
			throws RecordServicesException {
		defineSchemasManager().using(schema.withCodeInZeSchema());

		Transaction transaction = new Transaction();
		transaction.setOptimisticLockingResolution(OptimisticLockingResolution.EXCEPTION);
		for (int i = 0; i < 10000; i++) {
			Record record = new TestRecord(schema.zeDefaultSchema(), "" + i);
			record.set(Schemas.TITLE, "zeTitleInitial");
			record.set(Schemas.CODE, "" + i);
			transaction.addUpdate(record);
		}
		recordServices.execute(transaction);

		Set<String> ids = new HashSet<>();

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.sortDesc(zeSchema.metadata("code"));
		query.setCondition(from(schema.zeDefaultSchema()).where(Schemas.TITLE).isStartingWithText("zeTitle"));
		Iterator<Record> records = searchServices.recordsIteratorKeepingOrder(query, 25);

		int i = 0;
		while (records.hasNext()) {

			Record record = records.next();
			ids.add(record.getId());
			if (i % 10 == 0) {

				record.set(Schemas.TITLE, "zeTitleModified");
				recordServices.update(record);
			}
			System.out.println(++i);
		}

		assertThat(ids).hasSize(10000);

	}

	@Test
	public void givenTransientLazyMetadataThenNotSavedAndRetrievedOnRecordRecalculate()
			throws Exception {

		defineSchemasManager().using(schema.withANumberMetadata(
				whichIsCalculatedUsing(TitleLengthCalculator.class),
				whichHasTransiency(TRANSIENT_LAZY)));

		//TODO records in cache should lost volatile metadatas
		Record record = new TestRecord(zeSchema).set(TITLE, "Vodka Framboise");
		recordServices.add(record);

		assertThat(searchServices.hasResults(from(zeSchema.type()).where(zeSchema.numberMetadata()).isNotNull())).isFalse();

		assertThat(searchServices.searchSingleResult(from(zeSchema.type()).returnAll()).<Double>get(zeSchema.numberMetadata())).isNull();

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeSchema.typeCode()).setRecordCacheType(RecordCacheType.FULLY_CACHED);
			}
		});

		searchServices.searchSingleResult(from(zeSchema.type()).returnAll());
		Record recordInCache = getModelLayerFactory().getRecordsCaches().getCache(zeCollection).get(record.getId());
		assertThat(recordInCache.<Double>get(zeSchema.numberMetadata())).isNull();
	}

	@Test
	public void givenTransientEagerMetadataThenNotSavedAndRetrievedOnRecordRetrieval()
			throws Exception {

		defineSchemasManager().using(schema.withANumberMetadata(
				whichIsCalculatedUsing(TitleLengthCalculator.class),
				whichHasTransiency(TRANSIENT_EAGER)));

		Record record = new TestRecord(zeSchema).set(TITLE, "Vodka Framboise");
		recordServices.add(record);

		assertThat(searchServices.hasResults(from(zeSchema.type()).where(zeSchema.numberMetadata()).isNotNull())).isFalse();

		assertThat(searchServices.searchSingleResult(from(zeSchema.type()).returnAll()).<Double>get(zeSchema.numberMetadata()))
				.isEqualTo(15.0);

		getModelLayerFactory().getMetadataSchemasManager().modify(zeCollection, new MetadataSchemaTypesAlteration() {
			@Override
			public void alter(MetadataSchemaTypesBuilder types) {
				types.getSchemaType(zeSchema.typeCode()).setRecordCacheType(RecordCacheType.FULLY_CACHED);
			}
		});

		searchServices.searchSingleResult(from(zeSchema.type()).returnAll());
		Record recordInCache = getModelLayerFactory().getRecordsCaches().getCache(zeCollection).get(record.getId());
		assertThat(recordInCache.<Double>get(zeSchema.numberMetadata())).isEqualTo(15.0);

	}

	@Test
	public void whenFreeTextSearchingWithSlashesThenNotSplitted()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata(whichIsSearchable));

		Record record1 = newRecordOfZeSchema("record1");
		record1.set(zeSchema.stringMetadata(), "2007-2008/1");

		Record record2 = newRecordOfZeSchema("record2");
		record2.set(zeSchema.stringMetadata(), "2007-2008/100");

		Record record3 = newRecordOfZeSchema("record3");
		record3.set(zeSchema.stringMetadata(), "2007-2008/2");

		transaction.addUpdate(record1, record2, record3);
		recordServices.execute(transaction);

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(zeSchema.instance()).returnAll());

		query.setFreeTextQuery("\"2007-2008/1\"");
		assertThat(searchServices.search(query)).extracting("id").containsOnly("record1");

		query.setFreeTextQuery("\"2007-2008/100\"");
		assertThat(searchServices.search(query)).extracting("id").containsOnly("record2");

		query.setFreeTextQuery("\"2007-2008/2\"");
		assertThat(searchServices.search(query)).extracting("id").containsOnly("record3");

		query.setFreeTextQuery("2007-2008/1");
		assertThat(searchServices.search(query)).extracting("id").containsOnly("record1", "record2", "record3");

		query.setFreeTextQuery("2007-2008/100");
		assertThat(searchServices.search(query)).extracting("id").containsOnly("record1", "record2", "record3");

		query.setFreeTextQuery("2007-2008/2");
		assertThat(searchServices.search(query)).extracting("id").containsOnly("record1", "record2", "record3");
	}

	private void givenSchemasInDiferentsCollections()
			throws Exception {

		defineSchemasManager().using(schema.withATitle());
		transaction.addUpdate(zeSchemaRecord1 = newRecordOfZeSchema().set(zeSchema.metadata("title"), "Folder 1"));
		transaction.addUpdate(zeSchemaRecord2 = newRecordOfZeSchema().set(zeSchema.metadata("title"), "Folder 2"));
		recordServices.execute(transaction);

		defineSchemasManager().using(otherSchema.withATitle());
		Transaction transaction2 = new Transaction();
		transaction2.addUpdate(otherSchemaRecord1InCollection2 = newRecordOfOotherSchemaInCollection2().set(
				otherSchemaInCollection2.metadata("title"), "Folder 3"));
		transaction2.addUpdate(otherSchemaRecord2InCollection2 = newRecordOfOotherSchemaInCollection2().set(
				otherSchemaInCollection2.metadata("title"), "Folder 4"));
		recordServices.execute(transaction2);

	}

	@Test
	public void whenFreeTextSearchingOnLegacyIdsThenSameLegacyIdBoosted()
			throws Exception {
		defineSchemasManager().using(schema.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				schemaTypes.getSchema("zeSchemaType_default").get(Schemas.LEGACY_ID).setSearchable(true);
			}
		}));

		Record record3 = newRecordOfZeSchema("record3");
		record3.set(Schemas.LEGACY_ID, "10340");

		Record record2 = newRecordOfZeSchema("record2");
		record2.set(Schemas.LEGACY_ID, "103400");

		Record record1 = newRecordOfZeSchema("record1");
		record1.set(Schemas.LEGACY_ID, "1034002");

		transaction.addUpdate(record1, record2, record3);
		recordServices.execute(transaction);

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(zeSchema.instance()).returnAll());

		query.setFreeTextQuery("\"10340\"");
		assertThat(searchServices.search(query)).extracting("id").containsExactly("record3");

		query.setFreeTextQuery("\"103400\"");
		assertThat(searchServices.search(query)).extracting("id").containsOnly("record2");

		query.setFreeTextQuery("\"1034002\"");
		assertThat(searchServices.search(query)).extracting("id").containsOnly("record1");

		query.setFreeTextQuery("10340");
		assertThat(searchServices.search(query)).extracting("id").containsOnly("record3");

		query.setFreeTextQuery("103400");
		assertThat(searchServices.search(query)).extracting("id").containsOnly("record2");

		query.setFreeTextQuery("1034002");
		assertThat(searchServices.search(query)).extracting("id").containsOnly("record1");

	}

	@Test
	public void givenRecordsInAnotherDataStoreThenFoundDependingOnSearchedDataStore()
			throws Exception {
		defineSchemasManager().using(schema.whichIsIsStoredInDataStore("events"));
		assertThat(searchServices.getResultsCount(from(zeSchema.type()).returnAll())).isEqualTo(0);
		assertThat(searchServices.getResultsCount(from(anotherSchema.type()).returnAll())).isEqualTo(0);

		long otherRecordsInRecordsDataStore = searchServices.getResultsCount(fromEveryTypesOfEveryCollection().returnAll());

		Transaction tx = new Transaction();
		tx.add(new TestRecord(zeSchema.instance(), "record1"));
		tx.add(new TestRecord(zeSchema.instance(), "record2"));
		tx.add(new TestRecord(anotherSchema.instance(), "record3"));
		tx.add(new TestRecord(anotherSchema.instance(), "record4"));
		tx.add(new TestRecord(anotherSchema.instance(), "record5"));

		recordServices.execute(tx);

		assertThat(searchServices.getResultsCount(from(zeSchema.type()).returnAll())).isEqualTo(2);
		assertThat(searchServices.getResultsCount(from(anotherSchema.type()).returnAll())).isEqualTo(3);
		assertThat(searchServices.getResultsCount(fromEveryTypesOfEveryCollection().returnAll()))
				.isEqualTo(otherRecordsInRecordsDataStore + 3);
		assertThat(searchServices.getResultsCount(fromEveryTypesOfEveryCollectionInDataStore(RECORDS).returnAll()))
				.isEqualTo(otherRecordsInRecordsDataStore + 3);
		assertThat(searchServices.getResultsCount(fromEveryTypesOfEveryCollectionInDataStore(EVENTS).returnAll())).isEqualTo(2);
		assertThat(searchServices.getResultsCount(from(zeSchema.instance()).returnAll())).isEqualTo(2);
		assertThat(searchServices.getResultsCount(from(anotherSchema.instance()).returnAll())).isEqualTo(3);

	}

	@Test
	public void givenHiddenRecordsThenOnlyReturnedByQueriesIfShowHiddenEnable() throws Exception {
		defineSchemasManager().using(schema);

		Transaction tx = new Transaction();
		tx.add(newRecordOfZeSchema("r1").set(Schemas.TITLE, "Apple").set(Schemas.HIDDEN, true));
		tx.add(newRecordOfZeSchema("r2").set(Schemas.TITLE, "Banana"));
		tx.add(newRecordOfZeSchema("r3").set(Schemas.TITLE, "Kiwi").set(Schemas.HIDDEN, true));
		tx.add(newRecordOfZeSchema("r4").set(Schemas.TITLE, "Orange"));
		tx.add(newRecordOfZeSchema("r5").set(Schemas.TITLE, "Melon").set(Schemas.HIDDEN, true));
		recordServices.execute(tx);

		LogicalSearchQuery query = new LogicalSearchQuery(from(schema.zeDefaultSchemaType()).returnAll());

		assertThat(searchServices.searchRecordIds(query)).containsOnly("r2", "r4");
		assertThat(searchServices.searchRecordIds(query.filteredByVisibilityStatus(ALL)))
				.containsOnly("r1", "r2", "r3", "r4", "r5");
		assertThat(searchServices.searchRecordIds(query.filteredByVisibilityStatus(HIDDENS)))
				.containsOnly("r1", "r3", "r5");
		assertThat(searchServices.searchRecordIds(query.filteredByVisibilityStatus(VISIBLES)))
				.containsOnly("r2", "r4");

		assertThat(recordServices.getDocumentById("r1")).isNotNull();
	}

	@Test
	public void givenRegexBoostThenAffectScores()
			throws Exception {
		defineSchemasManager().using(schema.with(new MetadataSchemaTypesConfigurator() {
			@Override
			public void configure(MetadataSchemaTypesBuilder schemaTypes) {
				schemaTypes.getSchema("zeSchemaType_default").get(Schemas.LEGACY_ID).setSearchable(true);
			}
		}));

		Transaction tx = new Transaction();
		tx.add(newRecordOfZeSchema("r1").set(Schemas.TITLE, "Apple"));
		tx.add(newRecordOfZeSchema("r2").set(Schemas.TITLE, "Banana"));
		tx.add(newRecordOfZeSchema("r3").set(Schemas.TITLE, "Kiwi"));
		tx.add(newRecordOfZeSchema("r4").set(Schemas.TITLE, "Orange"));
		tx.add(newRecordOfZeSchema("r5").set(Schemas.TITLE, "Melon"));
		recordServices.execute(tx);

		SearchBoostManager searchBoostManager = getModelLayerFactory().getSearchBoostManager();
		searchBoostManager.add(zeCollection, createRegexBoost("title_s", "Kiwi", "My boost", 12.0));
		searchBoostManager.add(zeCollection, createRegexBoost("title_s", "*o*", "My boost", 5.0));
		searchBoostManager.add(zeCollection, createRegexBoost("title_s", "*O*", "My boost", 6.0));

		LogicalSearchQuery query = new LogicalSearchQuery().setCondition(from(zeSchema.instance()).returnAll())
				.setFreeTextQuery("*");
		query.setQueryBoosts(searchBoostManager.getAllSearchBoostsByQueryType(zeCollection));

		assertThat(searchServices.search(query)).extracting("id").containsExactly("r3", "r4", "r5", "r1", "r2");

	}

	@Test
	public void whenSearchingWithFreeTextThenQfDoesNotContainDuplicateAnalyzedFields()
			throws Exception {
		defineSchemasManager().using(
				schema.withAStringMetadata(whichIsSearchable).withAnotherStringMetadata(whichIsSearchable).withADateMetadata()
						.withAStringMetadataInAnotherSchema(whichIsSearchable).withANumberMetadataInAnotherSchema(whichIsSearchable));

		condition = from(asList(zeSchema.typeCode(), anotherSchema.typeCode()), zeSchema.collection()).returnAll();
		LogicalSearchQuery query = new LogicalSearchQuery(condition).setFreeTextQuery("myFreeText");
		ModifiableSolrParams modifiableSolrParams = searchServices.addSolrModifiableParams(query);
		String qf = modifiableSolrParams.get("qf");

		List<String> analyzedFields = asList(qf.split(" "));
		HashSet uniqueAnalyzedFields = new HashSet<>(analyzedFields);
		assertThat(uniqueAnalyzedFields).hasSize(analyzedFields.size());
		assertThat(uniqueAnalyzedFields).doesNotContain("dateMetadata.search_ss");
	}

	private void givenRecordsFromTwoSchemasTypesWithReferences()
			throws Exception {
		configureFolderAndRule();
		executeTransaction1();
		executeTransaction2();
	}

	private void executeTransaction2()
			throws RecordServicesException {
		Transaction transaction2 = new Transaction();
		transaction2.addUpdate(expectedRecord = zeSchemaRecord1.set(zeSchema.metadata("anotherSchemaRecord"),
				anotherSchemaRecord1.getId()));
		transaction2.addUpdate(expectedRecord2 = zeSchemaRecord2.set(zeSchema.metadata("anotherSchemaRecord"),
				anotherSchemaRecord2.getId()));
		transaction2.addUpdate(expectedRecord3 = zeSchemaRecord3.set(zeSchema.metadata("anotherSchemaRecord"),
				anotherSchemaRecord2.getId()));
		transaction2.addUpdate(expectedRecord4 = zeSchemaRecord4.set(zeSchema.metadata("anotherSchemaRecord"),
				anotherSchemaRecord2.getId()));
		recordServices.execute(transaction2);
	}

	private void executeTransaction1()
			throws RecordServicesException {
		transaction.addUpdate(zeSchemaRecord1 = newRecordOfZeSchema().set(zeSchema.metadata("title"), "Folder 1"));
		transaction.addUpdate(zeSchemaRecord2 = newRecordOfZeSchema().set(zeSchema.metadata("title"), "Folder 2"));
		transaction.addUpdate(zeSchemaRecord3 = newRecordOfZeSchema().set(zeSchema.metadata("title"), "Folder 3"));
		transaction.addUpdate(zeSchemaRecord4 = newRecordOfZeSchema().set(zeSchema.metadata("title"), "Other folder"));
		transaction.addUpdate(anotherSchemaRecord1 = newRecordOfAnotherSchema().set(anotherSchema.metadata("code"), "10"));
		transaction.addUpdate(anotherSchemaRecord2 = newRecordOfAnotherSchema().set(anotherSchema.metadata("code"), "20"));
		recordServices.execute(transaction);
	}

	private void configureFolderAndRule()
			throws Exception {
		MetadataBuilderConfigurator configurator = new MetadataBuilderConfigurator() {
			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {

				MetadataSchemaTypeBuilder zeSchemaTypeBuilder = schemaTypes.getSchemaType(ZE_SCHEMA_TYPE_CODE);
				MetadataSchemaTypeBuilder anotherSchemaTypeBuilder = schemaTypes.getSchemaType(ANOTHER_SCHEMA_TYPE_CODE);

				zeSchemaTypeBuilder.getDefaultSchema().create("anotherSchemaRecord")
						.defineReferencesTo(anotherSchemaTypeBuilder);

				anotherSchemaTypeBuilder.getDefaultSchema().create("code").setType(MetadataValueType.STRING);
			}
		};
		defineSchemasManager().using(schema.withAMetadata(configurator));
	}

	private void givenFourLettersValues()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata());
		transaction.addUpdate(expectedRecord = new TestRecord(zeSchema, "expectedRecord").set(zeSchema.stringMetadata(), "A"));
		transaction
				.addUpdate(expectedRecord2 = new TestRecord(zeSchema, "expectedRecord2").set(zeSchema.stringMetadata(), "B"));
		transaction
				.addUpdate(expectedRecord3 = new TestRecord(zeSchema, "expectedRecord3").set(zeSchema.stringMetadata(), "C"));
		transaction
				.addUpdate(expectedRecord4 = new TestRecord(zeSchema, "expectedRecord4").set(zeSchema.stringMetadata(), "D"));
		recordServices.execute(transaction);
	}

	private void givenFiveDateTimeValuesIncludingNullDateTime()
			throws Exception {
		defineSchemasManager().using(schema.withADateTimeMetadata());
		transaction.addUpdate(expectedRecord5 = new TestRecord(zeSchema, "recordWithNullDate"));
		transaction.addUpdate(
				expectedRecord = new TestRecord(zeSchema, "dateTime1Record").set(zeSchema.dateTimeMetadata(), DATE_TIME1));
		transaction.addUpdate(
				expectedRecord2 = new TestRecord(zeSchema, "dateTime2Record").set(zeSchema.dateTimeMetadata(), DATE_TIME2));
		transaction.addUpdate(
				expectedRecord3 = new TestRecord(zeSchema, "dateTime3Record").set(zeSchema.dateTimeMetadata(), DATE_TIME3));
		transaction.addUpdate(
				expectedRecord4 = new TestRecord(zeSchema, "dateTime4Record").set(zeSchema.dateTimeMetadata(), DATE_TIME4));
		recordServices.execute(transaction);
	}

	private void givenFiveDateValuesIncludingNullDate()
			throws Exception {
		defineSchemasManager().using(schema.withADateMetadata());
		transaction.addUpdate(expectedRecord5 = new TestRecord(zeSchema, "recordWithNullDate"));
		transaction.addUpdate(
				expectedRecord = new TestRecord(zeSchema, "date1Record").set(zeSchema.dateMetadata(), DATE1));
		transaction.addUpdate(
				expectedRecord2 = new TestRecord(zeSchema, "date2Record").set(zeSchema.dateMetadata(), DATE2));
		transaction.addUpdate(
				expectedRecord3 = new TestRecord(zeSchema, "date3Record").set(zeSchema.dateMetadata(), DATE3));
		transaction.addUpdate(
				expectedRecord4 = new TestRecord(zeSchema, "date4Record").set(zeSchema.dateMetadata(), DATE4));
		recordServices.execute(transaction);
	}

	private void givenFiveRelativeDatesDateValuesIncludingNullDate()
			throws Exception {
		defineSchemasManager().using(schema.withADateMetadata());
		transaction.addUpdate(expectedRecord5 = new TestRecord(zeSchema, "recordWithNullDate"));
		transaction.addUpdate(
				expectedRecord = new TestRecord(zeSchema, "date1Record")
						.set(zeSchema.dateMetadata(), NOW.toLocalDate().minusYears(1)));
		transaction.addUpdate(
				expectedRecord2 = new TestRecord(zeSchema, "date2Record")
						.set(zeSchema.dateMetadata(), NOW.toLocalDate().minusYears(2)));
		transaction.addUpdate(
				expectedRecord3 = new TestRecord(zeSchema, "date3Record")
						.set(zeSchema.dateMetadata(), NOW.toLocalDate().minusYears(4)));
		transaction.addUpdate(
				expectedRecord4 = new TestRecord(zeSchema, "date4Record")
						.set(zeSchema.dateMetadata(), NOW.toLocalDate().plusYears(1)));
		recordServices.execute(transaction);
	}

	private void givenFiveNumberValuesIncludingNegativeNumberAndMinIntegerValue()
			throws Exception {
		defineSchemasManager().using(schema.withANumberMetadata());
		transaction.addUpdate(expectedRecord = new TestRecord(zeSchema, "expectedRecord").set(zeSchema.numberMetadata(), null));
		transaction.addUpdate(expectedRecord = new TestRecord(zeSchema, "expectedRecord").set(zeSchema.numberMetadata(), -20));
		transaction
				.addUpdate(expectedRecord2 = new TestRecord(zeSchema, "expectedRecord2").set(zeSchema.numberMetadata(), -10));
		transaction.addUpdate(expectedRecord3 = new TestRecord(zeSchema, "expectedRecord3").set(zeSchema.numberMetadata(), 2));
		transaction.addUpdate(expectedRecord4 = new TestRecord(zeSchema, "expectedRecord4").set(zeSchema.numberMetadata(), 10));
		recordServices.execute(transaction);
	}

	@SuppressWarnings("rawtypes")
	private void givenNumberMultiValueMetadataWithThreeNumbers()
			throws Exception {
		defineSchemasManager().using(schema.withANumberMetadata(multiValueConfigurator()));
		List numberList = Arrays.asList(-10, 0, 1);
		transaction.addUpdate(expectedRecord = new TestRecord(zeSchema, "expectedRecord").set(zeSchema.numberMetadata(),
				numberList));
		recordServices.execute(transaction);
	}

	@SuppressWarnings("rawtypes")
	private void givenTextMultiValueMetadataWithThreeTexts()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata(multiValueConfigurator()));
		List textList = Arrays.asList("Chuck Norris", "Edouard Norris", "Edouard Lechat");
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.stringMetadata(), textList));
		recordServices.execute(transaction);
	}

	@SuppressWarnings("rawtypes")
	private void givenDateTimeMultiValueMetadataWithThreeDateTimes()
			throws Exception {
		defineSchemasManager().using(schema.withADateTimeMetadata(multiValueConfigurator()));
		List dateList = Arrays.asList(DATE_TIME1, DATE_TIME2, DATE_TIME3);
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.dateTimeMetadata(), dateList));
		recordServices.execute(transaction);
	}

	@SuppressWarnings("rawtypes")
	private void givenDateMultiValueMetadataWithThreeDates()
			throws Exception {
		defineSchemasManager().using(schema.withADateMetadata(multiValueConfigurator()));
		List dateList = Arrays.asList(DATE1, DATE2, DATE3);
		transaction.addUpdate(expectedRecord = newRecordOfZeSchema().set(zeSchema.dateMetadata(), dateList));
		recordServices.execute(transaction);
	}

	private MetadataBuilderConfigurator multiValueConfigurator() {
		MetadataBuilderConfigurator configurator = new MetadataBuilderConfigurator() {
			@Override
			public void configure(MetadataBuilder builder, MetadataSchemaTypesBuilder schemaTypes) {
				builder.setMultivalue(true);
			}
		};
		return configurator;
	}

	private void givenFiveTextValues()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata());
		transaction.addUpdate(expectedRecord = new TestRecord(zeSchema, "expectedRecord").set(zeSchema.stringMetadata(),
				"Chuck Lechat"));
		transaction.addUpdate(expectedRecord2 = new TestRecord(zeSchema, "expectedRecord2").set(zeSchema.stringMetadata(),
				"Chuck Norris"));
		transaction.addUpdate(expectedRecord3 = new TestRecord(zeSchema, "expectedRecord3").set(zeSchema.stringMetadata(),
				"Edouard Lechat"));
		transaction.addUpdate(expectedRecord4 = new TestRecord(zeSchema, "expectedRecord4").set(zeSchema.stringMetadata(),
				"zeEdouard Lechat"));
		transaction.addUpdate(expectedRecord5 = new TestRecord(zeSchema, "expectedRecord5").set(zeSchema.stringMetadata(),
				"zeEdouard *"));
		recordServices.execute(transaction);
	}

	private void givenTwoZeSchemaRecords()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata());
		transaction.addUpdate(zeSchemaRecord1 = new TestRecord(zeSchema, "expectedRecord").set(zeSchema.stringMetadata(),
				"Chuck Lechat"));
		transaction.addUpdate(zeSchemaRecord2 = new TestRecord(zeSchema, "unexpectedRecord").set(zeSchema.stringMetadata(),
				"Chuck Norris"));
		recordServices.execute(transaction);
	}

	private void givenFourTextValuesIncludingNull()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata());
		transaction.addUpdate(expectedRecord = new TestRecord(zeSchema, "expectedRecord").set(zeSchema.stringMetadata(),
				"__A__"));
		transaction
				.addUpdate(expectedRecord2 = new TestRecord(zeSchema, "expectedRecord2").set(zeSchema.stringMetadata(), null));
		transaction.addUpdate(expectedRecord3 = new TestRecord(zeSchema, "expectedRecord3").set(zeSchema.stringMetadata(),
				"__T__"));
		transaction.addUpdate(expectedRecord4 = new TestRecord(zeSchema, "expectedRecord4").set(zeSchema.stringMetadata(),
				"__Z__"));
		recordServices.execute(transaction);
	}

	private List<Record> findRecords(LogicalSearchCondition condition) {
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		return searchServices.search(query);
	}

	private SPEQueryResponse query(LogicalSearchCondition condition) {
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		return searchServices.query(query);
	}

	private List<String> findRecordIds(LogicalSearchCondition condition) {
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(condition);
		return searchServices.searchRecordIds(query);
	}

	private Record givenARecord(String record) {
		return new TestRecord(zeSchema, record);
	}

	private Record newRecordOfZeSchema() {
		return recordServices.newRecordWithSchema(zeSchema.instance());
	}

	private Record newRecordOfZeSchema(String id) {
		return recordServices.newRecordWithSchema(zeSchema.instance(), id);
	}

	private Record newRecordOfAnotherSchema() {
		return new TestRecord(anotherSchema);
	}

	private Record newRecordOfOotherSchemaInCollection2() {
		return new TestRecord(otherSchemaInCollection2);
	}

	public static final class TitleLengthCalculator extends AbstractMetadataValueCalculator<Double> {

		LocalDependency<String> titleDependency = LocalDependency.toAString(Schemas.TITLE.getLocalCode());

		@Override
		public Double calculate(CalculatorParameters parameters) {
			return (double) parameters.get(titleDependency).length();
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
			return asList(titleDependency);
		}
	}
}
