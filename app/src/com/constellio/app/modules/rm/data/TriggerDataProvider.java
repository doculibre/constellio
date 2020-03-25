package com.constellio.app.modules.rm.data;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.triggers.Trigger;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;

public class TriggerDataProvider extends RecordVODataProvider {

	private MetadataSchemasManager metadataSchemasManager;
	private RMSchemasRecordsServices rm;
	private String id;
	private MetadataSchemaType metadataSchemaType;

	public TriggerDataProvider(String id,
							   AppLayerFactory appLayerFactory,
							   SessionContext sessionContext) {
		super(getTriggerSchemaVO(sessionContext, appLayerFactory.getModelLayerFactory().getMetadataSchemasManager()), new RecordToVOBuilder(), appLayerFactory.getModelLayerFactory(), sessionContext);

		this.metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.rm = new RMSchemasRecordsServices(sessionContext.getCurrentCollection(), appLayerFactory);
		this.id = id;
		this.metadataSchemaType = this.metadataSchemasManager.getSchemaTypes(sessionContext.getCurrentCollection()).getSchemaType(Trigger.SCHEMA_TYPE);
		// Redo init because we need attribute of this class in getQuery()
		super.init(appLayerFactory.getModelLayerFactory());
	}

	private static MetadataSchemaVO getTriggerSchemaVO(SessionContext sessionContext,
													   MetadataSchemasManager metadataSchemasManager) {
		return new MetadataSchemaToVOBuilder().build(getTriggerSchema(sessionContext, metadataSchemasManager), VIEW_MODE.TABLE, sessionContext);
	}

	private static MetadataSchema getTriggerSchema(SessionContext sessionContext,
												   MetadataSchemasManager metadataSchemasManager) {
		return metadataSchemasManager.getSchemaTypes(sessionContext.getCurrentCollection()).getSchema(Trigger.DEFAULT_SCHEMA);
	}

	@Override
	public LogicalSearchQuery getQuery() {
		if (id != null) {
			return new LogicalSearchQuery(LogicalSearchQueryOperators.from(metadataSchemaType).where(rm.trigger.target()).isEqualTo(id));
		}
		{
			return new LogicalSearchQuery();
		}
	}
}
