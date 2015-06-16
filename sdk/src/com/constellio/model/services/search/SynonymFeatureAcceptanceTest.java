/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.model.services.search;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static com.constellio.sdk.tests.schemas.TestsSchemasSetup.whichIsSearchable;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.data.StringView;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.SolrSafeConstellioAcceptanceTest;

public class SynonymFeatureAcceptanceTest extends SolrSafeConstellioAcceptanceTest {
	Record tv, television;

	@Test
	@Ignore
	// TODO Majid: Fails on CI server
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
	@Ignore
	// TODO Majid: Fails on CI server
	public void givenSynonymFeatureEnabledWhenIndexingDocumentsAndSearchForAWordThenAllDocumentsContainTheWordAndItsSynonymAreReturned()
			throws Exception {
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
		String synontyFilePath = "/conf/synonyms.txt";
		BigVaultServer server = getDataLayerFactory().getRecordsVaultServer();
		AtomicFileSystem solrFileSystem = server.getSolrFileSystem();

		DataWithVersion readData = solrFileSystem.readData(synontyFilePath);
		StringView aStringView = readData.getView(new StringView());
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
