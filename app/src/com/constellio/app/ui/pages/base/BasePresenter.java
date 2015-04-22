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
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.SearchServices;

@SuppressWarnings("serial")
public abstract class BasePresenter<T extends BaseView> implements Serializable {

	protected final T view;

	protected final String collection;

	protected transient ModelLayerFactory modelLayerFactory;

	protected transient AppLayerFactory appLayerFactory;

	private BasePresenterUtils presenterUtils;

	public BasePresenter(T view) {
		this(view, view.getConstellioFactories(), view.getSessionContext());
	}

	public BasePresenter(T view, ConstellioFactories constellioFactories, SessionContext sessionContext) {
		this.view = view;
		this.collection = sessionContext.getCurrentCollection();
		this.presenterUtils = new BasePresenterUtils(constellioFactories, sessionContext);
		init();
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init();
	}

	private void init() {
		modelLayerFactory = presenterUtils.modelLayerFactory();
		appLayerFactory = presenterUtils.appLayerFactory();
	}

	protected SchemasRecordsServices coreSchemas() {
		return new SchemasRecordsServices(collection, modelLayerFactory);
	}

	protected MetadataSchemaTypes types() {
		return presenterUtils.types();
	}

	protected MetadataSchemaType schemaType(String code) {
		return presenterUtils.schemaType(code);
	}

	protected MetadataSchema schema(String code) {
		return presenterUtils.schema(code);
	}

	protected User getCurrentUser() {
		return presenterUtils.getCurrentUser();
	}

	protected Locale getCurrentLocale() {
		return presenterUtils.getCurrentLocale();
	}

	protected final RecordServices recordServices() {
		return presenterUtils.recordServices();
	}

	protected final PresenterService presenterService() {
		return presenterUtils.presenterService();
	}

	protected final SearchServices searchServices() {
		return presenterUtils.searchServices();
	}

	protected final SchemasDisplayManager schemasDisplayManager() {
		return presenterUtils.schemasDisplayManager();
	}

	public String getTitlesStringFromIds(List<String> ids) {
		return presenterUtils.getTitlesStringFromIds(ids);
	}

	public String buildString(List<String> list) {
		return presenterUtils.buildString(list);
	}

	public final List<String> getAllRecordIds(String schemaCode) {
		return presenterUtils.getAllRecordIds(schemaCode);
	}


}
