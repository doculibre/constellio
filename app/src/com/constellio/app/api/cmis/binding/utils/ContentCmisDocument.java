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

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Schemas;

public class ContentCmisDocument {

	public static final String DOCUMENT_ID_PREFIX = "content_";

	Content content;
	String versionLabel;
	Record record;
	String metadataLocalCode;
	private boolean isPrivateWorkingCopy;

	public ContentCmisDocument(Content content, String version, Record record, String metadataLocalCode,
			boolean isPrivateWorkingCopy) {
		this.content = content;
		this.versionLabel = version;
		this.record = record;
		this.metadataLocalCode = metadataLocalCode;
		this.isPrivateWorkingCopy = isPrivateWorkingCopy;

		if (content == null) {
			throw new RuntimeException("Content must not be null");
		}

		if (version == null) {
			throw new RuntimeException("Version must not be null");
		}

		if (record == null) {
			throw new RuntimeException("Record must not be null");
		}

		if (metadataLocalCode == null) {
			throw new RuntimeException("metadataLocalCode must not be null");
		}

	}

	public static ContentCmisDocument createForCurrentVersion(Content content, Record record, String metadataLocalCode) {
		String version = content.getCurrentVersion().getVersion();
		return new ContentCmisDocument(content, version, record, metadataLocalCode, false);
	}

	public static ContentCmisDocument createForVersionSeenBy(Content content, Record record, String metadataLocalCode,
			User user) {

		if (user.getId().equals(content.getCheckoutUserId())) {
			return new ContentCmisDocument(content, content.getCurrentCheckedOutVersion().getVersion(), record, metadataLocalCode,
					true);
		} else {
			return new ContentCmisDocument(content, content.getCurrentVersion().getVersion(), record, metadataLocalCode, false);
		}

	}

	public Content getContent() {
		return content;
	}

	public String getVersionLabel() {
		return versionLabel;
	}

	public ContentVersion getContentVersion() {
		if (isPrivateWorkingCopy) {
			return content.getCurrentCheckedOutVersion();
		} else {
			return content.getVersion(versionLabel);
		}
	}

	public ContentVersion getCheckedOutContentVersion() {
		return content.getCurrentCheckedOutVersion();
	}

	public String getContentVersionId() {
		if (isPrivateWorkingCopy()) {
			return getPrivateWorkingCopyVersionId();
		} else {
			return getVersionSeriesId() + "_" + versionLabel;
		}
	}

	public Record getRecord() {
		return record;
	}

	public String getVersionSeriesId() {
		return DOCUMENT_ID_PREFIX + record.getId() + "_" + metadataLocalCode + "_" + content.getId();
	}

	public String getDocumentId() {
		if (isPrivateWorkingCopy()) {
			return getPrivateWorkingCopyVersionId();
		} else {
			return getVersionSeriesId() + "_" + content.getCurrentVersion().getVersion();
		}
	}

	public String getContentStreamId() {
		return getContentVersion().getHash();
	}

	public String getCheckedOutContentStreamId() {
		return getCheckedOutContentVersion().getHash();
	}

	public String getPrivateWorkingCopyVersionId() {
		return getVersionSeriesId() + "_co";
	}

	public boolean isPrivateWorkingCopy() {
		return isPrivateWorkingCopy;
	}

	public boolean isMajor() {
		return versionLabel.endsWith(".0") && !isPrivateWorkingCopy;
	}

	public boolean isLatest() {
		if (isPrivateWorkingCopy()) {
			return false;
		} else {
			return content.getCurrentVersion().getVersion().equals(versionLabel);
		}
	}

	public String getPath() {
		return record.get(Schemas.PRINCIPAL_PATH) + "/content_" + content.getId();
	}

	public boolean isLatestMajor() {

		String latestMajor = content.getCurrentVersion().getMajor() + ".0";
		return versionLabel.endsWith(latestMajor) && !isPrivateWorkingCopy;
	}

	public boolean isCheckedOut() {
		return content.getCheckoutUserId() != null;
	}

	public String getMetadataLocalCode() {
		return metadataLocalCode;
	}
}
