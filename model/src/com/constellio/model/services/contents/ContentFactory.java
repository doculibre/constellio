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
package com.constellio.model.services.contents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDateTime;

import com.constellio.data.utils.LazyIterator;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.StructureFactory;
import com.constellio.model.services.search.query.logical.criteria.IsContainingTextCriterion;
import com.constellio.model.utils.Lazy;

public class ContentFactory implements StructureFactory {

	public static final String INFO_SEPARATOR = ":";
	private static final String NULL_STRING = "null";

	public static IsContainingTextCriterion isCheckedOutBy(User user) {
		return isCheckedOutBy(user.getId());
	}

	public static IsContainingTextCriterion isCheckedOutBy(String userId) {
		return new IsContainingTextCriterion(INFO_SEPARATOR + userId + INFO_SEPARATOR);
	}

	public static IsContainingTextCriterion isFilename(String filename) {
		return new IsContainingTextCriterion(INFO_SEPARATOR + "f=" + filename + INFO_SEPARATOR);
	}

	public static IsContainingTextCriterion isCurrentFilename(String filename) {
		return new IsContainingTextCriterion(INFO_SEPARATOR + "cf=" + filename + INFO_SEPARATOR);
	}

	public static IsContainingTextCriterion isHash(String hash) {
		return new IsContainingTextCriterion(INFO_SEPARATOR + "h=" + hash + INFO_SEPARATOR);
	}

	public static IsContainingTextCriterion isMimetype(String mimetype) {
		return new IsContainingTextCriterion(INFO_SEPARATOR + "m=" + mimetype + INFO_SEPARATOR);
	}

	public static IsContainingTextCriterion checkedOut() {
		return new IsContainingTextCriterion(INFO_SEPARATOR + "co=true" + INFO_SEPARATOR);
	}

	private static Iterator<String> newPartsIterator(final String string) {
		return new LazyIterator<String>() {

			int currentIndex = -2;

			@Override
			protected String getNextOrNull() {
				int nextSeparator = string.indexOf("::", currentIndex + 1);
				if (nextSeparator == -1) {
					return null;
				}
				String content = string.substring(currentIndex + 2, nextSeparator);
				currentIndex = nextSeparator;
				return content;
			}
		};
	}

	@Override
	public ModifiableStructure build(String string) {
		Iterator<String> iterator = newPartsIterator(string);

		String id = iterator.next();
		skipCurrentFileName(iterator);
		skipIsCheckedOut(iterator);
		ContentVersion current = toContentVersion(iterator.next());
		ContentVersion currentCheckedOut = toContentVersion(iterator.next());

		String nextLine = iterator.next();
		boolean emptyVersion = false;
		if ("_EMPTY_VERSION_".equals(nextLine)) {
			emptyVersion = true;
			nextLine = iterator.next();
		}

		String checkedOutBy = toNullableString(nextLine);
		LocalDateTime checkedOutDateTime = toDateTime(iterator.next());
		Lazy<List<ContentVersion>> lazyLoadedHistory = newLazyLoadedHistory(iterator);

		return new ContentImpl(id, current, lazyLoadedHistory, currentCheckedOut, checkedOutDateTime, checkedOutBy, emptyVersion);

	}

	private void skipCurrentFileName(Iterator<String> iterator) {
		iterator.next();
	}

	private void skipIsCheckedOut(Iterator<String> iterator) {
		iterator.next();
	}

	private Lazy<List<ContentVersion>> newLazyLoadedHistory(final Iterator<String> iterator) {
		return new Lazy<List<ContentVersion>>() {
			@Override
			protected List<ContentVersion> load() {
				List<ContentVersion> versions = new ArrayList<>();

				while (iterator.hasNext()) {
					versions.add(toContentVersion(iterator.next()));
				}

				return versions;
			}
		};
	}

	private String toNullableString(String string) {
		return NULL_STRING.equals(string) ? null : string;
	}

	private String afterEqualSign(String string) {
		int index = string.indexOf("=");
		return string.substring(index + 1);
	}

	@Override
	public String toString(ModifiableStructure value) {
		ContentImpl contentInfo = (ContentImpl) value;
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(contentInfo.getId());
		stringBuilder.append("::cf=");
		stringBuilder.append(contentInfo.getCurrentVersion().getFilename());
		stringBuilder.append("::co=");
		stringBuilder.append(contentInfo.getCheckoutUserId() == null ? "false" : "true");
		stringBuilder.append(":");
		stringBuilder.append(toString(contentInfo.getCurrentVersion()));
		stringBuilder.append(toString(contentInfo.getCurrentCheckedOutVersion()));
		stringBuilder.append(":");
		if (contentInfo.isEmptyVersion()) {
			stringBuilder.append("_EMPTY_VERSION_::");
		}

		stringBuilder.append(contentInfo.getCheckoutUserId());
		stringBuilder.append("::");
		stringBuilder.append(toString(contentInfo.getCheckoutDateTime()));
		stringBuilder.append(":");
		for (ContentVersion historyVersion : contentInfo.getHistoryVersions()) {
			stringBuilder.append(toString(historyVersion));
		}
		stringBuilder.append(":");
		return stringBuilder.toString();
	}

	private String toString(ContentVersion contentVersion) {
		if (contentVersion == null) {
			return ":" + NULL_STRING + ":";
		}

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(":f=");
		stringBuilder.append(contentVersion.getFilename());
		stringBuilder.append(":h=");
		stringBuilder.append(contentVersion.getHash());
		stringBuilder.append(":l=");
		stringBuilder.append(contentVersion.getLength());
		stringBuilder.append(":m=");
		stringBuilder.append(contentVersion.getMimetype());
		stringBuilder.append(":u=");
		stringBuilder.append(contentVersion.getModifiedBy());
		stringBuilder.append(":t=");
		stringBuilder.append(toString(contentVersion.getLastModificationDateTime()));
		stringBuilder.append(":v=");
		stringBuilder.append(contentVersion.getVersion());
		stringBuilder.append(":");
		return stringBuilder.toString();
	}

	private ContentVersion toContentVersion(String string) {
		if (string.equals(NULL_STRING)) {
			return null;
		}

		StringTokenizer tokenizer = new StringTokenizer(string, ":");
		String filename = afterEqual(tokenizer.nextToken());
		String hash = afterEqual(tokenizer.nextToken());
		String lengthStr = afterEqual(tokenizer.nextToken());
		String mimetype = afterEqual(tokenizer.nextToken());
		String modifiedBy = afterEqual(tokenizer.nextToken());
		String modifiedDateTime = afterEqual(tokenizer.nextToken());
		String version = afterEqual(tokenizer.nextToken());

		long length = Long.valueOf(lengthStr);
		ContentVersionDataSummary contentVersionDataSummary = new ContentVersionDataSummary(hash, mimetype, length);
		return new ContentVersion(contentVersionDataSummary, filename, version, modifiedBy, toDateTime(modifiedDateTime));
	}

	private LocalDateTime toDateTime(String dateTimeString) {
		if (dateTimeString.equals(NULL_STRING)) {
			return null;
		}
		Long timestamp = Long.valueOf(dateTimeString);
		return new LocalDateTime(timestamp);
	}

	private String toString(LocalDateTime localDateTime) {
		if (localDateTime == null) {
			return NULL_STRING;
		}
		long timestamp = localDateTime.toDate().getTime();
		return "" + timestamp;
	}

	private String afterEqual(String keyValue) {
		int equalIndex = keyValue.indexOf("=");
		return keyValue.substring(equalIndex + 1);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

}