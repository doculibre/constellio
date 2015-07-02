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
package com.constellio.app.modules.rm.agent.services;

import java.util.Map;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;

public class CheckInAgentServlet extends AuthenticatedAgentServlet<Boolean> {

	@Override
	protected Boolean doAuthenticated(Map<String, Object> inParams, String username) throws Exception {
		String recordId = (String) inParams.get("recordId");
		boolean majorVersion = (Boolean) inParams.get("majorVersion");
		
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		
		Record record = recordServices.getDocumentById(recordId);
		String collection = record.getCollection();
		
		RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory);
		Document document = rmSchemasRecordsServices.getDocument(recordId);
		Content content = document.getContent();
		content.checkIn();
		if (majorVersion) {
			content.finalizeVersion();
		}
		recordServices.update(document.getWrappedRecord());
		return true;
	}

}
