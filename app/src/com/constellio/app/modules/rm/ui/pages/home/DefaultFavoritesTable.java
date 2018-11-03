package com.constellio.app.modules.rm.ui.pages.home;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.PresenterService;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class DefaultFavoritesTable implements Serializable {
	private transient AppLayerFactory appLayerFactory;
	private transient SessionContext sessionContext;
	private transient RMSchemasRecordsServices rm;
	private transient User user;

	public DefaultFavoritesTable(AppLayerFactory appLayerFactory, SessionContext sessionContext) {
		init(appLayerFactory, sessionContext);
	}

	public List<RecordVODataProvider> getDataProviders() {
		final MetadataSchemaType documentSchemaType = rm.documentSchemaType();
		MetadataSchemaVO documentSchema = new MetadataSchemaToVOBuilder().build(documentSchemaType.getDefaultSchema(), VIEW_MODE.TABLE, sessionContext);
		RecordVODataProvider documentVODataProvider = new RecordVODataProvider(documentSchema, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), sessionContext) {
			@Override
			protected LogicalSearchQuery getQuery() {
				final Metadata documentFavoritesList = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(sessionContext.getCurrentCollection()).getMetadata(Document.DEFAULT_SCHEMA + "_" + Document.FAVORITES);
				return new LogicalSearchQuery(from(documentSchemaType).where(documentFavoritesList).isContaining(asList(user.getId())));
			}
		};

		final MetadataSchemaType folderSchemaType = rm.folderSchemaType();
		MetadataSchemaVO folderSchema = new MetadataSchemaToVOBuilder().build(folderSchemaType.getDefaultSchema(), VIEW_MODE.TABLE, sessionContext);
		RecordVODataProvider folderVODataProvider = new RecordVODataProvider(folderSchema, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), sessionContext) {
			@Override
			protected LogicalSearchQuery getQuery() {
				return new LogicalSearchQuery(from(folderSchemaType).where(rm.folder.favorites()).isContaining(asList(user.getId())));
			}
		};

		final MetadataSchemaType containerSchemaType = rm.containerRecord.schemaType();
		MetadataSchemaVO containerSchema = new MetadataSchemaToVOBuilder().build(containerSchemaType.getDefaultSchema(), VIEW_MODE.TABLE, sessionContext);
		RecordVODataProvider containerVODataProvider = new RecordVODataProvider(containerSchema, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), sessionContext) {
			@Override
			protected LogicalSearchQuery getQuery() {
				final Metadata containerFavoritesList = modelLayerFactory.getMetadataSchemasManager().getSchemaTypes(sessionContext.getCurrentCollection()).getMetadata(ContainerRecord.DEFAULT_SCHEMA + "_" + ContainerRecord.FAVORITES);
				return new LogicalSearchQuery(from(containerSchemaType).where(containerFavoritesList).isContaining(asList(user.getId())));
			}
		};

		return asList(documentVODataProvider, folderVODataProvider, containerVODataProvider);
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
}
