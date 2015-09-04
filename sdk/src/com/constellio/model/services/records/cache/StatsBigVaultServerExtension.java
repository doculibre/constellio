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
package com.constellio.model.services.records.cache;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.params.SolrParams;

import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.extensions.BigVaultServerExtension;

public class StatsBigVaultServerExtension extends BigVaultServerExtension {

	public List<String> byIds = new ArrayList<>();
	public List<SolrParams> queries = new ArrayList<>();

	@Override
	public void afterUpdate(BigVaultServerTransaction transaction, long qtime) {
		super.afterUpdate(transaction, qtime);
	}

	static String GET_BY_ID_PREFIX = "id:";

	@Override
	public void afterQuery(SolrParams solrParams, long qtime) {

		String[] filterQueries = solrParams.getParams("fq");

		if (filterQueries.length == 1 && filterQueries[0].startsWith(GET_BY_ID_PREFIX)) {
			byIds.add(filterQueries[0].replace(GET_BY_ID_PREFIX, ""));
		}
		queries.add(solrParams);
	}

	public void clear() {
		byIds.clear();
		queries.clear();
	}
}
