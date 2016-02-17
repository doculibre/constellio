package com.constellio.model.services.search;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.data.TextView;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.SolrSafeConstellioAcceptanceTest;

public class SynonymFeatureAcceptanceTest extends SolrSafeConstellioAcceptanceTest {
	Record tv, television;

	@Test
	public void givenSynonymFeatureDiabeledWhenIndexingDocumentsAndSearchForAWordThenNoDocumentContainTheWordsSynonymIsReturned()
			throws Exception {
		//given
		String synonymFileContent = "";
		setUpSynonymTests(synonymFileContent);

		//when
		String text = "tv";
		condition = fromAllSchemasIn(zeCollection).returnAll();
		LogicalSearchQuery query = new LogicalSearchQuery(condition).setFreeTextQuery(text);
		List<Record> results = searchServices.search(query);

		//then
		assertThat(results).containsOnly(tv);
	}

	@Test
	public void givenSynonymFeatureEnabledWhenIndexingDocumentsAndSearchForAWordThenAllDocumentsContainTheWordAndItsSynonymAreReturned()
			throws Exception {
		if (!getDataLayerFactory().getDataLayerConfiguration().isLocalHttpSolrServer()) {
			return;
		}
		//given
		String synonymFileContent = "tv, television\n";
		setUpSynonymTests(synonymFileContent);

		//when
		String text = "tv";
		condition = fromAllSchemasIn(zeCollection).returnAll();
		LogicalSearchQuery query = new LogicalSearchQuery(condition).setFreeTextQuery(text);
		List<Record> results = searchServices.search(query);

		//then
		assertThat(results).containsOnly(tv, television);

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
		defineSchemasManager().using(schema.withAStringMetadata(whichIsSearchable));
		transaction.addUpdate(tv = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "tv"));
		transaction.addUpdate(television = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "television"));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "radio"));
		recordServices.execute(transaction);
	}

}
