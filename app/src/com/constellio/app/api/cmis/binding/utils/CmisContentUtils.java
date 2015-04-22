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
package com.constellio.app.api.cmis.binding.utils;

import java.util.ArrayList;
import java.util.List;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;

public class CmisContentUtils {

	public static ContentCmisDocument getContent(String id, RecordServices recordServices, MetadataSchemaTypes types) {
		String[] idParts = id.split("_");
		//Code at position 0 is a constant
		String recordId = idParts[1];
		String metadataLocalCode = idParts[2];
		String contentId = idParts[3];
		String contentVersion = idParts[4];

		Record record = recordServices.getDocumentById(recordId);
		MetadataSchema schema = types.getSchema(record.getSchemaCode());
		Metadata metadata = schema.getMetadata(metadataLocalCode);

		Content content = null;
		if (metadata.isMultivalue() == true) {
			for (Object aContent : record.getList(metadata)) {
				if (((Content) aContent).getId().equals(contentId)) {
					content = (Content) aContent;
					break;
				}
			}
		} else {
			content = record.get(metadata);
		}
		if (content == null || !contentId.equals(content.getId())) {
			throw new RuntimeException("No such content with id '" + id + "'");
		}

		return createContentCmisDocument(content, metadataLocalCode, contentVersion, record);
	}

	private static ContentCmisDocument createContentCmisDocument(Content content, String metadataLocalCode, String contentVersion,
			Record record) {

		boolean isPrivateWorkingCopy = false;
		String returnedContentVersion = contentVersion;
		if (contentVersion.equals("co")) {
			if (content.getCurrentCheckedOutVersion() != null) {
				returnedContentVersion = content.getCurrentCheckedOutVersion().getVersion();
				isPrivateWorkingCopy = true;
			} else {
				returnedContentVersion = content.getCurrentVersion().getVersion();
			}
		}

		return new ContentCmisDocument(content, returnedContentVersion, record, metadataLocalCode, isPrivateWorkingCopy);

	}

	public static List<ContentCmisDocument> getAllVersions(String id, RecordServices recordServices, MetadataSchemaTypes types,
			User user) {
		String[] idParts = id.split("_");
		String metadataLocalCode = idParts[2];
		ContentCmisDocument aContentVersion = getContent(id, recordServices, types);
		Content content = aContentVersion.getContent();
		List<ContentCmisDocument> allVersions = new ArrayList<>();

		for (ContentVersion version : content.getHistoryVersions()) {
			allVersions
					.add(0, new ContentCmisDocument(content, version.getVersion(), aContentVersion.getRecord(),
							metadataLocalCode, false));
		}

		allVersions
				.add(0, new ContentCmisDocument(content, content.getCurrentVersion().getVersion(), aContentVersion.getRecord(),
						metadataLocalCode, false));

		if (user.getId().equals(content.getCheckoutUserId())) {
			allVersions
					.add(0, new ContentCmisDocument(content, content.getCurrentCheckedOutVersion().getVersion(),
							aContentVersion.getRecord(), metadataLocalCode, true));
		}
		return allVersions;
	}

}


