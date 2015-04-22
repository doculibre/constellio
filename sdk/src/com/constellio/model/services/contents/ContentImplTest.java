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

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentImplRuntimeException.ContentImplRuntimeException_InvalidArgument;
import com.constellio.model.utils.Lazy;
import com.constellio.sdk.tests.ConstellioTest;

public class ContentImplTest extends ConstellioTest {

	@Mock InputStream inputStream;

	String aliceId = "aliceId";
	String bobId = "bobId";
	String charlesId = "charlesId";
	String dakotaId = "dakotaId";
	@Mock User alice;
	@Mock User bob;
	@Mock User charles;
	@Mock User dakota;

	String contentId1 = aString();
	String contentId2 = aString();
	String contentId53 = aString();

	long zeLength = aLong();
	long zeFirstLength = aLong();
	long zeSecondLength = aLong();
	long zeNewLength = aLong();
	long zeVeryNewLength = aLong();

	LocalDateTime smashOClock = new LocalDateTime().minusHours(4);
	ContentVersion firstHistoryVersion = new ContentVersion(
			new ContentVersionDataSummary("zeFirstHash", "zeFirstMime", zeFirstLength), "zeFirstFile.pdf", "0.1", bobId,
			smashOClock);
	LocalDateTime meetingOClock = new LocalDateTime().minusHours(4);
	LocalDateTime shishOClock = new LocalDateTime().minusHours(2);
	ContentVersion currentVersion = new ContentVersion(new ContentVersionDataSummary("zeCurrentHash", "zeMime", zeLength),
			"zeFile.pdf", "1.0", charlesId, shishOClock);
	LocalDateTime teaOClock = new LocalDateTime().minusHours(1);
	ContentVersion modifiedCheckedOut = new ContentVersion(new ContentVersionDataSummary("zeNewHash", "zeNewMime", zeNewLength),
			"zeNewFile.pdf", "1.1", aliceId, teaOClock);
	LocalDateTime tockOClock = new LocalDateTime().minusHours(1);
	@Mock Lazy<List<ContentVersion>> history;

	ContentImpl content;

	@Before
	public void setUp()
			throws Exception {

		when(alice.getId()).thenReturn(aliceId);
		when(bob.getId()).thenReturn(bobId);
		when(charles.getId()).thenReturn(charlesId);
		when(dakota.getId()).thenReturn(dakotaId);

		when(history.get()).thenReturn(Arrays.asList(firstHistoryVersion));

	}

	@Test
	public void whenCreatingContentImplThenHistoryIsLazyLoadedAndNotDirty()
			throws Exception {

		givenNonCheckedOutContent();

		verify(history, never()).get();
		assertThat(content.isDirty()).isFalse();
		assertThat(content.getCurrentCheckedOutVersion()).isNull();

	}

	@Test
	public void whenCreatingContentImplThenHasCorrectInfos()
			throws Exception {

		givenModifiedCheckedOutContent();

		assertThat(content.getCurrentVersion().getModifiedBy()).isEqualTo(charlesId);
		assertThat(content.getCurrentVersion().getLastModificationDateTime()).isEqualTo(shishOClock);
		assertThat(content.getCurrentVersion().getFilename()).isEqualTo("zeFile.pdf");
		assertThat(content.getCurrentVersion().getVersion()).isEqualTo("1.0");
		assertThat(content.getCurrentVersion().getHash()).isEqualTo("zeCurrentHash");
		assertThat(content.getCurrentVersion().getMimetype()).isEqualTo("zeMime");
		assertThat(content.getCurrentVersion().getLength()).isEqualTo(zeLength);

		assertThat(content.getHistoryVersions().get(0).getModifiedBy()).isEqualTo(bobId);
		assertThat(content.getHistoryVersions().get(0).getLastModificationDateTime()).isEqualTo(smashOClock);
		assertThat(content.getHistoryVersions().get(0).getFilename()).isEqualTo("zeFirstFile.pdf");
		assertThat(content.getHistoryVersions().get(0).getVersion()).isEqualTo("0.1");
		assertThat(content.getHistoryVersions().get(0).getHash()).isEqualTo("zeFirstHash");
		assertThat(content.getHistoryVersions().get(0).getMimetype()).isEqualTo("zeFirstMime");
		assertThat(content.getHistoryVersions().get(0).getLength()).isEqualTo(zeFirstLength);

		assertThat(content.getCurrentCheckedOutVersion().getModifiedBy()).isEqualTo(aliceId);
		assertThat(content.getCurrentCheckedOutVersion().getLastModificationDateTime()).isEqualTo(teaOClock);
		assertThat(content.getCurrentCheckedOutVersion().getFilename()).isEqualTo("zeNewFile.pdf");
		assertThat(content.getCurrentCheckedOutVersion().getVersion()).isEqualTo("1.1");
		assertThat(content.getCurrentCheckedOutVersion().getHash()).isEqualTo("zeNewHash");
		assertThat(content.getCurrentCheckedOutVersion().getMimetype()).isEqualTo("zeNewMime");
		assertThat(content.getCurrentCheckedOutVersion().getLength()).isEqualTo(zeNewLength);

		assertThat(content.getCheckoutUserId()).isEqualTo(aliceId);
		assertThat(content.getCheckoutDateTime()).isEqualTo(teaOClock);
	}

	@Test
	public void givenAddedNewContentWhenSetVersionAndMimeTypeThenNewCurrentVersionAndPreviousCurrentVersionInHistory()
			throws Exception {

		givenNonCheckedOutContent();

		givenTimeIs(meetingOClock);
		content.updateContent(alice, new ContentVersionDataSummary("zeNewHash", "zeNewMime", zeNewLength), false);

		assertThat(content.getCurrentVersion().getLastModificationDateTime()).isEqualTo(meetingOClock);
		assertThat(content.getCurrentVersion().getModifiedBy()).isEqualTo(aliceId);
		assertThat(content.getCurrentVersion().getVersion()).isEqualTo("1.1");
		assertThat(content.getCurrentVersion().getMimetype()).isEqualTo("zeNewMime");
		assertThat(content.getCurrentVersion().getLength()).isEqualTo(zeNewLength);
		assertThat(content.getCurrentVersion().getFilename()).isEqualTo("zeFile.pdf");
		assertThat(content.getCurrentVersion().getHash()).isEqualTo("zeNewHash");
		assertThat(content.getHistoryVersions()).containsExactly(firstHistoryVersion, currentVersion);
		assertThat(content.getCurrentCheckedOutVersion()).isNull();
	}

	@Test
	public void whenContentIsCheckedOutThenCheckedOutContentIsSameAsCurrentVersion()
			throws Exception {

		givenNonCheckedOutContent();

		givenTimeIs(shishOClock);
		content.checkOut(alice);

		verify(history, never()).get();
		assertThat(content.getCurrentVersion()).isEqualTo(currentVersion);
		assertThat(content.getCurrentCheckedOutVersion()).isEqualTo(currentVersion);
		assertThat(content.getCheckoutUserId()).isEqualTo(aliceId);
		assertThat(content.getCheckoutDateTime()).isEqualTo(shishOClock);
	}

	@Test
	public void givenAddedNewContentToCheckedOutContentWhenSetVersionAndMimeTypeThenNewCurrentVersionAndPreviousCurrentVersionInHistory()
			throws Exception {

		givenCheckedOutContentNotYetModified();

		givenTimeIs(meetingOClock);
		content.updateCheckedOutContent(new ContentVersionDataSummary("zeNewHash", "zeNewMime", zeNewLength));

		assertThat(content.getCurrentVersion()).isEqualTo(currentVersion);
		assertThat(content.getCurrentCheckedOutVersion().getLastModificationDateTime()).isEqualTo(meetingOClock);
		assertThat(content.getCurrentCheckedOutVersion().getModifiedBy()).isEqualTo(aliceId);
		assertThat(content.getCurrentCheckedOutVersion().getVersion()).isEqualTo("1.1");
		assertThat(content.getCurrentCheckedOutVersion().getMimetype()).isEqualTo("zeNewMime");
		assertThat(content.getCurrentCheckedOutVersion().getLength()).isEqualTo(zeNewLength);
		assertThat(content.getCurrentCheckedOutVersion().getFilename()).isEqualTo("zeFile.pdf");
		assertThat(content.getCurrentCheckedOutVersion().getHash()).isEqualTo("zeNewHash");
		assertThat(content.getHistoryVersions()).containsExactly(firstHistoryVersion);
		assertThat(content.getCheckoutUserId()).isEqualTo(aliceId);
		assertThat(content.getCheckoutDateTime()).isEqualTo(teaOClock);
	}

	@Test
	public void givenModifiedCheckedOutContentWhenSetNewContentThenModifiedContentIsRemoved()
			throws Exception {

		givenCheckedOutContentNotYetModified();

		givenTimeIs(shishOClock.minusMinutes(10));
		content.updateCheckedOutContent(new ContentVersionDataSummary("zeNewHash", "zeNewMime", zeNewLength));

		givenTimeIs(meetingOClock);
		content.renameCurrentVersion("zeNew:Name.pdf")
				.updateCheckedOutContent(new ContentVersionDataSummary("zeVeryNewHash", "zeVeryNewMime", zeVeryNewLength));

		assertThat(content.getCurrentVersion()).isEqualTo(currentVersion);
		assertThat(content.getCurrentCheckedOutVersion().getLastModificationDateTime()).isEqualTo(meetingOClock);
		assertThat(content.getCurrentCheckedOutVersion().getModifiedBy()).isEqualTo(aliceId);
		assertThat(content.getCurrentCheckedOutVersion().getVersion()).isEqualTo("1.1");
		assertThat(content.getCurrentCheckedOutVersion().getMimetype()).isEqualTo("zeVeryNewMime");
		assertThat(content.getCurrentCheckedOutVersion().getLength()).isEqualTo(zeVeryNewLength);
		assertThat(content.getCurrentCheckedOutVersion().getFilename()).isEqualTo("zeNewName.pdf");
		assertThat(content.getCurrentCheckedOutVersion().getHash()).isEqualTo("zeVeryNewHash");
		assertThat(content.getHistoryVersions()).containsExactly(firstHistoryVersion);
		assertThat(content.getCheckoutUserId()).isEqualTo(aliceId);
		assertThat(content.getCheckoutDateTime()).isEqualTo(teaOClock);
	}

	@Test
	public void givenPreviouslyModifiedCheckedOutContentWhenSetNewContentThenModifiedContentIsRemoved()
			throws Exception {

		givenModifiedCheckedOutContent();

		givenTimeIs(meetingOClock);
		content.renameCurrentVersion("zeNew:Name.pdf").updateCheckedOutContent(
				new ContentVersionDataSummary("zeVeryNewHash", "zeVeryNewMime", zeVeryNewLength));

		assertThat(content.getCurrentVersion()).isEqualTo(currentVersion);
		assertThat(content.getCurrentCheckedOutVersion().getLastModificationDateTime()).isEqualTo(meetingOClock);
		assertThat(content.getCurrentCheckedOutVersion().getModifiedBy()).isEqualTo(aliceId);
		assertThat(content.getCurrentCheckedOutVersion().getVersion()).isEqualTo("1.1");
		assertThat(content.getCurrentCheckedOutVersion().getMimetype()).isEqualTo("zeVeryNewMime");
		assertThat(content.getCurrentCheckedOutVersion().getLength()).isEqualTo(zeVeryNewLength);
		assertThat(content.getCurrentCheckedOutVersion().getFilename()).isEqualTo("zeNewName.pdf");
		assertThat(content.getCurrentCheckedOutVersion().getHash()).isEqualTo("zeVeryNewHash");
		assertThat(content.getHistoryVersions()).containsExactly(firstHistoryVersion);
		assertThat(content.getCheckoutUserId()).isEqualTo(aliceId);
		assertThat(content.getCheckoutDateTime()).isEqualTo(teaOClock);
	}

	@Test
	public void givenCheckedOutContentWhenCheckInAsMajorThenNotCheckedOutAnymoreAndNewVersionSet()
			throws Exception {

		givenCheckedOutContentNotYetModified();

		givenTimeIs(shishOClock);
		content.updateCheckedOutContent(new ContentVersionDataSummary("zeNewHash", "zeNewMime", zeNewLength));

		givenTimeIs(meetingOClock);
		content.checkInWithModification(new ContentVersionDataSummary("zeVeryNewHash", "zeVeryNewMime", zeVeryNewLength), true);

		assertThat(content.getCurrentCheckedOutVersion()).isNull();
		assertThat(content.getCurrentVersion().getLastModificationDateTime()).isEqualTo(meetingOClock);
		assertThat(content.getCurrentVersion().getModifiedBy()).isEqualTo(aliceId);
		assertThat(content.getCurrentVersion().getVersion()).isEqualTo("2.0");
		assertThat(content.getCurrentVersion().getMimetype()).isEqualTo("zeVeryNewMime");
		assertThat(content.getCurrentVersion().getLength()).isEqualTo(zeVeryNewLength);
		assertThat(content.getCurrentVersion().getHash()).isEqualTo("zeVeryNewHash");
		assertThat(content.getHistoryVersions()).containsExactly(firstHistoryVersion, currentVersion);
		assertThat(content.getCheckoutUserId()).isNull();
		assertThat(content.getCheckoutDateTime()).isNull();
	}

	@Test
	public void givenCheckedOutContentWhenCheckInAsMinorThenNotCheckedOutAnymoreAndNewVersionSet()
			throws Exception {

		givenCheckedOutContentNotYetModified();

		givenTimeIs(shishOClock);
		content.updateCheckedOutContent(new ContentVersionDataSummary("zeNewHash", "zeNewMime", zeNewLength));

		givenTimeIs(meetingOClock);
		content.checkInWithModification(new ContentVersionDataSummary("zeVeryNewHash", "zeVeryNewMime", zeVeryNewLength), false);

		assertThat(content.getCurrentCheckedOutVersion()).isNull();
		assertThat(content.getCurrentVersion().getLastModificationDateTime()).isEqualTo(meetingOClock);
		assertThat(content.getCurrentVersion().getModifiedBy()).isEqualTo(aliceId);
		assertThat(content.getCurrentVersion().getVersion()).isEqualTo("1.1");
		assertThat(content.getCurrentVersion().getMimetype()).isEqualTo("zeVeryNewMime");
		assertThat(content.getCurrentVersion().getLength()).isEqualTo(zeVeryNewLength);
		assertThat(content.getCurrentVersion().getHash()).isEqualTo("zeVeryNewHash");
		assertThat(content.getHistoryVersions()).containsExactly(firstHistoryVersion, currentVersion);
		assertThat(content.getCheckoutUserId()).isNull();
		assertThat(content.getCheckoutDateTime()).isNull();
	}

	@Test
	public void givenModifiedCheckedOutContentWhenCheckInAsMinorThenNotCheckedOutAnymoreAndNewVersionSet()
			throws Exception {

		givenModifiedCheckedOutContent();

		givenTimeIs(meetingOClock);
		content.checkInWithModification(new ContentVersionDataSummary("zeVeryNewHash", "zeVeryNewMime", zeVeryNewLength), false);

		assertThat(content.getCurrentCheckedOutVersion()).isNull();
		assertThat(content.getCurrentVersion().getLastModificationDateTime()).isEqualTo(meetingOClock);
		assertThat(content.getCurrentVersion().getModifiedBy()).isEqualTo(aliceId);
		assertThat(content.getCurrentVersion().getVersion()).isEqualTo("1.1");
		assertThat(content.getCurrentVersion().getMimetype()).isEqualTo("zeVeryNewMime");
		assertThat(content.getCurrentVersion().getLength()).isEqualTo(zeVeryNewLength);
		assertThat(content.getCurrentVersion().getHash()).isEqualTo("zeVeryNewHash");
		assertThat(content.getHistoryVersions()).containsExactly(firstHistoryVersion, currentVersion);
		assertThat(content.getCheckoutUserId()).isNull();
		assertThat(content.getCheckoutDateTime()).isNull();
	}

	@Test
	public void givenModifiedCheckedOutContentWhenCheckInThenCheckInAndVersionIncrementedAsMinor()
			throws Exception {

		givenCheckedOutContentNotYetModified();

		givenTimeIs(shishOClock);
		content.updateCheckedOutContent(new ContentVersionDataSummary("zeNewHash", "zeNewMime", zeNewLength));

		givenTimeIs(tockOClock);
		content.checkIn();

		assertThat(content.getCurrentCheckedOutVersion()).isNull();
		assertThat(content.getCurrentVersion().getLastModificationDateTime()).isEqualTo(shishOClock);
		assertThat(content.getCurrentVersion().getModifiedBy()).isEqualTo(aliceId);
		assertThat(content.getCurrentVersion().getVersion()).isEqualTo("1.1");
		assertThat(content.getCurrentVersion().getMimetype()).isEqualTo("zeNewMime");
		assertThat(content.getCurrentVersion().getLength()).isEqualTo(zeNewLength);
		assertThat(content.getCurrentVersion().getHash()).isEqualTo("zeNewHash");
		assertThat(content.getHistoryVersions()).containsExactly(firstHistoryVersion, currentVersion);
		assertThat(content.getCheckoutUserId()).isNull();
		assertThat(content.getCheckoutDateTime()).isNull();
	}

	@Test
	public void givenModifiedCheckedOutContentWhenCheckOutCancelledThenNoModification()
			throws Exception {

		givenCheckedOutContentNotYetModified();

		givenTimeIs(shishOClock);
		content.updateCheckedOutContent(new ContentVersionDataSummary("zeNewHash", "zeNewMime", zeNewLength));

		givenTimeIs(tockOClock);
		content.cancelCheckOut();

		assertThat(content.getCurrentCheckedOutVersion()).isNull();
		assertThat(content.getCurrentVersion()).isEqualTo(currentVersion);
		assertThat(content.getHistoryVersions()).containsExactly(firstHistoryVersion);
		assertThat(content.getCheckoutUserId()).isNull();
		assertThat(content.getCheckoutDateTime()).isNull();
	}

	@Test
	public void givenPreviouslyModifiedCheckedOutContentWhenCheckInThenCheckInAndVersionIncrementedAsMinor()
			throws Exception {

		givenModifiedCheckedOutContent();

		givenTimeIs(tockOClock);
		content.checkIn();

		assertThat(content.getCurrentCheckedOutVersion()).isNull();
		assertThat(content.getCurrentVersion().getLastModificationDateTime()).isEqualTo(teaOClock);
		assertThat(content.getCurrentVersion().getModifiedBy()).isEqualTo(aliceId);
		assertThat(content.getCurrentVersion().getVersion()).isEqualTo("1.1");
		assertThat(content.getCurrentVersion().getMimetype()).isEqualTo("zeNewMime");
		assertThat(content.getCurrentVersion().getLength()).isEqualTo(zeNewLength);
		assertThat(content.getCurrentVersion().getHash()).isEqualTo("zeNewHash");
		assertThat(content.getHistoryVersions()).containsExactly(firstHistoryVersion, currentVersion);
		assertThat(content.getCheckoutUserId()).isNull();
		assertThat(content.getCheckoutDateTime()).isNull();
	}

	@Test
	public void givenPreviouslyModifiedCheckedOutContentWhenCheckOutCancelledThenCheckInAndVersionIncrementedAsMinor()
			throws Exception {

		givenModifiedCheckedOutContent();

		givenTimeIs(tockOClock);
		content.cancelCheckOut();

		assertThat(content.getCurrentCheckedOutVersion()).isNull();
		assertThat(content.getCurrentVersion()).isEqualTo(currentVersion);
		assertThat(content.getHistoryVersions()).containsExactly(firstHistoryVersion);
		assertThat(content.getCheckoutUserId()).isNull();
		assertThat(content.getCheckoutDateTime()).isNull();
	}

	@Test
	public void givenPreviouslyModifiedCheckedOutContentWhenGetContentSeenByCheckOutUserThenReturnCheckOutVersion()
			throws Exception {

		givenModifiedCheckedOutContent();

		assertThat(content.getCurrentVersionSeenBy(alice)).isSameAs(modifiedCheckedOut);
		assertThat(content.getCurrentVersionSeenBy(bob)).isSameAs(currentVersion);
	}

	@Test
	public void givenModifiedCheckedOutContentWhenCheckInAndFinalizeThenCheckInAndVersionIncrementedAsMajor()
			throws Exception {

		givenCheckedOutContentNotYetModified();

		givenTimeIs(meetingOClock);
		content.updateCheckedOutContent(new ContentVersionDataSummary("zeNewHash", "zeNewMime", zeNewLength));
		content.finalizeVersion();

		assertThat(content.getCurrentCheckedOutVersion()).isNull();
		assertThat(content.getCurrentVersion().getLastModificationDateTime()).isEqualTo(meetingOClock);
		assertThat(content.getCurrentVersion().getModifiedBy()).isEqualTo(aliceId);
		assertThat(content.getCurrentVersion().getVersion()).isEqualTo("2.0");
		assertThat(content.getCurrentVersion().getMimetype()).isEqualTo("zeNewMime");
		assertThat(content.getCurrentVersion().getLength()).isEqualTo(zeNewLength);
		assertThat(content.getCurrentVersion().getHash()).isEqualTo("zeNewHash");
		assertThat(content.getHistoryVersions()).containsExactly(firstHistoryVersion, currentVersion);
		assertThat(content.getCheckoutUserId()).isNull();
		assertThat(content.getCheckoutDateTime()).isNull();
	}

	@Test
	public void givenNonModifiedCheckedOutContentWhenCheckInThenCheckInAndNoChanges()
			throws Exception {

		givenCheckedOutContentNotYetModified();

		givenTimeIs(meetingOClock);
		content.checkIn();

		givenTimeIs(tockOClock);

		assertThat(content.getCurrentCheckedOutVersion()).isNull();
		assertThat(content.getCurrentVersion()).isEqualTo(currentVersion);
		assertThat(content.getHistoryVersions()).containsExactly(firstHistoryVersion);
		assertThat(content.getCheckoutUserId()).isNull();
		assertThat(content.getCheckoutDateTime()).isNull();
	}

	@Test
	public void givenNonModifiedCheckedOutContentWhenCheckOutCancelledThenCheckInAndNoChanges()
			throws Exception {

		givenCheckedOutContentNotYetModified();

		givenTimeIs(meetingOClock);
		content.cancelCheckOut();

		givenTimeIs(tockOClock);

		assertThat(content.getCurrentCheckedOutVersion()).isNull();
		assertThat(content.getCurrentVersion()).isEqualTo(currentVersion);
		assertThat(content.getHistoryVersions()).containsExactly(firstHistoryVersion);
		assertThat(content.getCheckoutUserId()).isNull();
		assertThat(content.getCheckoutDateTime()).isNull();
	}

	@Test
	public void givenNonModifiedCheckedOutContentWhenCheckInAndFinalizeThenCheckInAndNoChanges()
			throws Exception {

		givenCheckedOutContentNotYetModified();

		givenTimeIs(meetingOClock);
		content.finalizeVersion();

		givenTimeIs(tockOClock);

		assertThat(content.getCurrentCheckedOutVersion()).isNull();
		assertThat(content.getCurrentVersion()).isEqualTo(currentVersion);
		assertThat(content.getHistoryVersions()).containsExactly(firstHistoryVersion);
		assertThat(content.getCheckoutUserId()).isNull();
		assertThat(content.getCheckoutDateTime()).isNull();
	}

	@Test
	public void whenCreatingAMajorContentWithColonsThenColonsAreRemovedAndContentCreatedNormally()
			throws Exception {
		givenTimeIs(shishOClock);

		ContentImpl content = (ContentImpl) ContentImpl
				.create("zeId", alice, "file:name.pdf", new ContentVersionDataSummary("zeHash", "zeMime", zeLength), true);
		assertThat(content.isDirty()).isTrue();

		assertThat(content.getCurrentVersion().getFilename()).isEqualTo("filename.pdf");
		assertThat(content.getCurrentVersion().getHash()).isEqualTo("zeHash");
		assertThat(content.getCurrentVersion().getMimetype()).isEqualTo("zeMime");
		assertThat(content.getCurrentVersion().getLength()).isEqualTo(zeLength);
		assertThat(content.getCurrentVersion().getVersion()).isEqualTo("1.0");
		assertThat(content.getCurrentVersion().getModifiedBy()).isEqualTo(aliceId);
		assertThat(content.getCurrentVersion().getLastModificationDateTime()).isEqualTo(shishOClock);
		assertThat(content.getCheckoutUserId()).isNull();
		assertThat(content.getCheckoutDateTime()).isNull();
		assertThat(content.getHistoryVersions()).isEmpty();
		assertThat(content.isDirty()).isTrue();

	}

	@Test
	public void whenCreatingAMinorContentWithColonsThenColonsAreRemovedAndContentCreatedNormally()
			throws Exception {
		givenTimeIs(shishOClock);

		ContentImpl content = (ContentImpl) ContentImpl
				.create("zeId", alice, "file:name.pdf", new ContentVersionDataSummary("zeHash", "zeMime", zeLength), false);
		assertThat(content.isDirty()).isTrue();

		assertThat(content.getCurrentVersion().getFilename()).isEqualTo("filename.pdf");
		assertThat(content.getCurrentVersion().getHash()).isEqualTo("zeHash");
		assertThat(content.getCurrentVersion().getMimetype()).isEqualTo("zeMime");
		assertThat(content.getCurrentVersion().getLength()).isEqualTo(zeLength);
		assertThat(content.getCurrentVersion().getVersion()).isEqualTo("0.1");
		assertThat(content.getCurrentVersion().getModifiedBy()).isEqualTo(aliceId);
		assertThat(content.getCurrentVersion().getLastModificationDateTime()).isEqualTo(shishOClock);
		assertThat(content.getCheckoutUserId()).isNull();
		assertThat(content.getCheckoutDateTime()).isNull();
		assertThat(content.getHistoryVersions()).isEmpty();
		assertThat(content.isDirty()).isTrue();

	}

	@Test
	public void givenInvalidMimeOrHashThenException()
			throws Exception {
		givenTimeIs(shishOClock);

		try {
			ContentImpl.create("zeId", alice, "file:name.pdf", null, false);
			fail("ContentImplRuntimeException_InvalidArgument expected");
		} catch (ContentImplRuntimeException_InvalidArgument e) {
			//OK
		}

		try {
			ContentImpl.create("zeId", alice, "file:name.pdf", new ContentVersionDataSummary(null, "zeMime", 1l), false);
			fail("ContentImplRuntimeException_InvalidArgument expected");
		} catch (ContentImplRuntimeException_InvalidArgument e) {
			//OK
		}

		try {
			ContentImpl.create("zeId", alice, "file:name.pdf", new ContentVersionDataSummary("zeHash", null, 1l), false);
			fail("ContentImplRuntimeException_InvalidArgument expected");
		} catch (ContentImplRuntimeException_InvalidArgument e) {
			//OK
		}

	}

	@Test
	public void givenInvalidFilenameOrInputStreamOrUserWhileCreatingAContentThenException()
			throws Exception {
		givenTimeIs(shishOClock);

		ContentVersionDataSummary dataSummary = new ContentVersionDataSummary("zeHash", "zeMime", 42);

		try {
			ContentImpl.create(null, alice, "file:name.pdf", dataSummary, false);
			fail("ContentImplRuntimeException_InvalidArgument expected");
		} catch (ContentImplRuntimeException_InvalidArgument e) {
			//OK
		}

		try {
			ContentImpl.create(" ", alice, "file:name.pdf", dataSummary, false);
			fail("ContentImplRuntimeException_InvalidArgument expected");
		} catch (ContentImplRuntimeException_InvalidArgument e) {
			//OK
		}

		try {
			ContentImpl.create("zeId", null, "file:name.pdf", dataSummary, false);
			fail("ContentImplRuntimeException_InvalidArgument expected");
		} catch (ContentImplRuntimeException_InvalidArgument e) {
			//OK
		}

		try {
			ContentImpl.create("zeId", alice, null, dataSummary, false);
			fail("ContentImplRuntimeException_InvalidArgument expected");
		} catch (ContentImplRuntimeException_InvalidArgument e) {
			//OK
		}

		try {
			ContentImpl.create("zeId", alice, " ", dataSummary, false);
			fail("ContentImplRuntimeException_InvalidArgument expected");
		} catch (ContentImplRuntimeException_InvalidArgument e) {
			//OK
		}

		try {
			ContentImpl.create("zeId", alice, "file:name.pdf", null, false);
			fail("ContentImplRuntimeException_InvalidArgument expected");
		} catch (ContentImplRuntimeException_InvalidArgument e) {
			//OK
		}

	}

	@Test
	public void whenCheckingWithModificationThenAllArgumentsMustBeValid() {
		givenCheckedOutContentNotYetModified();

		try {
			content.checkInWithModification(null, true);
			fail("ContentImplRuntimeException_InvalidArgument expected");
		} catch (ContentImplRuntimeException_InvalidArgument e) {
			//OK
		}

	}

	@Test
	public void whenRenamingCurrentVersionThenAllArgumentsMustBeValid() {
		givenCheckedOutContentNotYetModified();

		try {
			content.renameCurrentVersion(null);
			fail("ContentImplRuntimeException_InvalidArgument expected");
		} catch (ContentImplRuntimeException_InvalidArgument e) {
			//OK
		}

		try {
			content.renameCurrentVersion(" ");
			fail("ContentImplRuntimeException_InvalidArgument expected");
		} catch (ContentImplRuntimeException_InvalidArgument e) {
			//OK
		}

	}

	@Test
	public void whenUpdatingContentThenAllArgumentsMustBeValid() {
		givenNonCheckedOutContent();

		try {
			content.updateContent(null, new ContentVersionDataSummary("zeHash", "zeMime", 42), true);
			fail("ContentImplRuntimeException_InvalidArgument expected");
		} catch (ContentImplRuntimeException_InvalidArgument e) {
			//OK
		}

		try {
			content.updateContent(alice, null, true);
			fail("ContentImplRuntimeException_InvalidArgument expected");
		} catch (ContentImplRuntimeException_InvalidArgument e) {
			//OK
		}

	}

	@Test
	public void whenUpdatingCheckedOutContentThenAllArgumentsMustBeValid() {
		givenCheckedOutContentNotYetModified();

		try {
			content.updateCheckedOutContent(null);
			fail("ContentImplRuntimeException_InvalidArgument expected");
		} catch (ContentImplRuntimeException_InvalidArgument e) {
			//OK
		}

	}

	private void givenNonCheckedOutContent() {
		content = new ContentImpl(contentId1, currentVersion, history, null, null, null);
	}

	private void givenCheckedOutContentNotYetModified() {
		content = new ContentImpl(contentId1, currentVersion, history, currentVersion, teaOClock, aliceId);
	}

	private void givenModifiedCheckedOutContent() {
		content = new ContentImpl(contentId1, currentVersion, history, modifiedCheckedOut, teaOClock, aliceId);
	}
}
