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
package com.constellio.app.api.cmis.requests.object;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.api.cmis.CmisExceptions.CmisExceptions_ObjectNotFound;
import com.constellio.app.api.cmis.ConstellioCmisException;
import com.constellio.app.api.cmis.binding.collection.ConstellioCollectionRepository;
import com.constellio.app.api.cmis.binding.utils.CmisUtils;
import com.constellio.app.api.cmis.binding.utils.ContentCmisDocument;
import com.constellio.app.api.cmis.requests.CmisCollectionRequest;
import com.constellio.app.services.factories.AppLayerFactory;

public class CreateObjectRequest extends CmisCollectionRequest<ObjectData> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmisCollectionRequest.class);
	private final CallContext context;
	private final Properties properties;
	private final ObjectInfoHandler objectInfos;
	CreateFolderRequest createFolderRequest;
	CreateDocumentRequest createDocumentRequest;

	public CreateObjectRequest(ConstellioCollectionRepository repository, AppLayerFactory appLayerFactory,
			CreateFolderRequest createFolderRequest, CreateDocumentRequest createDocumentRequest, CallContext context,
			Properties properties, String folderId, ContentStream contentStream, VersioningState versioningState,
			ObjectInfoHandler objectInfos) {
		super(repository, appLayerFactory);
		this.createFolderRequest = createFolderRequest;
		this.createDocumentRequest = createDocumentRequest;
		this.context = context;
		this.properties = properties;
		this.objectInfos = objectInfos;
	}

	@Override
	public ObjectData process()
			throws ConstellioCmisException {

		boolean userReadOnly = false;

		String typeId = CmisUtils.getObjectTypeId(properties);
		TypeDefinition type = repository.getTypeDefinitionsManager().getInternalTypeDefinition(typeId);
		if (type == null) {
			throw new CmisExceptions_ObjectNotFound("Type", typeId);
		}

		if (type.getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT) {
			ContentCmisDocument contentCmisDocument = createDocumentRequest.process();
			return newContentObjectDataBuilder().build(context, contentCmisDocument, null, false, userReadOnly, objectInfos);
		} else if (type.getBaseTypeId() == BaseTypeId.CMIS_FOLDER) {
			String objectId = createFolderRequest.process();
			return newObjectDataBuilder().build(context, modelLayerFactory.newRecordServices()
					.getDocumentById(objectId), null, false, userReadOnly, objectInfos);
		} else {
			throw new CmisExceptions_ObjectNotFound("Type", typeId);
		}

	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
