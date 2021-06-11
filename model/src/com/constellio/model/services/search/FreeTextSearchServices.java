package com.constellio.model.services.search;

import com.constellio.data.dao.services.records.DataStore;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.dao.services.solr.SolrServers;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.FilterUtils;
import com.constellio.model.services.search.query.logical.FreeTextQuery;
import com.constellio.model.services.security.SecurityTokenManager;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FreeTextSearchServices {
	Logger LOGGER = LoggerFactory.getLogger(FreeTextSearchServices.class);

	RecordDao recordDao;
	RecordDao eventsDao;
	RecordServices recordServices;
	UserServices userServices;
	SecurityTokenManager securityTokenManager;
	MetadataSchemasManager metadataSchemasManager;
	SolrServers solrServers;

	public FreeTextSearchServices(ModelLayerFactory modelLayerFactory) {
		super();
		this.recordDao = modelLayerFactory.getDataLayerFactory().newRecordDao();
		this.eventsDao = modelLayerFactory.getDataLayerFactory().newEventsDao();
		this.solrServers = modelLayerFactory.getDataLayerFactory().getSolrServers();
		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.userServices = modelLayerFactory.newUserServices();
		this.securityTokenManager = modelLayerFactory.getSecurityTokenManager();
	}

	public QueryResponse search(FreeTextQuery query) {
		return search(DataStore.RECORDS, query);
	}

	public QueryResponse search(String core, FreeTextQuery query) {
		ModifiableSolrParams modifiableSolrParams = new ModifiableSolrParams(query.getSolrParams());

		if (query.getUserFilter() != null && isSecurityEnabled(modifiableSolrParams)) {
			String filter = FilterUtils.multiCollectionUserReadFilter(query.getUserFilter(), userServices, securityTokenManager);
			modifiableSolrParams.add("fq", filter);
		}

		if (query.isSearchingEvents()) {
			modifiableSolrParams.add("fq", "schema_s:event*");
		} else {
			modifiableSolrParams.add("fq", "-schema_s:event*");
		}
		//LOGGER.info(LoggerUtils.toParamsString(modifiableSolrParams));

		if (core == null || core.equals(DataStore.RECORDS)) {
			return recordDao.nativeQuery(modifiableSolrParams);
		} else {
			try {
				return solrServers.getSolrServer(core).getNestedSolrServer().query(modifiableSolrParams);
			} catch (SolrServerException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public boolean isSecurityEnabled(SolrParams params) {
		List<String> collections = new ArrayList<>();
		String schemaType = null;
		if (params.getParams("fq") != null) {
			for (String filterQuery : params.getParams("fq")) {
				if (filterQuery.startsWith("schema_s:")) {
					schemaType = StringUtils.substringBefore(filterQuery.substring("schema_s:".length()), "_");
				}
				if (!filterQuery.contains(" AND ") && StringUtils.countMatches(filterQuery, "collection_s:") == (StringUtils.countMatches(filterQuery, " OR ") + 1)) {
					if (filterQuery.contains(" OR ")) {
						String[] collectionsSplited = filterQuery.split(" OR ");
						for (String currentCollectionWithIdentifier : collectionsSplited) {
							collections.add(StringUtils.substringBefore(currentCollectionWithIdentifier.substring("collection_s:".length()), "_"));
						}
					} else if (filterQuery.startsWith("collection_s:")) {
						collections.add(StringUtils.substringBefore(filterQuery.substring("collection_s:".length()), "_"));
					}
				}
			}
		}

		String q = params.get("q");
		if (q != null && q.startsWith("schema_s:")) {
			schemaType = q.substring("schema_s:".length());
		}
		if (q != null && q.startsWith("collection_s:")) {
			collections.add(StringUtils.substringBefore(q.substring("collection_s:".length()), "_"));
		}

		if (schemaType != null) {
			schemaType = StringUtils.substringBefore(schemaType, "_");
			schemaType = StringUtils.substringBefore(schemaType, "*");
		}

		boolean security = true;
		if (collections.size() > 0 && schemaType != null && security) {
			try {
				for (String currentCollection : collections) {
					MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(currentCollection);
					security = types.getSchemaType(schemaType).hasSecurity();

					if (security) {
						break;
					}
				}
			} catch (Exception e) {
				//OK
			}
		}
		return security;
	}
}
