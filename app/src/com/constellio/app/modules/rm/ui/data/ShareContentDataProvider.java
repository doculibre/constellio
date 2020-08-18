package com.constellio.app.modules.rm.ui.data;

import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

import java.util.List;
import java.util.stream.Collectors;

public abstract class ShareContentDataProvider extends RecordVODataProvider {
	private SchemasRecordsServices schemasRecordsServices;
	private MetadataSchemasManager metadataSchemasManager;
	private SearchServices searchServices;
	private User user;

	public ShareContentDataProvider(MetadataSchemaVO metadataSchemaVO, ModelLayerFactory modelLayerFactory,
									SessionContext context) {
		super(metadataSchemaVO, new RecordToVOBuilder(), modelLayerFactory, context);
	}

	private void init() {
		if (this.schemasRecordsServices == null) {
			SessionContext sessionContext = getSessionContext();
			this.schemasRecordsServices = new SchemasRecordsServices(sessionContext.getCurrentCollection(), modelLayerFactory);
			this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
			this.user = modelLayerFactory.newUserServices().getUserInCollection(sessionContext.getCurrentUser().getUsername(), sessionContext.getCurrentCollection());
		}
	}

	private SearchServices getSearchServices() {
		if (searchServices == null) {
			searchServices = modelLayerFactory.newSearchServices();
		}

		return searchServices;
	}

	@Override
	public final LogicalSearchQuery getQuery() {
		init();

		SearchServices searchServices = getSearchServices();
		List<Authorization> recordList = schemasRecordsServices.wrapSolrAuthorizationDetailss(searchServices.search(getAuthorizationQuery()));
		List<String> id = recordList.stream().map(authorization -> authorization.getTarget()).collect(Collectors.toList());
		LogicalSearchQuery queryOnRecord = new LogicalSearchQuery(LogicalSearchQueryOperators.from(metadataSchemasManager
				.getSchemaTypes(sessionContext.getCurrentCollection()).getSchemaType(getSchema().getTypeCode())).where(Schemas.IDENTIFIER).isIn(id));
		queryOnRecord.filteredWithUser(user);
		queryOnRecord.setLanguage(sessionContext.getCurrentLocale());

		return queryOnRecord;
	}

	public abstract LogicalSearchQuery getAuthorizationQuery();
}
