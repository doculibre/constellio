package com.constellio.model.services.search.reliability;

import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.Transaction;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup;
import com.constellio.sdk.tests.schemas.TestsSchemasSetup.ZeSchemaMetadatas;
import org.assertj.core.data.Index;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class EnglishSimpleSearchReliabilityAcceptanceTest extends ConstellioTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(EnglishSimpleSearchReliabilityAcceptanceTest.class);

	TestsSchemasSetup schemas = new TestsSchemasSetup(zeCollection);
	ZeSchemaMetadatas zeSchema = schemas.new ZeSchemaMetadatas();
	RecordServices recordServices;
	SearchServices searchServices;
	Transaction transaction;

	int i = 1;
	String record1, record2, record3, record4, record5, record6, record7, record8, record9, record10;

	@Before
	public void setUp()
			throws Exception {
		givenSystemLanguageIs("en");
		givenCollection(zeCollection, asList("en"));
		defineSchemasManager().using(schemas.withAStringMetadata(whichIsSearchable));
		recordServices = getModelLayerFactory().newRecordServices();
		searchServices = getModelLayerFactory().newSearchServices();
		transaction = new Transaction();

	}

	@Test
	public void whenSearchingThenSameResultsIfDifferenceInAccentsSingularPluralAndCase()
			throws Exception {

		record1 = transaction.add(recordWithStringValue("café")).getId();
		record2 = transaction.add(recordWithStringValue("cafe")).getId();
		record3 = transaction.add(recordWithStringValue("cafés")).getId();
		record4 = transaction.add(recordWithStringValue("cafes")).getId();
		record5 = transaction.add(recordWithStringValue("cafs")).getId();
		recordServices.execute(transaction);

		// BROKEN assertThat(simpleSearch("cafe")).containsOnly(record1, record2, record3, record4);
		// BROKEN assertThat(simpleSearch("café")).containsOnly(record1, record2, record3, record4);
		// BROKEN assertThat(simpleSearch("cafes")).containsOnly(record1, record2, record3, record4);
		// BROKEN assertThat(simpleSearch("cafés")).containsOnly(record1, record2, record3, record4);
		// BROKEN assertThat(simpleSearch("CAFÉ")).containsOnly(record1, record2, record3, record4);
		// BROKEN assertThat(simpleSearch("cAfÉ")).containsOnly(record1, record2, record3, record4);
		// BROKEN assertThat(simpleSearch("CAFÉS")).containsOnly(record1, record2, record3, record4);
		// BROKEN assertThat(simpleSearch("cAfÉs")).containsOnly(record1, record2, record3, record4);
		assertThat(simpleSearch("cafs")).containsOnly(record5);

	}

	@Test
	public void whenSearchingWithMultipleWordsThenMoreRelevanceToRecordsWithAllOfThem()
			throws Exception {

		record1 = transaction.add(recordWithStringValue("recherche")).getId();
		record2 = transaction.add(recordWithStringValue("recherche entreprise")).getId();
		record3 = transaction.add(recordWithStringValue("Constellio entreprise")).getId();
		record4 = transaction.add(recordWithStringValue("Constellio recherche entreprise")).getId();
		record5 = transaction.add(recordWithStringValue("Constellio recherche")).getId();

		recordServices.execute(transaction);

		//TODO BROKEN : record1 should not be returned
		assertThat(simpleSearch("recherche constellio entreprise"))
				.containsOnly(record2, record3, record4, record5, record1)
				.startsWith(record4).endsWith(record1);

		//TODO BROKEN assertThat(simpleSearch("recherche robot")).containsOnly(record1, record2, record4, record5).isEmpty();

	}

	@Test
	public void givenFormattedDatesWhenFindUsingPartThenFindRecords()
			throws Exception {
		record1 = transaction.add(recordWithStringValue("2006/01/03")).getId();
		record2 = transaction.add(recordWithStringValue("2007/02/04")).getId();
		record3 = transaction.add(recordWithStringValue("2008-01-06")).getId();
		record4 = transaction.add(recordWithStringValue("2009_01_07")).getId();
		record5 = transaction.add(recordWithStringValue("CO-48621")).getId();
		record6 = transaction.add(recordWithStringValue("CO-48622")).getId();
		record7 = transaction.add(recordWithStringValue("CO-42621")).getId();

		recordServices.execute(transaction);

		assertThat(simpleSearch("2006")).containsOnly(record1);
		assertThat(simpleSearch("2009")).containsOnly(record4);
		assertThat(simpleSearch("2008")).containsOnly(record3);
		assertThat(simpleSearch("2008-01")).contains(record3, Index.atIndex(0));
		assertThat(simpleSearch("2008-01")).contains(record3, record3, record4);
		assertThat(simpleSearch("2008-01*")).containsOnly(record3);
		assertThat(simpleSearch("2007-02-04")).containsOnly(record2);
		assertThat(simpleSearch("01")).containsOnly(record1, record3, record4);
		assertThat(simpleSearch("CO*")).containsOnly(record5, record6, record7);

		assertThat(simpleSearch("48621")).containsOnly(record5);
		assertThat(simpleSearch("Co")).containsOnly(record5, record6, record7);
		//? operator removes record6 as a match
		assertThat(simpleSearch("CO-4?621")).containsOnly(record5, record7);
		assertThat(simpleSearch("CO-48*")).containsOnly(record5, record6);
	}

	@Test
	public void whenSearchingWithAndORNotThenOK()
			throws Exception {
		record1 = transaction.add(recordWithStringValue("Arbre à guimauve")).getId();
		record2 = transaction.add(recordWithStringValue("Pomme de terre")).getId();
		record3 = transaction.add(recordWithStringValue("Salade de pomme de terre")).getId();
		record4 = transaction.add(recordWithStringValue("Guimauve bleue")).getId();
		record5 = transaction.add(recordWithStringValue("Base de donnée bleue")).getId();
		record6 = transaction.add(recordWithStringValue("Vache à lait")).getId();
		record7 = transaction.add(recordWithStringValue("vache sacrée")).getId();

		recordServices.execute(transaction);

		assertThat(simpleSearch("guimauve")).containsOnly(record1, record4);
		assertThat(simpleSearch("guimauve AND bleue")).containsOnly(record4);
		assertThat(simpleSearch("guimauve AND (bleue OR arbre)")).containsOnly(record1, record4);
		assertThat(simpleSearch("(guimauve OR vache) NOT arbre NOT sacrée")).containsOnly(record4, record6);

		//BROKEN assertThat(simpleSearch("CO-48*")).containsOnly(record5, record6);
	}

	@Test
	public void givenFileNameThenFindThemWithoutExtensions()
			throws Exception {
		record1 = transaction.add(recordWithStringValue("fichier.pdf")).getId();
		record2 = transaction.add(recordWithStringValue("fichier")).getId();
		record3 = transaction.add(recordWithStringValue("fichier de test.pdf")).getId();

		recordServices.execute(transaction);

		//BROKEN assertThat(simpleSearch("fichier")).containsOnly(record1, record2, record3);
		//BROKEN assertThat(simpleSearch("test")).containsOnly(record3);
	}

	// -------------------------------------------------------------------------------------------------------------------

	private List<String> simpleSearch(String terms) {

		getDataLayerFactory().getDataLayerLogger().setPrintAllQueriesLongerThanMS(0);
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
