package com.constellio.app.modules.rm.ui.data;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.cache.SerializableSearchCache;
import com.constellio.model.services.search.cache.SerializedCacheSearchService;
import com.constellio.model.services.search.query.SearchQuery;
import com.constellio.model.services.search.query.list.RecordListSearchQuery;

import java.util.List;
import java.util.stream.Collectors;

public abstract class ShareContentDataProvider extends RecordVODataProvider {
	private SerializableSearchCache queryCacheForAuthorizations;
	private SchemasRecordsServices schemasRecordsServices;
	private MetadataSchemasManager metadataSchemasManager;
	private User user;

	public ShareContentDataProvider(MetadataSchemaVO metadataSchemaVO, ModelLayerFactory modelLayerFactory,
									SessionContext context) {
		super(metadataSchemaVO, new RecordToVOBuilder(), modelLayerFactory, context);
	}

	private SerializableSearchCache getQueryCacheForAuthorizations() {
		if (queryCacheForAuthorizations == null) {
			queryCacheForAuthorizations = new SerializableSearchCache();
		}

		return queryCacheForAuthorizations;
	}

	private void init() {
		if (this.schemasRecordsServices == null) {
			SessionContext sessionContext = getSessionContext();
			this.schemasRecordsServices = new SchemasRecordsServices(sessionContext.getCurrentCollection(), modelLayerFactory);
			this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
			this.user = modelLayerFactory.newUserServices().getUserInCollection(sessionContext.getCurrentUser().getUsername(), sessionContext.getCurrentCollection());
		}
	}

	@Override
	public final RecordListSearchQuery getQuery() {
		init();

		SerializedCacheSearchService authorizationCacheSearchService = new SerializedCacheSearchService(modelLayerFactory, getQueryCacheForAuthorizations(), false);

		List<Authorization> recordList = schemasRecordsServices.wrapSolrAuthorizationDetailss(authorizationCacheSearchService.search(getAuthorizationQuery()));

		List<String> id = recordList.stream().map(authorization -> authorization.getTarget()).collect(Collectors.toList());

		RecordListSearchQuery queryOnRecord = (RecordListSearchQuery) RecordListSearchQuery.createFromIds(id)
				.convertIdsToSummaryRecords(schemasRecordsServices.getModelLayerFactory())
				.filteredWithUser(user)
				.setLanguage(sessionContext.getCurrentLocale());

		return queryOnRecord;
	}

	public abstract SearchQuery getAuthorizationQuery();
}
