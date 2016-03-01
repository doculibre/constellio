package com.constellio.model.services.search;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.Elevations.QueryElevation;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.services.ElevationServiceImpl;
import com.constellio.sdk.tests.SolrSafeConstellioAcceptanceTest;

public class ElevationFeatureAcceptanceTest extends SolrSafeConstellioAcceptanceTest {
	Record relevantDoc, elevatedDoc;

	@Test
	public void givenANonRelevantDocumentWhenSettingUpElevationWithItForAQueryThenItAppearsInTheTop()
			throws Exception {
		//given
		defineSchemasManager().using(schema.withAStringMetadata(whichIsSearchable));
		transaction.addUpdate(relevantDoc = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "A relevant document."));
		transaction.addUpdate(elevatedDoc = newRecordOfZeSchema().set(zeSchema.stringMetadata(), "An elevated document."));
		transaction.addUpdate(newRecordOfZeSchema().set(zeSchema.stringMetadata(), "radio"));
		recordServices.execute(transaction);

		String text = "relevant";
		condition = fromAllSchemasIn(zeCollection).returnAll();
		LogicalSearchQuery query = new LogicalSearchQuery().setCondition(condition).setFreeTextQuery(text);

		List<Record> results = searchServices.search(query);
		assertThat(results).containsExactly(relevantDoc);

		//when
		BigVaultServer server = getDataLayerFactory().getRecordsVaultServer();
		AtomicFileSystem solrFileSystem = server.getSolrFileSystem();
		DataWithVersion readData = solrFileSystem.readData(ElevationServiceImpl.ELEVATE_FILE_NAME);
		ElevationsView anElevationsView = readData.getView(new ElevationsView());
		Elevations elevations = anElevationsView.getData();
		QueryElevation queryElevation = new QueryElevation(text);
		queryElevation.getDocElevations().add(new QueryElevation.DocElevation(elevatedDoc.getId(), false));
		elevations.getQueryElevations().add(queryElevation);

		anElevationsView.setData(elevations);
		readData.setDataFromView(anElevationsView);

		solrFileSystem.writeData(ElevationServiceImpl.ELEVATE_FILE_NAME, readData);
		server.reload();

		//then
		results = searchServices.search(query);
		assertThat(results).containsExactly(elevatedDoc, relevantDoc);
	}

}
