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
package com.constellio.app.ui.entities;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

public class ContentVersionVO implements Serializable {
	
	public static interface InputStreamProvider extends Serializable {

		InputStream getInputStream(String streamName);
		
		void deleteTemp();
		
	}
	
	private String contentId;
	
	private String hash;
	
	private String fileName;
	
	private String mimeType;
	
	private long length;
	
	private String version;
	
	private Boolean majorVersion;

	private final Date lastModificationDateTime;

	private final String lastModifiedBy;
	
	private InputStreamProvider inputStreamProvider;
	
	private String checkoutUserId;
	
	private LocalDateTime checkoutDateTime;

	public ContentVersionVO(String contentId, String hash, String fileName, String mimeType, long length, String version, Date lastModificationDateTime, String lastModifiedBy, String checkoutUserId, LocalDateTime checkoutDateTime, InputStreamProvider inputStreamProvider) {
		super();
		this.contentId = contentId;
		this.hash = hash;
		this.fileName = fileName;
		this.mimeType = mimeType;
		this.length = length;
		this.version = version;
		this.lastModificationDateTime = lastModificationDateTime;
		this.lastModifiedBy = lastModifiedBy;
		this.inputStreamProvider = inputStreamProvider;
		this.checkoutUserId = checkoutUserId;
		this.checkoutDateTime = checkoutDateTime;
	}

	public final String getContentId() {
		return contentId;
	}
	
	public final void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public final String getHash() {
		return hash;
	}
	
	public final void setHash(String id) {
		this.hash = id;
	}

	public final Boolean isMajorVersion() {
		return majorVersion;
	}

	public final void setMajorVersion(Boolean majorVersion) {
		this.majorVersion = majorVersion;
	}

	public final String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public final String getFileName() {
		return fileName;
	}

	public final String getMimeType() {
		return mimeType;
	}

	public final long getLength() {
		return length;
	}

	public final Date getLastModificationDateTime() {
		return lastModificationDateTime;
	}

	public final String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public final String getCheckoutUserId() {
		return checkoutUserId;
	}

	public final LocalDateTime getCheckoutDateTime() {
		return checkoutDateTime;
	}

	public final InputStreamProvider getInputStreamProvider() {
		return inputStreamProvider;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(fileName);
		if (StringUtils.isNotBlank(version)) {
			sb.append(" [" + version + "]");
		}
		sb.append(" (" + FileUtils.byteCountToDisplaySize(length) + ")");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contentId == null) ? 0 : contentId.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (getClass() != obj.getClass()) {
			return false;
		} else {
			ContentVersionVO other = (ContentVersionVO) obj;
			if (contentId == null && other.contentId != null) {
				return false;
			} else if (contentId != null && other.contentId == null) {
				return false;
			} else if (contentId != null && !contentId.equals(other.contentId)) {
				return false;
			} else  if (version == null && other.version != null) {
				return false;
			} else  if (version != null && other.version == null) {
				return false;
			} else if (version != null && !version.equals(other.version)) {
				return false;
			} else if (contentId == null && version == null) {
				if (fileName != null && !fileName.equals(other.fileName)) {
					return false;
				} else if (length != other.length) {
					return false;
				}
			}
		}	
		return true;
	}

}
