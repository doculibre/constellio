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

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.constellio.app.modules.rm.agent.exceptions.AgentFeatureDisabledException.AgentFeatureDisabledException_EditUserDocuments;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;

public class GetUserDocumentsAgentServlet extends AuthenticatedAgentServlet<Map<String, List<Map<String, String>>>> {

	@Override
	protected Map<String, List<Map<String, String>>> doAuthenticated(Map<String, Object> inParams, String username)
			throws Exception {
		if (getRMConfigs().isAgentEditUserDocuments()) {
			Map<String, List<Map<String, String>>> userDocuments = new HashMap<>();
			
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
			UserServices userServices = modelLayerFactory.newUserServices();
			SearchServices searchServices = modelLayerFactory.newSearchServices();

			UserCredential userCredential = userServices.getUser(username);
			for (String collection : userCredential.getCollections()) {
				User user = userServices.getUserInCollection(username, collection);
				
				MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
				MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
				
				MetadataSchema userDocumentsSchema = types.getSchema(UserDocument.DEFAULT_SCHEMA);
				Metadata userMetadata = userDocumentsSchema.getMetadata(UserDocument.USER);
				LogicalSearchQuery query = new LogicalSearchQuery();
				query.setCondition(from(userDocumentsSchema).where(userMetadata).is(user.getId()));
				query.sortDesc(Schemas.MODIFIED_ON);
				
				List<Map<String, String>> collectionUserDocuments = new ArrayList<>();
				userDocuments.put(collection, collectionUserDocuments);
				List<Record> results = searchServices.search(query);
				for (Record record : results) {
					Map<String, String> userDocument = new HashMap<>();
					collectionUserDocuments.add(userDocument);
					UserDocument document = new UserDocument(record, types);
					userDocument.put("id", record.getId());
					userDocument.put("collection", collection);
					userDocument.put("username", username);
					userDocument.put("filename", document.getContent().getCurrentVersion().getFilename());
				}
			}
			return userDocuments;
		} else {
			throw new AgentFeatureDisabledException_EditUserDocuments();			
		}
	}

}
