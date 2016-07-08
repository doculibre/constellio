package com.constellio.model.services.contents;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.ModelLayerConfigurationAlteration;

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

	@Test
	public void givenContentImportFolderConfiguredThenImportFileUnmodifiedFor10Seconds()
			throws Exception {
		final File contentImportFile = newTempFolder();
		configure(new ModelLayerConfigurationAlteration() {
			@Override
			public void alter(ModelLayerConfiguration configuration) {

				when(configuration.getContentImportThreadFolder()).thenReturn(contentImportFile);
			}
		});

		File folder1 = new File(contentImportFile, "folder1");
		folder1.mkdirs();
		File folder2 = new File(contentImportFile, "folder2");
		folder2.mkdirs();

		FileUtils.write(new File(contentImportFile, "file.html"), htmlWithBody("Chuck Norris"));
		FileUtils.write(new File(contentImportFile, "file2.html"), htmlWithBody("Dakota l'Indien"));
		FileUtils.write(new File(contentImportFile, folder1 + "file.html"), htmlWithBody("Edouard Lechat"));
		FileUtils.write(new File(contentImportFile, folder2 + "file.html"), htmlWithBody("Darth Vador"));
		String hash1 = getIOLayerFactory().newHashingService().getHashFromFile(new File(contentImportFile, "file.html"));
		String hash2 = getIOLayerFactory().newHashingService().getHashFromFile(new File(contentImportFile, "file2.html"));
		String hash3 = getIOLayerFactory().newHashingService().getHashFromFile(new File(folder1, "file.html"));
		String hash4 = getIOLayerFactory().newHashingService().getHashFromFile(new File(folder2, "file.html"));

		LocalDateTime momentWhereFilesWereAdded = LocalDateTime.now();

		givenTimeIs(momentWhereFilesWereAdded);
		contentManager.uploadFilesInImportFolder();
		assertThat(contentImportFile.listFiles()).hasSize(4);
		assertNoContentAndParsedContentWithHash(hash1, hash2, hash3, hash4);

		givenTimeIs(momentWhereFilesWereAdded.plusSeconds(7));
		contentManager.uploadFilesInImportFolder();
		assertThat(contentImportFile.listFiles()).hasSize(4);
		assertNoContentAndParsedContentWithHash(hash1, hash2, hash3, hash4);
		assertThat(contentManager.getImportedFilesMap()).isEmpty();

		givenTimeIs(momentWhereFilesWereAdded.plusSeconds(11));
		contentManager.uploadFilesInImportFolder();
		assertThat(contentImportFile.listFiles()).hasSize(1);
		assertContentAndParsedContentWithHash(hash1, hash2, hash3, hash4);
		assertThat(contentManager.getParsedContent(hash1).getParsedContent()).isEqualTo("Chuck Norris");
		assertThat(contentManager.getParsedContent(hash1).getParsedContent()).isEqualTo("Dakota l'Indien");
		assertThat(contentManager.getParsedContent(hash1).getParsedContent()).isEqualTo("Edouard Lechat");
		assertThat(contentManager.getParsedContent(hash1).getParsedContent()).isEqualTo("Darth Vador");
		assertThat(contentManager.getImportedFilesMap()).containsOnly(
				entry("file.html", hash1),
				entry("file2.html", hash2),5
				entry("folder1" + File.separator + "file.html", hash3),
				entry("folder2" + File.separator + "file.html", hash4)
		);
	}

	private String htmlWithBody(String body) {
		return "<html><body>" + body + "</body></html>";
	}

	private void assertNoContentAndParsedContentWithHash(String... hashes) {
		for (String hash : hashes) {
			try {
				contentManager.getContentInputStream(hash, SDK_STREAM);
				fail("Content " + hash + " was found");
			} catch (ContentManagerRuntimeException_NoSuchContent e) {
				//OK
			}

			try {
				contentManager.getParsedContent(hash);
				fail("Content " + hash + " was found");
			} catch (ContentManagerRuntimeException_NoSuchContent e) {
				//OK
			}
		}
	}

	private void assertContentAndParsedContentWithHash(String... hashes) {
		for (String hash : hashes) {
			contentManager.getContentInputStream(hash, SDK_STREAM);
			contentManager.getParsedContent(hash);
		}
	}

}