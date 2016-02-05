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
import com.constellio.model.services.security.SecurityTokenManager;
import com.constellio.model.services.users.UserServices;

public class FreeTextSearchServices {
	Logger LOGGER = LoggerFactory.getLogger(FreeTextSearchServices.class);

	RecordDao recordDao;
	RecordDao eventsDao;
	RecordServices recordServices;
	UserServices userServices;
	SecurityTokenManager securityTokenManager;

	public FreeTextSearchServices(RecordDao recordDao, RecordDao eventsDao, UserServices userServices,
			SecurityTokenManager securityTokenManager) {
		super();
		this.recordDao = recordDao;
		this.eventsDao = eventsDao;
		this.userServices = userServices;
		this.securityTokenManager = securityTokenManager;
	}

	public QueryResponse search(FreeTextQuery query) {
		ModifiableSolrParams modifiableSolrParams = new ModifiableSolrParams(query.getSolrParams());

		if (query.getUserFilter() != null) {
			String filter = FilterUtils.multiCollectionUserReadFilter(query.getUserFilter(), userServices, securityTokenManager);
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
