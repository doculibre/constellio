package com.constellio.app.modules.rm.ui.pages.home;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
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
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.io.IOException;
import java.io.Serializable;

import static com.constellio.model.services.contents.ContentFactory.isCheckedOutBy;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class CheckedOutDocumentsTable implements Serializable {
	private transient AppLayerFactory appLayerFactory;
	private transient SessionContext sessionContext;
	private transient RMSchemasRecordsServices rm;
	private transient User user;

	public CheckedOutDocumentsTable(AppLayerFactory appLayerFactory, SessionContext sessionContext) {
		init(appLayerFactory, sessionContext);
	}

	public RecordVODataProvider getDataProvider() {
		MetadataSchemaVO schema = new MetadataSchemaToVOBuilder().build(
				rm.documentSchemaType().getDefaultSchema(), VIEW_MODE.TABLE, sessionContext);
		return new RecordVODataProvider(schema, new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), sessionContext) {
			@Override
			public LogicalSearchQuery getQuery() {
				MetadataSchemaType document = rm.documentSchemaType();
				return new LogicalSearchQuery(from(document).where(rm.documentContent()).is(isCheckedOutBy(user)).andWhere(Schemas.LOGICALLY_DELETED_STATUS).isFalseOrNull())
						.sortDesc(Schemas.MODIFIED_ON);
			}
		};
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
