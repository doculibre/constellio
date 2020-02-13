package com.constellio.model.entities.records;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.LocalDateTime;

import java.io.Serializable;

public class ContentVersion implements Serializable {

	private final ContentVersionDataSummary contentVersionDataSummary;

	private final String filename;

	private final String version;

	private final String lastModifiedBy;

	private final String comment;

	private final LocalDateTime lastModificationDateTime;

	public ContentVersion(ContentVersionDataSummary contentVersionDataSummary, String filename, String version,
						  String lastModifiedBy, LocalDateTime lastModificationDateTime, String comment) {
		this.contentVersionDataSummary = contentVersionDataSummary;
		this.filename = filename;
		this.version = version;
		this.lastModifiedBy = lastModifiedBy;
		this.lastModificationDateTime = lastModificationDateTime;
		this.comment = comment;
	}

	public String getComment() {
		return comment;
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
		return new ContentVersion(contentVersionDataSummary, newFilename, version, lastModifiedBy, lastModificationDateTime,
				comment);
	}

	public ContentVersion withVersion(String newVersion) {
		return new ContentVersion(contentVersionDataSummary, filename, newVersion, lastModifiedBy, lastModificationDateTime,
				comment);
	}

	public ContentVersion withComment(String comment) {
		return new ContentVersion(contentVersionDataSummary, filename, version, lastModifiedBy, lastModificationDateTime,
				comment);
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

	public ContentVersion withModificationDatetime(LocalDateTime modificationDatetime) {
		return new ContentVersion(contentVersionDataSummary, filename, version, lastModifiedBy, modificationDatetime,
				comment);
	}

	public ContentVersion withLastModifiedBy(String lastModifiedBy) {
		return new ContentVersion(contentVersionDataSummary, filename, version, lastModifiedBy, lastModificationDateTime,
				comment);
	}
}
