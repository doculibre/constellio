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
import java.util.Date;
import java.util.Map;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;

/**
 * -1: Doesn't exist
 * 0 : Content match
 * 1 : Content mismatch - Server file more recent
 * 2 : Content mismatch - Agent file more recent
 * 
 * @author Vincent
 */
public class CompareContentAgentServlet extends AuthenticatedAgentServlet<Integer> {

	@Override
	protected Integer doAuthenticated(Map<String, Object> inParams, String username) throws Exception {
		String recordId = (String) inParams.get("recordId");
		String agentFileHash = (String) inParams.get("hash");
		Date agentFileLastModified = (Date) inParams.get("lastModified");
		
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		ContentManager contentManager = modelLayerFactory.getContentManager();
		
		int comparisonResult;
		try {
			Record record = recordServices.getDocumentById(recordId);
			String collection = record.getCollection();
			
			MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
			MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);

			String schemaCode = record.getSchemaCode();
			String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
			
			ContentVersion contentVersion;
			if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
				RMSchemasRecordsServices rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, modelLayerFactory);
				Document document = rmSchemasRecordsServices.getDocument(recordId);
				Content content = document.getContent();
				if (content != null) {
					contentVersion = content.getCurrentCheckedOutVersion();
					if (contentVersion == null) {
						contentVersion = content.getCurrentVersion();
					}
					record = document.getWrappedRecord();
				} else {
					contentVersion = null;
					comparisonResult = -1;
				}
			} else {
				UserDocument userDocument = new UserDocument(record, types);
				contentVersion = userDocument.getContent().getCurrentVersion();
				record = userDocument.getWrappedRecord();
			}
			
			if (contentVersion != null) {
				String contentHash = contentVersion.getHash();
				InputStream in = contentManager.getContentInputStream(contentHash, "GetContentAgentServlet");
				String comparisonHash = getHash(in);
				Date contentLastModified = contentVersion.getLastModificationDateTime().toDate();
				
				if (agentFileHash.equals(comparisonHash)) {
					comparisonResult = 0;
				} else if (agentFileLastModified.before(contentLastModified)) {
					comparisonResult = 1;
				} else {
					comparisonResult = 2;
				}
			} else {
				comparisonResult = -1;
			}
		} catch (NoSuchRecordWithId e) {
			comparisonResult = -1;
		}
		return comparisonResult;
	}

}
