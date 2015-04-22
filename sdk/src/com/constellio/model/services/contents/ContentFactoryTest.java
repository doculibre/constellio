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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.utils.Lazy;
import com.constellio.sdk.tests.ConstellioTest;

public class ContentFactoryTest extends ConstellioTest {

	String aliceId = "aliceId";
	String bobId = "bobId";
	String charlesId = "charlesId";
	String dakotaId = "dakotaId";
	@Mock User alice;
	@Mock User bob;
	@Mock User charles;
	@Mock User dakota;
	@Mock InputStream inputStream;

	LocalDateTime meetingOClock = new LocalDateTime().minusHours(4);
	LocalDateTime shishOClock = new LocalDateTime().minusHours(2);
	LocalDateTime teaOClock = new LocalDateTime().minusHours(1);
	LocalDateTime tockOClock = new LocalDateTime().minusHours(1);
	LocalDateTime sushiOClock = new LocalDateTime().minusHours(1);
	ContentVersion currentVersion, currentCOVersion;
	ContentVersion firstVersion;
	ContentVersion secondVersion;
	Content content;
	long zeLength1;
	long zeLength2;
	long zeLength3;
	long zeCOLength3;
	private ContentFactory factory = new ContentFactory();

	@Before
	public void setUp()
			throws Exception {
		currentVersion = new ContentVersion(new ContentVersionDataSummary("zeHash", "zeMime3", zeLength3), "zeFileName_3.pdf",
				"1.0", charlesId, teaOClock);
		currentCOVersion = new ContentVersion(new ContentVersionDataSummary("zeCOHash", "zeCOMime3", zeCOLength3),
				"zeCOFileName_3.pdf", "1.1", dakotaId, sushiOClock);
		firstVersion = new ContentVersion(new ContentVersionDataSummary("ze1stHash", "zeMime1", zeLength1), "zeFileName_1.pdf",
				"0.1", aliceId, meetingOClock);
		secondVersion = new ContentVersion(new ContentVersionDataSummary("ze2ndHash", "zeMime2", zeLength2), "zeFileName_2.pdf",
				"0.2", bobId, shishOClock);
		content = new ContentImpl("zeContent", currentVersion, lazy(firstVersion, secondVersion), currentCOVersion, tockOClock,
				dakotaId);

		when(alice.getId()).thenReturn(aliceId);
		when(bob.getId()).thenReturn(bobId);
		when(charles.getId()).thenReturn(charlesId);
		when(dakota.getId()).thenReturn(dakotaId);
	}

	@Test
	public void givenContentInfoConvertedBetweenStringAndObjectThenValueIsConserved() {

		String text = factory.toString(content);
		Content content2 = (Content) factory.build(text);
		String text2 = factory.toString(content2);

		assertThat(content2.getHistoryVersions()).hasSize(2);
		assertThat(content2.getId()).isEqualTo("zeContent");

		assertThat(content2.getCurrentVersion().getHash()).isEqualTo("zeHash");
		assertThat(content2.getCurrentCheckedOutVersion().getHash()).isEqualTo("zeCOHash");
		assertThat(content2.getHistoryVersions().get(0).getHash()).isEqualTo("ze1stHash");
		assertThat(content2.getHistoryVersions().get(1).getHash()).isEqualTo("ze2ndHash");

		assertThat(content2.getCurrentVersion().getFilename()).isEqualTo("zeFileName_3.pdf");
		assertThat(content2.getCurrentCheckedOutVersion().getFilename()).isEqualTo("zeCOFileName_3.pdf");
		assertThat(content2.getHistoryVersions().get(0).getFilename()).isEqualTo("zeFileName_1.pdf");
		assertThat(content2.getHistoryVersions().get(1).getFilename()).isEqualTo("zeFileName_2.pdf");

		assertThat(content2.getCurrentVersion().getMimetype()).isEqualTo("zeMime3");
		assertThat(content2.getCurrentCheckedOutVersion().getMimetype()).isEqualTo("zeCOMime3");
		assertThat(content2.getHistoryVersions().get(0).getMimetype()).isEqualTo("zeMime1");
		assertThat(content2.getHistoryVersions().get(1).getMimetype()).isEqualTo("zeMime2");

		assertThat(content2.getCurrentVersion().getLength()).isEqualTo(zeLength3);
		assertThat(content2.getCurrentCheckedOutVersion().getLength()).isEqualTo(zeCOLength3);
		assertThat(content2.getHistoryVersions().get(0).getLength()).isEqualTo(zeLength1);
		assertThat(content2.getHistoryVersions().get(1).getLength()).isEqualTo(zeLength2);

		assertThat(content2.getCurrentVersion().getVersion()).isEqualTo("1.0");
		assertThat(content2.getCurrentCheckedOutVersion().getVersion()).isEqualTo("1.1");
		assertThat(content2.getHistoryVersions().get(0).getVersion()).isEqualTo("0.1");
		assertThat(content2.getHistoryVersions().get(1).getVersion()).isEqualTo("0.2");

		assertThat(content2.getCurrentVersion().getModifiedBy()).isEqualTo(charlesId);
		assertThat(content2.getCurrentCheckedOutVersion().getModifiedBy()).isEqualTo(dakotaId);
		assertThat(content2.getHistoryVersions().get(0).getModifiedBy()).isEqualTo(aliceId);
		assertThat(content2.getHistoryVersions().get(1).getModifiedBy()).isEqualTo(bobId);

		assertThat(content2.getCurrentVersion().getLastModificationDateTime()).isEqualTo(teaOClock);
		assertThat(content2.getCurrentCheckedOutVersion().getLastModificationDateTime()).isEqualTo(sushiOClock);
		assertThat(content2.getHistoryVersions().get(0).getLastModificationDateTime()).isEqualTo(meetingOClock);
		assertThat(content2.getHistoryVersions().get(1).getLastModificationDateTime()).isEqualTo(shishOClock);

		assertThat(content2.getCheckoutDateTime()).isEqualTo(tockOClock);
		assertThat(content2.getCheckoutUserId()).isEqualTo(dakotaId);

		assertThat(text2).isEqualTo(text);
	}

	@Test
	public void givenContentInfoConvertedToStringThenCanFindTheFilenames() {

		String text = factory.toString(content);

		assertThat(text).contains(ContentFactory.isFilename("zeFileName_1.pdf").getText());
		assertThat(text).contains(ContentFactory.isFilename("zeFileName_2.pdf").getText());
		assertThat(text).contains(ContentFactory.isFilename("zeFileName_3.pdf").getText());
		assertThat(text).contains(ContentFactory.isFilename("zeCOFileName_3.pdf").getText());

		assertThat(text).doesNotContain(ContentFactory.isFilename("zeFileName_1").getText());
		assertThat(text).doesNotContain(ContentFactory.isFilename("zeFileName_2").getText());
		assertThat(text).doesNotContain(ContentFactory.isFilename("zeFileName_3").getText());
		assertThat(text).doesNotContain(ContentFactory.isFilename("zeCOFileName_3").getText());

		assertThat(text).doesNotContain(ContentFactory.isFilename("FileName_1.pdf").getText());
		assertThat(text).doesNotContain(ContentFactory.isFilename("FileName_2.pdf").getText());
		assertThat(text).doesNotContain(ContentFactory.isFilename("FileName_3.pdf").getText());
		assertThat(text).doesNotContain(ContentFactory.isFilename("COFileName_3.pdf").getText());

		assertThat(text).doesNotContain(ContentFactory.isCurrentFilename("zeFileName_1.pdf").getText());
		assertThat(text).doesNotContain(ContentFactory.isCurrentFilename("zeFileName_2.pdf").getText());
		assertThat(text).contains(ContentFactory.isCurrentFilename("zeFileName_3.pdf").getText());
		assertThat(text).doesNotContain(ContentFactory.isCurrentFilename("zeCOFileName_3.pdf").getText());
	}

	@Test
	public void givenContentInfoConvertedToStringThenCanFindTheFilenameIndependentlyOfColons()
			throws Exception {

		String text = factory.toString(content);
		assertThat(text).contains(ContentFactory.isFilename("zeFileName_1.pdf").getText());
		assertThat(text).contains(ContentFactory.isFilename("zeFileName_2.pdf").getText());
		assertThat(text).contains(ContentFactory.isFilename("zeFileName_3.pdf").getText());

	}

	@Test
	public void givenContentInfoConvertedToStringThenCanFindHashInText()
			throws Exception {

		String text = factory.toString(content);
		assertThat(text).contains(ContentFactory.isHash("zeHash").getText());
		assertThat(text).contains(ContentFactory.isHash("zeCOHash").getText());
		assertThat(text).contains(ContentFactory.isHash("ze1stHash").getText());
		assertThat(text).contains(ContentFactory.isHash("ze2ndHash").getText());

		assertThat(text).doesNotContain(ContentFactory.isHash("Hash").getText());
		assertThat(text).doesNotContain(ContentFactory.isHash("COHash").getText());
		assertThat(text).doesNotContain(ContentFactory.isHash("1stHash").getText());
		assertThat(text).doesNotContain(ContentFactory.isHash("2ndHash").getText());

		assertThat(text).doesNotContain(ContentFactory.isHash("ze").getText());
		assertThat(text).doesNotContain(ContentFactory.isHash("zeCO").getText());
		assertThat(text).doesNotContain(ContentFactory.isHash("ze1st").getText());
		assertThat(text).doesNotContain(ContentFactory.isHash("ze2nd").getText());

	}

	@Test
	public void givenContentInfoConvertedToStringThenCanFinMimeInText()
			throws Exception {

		String text = factory.toString(content);
		assertThat(text).contains(ContentFactory.isMimetype("zeMime1").getText());
		assertThat(text).contains(ContentFactory.isMimetype("zeMime2").getText());
		assertThat(text).contains(ContentFactory.isMimetype("zeMime3").getText());
		assertThat(text).contains(ContentFactory.isMimetype("zeCOMime3").getText());

		assertThat(text).doesNotContain(ContentFactory.isMimetype("Mime1").getText());
		assertThat(text).doesNotContain(ContentFactory.isMimetype("Mime2").getText());
		assertThat(text).doesNotContain(ContentFactory.isMimetype("Mime3").getText());
		assertThat(text).doesNotContain(ContentFactory.isMimetype("COMime3").getText());

		assertThat(text).doesNotContain(ContentFactory.isMimetype("zeMime").getText());

	}

	@Test
	public void givenCheckedOutContentInfoConvertedToStringThenCanFindThatItISCheckedOut()
			throws Exception {

		String text = factory.toString(content);
		assertThat(text).contains(ContentFactory.checkedOut().getText());

		text = factory.toString(content.cancelCheckOut());
		assertThat(text).doesNotContain(ContentFactory.checkedOut().getText());

	}

	private Lazy<List<ContentVersion>> lazy(final ContentVersion... contentVersions) {
		return new Lazy<List<ContentVersion>>() {
			@Override
			protected List<ContentVersion> load() {
				return Arrays.asList(contentVersions);
			}
		};
	}
}
