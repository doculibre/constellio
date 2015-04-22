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
package com.constellio.sdk.dev.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;

public class RemoveFieldUtils {

	public static void main(String argv[])
			throws SolrServerException, IOException {

		String ZE_REMOVED_FIELD = "description_t";

		HttpSolrClient server = new HttpSolrClient("http://localhost:8983/solr/records");

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", ZE_REMOVED_FIELD + ":*");
		params.set("rows", 10000);
		params.set("fl", "id");

		Map<String, String> setNull = new HashMap<>();
		setNull.put("set", "");

		boolean hasMore = true;
		while (hasMore) {

			List<SolrInputDocument> modification = new ArrayList<>();
			System.out.println("Selecting 10000 records with ze field...");

			QueryResponse queryResponse = server.query(params);
			List<SolrDocument> documentsWithZeField = queryResponse.getResults();
			System.out.println("Handling the next " + documentsWithZeField.size() + " (total of " + queryResponse.getResults()
					.getNumFound());

			for (SolrDocument document : documentsWithZeField) {
				String id = (String) document.getFieldValue("id");

				SolrInputDocument solrInputDocument = new SolrInputDocument();
				solrInputDocument.setField("id", id);

				solrInputDocument.setField(ZE_REMOVED_FIELD, setNull);
				modification.add(solrInputDocument);

			}

			if (modification.isEmpty()) {
				hasMore = false;
			} else {
				System.out.println("Removing field of the selected documents\n");
				server.add(modification);
				server.commit(true, true, true);

			}
		}
		server.commit();

	}

}
