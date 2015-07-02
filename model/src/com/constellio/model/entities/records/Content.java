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

import java.util.List;
import java.util.Set;

import org.joda.time.LocalDateTime;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.services.contents.ContentVersionDataSummary;

public interface Content extends ModifiableStructure {

	String getId();

	ContentVersion getCurrentVersion();

	ContentVersion getCurrentCheckedOutVersion();

	ContentVersion getCurrentVersionSeenBy(User user);

	List<ContentVersion> getHistoryVersions();

	Content checkOut(User user);

	Content checkIn();

	Content cancelCheckOut();

	Content checkInWithModification(ContentVersionDataSummary newVersion, boolean finalized);

	Content checkInWithModificationAndName(ContentVersionDataSummary newVersion, boolean finalized, String name);

	Content renameCurrentVersion(String newFilename);

	Content updateContent(User user, ContentVersionDataSummary newVersion, boolean finalize);

	Content updateContentWithName(User user, ContentVersionDataSummary newVersion, boolean finalize, String name);

	LocalDateTime getCheckoutDateTime();

	String getCheckoutUserId();

	boolean isDirty();

	Content updateCheckedOutContent(ContentVersionDataSummary newVersion);

	Content updateCheckedOutContentWithName(ContentVersionDataSummary newVersion, String name);

	Content finalizeVersion();

	ContentVersion getVersion(String version);

	Content deleteVersion(String versionLabel, User user);

	Content deleteVersion(String versionLabel);

	boolean isEmptyVersion();

	// TODO Write a test
	boolean isDeleteContentVersionPossible(String version);

	Set<String> getHashOfAllVersions();

	//List<String> getDeletedVersionHashes();
}