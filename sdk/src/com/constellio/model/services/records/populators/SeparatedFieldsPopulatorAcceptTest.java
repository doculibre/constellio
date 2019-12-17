package com.constellio.model.services.records.populators;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.constellio.model.services.records.reindexing.ReindexationMode.RECALCULATE_AND_REWRITE;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsDuplicable;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static org.assertj.core.api.Assertions.assertThat;

public class SeparatedFieldsPopulatorAcceptTest extends ConstellioTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(SeparatedFieldsPopulatorAcceptTest.class);

	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	RecordServices recordServices;
	SearchServices searchServices;
	Transaction transaction;
	MetadataSchemasManager schemasManager;

	int i = 1;

	@Before
	public void setUp()
			throws Exception {

		prepareSystem(withZeCollection());
		defineSchemasManager().using(schemas.withAStringMetadata(whichIsSearchable, whichIsDuplicable));
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		transaction = new Transaction();
		schemasManager = getModelLayerFactory().getMetadataSchemasManager();
	}

	@Test
	public void whenSearchingSearchableStringMetadataNowEmpty()
			throws Exception {

		Record tmnt = transaction.add(recordWithStringValue("COWABUNGA"));
		recordServices.execute(transaction);
		assertThat(simpleSearch("COWABUNGA")).containsOnly(tmnt.getId());

		transaction = new Transaction(tmnt.set(zeSchema.stringMetadata(), ""));
		recordServices.execute(transaction);
		assertThat(simpleSearch("COWABUNGA")).isEmpty();
	}

	@Test
	public void whenSearchingSearchableStringMetadataNowEmptyAfterReindexation()
			throws Exception {

		Record mightyMorphin = transaction.add(recordWithStringValue("It's Morphin Time!"));
		recordServices.execute(transaction);
		assertThat(simpleSearch("It's Morphin Time!")).containsOnly(mightyMorphin.getId());

		transaction = new Transaction(mightyMorphin.set(zeSchema.stringMetadata(), ""));
		recordServices.execute(transaction);
		getModelLayerFactory().newReindexingServices().reindexCollections(RECALCULATE_AND_REWRITE);
		assertThat(simpleSearch("It's Morphin Time!")).isEmpty();
	}

	@Test
	public void whenSearchingSearchableStringMetadataNowFilledAfterReindexation()
			throws Exception {

		Record casper = transaction.add(recordWithStringValue(""));
		recordServices.execute(transaction);
		assertThat(simpleSearch("")).isEmpty();

		transaction = new Transaction(casper.set(zeSchema.stringMetadata(),
				"Carrigan! Are you a ghost yet? Carrigan! What a tragic waste. She had my favorite sunglasses."));
		recordServices.execute(transaction);
		getModelLayerFactory().newReindexingServices().reindexCollections(RECALCULATE_AND_REWRITE);
		assertThat(simpleSearch("Carrigan")).containsOnly(casper.getId());
	}

	// -------------------------------------------------------------------------------------------------------------------

	private List<String> simpleSearch(String terms) {

		LOGGER.info("Simple search with terms : " + terms);

		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(from(zeSchema.instance()).returnAll());
		query.setFreeTextQuery(terms);

		return searchServices.searchRecordIds(query);
	}

	private Record recordWithStringValue(String value) {

		String id = "record" + i++;
		return recordServices.newRecordWithSchema(zeSchema.instance(), id).set(zeSchema.stringMetadata(), value);
	}
}
