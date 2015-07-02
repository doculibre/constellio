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

import static com.constellio.model.services.contents.ContentFactory.isCheckedOutBy;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;

public class GetCheckedOutDocumentsAgentServlet extends AuthenticatedAgentServlet<Map<String, List<Map<String, String>>>> {

	@Override
	protected Map<String, List<Map<String, String>>> doAuthenticated(Map<String, Object> inParams, String username)
			throws Exception {
		Map<String, List<Map<String, String>>> checkedOutDocuments = new HashMap<>();
		
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		SearchServices searchServices = modelLayerFactory.newSearchServices();

		UserCredential userCredential = userServices.getUser(username);
		for (String collection : userCredential.getCollections()) {
			User user = userServices.getUserInCollection(username, collection);
			
			MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
			MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
			
			RMSchemasRecordsServices rm = new RMSchemasRecordsServices(collection, modelLayerFactory);
			MetadataSchemaType documentSchemaType = rm.documentSchemaType();
			Metadata contentMetadata = rm.documentContent();
			
			LogicalSearchQuery query = new LogicalSearchQuery();
			query.setCondition(from(documentSchemaType).where(contentMetadata).is(isCheckedOutBy(user)));
			query.sortDesc(Schemas.MODIFIED_ON);
			
			List<Map<String, String>> collectionBorrowedDocuments = new ArrayList<>();
			checkedOutDocuments.put(collection, collectionBorrowedDocuments);
			List<Record> results = searchServices.search(query);
			for (Record record : results) {
				Map<String, String> checkedOutDocument = new HashMap<>();
				collectionBorrowedDocuments.add(checkedOutDocument);
				Document document = new Document(record, types);
				checkedOutDocument.put("id", record.getId());
				checkedOutDocument.put("collection", collection);
				checkedOutDocument.put("username", username);
				checkedOutDocument.put("filename", document.getContent().getCurrentCheckedOutVersion().getFilename());
			}
		}
		return checkedOutDocuments;
	}

}
