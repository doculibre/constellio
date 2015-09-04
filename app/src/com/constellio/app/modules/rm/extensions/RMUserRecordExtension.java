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
package com.constellio.app.modules.rm.extensions;

import static java.util.Arrays.asList;

import com.constellio.app.modules.rm.constants.RMRoles;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.extensions.behaviors.RecordExtension;
import com.constellio.model.extensions.events.records.RecordInCreationEvent;
import com.constellio.model.extensions.events.records.RecordInModificationEvent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.search.SearchServices;

public class RMUserRecordExtension extends RecordExtension {

	String collection;

	ModelLayerFactory modelLayerFactory;

	RMSchemasRecordsServices rm;

	SearchServices searchServices;

	public RMUserRecordExtension(String collection, ModelLayerFactory modelLayerFactory) {
		this.modelLayerFactory = modelLayerFactory;
		this.collection = collection;
		this.rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
	}

	@Override
	public void recordInModification(RecordInModificationEvent event) {
		if (event.isSchemaType(User.SCHEMA_TYPE)) {
			handle(event.getRecord());
		}
	}

	@Override
	public void recordInCreation(RecordInCreationEvent event) {
		if (event.isSchemaType(User.SCHEMA_TYPE)) {
			handle(event.getRecord());
		}
	}

	private void handle(Record record) {
		User user = rm.wrapUser(record);
		if (user.getUserRoles() == null || user.getUserRoles().isEmpty()) {
			user.setUserRoles(asList(RMRoles.USER));
		}
	}

}
