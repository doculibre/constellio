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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.constellio.model.entities.Language;

public class ParsedContent {

	private String parsedContent;

	private String language;

	private String mimeType;

	private Map<String, Object> properties;
	private long length;

	public ParsedContent(String parsedContent, String language, String mimeType, long length, Map<String, Object> properties) {
		this.parsedContent = parsedContent;
		this.language = language;
		this.mimeType = mimeType;
		this.length = length;
		this.properties = Collections.unmodifiableMap(properties);
	}

	public static ParsedContent unparsable(String mimeType, long length) {
		return new ParsedContent("", Language.UNKNOWN.getCode(), mimeType, length, new HashMap<String, Object>());
	}

	public String getParsedContent() {
		return parsedContent;
	}

	public String getLanguage() {
		return language;
	}

	public String getMimeType() {
		return mimeType;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}
}
