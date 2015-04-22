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
package com.constellio.model.entities.records;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.LocalDateTime;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.services.contents.ContentVersionDataSummary;

public class ContentVersion {

	private final ContentVersionDataSummary contentVersionDataSummary;

	private final String filename;

	private final String version;

	private final String lastModifiedBy;

	private final LocalDateTime lastModificationDateTime;

	public ContentVersion(ContentVersionDataSummary contentVersionDataSummary, String filename, String version,
			String lastModifiedBy,
			LocalDateTime lastModificationDateTime) {
		this.contentVersionDataSummary = contentVersionDataSummary;
		this.filename = filename;
		this.version = version;
		this.lastModifiedBy = lastModifiedBy;
		this.lastModificationDateTime = lastModificationDateTime;
	}

	public String getHash() {
		return contentVersionDataSummary.getHash();
	}

	public long getLength() {
		return contentVersionDataSummary.getLength();
	}

	public String getFilename() {
		return filename;
	}

	public String getMimetype() {
		return contentVersionDataSummary.getMimetype();
	}

	public String getVersion() {
		return version;
	}

	public int getMajor() {
		int dotIndex = version.indexOf(".");
		return Integer.valueOf(version.substring(0, dotIndex));
	}

	public int getMinor() {
		int dotIndex = version.indexOf(".");
		return Integer.valueOf(version.substring(dotIndex + 1));
	}

	public boolean isMajor() {
		return getMinor() == 0;
	}

	public final ContentVersion withFilename(String newFilename) {
		return new ContentVersion(contentVersionDataSummary, newFilename, version, lastModifiedBy, lastModificationDateTime);
	}

	public ContentVersion withVersion(String newVersion) {
		return new ContentVersion(contentVersionDataSummary, filename, newVersion, lastModifiedBy, lastModificationDateTime);
	}

	public String getModifiedBy() {
		return lastModifiedBy;
	}

	public LocalDateTime getLastModificationDateTime() {
		return lastModificationDateTime;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public boolean hasSameHash(ContentVersion otherVersion) {
		return LangUtils.areNullableEqual(contentVersionDataSummary.getHash(), otherVersion.getHash());
	}
}
