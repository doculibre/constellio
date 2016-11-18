package com.constellio.model.services.contents;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;

import com.constellio.model.services.contents.icap.IcapException;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;

public class ContentManagerAcceptanceTest extends ConstellioTest {

	private static final String UNKNOWN_LANGUAGE = Language.UNKNOWN.getCode();

	@Mock User theUser;

	FileParser fileParser;
	ContentManager contentManager;

	String textContent = "The quick brown fox jumps over the lazy dog";
	InputStream contentStream, contentStream2, rawContentInputStream, expectedContentInputStream;

	@Before
	public void setUp()
			throws Exception {
		fileParser = spy(getModelLayerFactory().newFileParser());
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		contentManager = new ContentManager(getModelLayerFactory());
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

		assertThat(dataSummary.getHash()).isEqualTo("L9ThxnotKPzthJ7hu3bnORuT6xI=");
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
		String hash = contentManager.upload(contentStream).getHash();
		verify(fileParser, times(1)).parse(any(StreamFactory.class), anyInt());

		contentStream = newFileInputStream(textContentFile);
		contentManager.upload(contentStream);
		verify(fileParser, times(1)).parse(any(StreamFactory.class), anyInt());
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
		String hash = contentManager.upload(contentStream).getHash();
		verify(fileParser, times(1)).parse(any(StreamFactory.class), anyInt());

		contentStream = newFileInputStream(contentFile);
		contentManager.upload(contentStream);
		verify(fileParser, times(1)).parse(any(StreamFactory.class), anyInt());
		contentManager.getParsedContent(hash);
	}

    @Test(expected = IcapException.CommunicationFailure.class)
    public void givenInfectedNonYetParsedContentWhenUploadedThenCommunicationExceptionThrown()
            throws Exception {
        // Given
        getModelLayerFactory().getSystemConfigurationsManager().setValue(ConstellioEIMConfigs.ICAP_SERVER_URL, "icap://10.0.0.0:1344/squidclamav");
        contentStream = newFileInputStream(modifyFileSystem().newTempFileWithContent("any content"));

        // When
        contentManager.upload(contentStream, "someFileName");

        // Then, the exception is thrown.
    }

	@Test(expected = IcapException.ThreatFoundException.class)
	public void givenInfectedNonYetParsedContentWhenUploadedThenThreatFoundExceptionThrown()
			throws Exception {
		// Given
        getModelLayerFactory().getSystemConfigurationsManager().setValue(ConstellioEIMConfigs.ICAP_SERVER_URL, "icap://132.203.123.103:1344/squidclamav");
		contentStream = newFileInputStream(modifyFileSystem().newTempFileWithContent("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*\n"));

		// When
		contentManager.upload(contentStream, "someFileName");

		// Then, the exception is thrown.
	}

}