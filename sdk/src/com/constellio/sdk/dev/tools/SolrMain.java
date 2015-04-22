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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.params.ModifiableSolrParams;

public class SolrMain {

	public static void main(String argv[])
			throws SolrServerException, IOException {

		HttpSolrClient server = new HttpSolrClient("http://localhost:8983/solr/records");
		server.commit();
		long start = new Date().getTime();

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "pathParts_ss:taxo1_0_*");
		params.set("group.facet", "true");
		params.set("group.field", "pathParts_ss");

		//			params.set("fq", "{!cache=true cost=0}id0_s:" + id.substring(10, 11));
		//			params.set("fq", "id1_s:" + id.substring(9, 10));
		//			params.set("fq", "id2_s:" + id.substring(8, 9));
		//			params.set("fq", "id3_s:" + id.substring(7, 8));
		//			params.set("fq", "id4_s:" + id.substring(6, 7));

		System.out.println(server.query(params).getResults());

	}

	private static Map<String, Object> newSetMap(String value) {
		Map<String, Object> map = new HashMap<>();
		map.put("set", value);
		return map;
	}

	private static String toId(int i) {
		String idWithTooMuchZeros = "0000000000" + i;

		return idWithTooMuchZeros.substring(idWithTooMuchZeros.length() - 11);
	}

}
