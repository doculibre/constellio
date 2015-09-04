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
package com.constellio.app.modules.rm.ui.pages.home;

import static com.constellio.model.services.contents.ContentFactory.isCheckedOutBy;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.io.IOException;
import java.io.Serializable;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
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
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

public class CheckedOutDocumentsTable implements Serializable {
	private transient ModelLayerFactory modelLayerFactory;
	private transient SessionContext sessionContext;
	private transient RMSchemasRecordsServices rm;
	private transient User user;

	public CheckedOutDocumentsTable(ModelLayerFactory modelLayerFactory, SessionContext sessionContext) {
		init(modelLayerFactory, sessionContext);
	}

	public RecordVODataProvider getDataProvider() {
		MetadataSchemaVO schema = new MetadataSchemaToVOBuilder().build(
				rm.documentSchemaType().getDefaultSchema(), VIEW_MODE.TABLE, sessionContext);
		return new RecordVODataProvider(schema, new RecordToVOBuilder(), modelLayerFactory, sessionContext) {
			@Override
			protected LogicalSearchQuery getQuery() {
				MetadataSchemaType document = rm.documentSchemaType();
				return new LogicalSearchQuery(from(document).where(rm.documentContent()).is(isCheckedOutBy(user)))
						.sortDesc(Schemas.MODIFIED_ON);
			}
		};
	}

	private void init(ModelLayerFactory modelLayerFactory, SessionContext sessionContext) {
		this.modelLayerFactory = modelLayerFactory;
		this.sessionContext = sessionContext;
		rm = new RMSchemasRecordsServices(sessionContext.getCurrentCollection(), modelLayerFactory);
		user = new PresenterService(modelLayerFactory).getCurrentUser(sessionContext);
	}

	private void readObject(java.io.ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		init(ConstellioFactories.getInstance().getModelLayerFactory(), ConstellioUI.getCurrentSessionContext());
	}
}
