package com.constellio.model.services.records.reindexing;

import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.ConditionTemplate;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.TestRecord;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.setups.Users;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.constellio.model.services.records.reindexing.ReindexationMode.RECALCULATE;
import static com.constellio.model.services.records.reindexing.ReindexationMode.RECALCULATE_AND_REWRITE;
import static com.constellio.model.services.records.reindexing.ReindexationMode.REWRITE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory.autocompleteFieldMatching;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSchemaAutocomplete;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static org.assertj.core.api.Assertions.assertThat;

// Confirm @SlowTest
public class ReindexingServicesOneSchemaWithPopulatedFieldsAcceptanceTest extends ConstellioTest {

	LocalDateTime shishOClock = new LocalDateTime();
	LocalDateTime tockOClock = shishOClock.plusHours(5);

	TestsSchemasSetup schemas = new TestsSchemasSetup();
	TestsSchemasSetup.ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	//TestsSchemasSetup.AnotherSchemaMetadatas anotherSchema = schemas.new AnotherSchemaMetadatas();

	RecordServices recordServices;
	ReindexingServices reindexingServices;
	RecordDao recordDao;

	Users users = new Users();
	String dakotaId;

	@Before
	public void setup()
			throws Exception {
		givenDisabledAfterTestValidations();
		prepareSystem(
				withZeCollection().withAllTest(users)
		);
		inCollection(zeCollection).giveWriteAccessTo(dakota);

		recordServices = getModelLayerFactory().newRecordServices();
		reindexingServices = getModelLayerFactory().newReindexingServices();
		recordDao = getDataLayerFactory().newRecordDao();

		defineSchemasManager().using(schemas
				.withAStringMetadata(whichIsSchemaAutocomplete)
				.withALargeTextMetadata(whichIsSearchable));
	}

	@Test
	public void te()
			throws Exception {

		givenTimeIs(shishOClock);
		Transaction transaction = new Transaction();
		transaction.setUser(users.dakotaLIndienIn(zeCollection));
		transaction.add(new TestRecord(zeSchema, "000042"))
				.set(zeSchema.stringMetadata(), "AC42")
				.set(zeSchema.largeTextMetadata(), "Il y a un serpent dans ma botte");

		transaction.add(new TestRecord(zeSchema, "000666"))
				.set(zeSchema.stringMetadata(), "AC666")
				.set(zeSchema.largeTextMetadata(), "Votre manque de foi me consterne");

		recordServices.execute(transaction);

		assertThat(freeTextSearch("serpent")).containsOnly("000042");
		assertThat(autocompleteSearch("AC6")).containsOnly("000666");

		reindexingServices.reindexCollections(new ReindexationParams(RECALCULATE).setBatchSize(1));
		assertThat(freeTextSearch("serpent")).containsOnly("000042");
		assertThat(autocompleteSearch("AC6")).containsOnly("000666");

		givenTimeIs(shishOClock.plusHours(1));
		reindexingServices.reindexCollections(new ReindexationParams(REWRITE).setBatchSize(1));
		assertThat(freeTextSearch("serpent")).containsOnly("000042");
		assertThat(autocompleteSearch("AC6")).containsOnly("000666");

		givenTimeIs(shishOClock.plusHours(2));
		reindexingServices.reindexCollections(new ReindexationParams(RECALCULATE_AND_REWRITE).setBatchSize(1));
		assertThat(freeTextSearch("serpent")).containsOnly("000042");
		assertThat(autocompleteSearch("AC6")).containsOnly("000666");

	}

	private List<String> freeTextSearch(String terms) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		ConditionTemplateFactory factory = new ConditionTemplateFactory(getModelLayerFactory(), zeCollection);
		ConditionTemplate conditionTemplate = factory.metadatasHasAnalyzedValue(terms, zeSchema.largeTextMetadata());
		LogicalSearchCondition condition = from(zeSchema.instance()).where(conditionTemplate);
		return searchServices.searchRecordIds(new LogicalSearchQuery(condition));
	}

	private List<String> autocompleteSearch(String terms) {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		LogicalSearchCondition condition = from(zeSchema.instance()).where(autocompleteFieldMatching(terms));
		return searchServices.searchRecordIds(new LogicalSearchQuery(condition));
	}

}
