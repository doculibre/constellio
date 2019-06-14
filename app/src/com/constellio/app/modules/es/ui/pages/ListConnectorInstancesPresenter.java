package com.constellio.app.modules.es.ui.pages;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
import com.constellio.app.modules.es.navigation.ESViews;
import com.constellio.app.modules.es.services.ConnectorDeleteService;
import com.constellio.app.modules.es.services.ESSchemasRecordsServices;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.framework.data.RecordVODataProvider;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

public class ListConnectorInstancesPresenter extends BasePresenter<ListConnectorInstancesView> {

	private transient MetadataSchemasManager metadataSchemasManager;
	private transient UserServices userServices;

	public ListConnectorInstancesPresenter(ListConnectorInstancesView view) {
		super(view);
		init();

	}

	private void init() {
		metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		userServices = modelLayerFactory.newUserServices();
	}

	public List<Metadata> columnToRemove() {
		List<Metadata> toRemove = new ArrayList<>();
		MetadataSchema connectorInstanceDefaultSchema = metadataSchemasManager.getSchemaTypes(collection)
				.getDefaultSchema(ConnectorInstance.SCHEMA_TYPE);
		toRemove.add(connectorInstanceDefaultSchema.get(ConnectorInstance.TRAVERSAL_CODE));
		return toRemove;
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return userServices.has(user).globalPermissionInAnyCollection(CorePermissions.MANAGE_CONNECTORS);
	}

	public RecordVODataProvider getDataProvider() {

		MetadataSchema connectorInstanceDefaultSchema = metadataSchemasManager.getSchemaTypes(collection)
				.getDefaultSchema(ConnectorInstance.SCHEMA_TYPE);

		List<String> metadataCodes = Arrays
				.asList(ConnectorInstance.TITLE, ConnectorInstance.CODE, ConnectorInstance.CONNECTOR_TYPE,
						ConnectorInstance.ENABLED);
		MetadataSchemaVO schemaVO = new MetadataSchemaToVOBuilder()
				.build(connectorInstanceDefaultSchema, VIEW_MODE.TABLE, metadataCodes, view.getSessionContext());
		RecordToVOBuilder voBuilder = new RecordToVOBuilder();
		RecordVODataProvider dataProvider = new RecordVODataProvider(schemaVO, voBuilder, modelLayerFactory,
				view.getSessionContext()) {
			@Override
			public LogicalSearchQuery getQuery() {
				MetadataSchemaType connectorInstanceSchemaType = modelLayerFactory.getMetadataSchemasManager()
						.getSchemaTypes(collection)
						.getSchemaType(ConnectorInstance.SCHEMA_TYPE);
				LogicalSearchQuery query = new LogicalSearchQuery(
						from(connectorInstanceSchemaType).returnAll())
						.sortAsc(Schemas.TITLE)
						.sortDesc(Schemas.MODIFIED_ON);
				return query;
			}
		};
		return dataProvider;
	}

	public void displayButtonClicked(RecordVO entity) {
		view.navigate().to(ESViews.class).displayConnectorInstance(entity.getId());
	}

	public void editButtonClicked(RecordVO entity) {
		view.navigate().to(ESViews.class).editConnectorInstance(entity.getId());
	}

	public void deleteButtonClicked(RecordVO entity) {
		ESSchemasRecordsServices es = new ESSchemasRecordsServices(collection, appLayerFactory);
		new ConnectorDeleteService(collection, appLayerFactory).deleteConnector(es.getConnectorInstance(entity.getId()));
		view.refreshTable();
	}

	public void addButtonClicked() {
		view.navigate().to(ESViews.class).wizardConnectorInstance();
	}

	public void editSchemasButtonClicked(RecordVO entity) {
		view.navigate().to(ESViews.class).displayConnectorMappings(entity.getId());
	}
}
