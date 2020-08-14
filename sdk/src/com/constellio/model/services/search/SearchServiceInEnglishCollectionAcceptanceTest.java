package com.constellio.model.services.search;

import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.ongoing.OngoingLogicalSearchConditionWithDataStoreFields;
import com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

// Confirm @SlowTest
public class SearchServiceInEnglishCollectionAcceptanceTest extends ConstellioTest {
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

	ConditionTemplateFactory factory;

	Transaction transaction;

	Record record1, record2, record3, record4, record5;

	@Before
	public void setUp() {

		givenSystemLanguageIs(Language.English.getCode());
		givenCollection(zeCollection, Arrays.asList(Language.English.getCode()));
		recordServices = getModelLayerFactory().newRecordServices();
		recordDao = spy(getDataLayerFactory().newRecordDao());
		searchServices = new SearchServices(recordDao, getModelLayerFactory());

		transaction = new Transaction();
		factory = new ConditionTemplateFactory(getModelLayerFactory(), zeCollection);
	}

	@Test
	public void whenSearchingWithPrefereAnalyzedFlagInEnglishCollectionThenUseAnalyzedFields()
			throws Exception {
		defineSchemasManager().using(schema.withAStringMetadata(whichIsSearchable).withAnotherStringMetadata());
		transaction.addUpdate(record1 = givenARecord("record1")
				.set(zeSchema.stringMetadata(),
						"When Alexander Bell invented the telephone he had 3 missed calls from Chuck Norris"));
		transaction.addUpdate(record2 = givenARecord("record2")
				.set(zeSchema.stringMetadata(),
						"Fear of spiders is aracnaphobia, fear of tight spaces is chlaustraphobia, fear of Chuck Norris is called Logic"));
		transaction.addUpdate(record3 = givenARecord("record3")
				.set(zeSchema.stringMetadata(), "Some magicans can walk on water, Chuck Norris can swim through land."));
		transaction.addUpdate(record4 = givenARecord("record4")
				.set(zeSchema.stringMetadata(),
						"When the Boogeyman goes to sleep every night, he checks in fear his closet for Chuck Norris.")
				.set(zeSchema.anotherStringMetadata(),
						"Dakota l'indien is Chuck Norris idol"));
		transaction.addUpdate(record5 = givenARecord("record5").set(zeSchema.stringMetadata(), "Chuck Norris"));
		recordServices.execute(transaction);

		OngoingLogicalSearchConditionWithDataStoreFields whereStringMetadata = from(zeSchema.instance())
				.where(zeSchema.stringMetadata());

		assertThat(searchServices.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("Chuck Norris"))))
				.containsOnly(record5);
		assertThat(searchServices.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("Boogeyman"))))
				.isEmpty();
		assertThat(searchServices.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("boogeyman"))))
				.isEmpty();
		assertThat(searchServices.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("swim"))))
				.isEmpty();
		assertThat(searchServices.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("magican"))))
				.isEmpty();
		assertThat(searchServices.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("idol"))))
				.isEmpty();
		assertThat(searchServices.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("Chuck Norris")
				.andWhere(zeSchema.anotherStringMetadata()).isEqualTo("Dakota"))))
				.isEmpty();

		assertThat(searchServices
				.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("Chuck Norris")).setPreferAnalyzedFields(true)))
				.containsOnly(record1, record2, record3, record4, record5);
		assertThat(searchServices
				.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("Boogeyman")).setPreferAnalyzedFields(true)))
				.containsOnly(record4);
		assertThat(searchServices
				.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("boogeyman")).setPreferAnalyzedFields(true)))
				.containsOnly(record4);
		assertThat(searchServices
				.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("swim")).setPreferAnalyzedFields(true)))
				.containsOnly(record3);
		assertThat(searchServices
				.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("magican")).setPreferAnalyzedFields(true)))
				.containsOnly(record3);
		assertThat(searchServices
				.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("idol")).setPreferAnalyzedFields(true)))
				.isEmpty();
		assertThat(searchServices.search(new LogicalSearchQuery(whereStringMetadata.isEqualTo("Chuck Norris")
				.andWhere(zeSchema.anotherStringMetadata()).isEqualTo("Dakota")).setPreferAnalyzedFields(true)))
				.isEmpty();
	}

	private Record givenARecord(String record) {
		return new TestRecord(zeSchema, record);
	}
}
