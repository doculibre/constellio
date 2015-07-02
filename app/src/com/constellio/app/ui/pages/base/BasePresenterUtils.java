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
package com.constellio.app.ui.pages.base;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.services.schemasDisplay.SchemasDisplayManager;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.security.roles.Roles;

public class BasePresenterUtils implements Serializable {

	protected SessionContext sessionContext;

	private transient User currentUser;

	private transient ConstellioFactories constellioFactories;
	private transient AppLayerFactory appLayerFactory;
	private transient ModelLayerFactory modelLayerFactory;
	private transient RecordServices recordServices;
	private transient PresenterService presenterService;
	private transient SearchServices searchServices;
	private transient SchemasDisplayManager schemasDisplayManager;

	public BasePresenterUtils(ConstellioFactories constellioFactories, SessionContext sessionContext) {
		this.constellioFactories = constellioFactories;
		this.sessionContext = sessionContext;
		initTransientObjects();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		initTransientObjects();
	}

	private void initTransientObjects() {
		if (constellioFactories == null) {
			constellioFactories = ConstellioFactories.getInstance();
		}
		appLayerFactory = constellioFactories.getAppLayerFactory();
		modelLayerFactory = constellioFactories.getModelLayerFactory();
	}

	public final String getCollection() {
		return sessionContext.getCurrentCollection();
	}

	public final ConstellioFactories getConstellioFactories() {
		return constellioFactories;
	}

	public final MetadataSchemaTypes types() {
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		return metadataSchemasManager.getSchemaTypes(sessionContext.getCurrentCollection());
	}

	public final MetadataSchemaType schemaType(String code) {
		return types().getSchemaType(code);
	}

	public final MetadataSchema schema(String code) {
		return types().getSchema(code);
	}

	public final User getCurrentUser() {
		if (currentUser == null) {
			currentUser = presenterService().getCurrentUser(sessionContext);
		}
		return currentUser;
	}

	public final Locale getCurrentLocale() {
		return sessionContext.getCurrentLocale();
	}

	public final ModelLayerFactory modelLayerFactory() {
		return modelLayerFactory;
	}

	public final AppLayerFactory appLayerFactory() {
		return appLayerFactory;
	}

	public final RecordServices recordServices() {
		if (recordServices == null) {
			recordServices = modelLayerFactory.newRecordServices();
		}
		return recordServices;
	}

	public final PresenterService presenterService() {
		if (presenterService == null) {
			presenterService = appLayerFactory.newPresenterService();
		}
		return presenterService;
	}

	public final SearchServices searchServices() {
		if (searchServices == null) {
			searchServices = modelLayerFactory.newSearchServices();
		}
		return searchServices;
	}

	public final SchemasDisplayManager schemasDisplayManager() {
		if (schemasDisplayManager == null) {
			schemasDisplayManager = appLayerFactory.getMetadataSchemasDisplayManager();
		}
		return schemasDisplayManager;
	}

	public final String getTitlesStringFromIds(List<String> ids) {
		String collection = sessionContext.getCurrentCollection();
		List<String> titles = recordServices().getRecordTitles(collection, ids);
		return buildString(titles);
	}

	public final String buildString(List<String> list) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			stringBuilder.append(list.get(i));
			if (i != list.size() - 1) {
				stringBuilder.append("<br/>");
			}
		}
		return stringBuilder.toString();
	}

	public final List<String> getAllRecordIds(String schemaCode) {
		String collection = sessionContext.getCurrentCollection();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypes schemaTypes = metadataSchemasManager.getSchemaTypes(collection);
		MetadataSchema schema = schemaTypes.getSchema(schemaCode);
		LogicalSearchQuery query = new LogicalSearchQuery();
		query.setCondition(LogicalSearchQueryOperators.from(schema).returnAll());
		return searchServices.searchRecordIds(query);
	}

	public User wrapUser(Record record) {
		return new User(record, types(), getCollectionRoles());
	}

	public Roles getCollectionRoles() {
		String collection = getCollection();
		return modelLayerFactory.getRolesManager().getCollectionRoles(collection);
	}

}
