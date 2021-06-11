package com.constellio.model.services.contents;

import com.constellio.data.dao.services.contents.FileSystemContentDao;
import com.constellio.data.dao.services.contents.FileSystemContentDaoExternalResourcesExtension;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManager.ParseOptions;
import com.constellio.model.services.contents.ContentManager.UploadOptions;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent;
import com.constellio.model.services.contents.icap.IcapException;
import com.constellio.model.services.contents.icap.IcapResponse;
import com.constellio.model.services.contents.icap.IcapService;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InternetTest;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static com.constellio.model.services.migrations.ConstellioEIMConfigs.ICAP_SCAN_ACTIVATED;
import static com.constellio.model.services.migrations.ConstellioEIMConfigs.ICAP_SERVER_URL;
import static java.util.Arrays.asList;
import static org.apache.commons.io.IOUtils.readLines;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContentManagerAcceptanceTest extends ConstellioTest {

	private static final String UNKNOWN_LANGUAGE = Language.UNKNOWN.getCode();

	@Mock IcapResponse icapResponse;
	@Mock User theUser;
	FileParser fileParser;
	ContentManager contentManager;
	IcapService icapService;

	String textContent = "The quick brown fox jumps over the lazy dog";
	InputStream contentStream, contentStream2, rawContentInputStream, expectedContentInputStream;

	@Before
	public void setUp()
			throws Exception {
		fileParser = spy(getModelLayerFactory().newFileParser());
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		icapService = spy(new IcapService(getModelLayerFactory()));
		contentManager = new ContentManager(getModelLayerFactory(), icapService);
		contentManager.fileParser = fileParser;
		when(theUser.getUsername()).thenReturn("theUser");
	}

	@After
	public void tearDown()
			throws Exception {
		IOUtils.closeQuietly(contentStream);
		IOUtils.closeQuietly(contentStream2);
		IOUtils.closeQuietly(rawContentInputStream);
		IOUtils.closeQuietly(expectedContentInputStream);
	}

	@Test
	public void whenUploadingContentThenContentVersionDataSummaryHasValidInformations()
			throws Exception {
		File textContentFile = modifyFileSystem().newTempFileWithContent(textContent);
		contentStream = newFileInputStream(textContentFile);

		ContentVersionDataSummary dataSummary = contentManager.upload(contentStream);


		assertThat(dataSummary.getHash()).isEqualTo("F7KODRT2FUUPZ3MET3Q3W5XHHENZH2YS");
		assertThat(dataSummary.getMimetype()).isEqualTo("text/plain; charset=ISO-8859-1");
		assertThat(dataSummary.getLength()).isEqualTo(43L);

	}

	@Test
	public void whenUploadingContentThenParsedContentAndRawContentAreAvailable()
			throws Exception {
		File textContentFile = modifyFileSystem().newTempFileWithContent(textContent);
		contentStream = newFileInputStream(textContentFile);

		ContentVersionDataSummary dataSummary = contentManager.upload(contentStream);

		ParsedContent parsedContent = contentManager.getParsedContent(dataSummary.getHash());
		assertThat(parsedContent.getParsedContent()).isEqualTo(textContent);
		assertThat(parsedContent.getLanguage()).isEqualTo(Language.English.getCode());
		assertThat(parsedContent.getMimeType()).isEqualTo("text/plain; charset=ISO-8859-1");

		rawContentInputStream = contentManager.getContentInputStream(dataSummary.getHash(), SDK_STREAM);
		expectedContentInputStream = newFileInputStream(textContentFile);
		assertThat(rawContentInputStream).hasContentEqualTo(expectedContentInputStream);

	}

	@Test
	public void givenContentRetrievalExtensionConfiguredThenUsed()
			throws Exception {

		FileSystemContentDao fsContentDao = (FileSystemContentDao) getDataLayerFactory().getContentsDao();
		fsContentDao.register(new FileSystemContentDaoExternalResourcesExtension("42") {
			@Override
			public InputStream get(String hash, String streamName) {
				if (hash.equals("1234")) {
					return IOUtils.toInputStream("Chuck Norris");

				} else if (hash.equals("2345")) {
					return IOUtils.toInputStream("Édouard lechat");
				}

				return null;
			}
		});

		fsContentDao.register(new FileSystemContentDaoExternalResourcesExtension("666") {
			@Override
			public InputStream get(String hash, String streamName) {
				if (hash.equals("1234")) {
					return IOUtils.toInputStream("Alice");

				} else if (hash.equals("2345")) {
					return IOUtils.toInputStream("Dakota");
				}

				return null;
			}
		});

		assertThat(readLines(contentManager.getContentInputStream("#42=1234", SDK_STREAM))).isEqualTo(asList("Chuck Norris"));
		assertThat(readLines(contentManager.getContentInputStream("#42=2345", SDK_STREAM))).isEqualTo(asList("Édouard lechat"));
		assertThat(readLines(contentManager.getContentInputStream("#666=1234", SDK_STREAM))).isEqualTo(asList("Alice"));
		assertThat(readLines(contentManager.getContentInputStream("#666=2345", SDK_STREAM))).isEqualTo(asList("Dakota"));

		try {
			contentManager.getContentInputStream("#42=3456", SDK_STREAM);
			fail("Exception expected");
		} catch (ContentManagerRuntimeException_NoSuchContent e) {

		}

		try {
			contentManager.getContentInputStream("~777:3456", SDK_STREAM);
			fail("Exception expected");
		} catch (ContentManagerRuntimeException_NoSuchContent e) {
		}

	}

	@Test
	public void whenUploadingPDFContentThenParsedContentAndRawContentAreAvailable()
			throws Exception {
		File contentFile = getTestResourceFile("testFileWithProperties.pdf");
		contentStream = newFileInputStream(contentFile);

		ContentVersionDataSummary dataSummary = contentManager.upload(contentStream);

		ParsedContent parsedContent = contentManager.getParsedContent(dataSummary.getHash());
		assertThat(parsedContent.getParsedContent()).isEqualTo("This is an empty file...");
		assertThat(parsedContent.getLanguage()).isEqualTo(Language.English.getCode());
		assertThat(parsedContent.getMimeType()).isEqualTo("application/pdf");

		rawContentInputStream = contentManager.getContentInputStream(dataSummary.getHash(), SDK_STREAM);
		expectedContentInputStream = newFileInputStream(contentFile);
		assertThat(rawContentInputStream).hasContentEqualTo(expectedContentInputStream);

		assertThat(parsedContent.getProperties()).containsEntry("Title", "Ze title");
		assertThat(parsedContent.getProperties())
				.containsEntry("List:Keywords", asList("Ze keyword1", "Ze keyword2", "Ze keyword 3"));
		assertThat(parsedContent.getProperties()).containsEntry("Author", "Ze author");
		assertThat(parsedContent.getProperties()).containsEntry("Subject", "Ze subject");
	}

	@Test
	public void whenUploadingDOCXContentThenParsedContentAndRawContentAreAvailable()
			throws Exception {
		File contentFile = getTestResourceFile("testFileWithProperties.docx");
		contentStream = newFileInputStream(contentFile);

		ContentVersionDataSummary dataSummary = contentManager.upload(contentStream);

		ParsedContent parsedContent = contentManager.getParsedContent(dataSummary.getHash());
		assertThat(parsedContent.getParsedContent()).isEqualTo("This is an empty file...");
		assertThat(parsedContent.getLanguage()).isEqualTo(Language.English.getCode());
		assertThat(parsedContent.getMimeType())
				.isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document");

		rawContentInputStream = contentManager.getContentInputStream(dataSummary.getHash(), SDK_STREAM);
		expectedContentInputStream = newFileInputStream(contentFile);
		assertThat(rawContentInputStream).hasContentEqualTo(expectedContentInputStream);

		assertThat(parsedContent.getProperties()).containsEntry("Company", "Ze company");
		assertThat(parsedContent.getProperties()).containsEntry("Category", "Ze category");
		assertThat(parsedContent.getProperties()).containsEntry("Manager", "Ze ultimate manager");
		assertThat(parsedContent.getProperties()).containsEntry("Comments", "Ze very useful comments Line2");
		assertThat(parsedContent.getProperties()).containsEntry("Title", "Ze title");
		assertThat(parsedContent.getProperties())
				.containsEntry("List:Keywords", asList("Ze keyword1", "Ze keyword2", "Ze keyword 3"));
		assertThat(parsedContent.getProperties()).containsEntry("Author", "Ze author");
		assertThat(parsedContent.getProperties()).containsEntry("Subject", "Ze subject");
	}

	@Test
	public void whenUploadingAParsableContentTwiceThenOnlyParsedOnceAndSameParsedContent()
			throws Exception {
		File textContentFile = modifyFileSystem().newTempFileWithContent(textContent);
		contentStream = newFileInputStream(textContentFile);
		String hash = contentManager.upload(contentStream, new UploadOptions().setParseOptions(ParseOptions.PARSING_WITHOUT_OCR))
				.getContentVersionDataSummary().getHash();
		verify(fileParser, times(1)).parse(any(StreamFactory.class), anyInt(), eq(ParseOptions.PARSING_WITHOUT_OCR));

		contentStream = newFileInputStream(textContentFile);
		contentManager.upload(contentStream, new UploadOptions().setParseOptions(ParseOptions.PARSING_WITHOUT_OCR));
		verify(fileParser, times(1)).parse(any(StreamFactory.class), anyInt(), eq(ParseOptions.PARSING_WITHOUT_OCR));
		contentManager.getParsedContent(hash);
	}

	@Test
	public void whenSavingUnparsableContentThenEmptyParsedContentAndRawContentAreAvailable()
			throws Exception {
		File contentFile = getTestResourceFile("passwordProtected.pdf");
		contentStream = newFileInputStream(contentFile);

		String id = contentManager.upload(contentStream).getHash();

		ParsedContent parsedContent = contentManager.getParsedContent(id);
		assertThat(parsedContent.getParsedContent()).isEmpty();
		assertThat(parsedContent.getLanguage()).isEqualTo(UNKNOWN_LANGUAGE);
		assertThat(parsedContent.getMimeType()).isEqualTo("application/pdf");

		rawContentInputStream = contentManager.getContentInputStream(id, SDK_STREAM);
		expectedContentInputStream = newFileInputStream(contentFile);
		assertThat(rawContentInputStream).hasContentEqualTo(expectedContentInputStream);

	}

	@Test
	public void whenSavingAnUnparsableContentTwiceThenOnlyParsedOnce()
			throws Exception {

		File contentFile = getTestResourceFile("passwordProtected.pdf");
		contentStream = newFileInputStream(contentFile);
		String hash = contentManager.upload(contentStream, new UploadOptions().setParseOptions(ParseOptions.PARSING_WITHOUT_OCR))
				.getContentVersionDataSummary().getHash();
		verify(fileParser, times(1)).parse(any(StreamFactory.class), anyInt(), eq(ParseOptions.PARSING_WITHOUT_OCR));

		contentStream = newFileInputStream(contentFile);
		contentManager.upload(contentStream, new UploadOptions().setParseOptions(ParseOptions.PARSING_WITHOUT_OCR));
		verify(fileParser, times(1)).parse(any(StreamFactory.class), anyInt(), eq(ParseOptions.PARSING_WITHOUT_OCR));
		contentManager.getParsedContent(hash);
	}


	@Test(expected = IcapException.CommunicationFailure.class)
	public void givenIcapActivatedWhenUploadingInfectedNonYetParsedContentThenCommunicationExceptionThrown()
			throws Exception {
		// Given
		getModelLayerFactory().getSystemConfigurationsManager().setValue(ICAP_SCAN_ACTIVATED, true);
		getModelLayerFactory().getSystemConfigurationsManager().setValue(ICAP_SERVER_URL, "icap://localhost:1344/squidclamav");
		contentStream = newFileInputStream(modifyFileSystem().newTempFileWithContent("any content"));

		// When
		contentManager.upload(contentStream, "someFileName");

		// Then, the exception is thrown.
	}

	@InternetTest
	@Test
	public void givenIcapActivatedWhenUploadingInfectedNonYetParsedContentThenThreatFoundExceptionThrown()
			throws Exception {
		getModelLayerFactory().getSystemConfigurationsManager().setValue(ICAP_SCAN_ACTIVATED, true);
		getModelLayerFactory().getSystemConfigurationsManager()
				.setValue(ICAP_SERVER_URL, "icap://localhost:1344/squidclamav");
		contentStream = newFileInputStream(modifyFileSystem()
				.newTempFileWithContent("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*\n"));

		when(icapResponse.isNoThreatFound()).thenReturn(false);
		when(icapResponse.getThreatDescription()).thenReturn("A big bad virus");
		doReturn(icapResponse).when(icapService).tryScan(eq("someFileName"), any(InputStream.class));

		try {
			contentManager.upload(contentStream, "someFileName");
			fail("Exception expected");
		} catch (IcapException.ThreatFoundException e) {
			assertThat(e.getThreatName()).isEqualTo("A big bad virus");
		}
	}

	@InternetTest
	@Test(expected = IcapException.CommunicationFailure.class)
	public void givenIcapActivatedWhenIOExceptionWhenUploadingNonYetParsedContentThenCommunicationExceptionThrown()
			throws Exception {
		getModelLayerFactory().getSystemConfigurationsManager().setValue(ICAP_SCAN_ACTIVATED, true);
		getModelLayerFactory().getSystemConfigurationsManager()
				.setValue(ICAP_SERVER_URL, "icap://localhost:1344/squidclamav");
		contentStream = newFileInputStream(modifyFileSystem()
				.newTempFileWithContent("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*\n"));

		doThrow(IOException.class).when(icapService).tryScan(eq("someFileName"), any(InputStream.class));

		contentManager.upload(contentStream, "someFileName");
	}

	@InternetTest
	@Test(expected = IcapException.TimeoutException.class)
	public void givenIcapActivatedWhenTimeoutExceptionWhenUploadingNonYetParsedContentThenTimeoutExceptionThrown()
			throws Exception {
		getModelLayerFactory().getSystemConfigurationsManager().setValue(ICAP_SCAN_ACTIVATED, true);
		getModelLayerFactory().getSystemConfigurationsManager()
				.setValue(ICAP_SERVER_URL, "icap://localhost:1344/squidclamav");
		contentStream = newFileInputStream(modifyFileSystem()
				.newTempFileWithContent("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*\n"));

		when(icapResponse.isScanTimedout()).thenReturn(true);
		doReturn(icapResponse).when(icapService).tryScan(eq("someFileName"), any(InputStream.class));

		contentManager.upload(contentStream, "someFileName");
	}

	@InternetTest
	@Test
	public void givenIcapActivatedWhenUploadingNonInfectedThenNoExceptionThrown()
			throws Exception {
		getModelLayerFactory().getSystemConfigurationsManager().setValue(ICAP_SCAN_ACTIVATED, true);
		getModelLayerFactory().getSystemConfigurationsManager()
				.setValue(ICAP_SERVER_URL, "icap://132.203.123.103:1344/squidclamav");
		contentStream = newFileInputStream(modifyFileSystem()
				.newTempFileWithContent("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*\n"));

		when(icapResponse.isNoThreatFound()).thenReturn(true);
		doReturn(icapResponse).when(icapService).tryScan(eq("someFileName"), any(InputStream.class));

		contentManager.upload(contentStream, "someFileName");
	}

	@InternetTest
	@Test
	public void givenIcapIsNotActivatedWhenUploadingInfectedNonYetParsedContentThenNoThreatFoundExceptionThrown()
			throws Exception {
		// Given

		getModelLayerFactory().getSystemConfigurationsManager().setValue(ICAP_SCAN_ACTIVATED, false);
		getModelLayerFactory().getSystemConfigurationsManager()
				.setValue(ICAP_SERVER_URL, "icap://132.203.123.103:1344/squidclamav");
		contentStream = newFileInputStream(modifyFileSystem()
				.newTempFileWithContent("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*\n"));

		// When
		contentManager.upload(contentStream, "someFileName");

		// Then, the exception is thrown.
	}

}