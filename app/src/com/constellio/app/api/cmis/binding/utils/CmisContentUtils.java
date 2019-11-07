package com.constellio.app.api.cmis.binding.utils;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.records.RecordServices;

import java.util.ArrayList;
import java.util.List;

public class CmisContentUtils {

	public static ContentCmisDocument getContent(String id, RecordServices recordServices, MetadataSchemaTypes types) {
		String[] idParts = id.split("_");
		//Code at position 0 is a constant
		String recordId = idParts[1];
		String metadataLocalCode = idParts[2];
		String contentId = idParts[3];
		String contentVersion = idParts[4];

		Record record = recordServices.getDocumentById(recordId);
		MetadataSchema schema = types.getSchemaOf(record);
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

	private static ContentCmisDocument createContentCmisDocument(Content content, String metadataLocalCode,
																 String contentVersion,
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

	public static List<ContentCmisDocument> getAllVersions(String id, RecordServices recordServices,
														   MetadataSchemaTypes types,
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


