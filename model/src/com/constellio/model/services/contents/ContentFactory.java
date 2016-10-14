package com.constellio.model.services.contents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
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

	public static final String COLON_REPLACER = "$#$";
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

		int version = getFactoryVersion(string);
		Iterator<String> iterator;

		if (version == 1) {
			iterator = newPartsIterator(string);
		} else {
			iterator = newPartsIterator(string.substring(3));
		}

		return build(iterator, version);
	}

	private ModifiableStructure build(Iterator<String> iterator, int version) {
		String id = iterator.next();
		skipCurrentFileName(iterator);
		skipIsCheckedOut(iterator);
		ContentVersion current = toContentVersion(iterator.next(), version);
		ContentVersion currentCheckedOut = toContentVersion(iterator.next(), version);

		String nextLine = iterator.next();
		boolean emptyVersion = false;
		if ("_EMPTY_VERSION_".equals(nextLine)) {
			emptyVersion = true;
			nextLine = iterator.next();
		}

		String checkedOutBy = toNullableString(nextLine);
		LocalDateTime checkedOutDateTime = toDateTime(iterator.next());
		
		String lastKnownFilename = current != null ? current.getFilename() : null;
		String lastKnownModifiedBy = current != null ? current.getModifiedBy() : null;
		String lastKnownMimetype = current != null ? current.getMimetype() : null;
		
		Lazy<List<ContentVersion>> lazyLoadedHistory = newLazyLoadedHistory(iterator, version, lastKnownFilename, lastKnownModifiedBy, lastKnownMimetype);

		return new ContentImpl(id, current, lazyLoadedHistory, currentCheckedOut, checkedOutDateTime, checkedOutBy, emptyVersion);
	}

	private int getFactoryVersion(String string) {
		if (string.startsWith("v2:")) {
			return 2;
		} else {
			return 1;
		}

	}

	private void skipCurrentFileName(Iterator<String> iterator) {
		iterator.next();
	}

	private void skipIsCheckedOut(Iterator<String> iterator) {
		iterator.next();
	}

	private Lazy<List<ContentVersion>> newLazyLoadedHistory(final Iterator<String> iterator, final int version, final String lastKnownFilename, final String lastKnownModifiedBy, final String lastKnownMimetype) {
		
		return new Lazy<List<ContentVersion>>() {
			@Override
			protected List<ContentVersion> load() {
				List<ContentVersion> versions = new ArrayList<>();

				String currentLastKnownFilename = lastKnownFilename;
				String currentLastKnownModifiedBy = lastKnownModifiedBy;
				String currentLastKnownMimetype = lastKnownMimetype;
				while (iterator.hasNext()) {
					ContentVersion contentVersion = toContentVersion(iterator.next(), version, currentLastKnownFilename, currentLastKnownModifiedBy, currentLastKnownMimetype);
					currentLastKnownFilename = contentVersion.getFilename();
					currentLastKnownModifiedBy = contentVersion.getModifiedBy();
					currentLastKnownMimetype = contentVersion.getMimetype();
					versions.add(contentVersion);
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
		//Version 2
		StringBuilder stringBuilder = new StringBuilder("v2:");

		ContentImpl contentInfo = (ContentImpl) value;
		ContentVersion current = contentInfo.getCurrentVersion();

		String lastKnownFilename = current.getFilename();
		String lastKnownModifiedBy = current.getModifiedBy();
		String lastKnownMimetype = current.getMimetype();
		
		stringBuilder.append(contentInfo.getId());
		stringBuilder.append("::cf=");
		stringBuilder.append(current.getFilename());
		stringBuilder.append("::co=");
		stringBuilder.append(contentInfo.getCheckoutUserId() == null ? "false" : "true");
		stringBuilder.append(":");
		stringBuilder.append(toString(current, null, null, null));
		stringBuilder.append(toString(contentInfo.getCurrentCheckedOutVersion(), lastKnownFilename, lastKnownModifiedBy, lastKnownMimetype));
		stringBuilder.append(":");
		if (contentInfo.isEmptyVersion()) {
			stringBuilder.append("_EMPTY_VERSION_::");
		}

		stringBuilder.append(contentInfo.getCheckoutUserId());
		stringBuilder.append("::");
		stringBuilder.append(toString(contentInfo.getCheckoutDateTime()));
		stringBuilder.append(":");
		for (ContentVersion historyVersion : contentInfo.getHistoryVersions()) {
			stringBuilder.append(toString(historyVersion, lastKnownFilename, lastKnownModifiedBy, lastKnownMimetype));
			lastKnownFilename = historyVersion.getFilename();
			lastKnownModifiedBy = historyVersion.getModifiedBy();
			lastKnownMimetype = historyVersion.getMimetype();
		}
		stringBuilder.append(":");
		return stringBuilder.toString();
	}

	private String toString(ContentVersion contentVersion, String lastKnownFilename, String lastKnownModifiedBy, String lastKnownMimetype) {
		if (contentVersion == null) {
			return ":" + NULL_STRING + ":";
		}
		
		String filename = contentVersion.getFilename();
		String modifiedBy = contentVersion.getModifiedBy();
		String mimetype = contentVersion.getMimetype();
		if (filename.equals(lastKnownFilename)) {
			filename = "";
		}
		if (modifiedBy.equals(lastKnownModifiedBy)) {
			modifiedBy = "";
		}
		if (mimetype.equals(lastKnownMimetype)) {
			mimetype = "";
		}

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(":f=");
		stringBuilder.append(filename);
		stringBuilder.append(":h=");
		stringBuilder.append(contentVersion.getHash());
		stringBuilder.append(":l=");
		stringBuilder.append(contentVersion.getLength());
		stringBuilder.append(":m=");
		stringBuilder.append(mimetype);
		stringBuilder.append(":u=");
		stringBuilder.append(modifiedBy);
		stringBuilder.append(":t=");
		stringBuilder.append(toString(contentVersion.getLastModificationDateTime()));
		stringBuilder.append(":v=");
		stringBuilder.append(contentVersion.getVersion());
		stringBuilder.append(":");
		stringBuilder.append(writeComment(contentVersion.getComment()));
		stringBuilder.append(":");
		return stringBuilder.toString();
	}

	private ContentVersion toContentVersion(String string, int version) {
		return toContentVersion(string, version, null, null, null);
	}
	
	private ContentVersion toContentVersion(String string, int version, String lastKnownFilename, String lastKnownModifiedBy, String lastKnownMimetype) {
		if (version == 2) {
			return toContentVersion2(string, lastKnownFilename, lastKnownModifiedBy, lastKnownMimetype);
		} else {
			return toContentVersion1(string);
		}
	}

	private ContentVersion toContentVersion1(String string) {
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
		return new ContentVersion(contentVersionDataSummary, filename, version, modifiedBy, toDateTime(modifiedDateTime), null);
	}

	private ContentVersion toContentVersion2(String string, String lastKnownFilename, String lastKnownModifiedBy, String lastKnownMimetype) {
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
		String comment = readComment(afterEqual(tokenizer.nextToken()));
		
		if (StringUtils.isBlank(filename)) {
			filename = lastKnownFilename;
		}
		if (StringUtils.isBlank(modifiedBy)) {
			modifiedBy = lastKnownModifiedBy;
		}
		if (StringUtils.isBlank(mimetype)) {
			mimetype = lastKnownMimetype;
		}

		long length = Long.valueOf(lengthStr);
		ContentVersionDataSummary contentVersionDataSummary = new ContentVersionDataSummary(hash, mimetype, length);
		return new ContentVersion(contentVersionDataSummary, filename, version, modifiedBy, toDateTime(modifiedDateTime),
				comment);
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

	private String readComment(String comment) {
		return comment.equals(NULL_STRING) ? null : comment.replace(COLON_REPLACER, ":");
	}

	private String writeComment(String comment) {
		return StringUtils.isBlank(comment) ? null : comment.replace(":", COLON_REPLACER);
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