package com.constellio.model.services.search;

import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.valueCondition.ConditionTemplateFactory;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

public class SpellCheckerSearchServiceAcceptanceTest extends ConstellioTest {
	RecordServices recordServices;
	SearchServices searchServices;
	RecordDao recordDao;
	LogicalSearchCondition condition;
	ConditionTemplateFactory factory;
	Transaction transaction;

	SearchServiceAcceptanceTestSchemas schema = new SearchServiceAcceptanceTestSchemas(zeCollection);
	SearchServiceAcceptanceTestSchemas.ZeSchemaMetadatas zeSchema = schema.new ZeSchemaMetadatas();

	@Before
	public void setUp()
			throws Exception {
		prepareSystem(withZeCollection());
		recordServices = getModelLayerFactory().newRecordServices();
		recordDao = spy(getDataLayerFactory().newRecordDao());
		searchServices = new SearchServices(recordDao, getModelLayerFactory());

		transaction = new Transaction();
		factory = new ConditionTemplateFactory(getModelLayerFactory(), zeCollection);

		defineSchemasManager().using(schema.withAStringMetadata(whichIsSearchable));
		transaction.addUpdate(recordServices.newRecordWithSchema(zeSchema.instance())
											.set(zeSchema.stringMetadata(), "This is some amazing text in document number 42"));
		recordServices.execute(transaction);
	}

	@Test
	public void givenMisspelledQueryWhenSearchedThenSpellCheckerProvidesSuggestions()
			throws Exception {
		//given
		String queryText = "amazin";
		String expectedQuerySuggestion = "amazing";

		//when
		SPEQueryResponse response = makeAQuery(queryText);

		//then
		assertThat(response.isCorrectlySpelt()).isEqualTo(false);
		assertThat(response.getSpellCheckerSuggestions()).contains(expectedQuerySuggestion);
	}

	@Test
	public void givenMisspelledWordAndCorrectWordInQueryWhenSearchedThenSpellCheckerProvidesTheCorrectInItsSuggestions()
			throws Exception {
		//given
		String queryText = "amazin text";
		String expectedQuerySuggestion = "amazing text";

		//when
		SPEQueryResponse response = makeAQuery(queryText);

		//then
		assertThat(response.isCorrectlySpelt()).isEqualTo(false);
		assertThat(response.getSpellCheckerSuggestions()).contains(expectedQuerySuggestion);
	}

	@Test
	public void givenMisspelledWordAndStopWordInQueryWhenSearchedThenSpellCheckerProvidesTheStopWordInItsSuggestions()
			throws Exception {
		//given
		String queryText = "amazin this";
		String expectedQuerySuggestion = "amazing this";

		//when
		SPEQueryResponse response = makeAQuery(queryText);

		//then
		assertThat(response.isCorrectlySpelt()).isEqualTo(false);
		assertThat(response.getSpellCheckerSuggestions()).contains(expectedQuerySuggestion);
	}

	//FIXME
	@Test
	@Ignore
	public void givenMisspelledWordWithApostropheSInQueryWhenSearchedThenSpellCheckerProvidesTheApostropheSISInItsSuggestions()
			throws Exception {
		//given
		String queryText = "amazin's";
		String expectedQuerySuggestion = "amazing's";

		//when
		SPEQueryResponse response = makeAQuery(queryText);

		//then
		assertThat(response.isCorrectlySpelt()).isEqualTo(false);
		assertThat(response.getSpellCheckerSuggestions()).contains(expectedQuerySuggestion);
	}

	private SPEQueryResponse makeAQuery(String queryText) {
		condition = fromAllSchemasIn(zeCollection).returnAll();
		LogicalSearchQuery query = new LogicalSearchQuery(condition).setFreeTextQuery(queryText).setSpellcheck(true);
		return searchServices.query(query);
	}

}
