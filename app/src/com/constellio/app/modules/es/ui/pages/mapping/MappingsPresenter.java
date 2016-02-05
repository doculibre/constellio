package com.constellio.app.modules.es.ui.pages.mapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.es.ConstellioESModule;
import com.constellio.app.modules.es.extensions.api.ESModuleExtensions;
import com.constellio.app.modules.es.extensions.api.params.CustomTargetFlagsParams;
import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.modules.es.services.mapping.ConnectorField;
import com.constellio.app.modules.es.services.mapping.ConnectorMappingService;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.pages.base.BaseView;
import com.constellio.app.ui.pages.base.SingleSchemaBasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;

public abstract class MappingsPresenter<T extends BaseView> extends SingleSchemaBasePresenter<T> {
	private transient ConnectorInstance connectorInstance;
	private transient ConnectorMappingService mappingService;
	private transient ESSchemasRecordsServices schemas;

	protected String instanceId;

	public MappingsPresenter(T view) {
		super(view, ConnectorInstance.DEFAULT_SCHEMA);
	}

	public RecordVO getConnectorInstance() {
		return presenterService().getRecordVO(instanceId, VIEW_MODE.DISPLAY, view.getSessionContext());
	}

	public List<String> getCustomFlags() {
		ESModuleExtensions extensions = appCollectionExtentions.forModule(ConstellioESModule.ID);
		return extensions.getCustomTargetFlags(new CustomTargetFlagsParams());
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_CONNECTORS).globally();
	}

	protected Map<String, ConnectorField> getFieldMapById(String documentType) {
		Map<String, ConnectorField> result = new HashMap<>();
		for (ConnectorField field : mappingService().getConnectorFields(connectorInstance(), documentType)) {
			result.put(field.getId(), field);
		}
		return result;
	}

	protected MetadataVO getMetadataVO(String metadataCode) {
		return presenterService().getMetadataVO(metadataCode, view.getSessionContext());
	}

	protected ConnectorInstance connectorInstance() {
		if (connectorInstance == null) {
			connectorInstance = schemas().getConnectorInstance(instanceId);
		}
		return connectorInstance;
	}

	protected ConnectorMappingService mappingService() {
		if (mappingService == null) {
			mappingService = new ConnectorMappingService(schemas());
		}
		return mappingService;
	}

	private ESSchemasRecordsServices schemas() {
		if (schemas == null) {
			schemas = new ESSchemasRecordsServices(view.getCollection(), appLayerFactory);
		}
		return schemas;
	}
}
