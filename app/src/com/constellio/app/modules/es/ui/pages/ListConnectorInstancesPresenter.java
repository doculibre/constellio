/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.modules.es.ui.pages;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.constellio.app.modules.es.model.connectors.ConnectorInstance;
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
			protected LogicalSearchQuery getQuery() {
				MetadataSchemaType connectorInstanceSchemaType = modelLayerFactory.getMetadataSchemasManager()
						.getSchemaTypes(collection)
						.getSchemaType(ConnectorInstance.SCHEMA_TYPE);
				LogicalSearchQuery query = new LogicalSearchQuery(
						from(connectorInstanceSchemaType).returnAll())
						.sortDesc(Schemas.MODIFIED_ON);
				return query;
			}
		};
		return dataProvider;
	}

	public void displayButtonClicked(RecordVO entity) {
		view.navigateTo().displayConnectorInstance(entity.getId());
	}

	public void editButtonClicked(RecordVO entity) {
		view.navigateTo().editConnectorInstances(entity.getId());
	}

	public void deleteButtonClicked(RecordVO entity) {
		throw new UnsupportedOperationException();
		//		Record record = presenterService().getRecord(entity.getId());
		//		User user = presenterService().getCurrentUser(view.getSessionContext());
		//		recordServices().logicallyDelete(record, user);
		//		view.navigateTo().listConnectorInstances();
	}

	public void addButtonClicked() {
		view.navigateTo().wizardConnectorInstance();
	}

	public void editSchemasButtonClicked(RecordVO entity) {
		view.navigateTo().editSchemasConnectorInstance(entity.getId());
	}
}
