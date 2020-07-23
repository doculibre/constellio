package com.constellio.model.services.contents;

import com.constellio.model.entities.enums.ContentCheckoutSource;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.utils.Lazy;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.InputStream;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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

	LocalDateTime meetingOClock = new LocalDateTime(2012, 11, 4, 1, 2, 3);
	LocalDateTime shishOClock = meetingOClock.minusHours(2);
	LocalDateTime teaOClock = shishOClock.minusHours(1);
	LocalDateTime tockOClock = teaOClock.minusHours(1);
	LocalDateTime sushiOClock = tockOClock.minusHours(1);
	ContentVersion currentVersion, currentCOVersion;
	ContentVersion firstVersion;
	ContentVersion secondVersion;
	ContentVersion sameInfoAsCurrentVersion;
	ContentVersion sameInfoAsCurrentVersion2;
	ContentVersion differentInfoFromCurrentVersion;
	ContentVersion sameInfoAsCurrentVersion3;
	Content content, emptyContent, sameInfoAsCurrentContent;
	long zeLength1;
	long zeLength2;
	long zeLength3;
	long zeCOLength3;
	private ContentFactory factory = new ContentFactory();

	@Before
	public void setUp()
			throws Exception {
		currentVersion = new ContentVersion(new ContentVersionDataSummary("zeHash", "zeMime3", zeLength3), "zeFileName_3.pdf",
				"1.0", charlesId, teaOClock, "commentOf\nVersion::1.0");
		currentCOVersion = new ContentVersion(new ContentVersionDataSummary("zeCOHash", "zeCOMime3", zeCOLength3),
				"zeCOFileName_3.pdf", "1.1", dakotaId, sushiOClock, "commentOf::CheckedOutVersion");
		firstVersion = new ContentVersion(new ContentVersionDataSummary("ze1stHash", "zeMime1", zeLength1), "zeFileName_1.pdf",
				"0.1", aliceId, meetingOClock, "comment:Of\n\rVersion0.1");
		secondVersion = new ContentVersion(new ContentVersionDataSummary("ze2ndHash", "zeMime2", zeLength2), "zeFileName_2.pdf",
				"0.2", bobId, shishOClock, null);

		sameInfoAsCurrentVersion = new ContentVersion(new ContentVersionDataSummary("zeHash", "zeMime3", zeLength3), "zeFileName_3.pdf",
				"0.1", charlesId, teaOClock, "commentOf\nVersion::0.1");
		sameInfoAsCurrentVersion2 = new ContentVersion(new ContentVersionDataSummary("zeHash", "zeMime3", zeLength3), "zeFileName_3.pdf",
				"0.2", charlesId, teaOClock, "commentOf\nVersion::0.2");
		differentInfoFromCurrentVersion = new ContentVersion(new ContentVersionDataSummary("ze2ndHash", "zeMime2", zeLength2), "zeFileName_2.pdf",
				"0.3", bobId, shishOClock, null);
		sameInfoAsCurrentVersion3 = new ContentVersion(new ContentVersionDataSummary("zeHash", "zeMime3", zeLength3), "zeFileName_3.pdf",
				"0.4", charlesId, teaOClock, "commentOf\nVersion::0.4");

		content = new ContentImpl("zeContent", currentVersion, lazy(firstVersion, secondVersion), currentCOVersion, tockOClock,
				dakotaId, ContentCheckoutSource.CONSTELLIO.getValue(), false);
		emptyContent = new ContentImpl("zeContent", currentVersion, lazy(firstVersion, secondVersion), currentCOVersion,
				tockOClock, dakotaId, ContentCheckoutSource.CONSTELLIO.getValue(), true);
		sameInfoAsCurrentContent = new ContentImpl("zeContent", currentVersion, lazy(sameInfoAsCurrentVersion, sameInfoAsCurrentVersion2, differentInfoFromCurrentVersion, sameInfoAsCurrentVersion3), currentCOVersion, tockOClock,
				dakotaId, ContentCheckoutSource.CONSTELLIO.getValue(), false);

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

		System.out.println(text);

		assertThat(content2.getHistoryVersions()).hasSize(2);
		assertThat(content2.getId()).isEqualTo("zeContent");
		assertThat(content2.isEmptyVersion()).isFalse();

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

		assertThat(content2.getCurrentVersion().getComment()).isEqualTo("commentOf\nVersion::1.0");
		assertThat(content2.getCurrentCheckedOutVersion().getComment()).isEqualTo("commentOf::CheckedOutVersion");
		assertThat(content2.getHistoryVersions().get(0).getComment()).isEqualTo("comment:Of\n\rVersion0.1");
		assertThat(content2.getHistoryVersions().get(1).getComment()).isEqualTo(null);

		assertThat(content2.getCheckoutDateTime()).isEqualTo(tockOClock);
		assertThat(content2.getCheckoutUserId()).isEqualTo(dakotaId);
		assertThat(content2.getCheckoutSource()).isEqualTo(ContentCheckoutSource.CONSTELLIO.getValue());

		assertThat(text2).isEqualTo(text);
	}

	@Test
	public void givenLegacyContentStringWithoutContentWhenReadThenCorrectValues()
			throws Exception {

		String legacyContentStringValue = "zeContent::cf=zeFileName_3.pdf::co=true::"
										  + "f=zeFileName_3.pdf:h=zeHash:l=0:m=zeMime3:u=charlesId:t=1351994523000:v=1.0::"
										  + "f=zeCOFileName_3.pdf:h=zeCOHash:l=0:m=zeCOMime3:u=dakotaId:t=1351987323000:v=1.1::dakotaId::1351990923000::"
										  + "f=zeFileName_1.pdf:h=ze1stHash:l=0:m=zeMime1:u=aliceId:t=1352005323000:v=0.1::"
										  + "f=zeFileName_2.pdf:h=ze2ndHash:l=0:m=zeMime2:u=bobId:t=1351998123000:v=0.2::";

		Content content = (Content) factory.build(legacyContentStringValue);
		assertThat(content.getHistoryVersions()).hasSize(2);
		assertThat(content.getId()).isEqualTo("zeContent");
		assertThat(content.isEmptyVersion()).isFalse();
		assertThat(content.getCurrentVersion().getComment()).isNull();

		assertThat(content.getCurrentVersion().getHash()).isEqualTo("zeHash");
		assertThat(content.getCurrentCheckedOutVersion().getHash()).isEqualTo("zeCOHash");
		assertThat(content.getHistoryVersions().get(0).getHash()).isEqualTo("ze1stHash");
		assertThat(content.getHistoryVersions().get(1).getHash()).isEqualTo("ze2ndHash");

		assertThat(content.getCurrentVersion().getFilename()).isEqualTo("zeFileName_3.pdf");
		assertThat(content.getCurrentCheckedOutVersion().getFilename()).isEqualTo("zeCOFileName_3.pdf");
		assertThat(content.getHistoryVersions().get(0).getFilename()).isEqualTo("zeFileName_1.pdf");
		assertThat(content.getHistoryVersions().get(1).getFilename()).isEqualTo("zeFileName_2.pdf");

		assertThat(content.getCurrentVersion().getMimetype()).isEqualTo("zeMime3");
		assertThat(content.getCurrentCheckedOutVersion().getMimetype()).isEqualTo("zeCOMime3");
		assertThat(content.getHistoryVersions().get(0).getMimetype()).isEqualTo("zeMime1");
		assertThat(content.getHistoryVersions().get(1).getMimetype()).isEqualTo("zeMime2");

		assertThat(content.getCurrentVersion().getLength()).isEqualTo(zeLength3);
		assertThat(content.getCurrentCheckedOutVersion().getLength()).isEqualTo(zeCOLength3);
		assertThat(content.getHistoryVersions().get(0).getLength()).isEqualTo(zeLength1);
		assertThat(content.getHistoryVersions().get(1).getLength()).isEqualTo(zeLength2);

		assertThat(content.getCurrentVersion().getVersion()).isEqualTo("1.0");
		assertThat(content.getCurrentCheckedOutVersion().getVersion()).isEqualTo("1.1");
		assertThat(content.getHistoryVersions().get(0).getVersion()).isEqualTo("0.1");
		assertThat(content.getHistoryVersions().get(1).getVersion()).isEqualTo("0.2");

		assertThat(content.getCurrentVersion().getModifiedBy()).isEqualTo(charlesId);
		assertThat(content.getCurrentCheckedOutVersion().getModifiedBy()).isEqualTo(dakotaId);
		assertThat(content.getHistoryVersions().get(0).getModifiedBy()).isEqualTo(aliceId);
		assertThat(content.getHistoryVersions().get(1).getModifiedBy()).isEqualTo(bobId);

		assertThat(content.getCurrentVersion().getLastModificationDateTime()).isEqualTo(teaOClock);
		assertThat(content.getCurrentCheckedOutVersion().getLastModificationDateTime()).isEqualTo(sushiOClock);
		assertThat(content.getHistoryVersions().get(0).getLastModificationDateTime()).isEqualTo(meetingOClock);
		assertThat(content.getHistoryVersions().get(1).getLastModificationDateTime()).isEqualTo(shishOClock);

		assertThat(content.getCurrentVersion().getComment()).isNull();
		assertThat(content.getCurrentCheckedOutVersion().getComment()).isNull();
		assertThat(content.getHistoryVersions().get(0).getComment()).isNull();
		assertThat(content.getHistoryVersions().get(1).getComment()).isNull();

		assertThat(content.getCheckoutDateTime()).isEqualTo(tockOClock);
		assertThat(content.getCheckoutUserId()).isEqualTo(dakotaId);
		assertThat(content.getCheckoutSource()).isEqualTo(ContentCheckoutSource.CONSTELLIO.getValue());

	}

	@Test
	public void testName()
			throws Exception {
		//
		//		String text = "v2:00000428003::cf=avis-maintien(2).pdf::co=false::f=avis-maintien(2).pdf:h=RlShqsrHHvujQPFsbPIfmqwDBNg=:l=7819:m=application/pdf:u=00000000004:t=1445750306560:v=0.4:::null::null::null::f=Avis de maintien de permis 2010-2011.pdf:h=RlShqsrHHvujQPFsbPIfmqwDBNg=:l=7819:m=application/pdf:u=00000000004:t=1445750306559:v=0.1:::f=Avis de maintien de permis 2010-2011.pdf:h=RlShqsrHHvujQPFsbPIfmqwDBNg=:l=7819:m=application/pdf:u=00000000004:t=1445750306560:v=0.2:::f=Avis de maintien de permis 2010-2011.pdf:h=RlShqsrHHvujQPFsbPIfmqwDBNg=:l=7819:m=application/pdf:u=00000000004:t=1445750306560:v=0.3:::";
		//		Content content = (Content) factory.build(text);

	}

	@Test
	public void givenEmptyContentInfoConvertedBetweenStringAndObjectThenValueIsConserved() {

		String text = factory.toString(emptyContent);
		Content content2 = (Content) factory.build(text);
		String text2 = factory.toString(content2);

		assertThat(content2.getHistoryVersions()).hasSize(2);
		assertThat(content2.getId()).isEqualTo("zeContent");
		assertThat(content2.isEmptyVersion()).isTrue();

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
		assertThat(content2.getCheckoutSource()).isEqualTo(ContentCheckoutSource.CONSTELLIO.getValue());

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
	public void givenContentInfoConvertedToStringThenCanFindTheContentByBorrower()
			throws Exception {

		String text = factory.toString(content);
		assertThat(text).contains(ContentFactory.isCheckedOutBy(dakota).getText());
		assertThat(text).doesNotContain(ContentFactory.isCheckedOutBy(alice).getText());

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
	public void givenContentInfoConvertedToStringThenCanFindMimeInText()
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

	@Test
	public void givenSameInfoAsCurrentVersionWhenToStringThenFoundOnce() {

		String text = factory.toString(sameInfoAsCurrentContent);
		assertThat(StringUtils.countMatches(text, ":f=zeFileName_3.pdf")).isEqualTo(2);
		assertThat(StringUtils.countMatches(text, ":u=charlesId")).isEqualTo(2);
		assertThat(StringUtils.countMatches(text, ":m=zeMime3")).isEqualTo(2);
		System.out.println(text);
	}

	@Test
	public void givenStringWithoutDuplicatedInfoWhenBuildThenInfoSetOnAllVersions() {
		String text = "v2:zeContent::cf=zeFileName_3.pdf::co=true::f=zeFileName_3.pdf:h=zeHash:l=0:m=zeMime3:u=charlesId:t=1351994523000:v=1.0:commentOf\r\n" +
					  "Version$#$$#$1.0::f=zeCOFileName_3.pdf:h=zeCOHash:l=0:m=zeCOMime3:u=dakotaId:t=1351987323000:v=1.1:commentOf$#$$#$CheckedOutVersion::dakotaId::1351990923000::f=:h=zeHash:l=0:m=:u=:t=1351994523000:v=0.1:commentOf\r\n" +
					  "Version$#$$#$0.1::f=:h=zeHash:l=0:m=:u=:t=1351994523000:v=0.2:commentOf\r\n" +
					  "Version$#$$#$0.2::f=zeFileName_2.pdf:h=ze2ndHash:l=0:m=zeMime2:u=bobId:t=1351998123000:v=0.3:null::f=zeFileName_3.pdf:h=zeHash:l=0:m=zeMime3:u=charlesId:t=1351994523000:v=0.4:commentOf\r\n" +
					  "Version$#$$#$0.4::";

		Content content = (Content) factory.build(text);
		ContentVersion currentParsed = content.getCurrentVersion();
		ContentVersion currentCOParsed = content.getCurrentCheckedOutVersion();
		List<ContentVersion> historyVersions = content.getHistoryVersions();

		assertThat(currentParsed.getVersion()).isEqualTo("1.0");
		assertThat(currentParsed.getFilename()).isEqualTo("zeFileName_3.pdf");
		assertThat(currentParsed.getModifiedBy()).isEqualTo("charlesId");
		assertThat(currentParsed.getMimetype()).isEqualTo("zeMime3");

		assertThat(currentCOParsed.getVersion()).isEqualTo("1.1");
		assertThat(currentCOParsed.getFilename()).isEqualTo("zeCOFileName_3.pdf");
		assertThat(currentCOParsed.getModifiedBy()).isEqualTo("dakotaId");
		assertThat(currentCOParsed.getMimetype()).isEqualTo("zeCOMime3");

		assertThat(historyVersions.size()).isEqualTo(4);
		ContentVersion historyVersion1 = historyVersions.get(0);
		ContentVersion historyVersion2 = historyVersions.get(1);
		ContentVersion historyVersion3 = historyVersions.get(2);
		ContentVersion historyVersion4 = historyVersions.get(3);

		assertThat(historyVersion1.getVersion()).isEqualTo("0.1");
		assertThat(historyVersion1.getFilename()).isEqualTo("zeFileName_3.pdf");
		assertThat(historyVersion1.getModifiedBy()).isEqualTo("charlesId");
		assertThat(historyVersion1.getMimetype()).isEqualTo("zeMime3");

		assertThat(historyVersion2.getVersion()).isEqualTo("0.2");
		assertThat(historyVersion2.getFilename()).isEqualTo("zeFileName_3.pdf");
		assertThat(historyVersion2.getModifiedBy()).isEqualTo("charlesId");
		assertThat(historyVersion2.getMimetype()).isEqualTo("zeMime3");

		assertThat(historyVersion3.getVersion()).isEqualTo("0.3");
		assertThat(historyVersion3.getFilename()).isEqualTo("zeFileName_2.pdf");
		assertThat(historyVersion3.getModifiedBy()).isEqualTo("bobId");
		assertThat(historyVersion3.getMimetype()).isEqualTo("zeMime2");

		assertThat(historyVersion4.getVersion()).isEqualTo("0.4");
		assertThat(historyVersion4.getFilename()).isEqualTo("zeFileName_3.pdf");
		assertThat(historyVersion4.getModifiedBy()).isEqualTo("charlesId");
		assertThat(historyVersion4.getMimetype()).isEqualTo("zeMime3");
	}

	private Lazy<List<ContentVersion>> lazy(final ContentVersion... contentVersions) {
		return new Lazy<List<ContentVersion>>() {
			@Override
			protected List<ContentVersion> load() {
				return asList(contentVersions);
			}
		};
	}

	@Test
	public void TestCreateSystemFile() {
		Content content = ContentImpl.createSystemContent("testFileName.jpg", new ContentVersionDataSummary("zeHash", "zeMime3", zeLength3));
		assertThat(content).isNotNull();
		assertThat(content.getCheckoutUserId()).isNull();
		assertThat(content.getCheckoutSource()).isNull();
		assertThat(content.getCurrentVersion().getVersion()).isEqualTo("1.0");
	}

	@Test
	public void givenContentInfoVersion2ThenCheckoutContentSourceCorrectlyInitialized() {
		String text = "v2:00000000391::cf=proces.docx::co=true::f=proces.docx:h=ADIMNNRL566Z6IL7KKIPN2BBKOVN7BA3:l=31777:m=application/vnd.openxmlformats-officedocument.wordprocessingml.document:u=00000000061:t=1594674544534:v=0.1:null::f=proces.docx:h=ADIMNNRL566Z6IL7KKIPN2BBKOVN7BA3:l=31777:m=application/vnd.openxmlformats-officedocument.wordprocessingml.document:u=00000000061:t=1594674544534:v=0.1:null::00000000061::1594674575736::";

		Content content = (Content) factory.build(text);
		assertThat(content.getCheckoutSource()).isEqualTo(ContentCheckoutSource.CONSTELLIO.getValue());
	}
}