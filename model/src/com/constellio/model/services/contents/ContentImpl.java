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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.LocalDateTime;

import com.constellio.data.utils.TimeProvider;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_CannotDeleteLastVersion;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_ContentMustBeCheckedOut;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_ContentMustNotBeCheckedOut;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_InvalidArgument;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_NoSuchVersion;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_UserHasNoDeleteVersionPermission;
import com.constellio.model.utils.Lazy;

public class ContentImpl implements Content {

	private String id;

	private ContentVersion currentVersion;

	private ContentVersion currentCheckedOutVersion;

	private LocalDateTime checkoutDateTime;

	private String checkoutUserId;

	private Lazy<List<ContentVersion>> lazyHistory;
	private List<ContentVersion> history;

	private boolean dirty;

	public ContentImpl(String id, ContentVersion currentVersion, Lazy<List<ContentVersion>> lazyHistory,
			ContentVersion currentCheckedOutVersion, LocalDateTime checkoutDateTime, String checkoutUserId) {
		this.currentVersion = currentVersion;
		this.checkoutDateTime = checkoutDateTime;
		if (currentCheckedOutVersion == null && checkoutUserId != null) {
			this.currentCheckedOutVersion = currentVersion;
		} else {
			this.currentCheckedOutVersion = currentCheckedOutVersion;
		}
		this.checkoutUserId = checkoutUserId;
		this.lazyHistory = lazyHistory;
		this.id = id;
	}

	private ContentImpl() {

	}

	public static ContentImpl create(String id, User user, String filename, ContentVersionDataSummary newVersion, boolean major) {
		validateArgument("id", id);
		validateUserArgument(user);
		validateFilenameArgument(filename);
		valdiateNewVersionArgument(newVersion);

		LocalDateTime now = TimeProvider.getLocalDateTime();
		ContentImpl content = new ContentImpl();
		String correctFilename = correctFilename(filename);
		String version = major ? "1.0" : "0.1";
		content.id = id;
		content.history = new ArrayList<>();
		content.dirty = true;
		content.setNewCurrentVersion(new ContentVersion(newVersion, correctFilename, version, user.getId(), now));
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

	private static String correctFilename(String filename) {
		return filename.replace(":", "");
	}

	private static String getFirstVersion(boolean finalized) {
		return finalized ? "1.0" : "0.1";
	}

	private static String getVersionAfter(String version, boolean finalized) {
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

		if (newVersion.getHash() == null || newVersion.getMimetype() == null) {
			throw new ContentImplRuntimeException_InvalidArgument("new version");
		}

	}

	private static void validateArgument(String argumentName, String argumentValue) {
		if (!StringUtils.isNotBlank(argumentValue)) {
			throw new ContentImplRuntimeException_InvalidArgument(argumentName);
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

	public ContentImpl checkIn() {
		ensureCheckedOut();
		this.checkoutDateTime = null;
		this.checkoutUserId = null;
		this.dirty = true;

		if (!currentCheckedOutVersion.getVersion().equals(currentVersion.getVersion())) {
			setNewCurrentVersion(currentCheckedOutVersion);
		}
		this.currentCheckedOutVersion = null;

		return this;
	}

	public ContentImpl cancelCheckOut() {
		ensureCheckedOut();
		this.checkoutDateTime = null;
		this.checkoutUserId = null;
		this.dirty = true;
		this.currentCheckedOutVersion = null;

		return this;
	}

	public ContentImpl checkInWithModificationAndName(ContentVersionDataSummary newVersion, boolean finalize, String name) {
		ensureCheckedOut();
		valdiateNewVersionArgument(newVersion);
		LocalDateTime now = TimeProvider.getLocalDateTime();
		String correctFilename = correctFilename(name);
		String userId = this.getCheckoutUserId();
		String nextVersion = getNextVersion(finalize);
		setNewCurrentVersion(new ContentVersion(newVersion, correctFilename, nextVersion, userId, now));
		this.checkoutDateTime = null;
		this.checkoutUserId = null;
		this.currentCheckedOutVersion = null;
		this.dirty = true;
		return this;
	}

	public ContentImpl checkInWithModification(ContentVersionDataSummary newVersion, boolean finalize) {
		ensureCheckedOut();
		return checkInWithModificationAndName(newVersion, finalize, currentCheckedOutVersion.getFilename());
	}

	public ContentImpl checkOut(User user) {
		ensureNotCheckedOut();
		this.checkoutDateTime = TimeProvider.getLocalDateTime();
		this.checkoutUserId = user.getId();
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

	public ContentImpl updateContentWithName(User user, ContentVersionDataSummary newVersion, boolean finalize, String name) {
		ensureNotCheckedOut();
		validateUserArgument(user);
		valdiateNewVersionArgument(newVersion);
		LocalDateTime now = TimeProvider.getLocalDateTime();
		String version = getNextVersion(finalize);
		String correctedFilename = correctFilename(name);
		setNewCurrentVersion(new ContentVersion(newVersion, correctedFilename, version, user.getId(), now));
		this.dirty = true;
		return this;
	}

	public ContentImpl updateContent(User user, ContentVersionDataSummary newVersion, boolean finalize) {
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

	@Override
	public boolean isDirty() {
		return dirty;
	}

	public ContentImpl updateCheckedOutContentWithName(ContentVersionDataSummary newVersion, String name) {
		ensureCheckedOut();
		valdiateNewVersionArgument(newVersion);
		String correctedFilename = correctFilename(name);
		String version = currentCheckedOutVersion.getVersion();

		if (version.equals(getCurrentVersion().getVersion())) {
			version = getNextVersion(false);
		}

		LocalDateTime now = TimeProvider.getLocalDateTime();
		String userId = checkoutUserId;
		this.currentCheckedOutVersion = new ContentVersion(newVersion, correctedFilename, version, userId, now);
		this.dirty = true;
		return this;
	}

	public ContentImpl updateCheckedOutContent(ContentVersionDataSummary newVersion) {
		return updateCheckedOutContentWithName(newVersion, getCurrentCheckedOutVersion().getFilename());
	}

	public ContentImpl finalizeVersion() {
		this.dirty = true;
		if (currentCheckedOutVersion != null) {
			this.checkoutDateTime = null;
			this.checkoutUserId = null;

			if (!currentCheckedOutVersion.getVersion().equals(currentVersion.getVersion())) {
				String version = getVersionAfter(currentCheckedOutVersion.getVersion(), true);
				setNewCurrentVersion(currentCheckedOutVersion.withVersion(version));
			}
			this.currentCheckedOutVersion = null;

		} else {
			String finalizedVersionLabel = getVersionAfter(getCurrentVersion().getVersion(), true);
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

	private void setNewCurrentVersion(ContentVersion version) {
		ensureHistoryIsLoaded();
		if (currentVersion != null) {
			history.add(currentVersion);
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

}
