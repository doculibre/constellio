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

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Ignore;
import org.junit.Test;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServer;
import com.constellio.data.io.concurrent.data.DataWithVersion;
import com.constellio.data.io.concurrent.data.XmlView;
import com.constellio.data.io.concurrent.filesystem.AtomicFileSystem;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.sdk.tests.SolrSafeConstellioAcceptanceTest;

public class ElevationFeatureAcceptanceTest extends SolrSafeConstellioAcceptanceTest {
	Record relevantDoc, elevatedDoc;

	@Test
	@Ignore
	// TODO Majid: Fails on CI server
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
		List<Record> results = null;
		results = searchServices.search(query);
		assertThat(results).containsExactly(relevantDoc);

		//when
		String synontyFilePath = "/conf/elevate.xml";
		BigVaultServer server = getDataLayerFactory().getRecordsVaultServer();
		AtomicFileSystem solrFileSystem = server.getSolrFileSystem();

		DataWithVersion readData = solrFileSystem.readData(synontyFilePath);
		XmlView anXmlView = readData.getView(new XmlView());

		Document xmlDocument = anXmlView.getData();
		Element anQueryElevation = new Element("query");
		anQueryElevation.setAttribute(new Attribute("text", text));
		anQueryElevation.addContent(new Element("doc").setAttribute(new Attribute("id", elevatedDoc.getId())));
		xmlDocument.getRootElement().addContent(anQueryElevation);

		readData.setDataFromView(anXmlView.setData(xmlDocument));

		solrFileSystem.writeData(synontyFilePath, readData);
		server.reload();

		//then
		results = searchServices.search(query);
		assertThat(results).containsExactly(elevatedDoc, relevantDoc);
	}

}
