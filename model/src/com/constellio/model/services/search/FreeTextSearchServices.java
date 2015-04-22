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

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.utils.LoggerUtils;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.search.query.FilterUtils;
import com.constellio.model.services.search.query.logical.FreeTextQuery;
import com.constellio.model.services.users.UserServices;

public class FreeTextSearchServices {

	Logger LOGGER = LoggerFactory.getLogger(FreeTextSearchServices.class);

	RecordDao recordDao;
	RecordDao eventsDao;
	RecordServices recordServices;
	UserServices userServices;

	public FreeTextSearchServices(RecordDao recordDao, RecordDao eventsDao, UserServices userServices) {
		super();
		this.recordDao = recordDao;
		this.eventsDao = eventsDao;
		this.userServices = userServices;
	}

	public QueryResponse search(FreeTextQuery query) {
		ModifiableSolrParams modifiableSolrParams = new ModifiableSolrParams(query.getSolrParams());

		if (query.getUserFilter() != null) {
			String filter = FilterUtils.multiCollectionUserReadFilter(query.getUserFilter(), userServices);
			modifiableSolrParams.add("fq", filter);
		}

		if (query.isSearchingEvents()) {
			modifiableSolrParams.add("fq", "schema_s:event*");
		} else {
			modifiableSolrParams.add("fq", "-schema_s:event*");
		}
		LOGGER.info(LoggerUtils.toParamsString(modifiableSolrParams));
		return recordDao.nativeQuery(modifiableSolrParams);
	}

}
