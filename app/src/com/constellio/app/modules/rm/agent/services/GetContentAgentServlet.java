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

import java.io.InputStream;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_ContentMustBeCheckedOut;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.users.UserServices;

public class GetContentAgentServlet extends AuthenticatedAgentServlet<byte[]> {

	@Override
	protected byte[] doAuthenticated(Map<String, Object> inParams, String username) throws Exception {
		String recordId = (String) inParams.get("recordId");
		
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		UserServices userServices = modelLayerFactory.newUserServices();
		ContentManager contentManager = modelLayerFactory.getContentManager();
		
		Record record = recordServices.getDocumentById(recordId);
		String collection = record.getCollection();

		User user = userServices.getUserInCollection(username, collection);
		
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);

		String schemaCode = record.getSchemaCode();
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
		
		ContentVersion contentVersion;
		if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
			RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory);
			Document document = rmSchemasRecordsServices.getDocument(recordId);
			Content content = document.getContent();
			if (user.getId().equals(content.getCheckoutUserId())) {
				contentVersion = content.getCurrentCheckedOutVersion();
				record = document.getWrappedRecord();
			} else {
				throw new ContentImplRuntimeException_ContentMustBeCheckedOut(content.getId());
			}
		} else {
			UserDocument userDocument = new UserDocument(record, types);
			contentVersion = userDocument.getContent().getCurrentVersion();
			record = userDocument.getWrappedRecord();
		}
		
		String hash = contentVersion.getHash();
		InputStream in = contentManager.getContentInputStream(hash, "GetContentAgentServlet");
		try {
			byte[] fileContent = IOUtils.toByteArray(in);
			return fileContent;
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

}
