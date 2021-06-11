package com.constellio.model.services.search;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.data.TextView;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.SolrSafeConstellioAcceptanceTest;
import org.junit.Test;

import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static org.assertj.core.api.Assertions.assertThat;

// Confirm @SlowTest
public class SynonymFeatureAcceptanceTest extends SolrSafeConstellioAcceptanceTest {
	Record papa, television;

	//TODO Fail with Solr6
	@Test
	public void givenSynonymFeatureDiabeledWhenIndexingDocumentsAndSearchForAWordThenNoDocumentContainTheWordsSynonymIsReturned()
			throws Exception {
		//given
		String synonymFileContent = "";
		setUpSynonymTests(synonymFileContent);

		//when
		String text = "papa";
		condition = fromAllSchemasIn(zeCollection).returnAll();
		LogicalSearchQuery query = new LogicalSearchQuery(condition).setFreeTextQuery(text);
		List<Record> results = searchServices.search(query);

		//then
		assertThat(results).containsOnly(papa);
	}

	//TODO Fail with Solr6
	@Test
	public void givenSynonymFeatureEnabledWhenIndexingDocumentsAndSearchForAWordThenAllDocumentsContainTheWordAndItsSynonymAreReturned()
			throws Exception {
		if (!getDataLayerFactory().getDataLayerConfiguration().isLocalHttpSolrServer()) {
			return;
		}
		//given
		String synonymFileContent = "pizza, papa, television";
		setUpSynonymTests(synonymFileContent);

		//when
		String text = "papa";
		condition = fromAllSchemasIn(zeCollection).returnAll();
		LogicalSearchQuery query = new LogicalSearchQuery(condition).setFreeTextQuery(text);
		List<Record> results = searchServices.search(query);

		//then
		assertThat(results).containsOnly(papa, television);

	}

	private void setUpSynonymTests(String synonymFileContent)
			throws Exception {
		String synontyFilePath = "/synonyms.txt";
		BigVaultServer server = getDataLayerFactory().getRecordsVaultServer();
		AtomicFileSystem solrFileSystem = server.getSolrFileSystem();

		DataWithVersion readData = solrFileSystem.readData(synontyFilePath);
		TextView aStringView = readData.getView(new TextView());
		aStringView.setData(synonymFileContent);
		readData.setDataFromView(aStringView);

		solrFileSystem.writeData(synontyFilePath, readData);
		server.reload();

		//when
		defineSchemasManager().using(schema.withATitle(whichIsSearchable));
		transaction.addUpdate(papa = newRecordOfZeSchema().set(zeSchema.title(), "papa"));
		transaction.addUpdate(television = newRecordOfZeSchema().set(zeSchema.title(), "television"));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.title(), "radio"));
		recordServices.execute(transaction);
	}

}
