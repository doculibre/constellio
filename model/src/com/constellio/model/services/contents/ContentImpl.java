package com.constellio.model.services.contents;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.enums.ContentCheckoutSource;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_CannotDeleteLastVersion;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_ContentMustBeCheckedOut;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_ContentMustNotBeCheckedOut;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_InvalidArgument;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_NoSuchVersion;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_UserHasNoDeleteVersionPermission;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_VersionMustBeHigherThanPreviousVersion;
import com.constellio.model.utils.Lazy;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ContentImpl implements Content {

	private String id;

	private ContentVersion currentVersion;

	private ContentVersion currentCheckedOutVersion;

	private LocalDateTime checkoutDateTime;

	private String checkoutUserId;

	private Integer checkoutSource;

	private Lazy<List<ContentVersion>> lazyHistory;
	private List<ContentVersion> history;

	private boolean dirty;

	private boolean emptyVersion;

	/*public ContentImpl(String id, ContentVersion currentVersion, Lazy<List<ContentVersion>> lazyHistory,
					   ContentVersion currentCheckedOutVersion, LocalDateTime checkoutDateTime, String checkoutUserId,
					   boolean emptyVersion) {
		this(id, currentVersion, lazyHistory, currentCheckedOutVersion, checkoutDateTime, checkoutUserId,
				null, emptyVersion);
	}*/

	public ContentImpl(String id, ContentVersion currentVersion, Lazy<List<ContentVersion>> lazyHistory,
					   ContentVersion currentCheckedOutVersion, LocalDateTime checkoutDateTime, String checkoutUserId,
					   Integer checkoutSource, boolean emptyVersion) {
		this.currentVersion = currentVersion;
		this.emptyVersion = emptyVersion;
		this.checkoutDateTime = checkoutDateTime;
		if (currentCheckedOutVersion == null && checkoutUserId != null) {
			this.currentCheckedOutVersion = currentVersion;
		} else {
			this.currentCheckedOutVersion = currentCheckedOutVersion;
		}
		this.checkoutUserId = checkoutUserId;
		this.checkoutSource = checkoutSource;
		this.lazyHistory = lazyHistory;
		this.id = id;
	}

	private ContentImpl() {

	}

	public static ContentImpl create(String id, User user, String filename, ContentVersionDataSummary newVersion,
									 boolean major,
									 boolean empty) {
		String version = major ? "1.0" : "0.1";

		return createWithVersion(id, user, filename, newVersion, version, empty);
	}

	public static ContentImpl createWithVersion(String id, User user, String filename,
												ContentVersionDataSummary newVersion,
												String version, boolean empty) {
		version = validateVersion(version, null, false, false);
		validateArgument("id", id);
		validateUserArgument(user);
		validateFilenameArgument(filename);
		valdiateNewVersionArgument(newVersion);

		LocalDateTime now = TimeProvider.getLocalDateTime();
		ContentImpl content = new ContentImpl();
		String correctFilename = correctFilename(filename);
		content.id = id;
		content.history = new ArrayList<>();
		content.dirty = true;
		content.emptyVersion = empty;
		content.setNewCurrentVersion(new ContentVersion(newVersion, correctFilename, version, user.getId(), now, null));
		return content;
	}

	public static ContentImpl create(String id, ContentVersion currentVersion, List<ContentVersion> history) {
		validateArgument("id", id);
		ContentImpl content = new ContentImpl();
		content.dirty = true;
		content.id = id;
		content.currentVersion = currentVersion;
		content.history = history;
		return content;
	}

	public static ContentImpl createSystemContent(String filename, ContentVersionDataSummary newVersion) {
		validateFilenameArgument(filename);
		String fileName = correctFilename(filename);
		String id = UUID.randomUUID().toString();
		boolean major = true;
		ContentImpl content = new ContentImpl();
		content.id = id;
		content.history = new ArrayList<>();
		content.dirty = true;
		content.setNewCurrentVersion(
				new ContentVersion(newVersion, fileName, "1.0", null, TimeProvider.getLocalDateTime(), null));
		return content;
	}

	private static String correctFilename(String filename) {
		return filename.replace(":", "");
	}

	private static String getFirstVersion(boolean finalized) {
		return finalized ? "1.0" : "0.1";
	}

	public static String getVersionAfter(String version, boolean finalized) {
		int dotIndex = version.indexOf(".");
		Integer major = Integer.valueOf(version.substring(0, dotIndex));
		Integer minor = Integer.valueOf(version.substring(dotIndex + 1));
		if (finalized) {
			major++;
			minor = 0;
		} else {
			minor++;
		}
		return major + "." + minor;
	}

	private static void validateUserArgument(User argumentValue) {
		if (argumentValue == null) {
			throw new ContentImplRuntimeException_InvalidArgument("user");
		}
	}

	private static void valdiateNewVersionArgument(ContentVersionDataSummary newVersion) {
		if (newVersion == null) {
			throw new ContentImplRuntimeException_InvalidArgument("new version");
		}

		if (newVersion.getHash() == null) {
			throw new ContentImplRuntimeException_InvalidArgument("new version");
		}

		if (newVersion.getMimetype() == null) {
			throw new ContentImplRuntimeException_InvalidArgument("new version");
		}

	}

	private static void validateArgument(String argumentName, String argumentValue) {
		if (!StringUtils.isNotBlank(argumentValue)) {
			throw new ContentImplRuntimeException_InvalidArgument(argumentName);
		}
	}

	private static String validateVersion(String version, String currentVersionLabel, boolean empty,
										  boolean overwrite) {
		try {
			if (!empty && currentVersionLabel != null && version != null) {
				Integer versionCompare = versionCompare(currentVersionLabel, version);
				if (versionCompare > 0 || (versionCompare == 0 && !overwrite)) {
					throw new ContentImplRuntimeException_VersionMustBeHigherThanPreviousVersion(version, currentVersionLabel);
				}
			}
			Double.valueOf(version);
			if (version.indexOf(".") == -1) {
				version += ".0";
			}
			return version;
		} catch (NumberFormatException e) {
			throw new ContentImplRuntimeException_InvalidArgument("version");
		}
	}

	/**
	 * http://stackoverflow.com/questions/6701948/efficient-way-to-compare-version-strings-in-java
	 * <p>
	 * Compares two version strings.
	 * <p>
	 * Use this instead of String.compareTo() for a non-lexicographical
	 * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
	 *
	 * @param str1 a string of ordinal numbers separated by decimal points.
	 * @param str2 a string of ordinal numbers separated by decimal points.
	 * @return The result is a negative integer if str1 is _numerically_ less than str2.
	 * The result is a positive integer if str1 is _numerically_ greater than str2.
	 * The result is zero if the strings are _numerically_ equal.
	 * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
	 */
	public static Integer versionCompare(String str1, String str2) {
		String[] vals1 = str1.split("\\.");
		String[] vals2 = str2.split("\\.");
		int i = 0;
		// set index to first non-equal ordinal or length of shortest version string
		while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
			i++;
		}
		// compare first non-equal ordinal number
		if (i < vals1.length && i < vals2.length) {
			int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
			return Integer.signum(diff);
		} else {
			// the strings are equal or one string is a substring of the other
			// e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
			return Integer.signum(vals1.length - vals2.length);
		}
	}

	private static void validateFilenameArgument(String argumentValue) {
		validateArgument("filename", argumentValue);
	}

	public String getId() {
		return id;
	}

	public List<ContentVersion> getHistoryVersions() {
		ensureHistoryIsLoaded();
		return Collections.unmodifiableList(history);
	}

	@Override
	public List<ContentVersion> getVersions() {
		List<ContentVersion> versions = new ArrayList<>();
		versions.addAll(getHistoryVersions());
		versions.add(getCurrentVersion());
		return Collections.unmodifiableList(versions);
	}

	public ContentImpl checkIn() {
		ensureCheckedOut();
		this.checkoutDateTime = null;
		this.checkoutUserId = null;
		this.checkoutSource = null;
		this.dirty = true;

		if (emptyVersion) {
			currentVersion = currentCheckedOutVersion;
			emptyVersion = false;
		} else if (!currentCheckedOutVersion.getHash().equals(currentVersion.getHash())) {
			setNewCurrentVersion(currentCheckedOutVersion);
		}
		this.currentCheckedOutVersion = null;

		return this;
	}

	public ContentImpl cancelCheckOut() {
		ensureCheckedOut();
		this.checkoutDateTime = null;
		this.checkoutUserId = null;
		this.checkoutSource = null;
		this.dirty = true;
		this.currentCheckedOutVersion = null;

		return this;
	}

	@Override
	public ContentImpl checkInWithModificationAndNameInSameVersion(ContentVersionDataSummary newVersion, String name) {
		String version = getCurrentVersion().getVersion();
		checkInWithModificationAndNameAndVersion(newVersion, false, name, version);

		return this;
	}

	public ContentImpl checkInWithModificationAndName(ContentVersionDataSummary newVersion, boolean finalize,
													  String name) {
		String nextVersion;

		if (emptyVersion) {
			nextVersion = finalize ? "1.0" : "0.1";
		} else {
			nextVersion = getNextVersion(finalize);
		}

		return checkInWithModificationAndNameAndVersion(newVersion, finalize, name, nextVersion);
	}

	private ContentImpl checkInWithModificationAndNameAndVersion(ContentVersionDataSummary newVersion, boolean finalize,
																 String name, String nextVersion) {
		ensureCheckedOut();
		valdiateNewVersionArgument(newVersion);
		LocalDateTime now = TimeProvider.getLocalDateTime();
		String correctFilename = correctFilename(name);
		String userId = this.getCheckoutUserId();
		setNewCurrentVersion(new ContentVersion(newVersion, correctFilename, nextVersion, userId, now, null));
		if (emptyVersion) {
			this.history.clear();
			this.emptyVersion = false;
		}
		this.checkoutDateTime = null;
		this.checkoutUserId = null;
		this.checkoutSource = null;
		this.currentCheckedOutVersion = null;
		this.dirty = true;
		return this;
	}

	public ContentImpl checkInWithModification(ContentVersionDataSummary newVersion, boolean finalize) {
		ensureCheckedOut();
		return checkInWithModificationAndName(newVersion, finalize, currentCheckedOutVersion.getFilename());
	}

	public ContentImpl checkOut(User user) {
		return checkOut(user, ContentCheckoutSource.CONSTELLIO.getValue());
	}

	public ContentImpl checkOut(User user, int checkoutSource) {
		ensureNotCheckedOut();
		this.checkoutDateTime = TimeProvider.getLocalDateTime();
		this.checkoutUserId = user.getId();
		this.checkoutSource = checkoutSource;
		this.currentCheckedOutVersion = currentVersion;
		this.dirty = true;
		return this;
	}

	//	public ContentImpl setVersionHashAndMimeType(String hash, String mimetype, long length) {
	//
	//		validateArgument("hash", hash);
	//		validateArgument("mimetype", mimetype);
	//
	//		if (currentCheckedOutVersion == null) {
	//
	//			setNewCurrentVersion(new ContentVersion(hash, nextContentVersion.filename, mimetype, getNextVersion(),
	//					nextContentVersion.lastModifiedBy, nextContentVersion.lastModificationDate, length));
	//
	//		} else {
	//			String version = currentCheckedOutVersion.getVersion();
	//			if (version.endsWith(currentVersion.getVersion())) {
	//				version = getNextVersion();
	//			}
	//			currentCheckedOutVersion = new ContentVersion(hash, nextContentVersion.filename, mimetype, version,
	//					nextContentVersion.lastModifiedBy, nextContentVersion.lastModificationDate, length);
	//		}
	//
	//		this.nextContentVersion = null;
	//		this.dirty = true;
	//		return this;
	//	}

	public ContentImpl renameCurrentVersion(String newFilename) {
		validateFilenameArgument(newFilename);
		String correctedFilename = correctFilename(newFilename);
		if (currentCheckedOutVersion != null) {
			this.currentCheckedOutVersion = this.currentCheckedOutVersion.withFilename(correctedFilename);
		} else {
			this.currentVersion = this.currentVersion.withFilename(correctedFilename);
		}
		this.dirty = true;
		return this;
	}

	@Override
	public Content setVersionComment(String comment) {
		if (currentCheckedOutVersion != null) {
			this.currentCheckedOutVersion = this.currentCheckedOutVersion.withComment(comment);
		} else {
			this.currentVersion = this.currentVersion.withComment(comment);
		}
		this.dirty = true;
		return this;
	}

	@Override
	public Content setVersionModificationDatetime(LocalDateTime modificationDatetime) {
		if (currentCheckedOutVersion != null) {
			this.currentCheckedOutVersion = this.currentCheckedOutVersion.withModificationDatetime(modificationDatetime);
		} else {
			this.currentVersion = this.currentVersion.withModificationDatetime(modificationDatetime);
		}
		this.dirty = true;
		return this;
	}

	public ContentVersion getCurrentVersion() {
		return currentVersion;
	}

	@Override
	public ContentVersion getCurrentCheckedOutVersion() {
		return currentCheckedOutVersion;
	}

	public ContentVersion getCurrentVersionSeenBy(User user) {
		if (user.getId().equals(checkoutUserId)) {
			return currentCheckedOutVersion;
		} else {
			return currentVersion;
		}
	}

	@Override
	public ContentVersion getLastMajorContentVersion() {
		if (currentVersion.isMajor()) {
			return currentVersion;
		} else {
			List<ContentVersion> historyVersions = getHistoryVersions();
			for (int i = historyVersions.size() - 1; i >= 0; i--) {
				ContentVersion historyVersion = historyVersions.get(i);
				if (historyVersion.isMajor()) {
					return historyVersion;
				}
			}
		}
		return null;
	}

	public Content updateContentWithName(User user, ContentVersionDataSummary newVersion, boolean finalize,
										 String name) {
		String version;
		if (emptyVersion) {
			version = finalize ? "1.0" : "0.1";
		} else {
			version = getNextVersion(finalize);
		}
		return updateContentWithVersionAndName(user, newVersion, version, name);
	}

	@Override
	public Content updateContentWithVersionAndName(User user, ContentVersionDataSummary newVersion, String version,
												   String name) {
		return updateContentWithVersionAndName(user, newVersion, version, name, false);
	}

	private Content updateContentWithVersionAndName(User user, ContentVersionDataSummary newVersion, String version,
													String name, boolean overwrite) {
		ensureNotCheckedOut();
		version = validateVersion(version, currentVersion.getVersion(), isEmptyVersion(), overwrite);
		validateUserArgument(user);
		valdiateNewVersionArgument(newVersion);
		LocalDateTime now = TimeProvider.getLocalDateTime();

		String correctedFilename = correctFilename(name);
		if (emptyVersion) {
			emptyVersion = false;
			currentVersion = new ContentVersion(newVersion, correctedFilename, version, user.getId(), now, null);
		} else {
			setNewCurrentVersion(new ContentVersion(newVersion, correctedFilename, version, user.getId(), now, null));
		}
		this.dirty = true;
		return this;
	}

	@Override
	public Content replaceCurrentVersionContent(User user, ContentVersionDataSummary newVersion) {
		return updateContentWithVersionAndName(user, newVersion, currentVersion.getVersion(),
				currentVersion.getFilename(), true);
	}

	public Content updateContent(User user, ContentVersionDataSummary newVersion, boolean finalize) {
		return updateContentWithName(user, newVersion, finalize, getCurrentVersion().getFilename());
	}

	private String getNextVersion(boolean finalized) {
		if (currentVersion == null) {
			return getFirstVersion(finalized);
		} else {
			return getVersionAfter(currentVersion.getVersion(), finalized);
		}
	}

	public LocalDateTime getCheckoutDateTime() {
		return checkoutDateTime;
	}

	public String getCheckoutUserId() {
		return checkoutUserId;
	}

	public Integer getCheckoutSource() {
		return checkoutSource;
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public ContentImpl updateCheckedOutContentWithName(ContentVersionDataSummary newVersion, String name) {
		return updateCheckedOutContentWithName(newVersion, name, false);
	}

	private ContentImpl updateCheckedOutContentWithName(ContentVersionDataSummary newVersion, String name,
														boolean overwrite) {
		ensureCheckedOut();
		valdiateNewVersionArgument(newVersion);
		String correctedFilename = correctFilename(name);
		String version = currentCheckedOutVersion.getVersion();

		if (!emptyVersion && !overwrite && version.equals(getCurrentVersion().getVersion())) {
			version = getNextVersion(false);
		}

		LocalDateTime now = TimeProvider.getLocalDateTime();
		String userId = checkoutUserId;
		this.currentCheckedOutVersion = new ContentVersion(newVersion, correctedFilename, version, userId, now, null);
		this.dirty = true;
		return this;
	}

	public ContentImpl updateCheckedOutContent(ContentVersionDataSummary newVersion) {
		return updateCheckedOutContentWithName(newVersion, getCurrentCheckedOutVersion().getFilename());
	}

	@Override
	public Content replaceCheckedOutContent(ContentVersionDataSummary newVersion) {
		return updateCheckedOutContentWithName(newVersion, getCurrentCheckedOutVersion().getFilename(), true);
	}

	public ContentImpl finalizeVersion() {
		return updateVersion(true);
	}

	public ContentImpl updateMinorVersion() {
		return updateVersion(false);
	}

	public void changeHashCodesOfAllVersions() {
		List<ContentVersion> listOfContentVersion = new ArrayList<>();
		if (currentCheckedOutVersion != null) {
			currentCheckedOutVersion = changeHashOf(currentCheckedOutVersion);
		}
		ensureHistoryIsLoaded();
		if (history != null) {
			for (ContentVersion version : history) {
				listOfContentVersion.add(changeHashOf(version));
			}
			history = listOfContentVersion;
		}
		currentVersion = changeHashOf(currentVersion);
	}

	private ContentVersion changeHashOf(ContentVersion version) {
		if (!isDirty()) {
			if (version.getHash().contains("+") || version.getHash().contains("/")) {
				dirty = true;
			}
		}
		String newHash = version.getHash().replace("+", "-").replace("/", "_");
		ContentVersionDataSummary contentVersionDataSummary =
				new ContentVersionDataSummary(newHash, version.getMimetype(), version.getLength());
		return new ContentVersion(contentVersionDataSummary, version.getFilename(),
				version.getVersion(), version.getModifiedBy(), version.getLastModificationDateTime(), version.getComment());
	}

	private ContentImpl updateVersion(boolean toMajorVersion) {
		this.dirty = true;
		if (currentCheckedOutVersion != null) {
			this.checkoutDateTime = null;
			this.checkoutUserId = null;
			this.checkoutSource = null;

			if (!currentCheckedOutVersion.getVersion().equals(currentVersion.getVersion())) {
				String version = getVersionAfter(currentCheckedOutVersion.getVersion(), true);
				setNewCurrentVersion(currentCheckedOutVersion.withVersion(version));
			}
			this.currentCheckedOutVersion = null;

		} else {
			String finalizedVersionLabel = getVersionAfter(getCurrentVersion().getVersion(), toMajorVersion);
			ensureHistoryIsLoaded();
			currentVersion = getCurrentVersion().withVersion(finalizedVersionLabel);
		}
		return this;
	}

	@Override
	public ContentVersion getVersion(String version) {
		if (getCurrentVersion() != null && version.equals(getCurrentVersion().getVersion())) {
			return getCurrentVersion();
		} else if (getCurrentCheckedOutVersion() != null && version.equals(getCurrentCheckedOutVersion().getVersion())) {
			return getCurrentVersion();
		}
		ensureHistoryIsLoaded();
		for (ContentVersion historyVersion : history) {
			if (historyVersion.getVersion().equals(version)) {
				return historyVersion;
			}
		}

		throw new ContentImplRuntimeException_NoSuchVersion(version);
	}

	@Override
	public Content deleteVersion(String versionLabel, User user) {
		if (!user.has(CorePermissions.DELETE_CONTENT_VERSION).globally()) {
			throw new ContentImplRuntimeException_UserHasNoDeleteVersionPermission(user);
		}

		return deleteVersion(versionLabel);
	}

	@Override
	public Content deleteVersion(String versionLabel) {
		ensureHistoryIsLoaded();
		if (history.isEmpty()) {
			throw new ContentImplRuntimeException_CannotDeleteLastVersion();
		}
		if (versionLabel.equals(currentVersion.getVersion())) {
			dirty = true;
			this.currentVersion = history.get(history.size() - 1);
			history.remove(history.size() - 1);

		} else {

			for (Iterator<ContentVersion> iterator = history.iterator(); iterator.hasNext(); ) {
				ContentVersion version = iterator.next();
				if (versionLabel.equals(version.getVersion())) {
					dirty = true;
					iterator.remove();
				}
			}
		}

		return this;
	}

	@Override
	public boolean isEmptyVersion() {
		return emptyVersion;
	}

	@Override
	public boolean isCheckedOut() {
		return checkoutUserId != null;
	}

	private void setNewCurrentVersion(ContentVersion version) {
		ensureHistoryIsLoaded();
		if (currentVersion != null) {
			boolean exists = false;
			for (int i = 0; i < history.size(); i++) {
				if (history.get(i).getVersion().equals(currentVersion.getVersion())) {
					history.set(i, currentVersion);
					exists = true;
					break;
				}
			}
			if (!exists) {
				history.add(currentVersion);
			}
		}
		currentVersion = version;
	}

	private void ensureHistoryIsLoaded() {
		if (history == null) {
			history = new ArrayList<>(lazyHistory.get());
		}
	}

	private void ensureCheckedOut() {
		if (currentCheckedOutVersion == null) {
			throw new ContentImplRuntimeException_ContentMustBeCheckedOut(id);
		}
	}

	private void ensureNotCheckedOut() {
		if (currentCheckedOutVersion != null) {
			throw new ContentImplRuntimeException_ContentMustNotBeCheckedOut(id);
		}
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

	@Override
	public boolean isDeleteContentVersionPossible(String version) {
		ContentVersion currentVersion = getCurrentVersion();
		return currentVersion != null && !currentVersion.getVersion().equals(version);
	}

	@Override
	public Set<String> getHashOfAllVersions() {
		Set<String> hashes = new HashSet<>();
		hashes.add(currentVersion.getHash());
		if (currentCheckedOutVersion != null) {
			hashes.add(currentCheckedOutVersion.getHash());
		}
		for (ContentVersion version : getHistoryVersions()) {
			hashes.add(version.getHash());
		}
		return hashes;
	}

}