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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.users.UserServices;

public class ImportFileAgentServlet extends AuthenticatedAgentServlet<String> {
	
	private String getCollection(String username) {
		ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
		UserServices userServices = modelLayerFactory.newUserServices();
		UserCredential userCredential = userServices.getUserCredential(username);
		List<String> collections = userCredential.getCollections();
		
		User userInLastCollection = null;
		LocalDateTime lastLogin = null;

		for (String collection : collections) {
			User userInCollection = userServices.getUserInCollection(username, collection);
			if (userInLastCollection == null) {
				userInLastCollection = userInCollection;
				lastLogin = userInCollection.getLastLogin();
			} else {
				if (lastLogin == null && userInCollection.getLastLogin() != null) {
					userInLastCollection = userInCollection;
					lastLogin = userInCollection.getLastLogin();
				} else if (lastLogin != null && userInCollection.getLastLogin() != null && userInCollection.getLastLogin()
						.isAfter(lastLogin)) {
					userInLastCollection = userInCollection;
					lastLogin = userInCollection.getLastLogin();
				}
			}
		}
		return userInLastCollection.getCollection();
	}

	@Override
	protected String doAuthenticated(Map<String, Object> inParams, String username) throws Exception {
		String collection = getCollection(username);
		String filename = (String) inParams.get("filename");
		byte[] fileContent = (byte[]) inParams.get("fileContent");
		
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		ContentManager contentManager = modelLayerFactory.getContentManager();
		UserServices userServices = modelLayerFactory.newUserServices();
		RecordServices recordServices = modelLayerFactory.newRecordServices();

		User user = userServices.getUserInCollection(username, collection);
		
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypes types =  metadataSchemasManager.getSchemaTypes(collection);
		MetadataSchema userDocumentSchema = types.getDefaultSchema(UserDocument.SCHEMA_TYPE);
		
		Record userDocumentRecord = recordServices.newRecordWithSchema(userDocumentSchema);
		UserDocument userDocument = new UserDocument(userDocumentRecord, types);
		userDocument.setUser(user.getId());
		
		InputStream contentInputStream = new ByteArrayInputStream(fileContent);
		ContentVersionDataSummary newVersionDataSummary = contentManager.upload(contentInputStream);
		Content content = contentManager.createMajor(user, filename, newVersionDataSummary);
		userDocument.setContent(content);
		recordServices.add(userDocument);
		return userDocument.getId();
	}

}
