package com.constellio.model.entities.records;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.services.contents.ContentVersionDataSummary;
import org.joda.time.LocalDateTime;

import java.util.List;
import java.util.Set;

public interface Content extends ModifiableStructure {

	String getId();

	ContentVersion getCurrentVersion();

	ContentVersion getCurrentCheckedOutVersion();

	ContentVersion getCurrentVersionSeenBy(User user);

	ContentVersion getLastMajorContentVersion();

	/**
	 * Sorted by asc version label
	 */
	List<ContentVersion> getHistoryVersions();

	/**
	 * Sorted by asc version label
	 */
	List<ContentVersion> getVersions();

	Content checkOut(User user);

	Content checkOut(User user, int checkoutSource);

	Content checkIn();

	Content cancelCheckOut();

	Content checkInWithModificationAndNameInSameVersion(ContentVersionDataSummary newVersion, String name);

	Content checkInWithModification(ContentVersionDataSummary newVersion, boolean finalized);

	Content checkInWithModificationAndName(ContentVersionDataSummary newVersion, boolean finalized, String name);

	Content renameCurrentVersion(String newFilename);

	Content setVersionComment(String comment);

	Content setVersionModificationDatetime(LocalDateTime localDateTime);

	Content updateContent(User user, ContentVersionDataSummary newVersion, boolean finalize);

	Content updateContentWithName(User user, ContentVersionDataSummary newVersion, boolean finalize, String name);

	Content updateContentWithVersionAndName(User user, ContentVersionDataSummary newVersion, String version,
											String name);

	Content replaceCurrentVersionContent(User user, ContentVersionDataSummary contentVersionDataSummary);

	LocalDateTime getCheckoutDateTime();

	String getCheckoutUserId();

	Integer getCheckoutSource();

	boolean isDirty();

	Content updateCheckedOutContent(ContentVersionDataSummary newVersion);

	Content updateCheckedOutContentWithName(ContentVersionDataSummary newVersion, String name);

	Content replaceCheckedOutContent(ContentVersionDataSummary newVersion);

	Content finalizeVersion();

	Content updateMinorVersion();

	ContentVersion getVersion(String version);

	Content deleteVersion(String versionLabel, User user);

	Content deleteVersion(String versionLabel);

	boolean isEmptyVersion();

	boolean isCheckedOut();

	// TODO Write a test
	boolean isDeleteContentVersionPossible(String version);

	Set<String> getHashOfAllVersions();

	//List<String> getDeletedVersionHashes();
}