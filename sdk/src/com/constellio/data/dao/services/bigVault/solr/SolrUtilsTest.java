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
package com.constellio.data.dao.services.bigVault.solr;

import static com.constellio.data.dao.dto.records.RecordsFlushing.NOW;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.junit.Test;

import com.constellio.data.dao.services.solr.ConstellioSolrInputDocument;
import com.constellio.sdk.tests.ConstellioTest;

public class SolrUtilsTest extends ConstellioTest {

	@Test
	public void whenConvertSolrParamsToStringThenCorrectString()
			throws Exception {

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.add("firstKey", "firstValue");
		params.add("secondKey", "secondValue");

		assertThat(SolrUtils.toString(params)).isEqualTo("[firstKey=firstValue, secondKey=secondValue]");

	}

	@Test
	public void whenConvertSolrDocumentListToIdStringThenCorrectString()
			throws Exception {

		SolrDocumentList list = new SolrDocumentList();

		SolrDocument firstDocument = new SolrDocument();
		firstDocument.addField("id", "firstId");
		list.add(firstDocument);

		SolrDocument secondDocument = new SolrDocument();
		secondDocument.addField("id", "secondId");
		list.add(secondDocument);

		assertThat(SolrUtils.toIdString(list)).isEqualTo("[firstId, secondId]");

	}

	@Test
	public void whenConvertSolrDocumentToIdStringThenCorrectString()
			throws Exception {

		SolrInputDocument document = new ConstellioSolrInputDocument();
		document.addField("id", "zeId");

		assertThat(SolrUtils.toIdString(document)).isEqualTo("zeId");

	}

	@Test
	public void whenConvertListsOfAddedUpdatedDeletedRecordsToStringThenAllInformations() {
		SolrInputDocument addedDoc1 = new SolrInputDocument();
		SolrInputDocument addedDoc2 = new SolrInputDocument();
		SolrInputDocument modifiedDoc1 = new SolrInputDocument();
		SolrInputDocument modifiedDoc2 = new SolrInputDocument();

		addedDoc1.addField("id", "42");
		addedDoc1.addField("key1", "1");
		addedDoc1.addField("key1", "2");
		addedDoc1.addField("key2", "3");
		addedDoc2.addField("id", "666");
		addedDoc2.addField("key4", "4");

		modifiedDoc1.addField("id", "a38");
		modifiedDoc1.addField("key1", "5");
		modifiedDoc1.addField("key1", "6");
		modifiedDoc2.addField("id", "b72");
		modifiedDoc2.addField("key2", "7");

		List<SolrInputDocument> addedDocuments = Arrays.asList(addedDoc1, addedDoc2);
		List<SolrInputDocument> modifiedDocuments = Arrays.asList(modifiedDoc1, modifiedDoc2);
		List<String> deletedIds = Arrays.asList("id1", "id2");
		List<String> requests = Arrays.asList("req1", "req2");

		String expectedNewDocumentString = "New documents : \n" +
				"\tDocument '42' {\n" +
				"\t\tkey1 : [1, 2]\n" +
				"\t\tkey2 : [3]}\n" +
				"\tDocument '666' {\n" +
				"\t\tkey4 : [4]}\n";
		String expectedUpdatedDocumentString = "Updated documents : \n" +
				"\tDocument 'a38' {\n" +
				"\t\tkey1 : [5, 6]}\n" +
				"\tDocument 'b72' {\n" +
				"\t\tkey2 : [7]}\n";
		String expectedDeletedIdsString = "Delete ids : [id1, id2]\n";
		String expectedDeletedQueriesString = "Delete by queries : [req1, req2]\n";

		assertThat(
				SolrUtils.toString(new BigVaultServerTransaction(NOW, addedDocuments, modifiedDocuments, deletedIds, requests)))
				.isEqualTo(expectedNewDocumentString + expectedUpdatedDocumentString + expectedDeletedIdsString
						+ expectedDeletedQueriesString);

		assertThat(SolrUtils.toString(new BigVaultServerTransaction(NOW))).isEmpty();

	}

	@Test
	public void whenDeleteByQueryThenCombineQAndFQInOneQuery()
			throws Exception {

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "field1_s:123* OR field2_s:42");
		params.add("fq", "field3_s:Chuck field3_s:Norris");
		params.add("fq", "field4_s:Edouard field3_s:Lechat");

		String query = SolrUtils.toDeleteQueries(params);

		assertThat(query).isEqualTo(
				"((field1_s:123* OR field2_s:42) AND (field3_s:Chuck field3_s:Norris) AND (field4_s:Edouard field3_s:Lechat))");
	}

}
