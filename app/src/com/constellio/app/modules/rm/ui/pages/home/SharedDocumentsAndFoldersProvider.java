package com.constellio.app.modules.rm.ui.pages.home;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.Authorization;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.security.AuthorizationsServices;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.contents.ContentFactory.isCheckedOutBy;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class SharedDocumentsAndFoldersProvider {
	private transient AppLayerFactory appLayerFactory;
	private transient SessionContext sessionContext;
	private transient RMSchemasRecordsServices rm;
	private transient AuthorizationsServices authorizationsServices;
	private transient User user;

	public SharedDocumentsAndFoldersProvider(AppLayerFactory appLayerFactory, SessionContext sessionContext) {
		init(appLayerFactory, sessionContext);
	}

	public Map<String, RecordVODataProvider> getDataProviders() {
		MetadataSchemaVO schemaFolder = new MetadataSchemaToVOBuilder().build(
				rm.folderSchemaType().getDefaultSchema(), VIEW_MODE.TABLE, sessionContext);
		MetadataSchemaVO schemaDocument = new MetadataSchemaToVOBuilder().build(
				rm.documentSchemaType().getDefaultSchema(), VIEW_MODE.TABLE, sessionContext);
		Map<String, RecordVODataProvider> providers = new LinkedHashMap<>();

		providers.put($("HomeView.tab.sharedDocuments.foldersSharedByMe"),
				new RecordVODataProvider(schemaFolder, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), sessionContext) {
					@Override
					public LogicalSearchQuery getQuery() {
						MetadataSchemaType folder = rm.folderSchemaType();
						List<Authorization> auth = getAuthorizationsServices().getAllAuthorizationUserShared(user);
						List<String> targetIds = auth.stream().map(x -> x.getTarget()).collect(Collectors.toList());

						return new LogicalSearchQuery(from(folder).where(Schemas.IDENTIFIER).isIn(targetIds));
					}
				}
		);
		providers.put($("HomeView.tab.sharedDocuments.documentsSharedByMe"),
				new RecordVODataProvider(schemaDocument, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), sessionContext) {
					@Override
					public LogicalSearchQuery getQuery() {
						MetadataSchemaType document = rm.documentSchemaType();
						List<Authorization> auth = getAuthorizationsServices().getAllAuthorizationUserShared(user);
						List<String> targetIds = auth.stream().map(x -> x.getTarget()).collect(Collectors.toList());

						return new LogicalSearchQuery(from(document).where(Schemas.IDENTIFIER).isIn(targetIds));
					}
				}
		);
		providers.put($("HomeView.tab.sharedDocuments.foldersSharedToMe"),
				new RecordVODataProvider(schemaFolder, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), sessionContext) {
					@Override
					public LogicalSearchQuery getQuery() {
						MetadataSchemaType folder = rm.folderSchemaType();
						List<Authorization> auth = getAuthorizationsServices().getAllUserSharedRecords(user);
						List<String> targetIds = auth.stream().map(x -> x.getTarget()).collect(Collectors.toList());

						return new LogicalSearchQuery(from(folder).where(Schemas.IDENTIFIER).isIn(targetIds));
					}
				}
		);
		providers.put($("HomeView.tab.sharedDocuments.documentsSharedToMe"),
				new RecordVODataProvider(schemaDocument, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), sessionContext) {
					@Override
					public LogicalSearchQuery getQuery() {
						MetadataSchemaType document = rm.documentSchemaType();
						List<Authorization> auth = getAuthorizationsServices().getAllUserSharedRecords(user);
						List<String> targetIds = auth.stream().map(x -> x.getTarget()).collect(Collectors.toList());

						return new LogicalSearchQuery(from(document).where(Schemas.IDENTIFIER).isIn(targetIds));
					}
				}
		);

		return providers;
	}

	private void init(AppLayerFactory appLayerFactory, SessionContext sessionContext) {
		this.appLayerFactory = appLayerFactory;
		this.sessionContext = sessionContext;
		rm = new RMSchemasRecordsServices(sessionContext.getCurrentCollection(), appLayerFactory);
		user = new PresenterService(appLayerFactory.getModelLayerFactory()).getCurrentUser(sessionContext);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init(ConstellioFactories.getInstance().getAppLayerFactory(), ConstellioUI.getCurrentSessionContext());
	}

	private AuthorizationsServices getAuthorizationsServices() {
		if (this.authorizationsServices == null) {
			this.authorizationsServices = appLayerFactory.getModelLayerFactory().newAuthorizationsServices();
		}
		return this.authorizationsServices;
	}
}
