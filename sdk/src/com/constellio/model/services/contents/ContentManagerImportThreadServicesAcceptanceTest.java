package com.constellio.model.services.contents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.utils.hashing.HashingServiceException;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.ModelLayerConfigurationAlteration;

public class ContentManagerImportThreadServicesAcceptanceTest extends ConstellioTest {

	private String pdf1Hash = "KN8RjbrnBgq1EDDV2U71a6/6gd4=";
	private String pdf2Hash = "T+4zq4cGP/tXkdJp/qz1WVWYhoQ=";
	private String pdf3Hash = "2O9RyZlxNUL3asxk2yGDT6VIlbs=";
	private String docx1Hash = "Fss7pKBafi8ok5KaOwEpmNdeGCE=";
	private String docx2Hash = "TIKwSvHOXHOOtRd1K9t2fm4TQ4I=";
	private String passwordProtectedPDFHash = "passwordProtectedPDFHash";

	File contentImportFile;
	File toImport;
	File errorsEmpty;
	File errorsUnparsable;
	File indexProperties;

	ContentManager contentManager;

	File folder1, folder2;

	@Before
	public void setUp()
			throws Exception {
		SearchServices searchServices = getModelLayerFactory().newSearchServices();
		MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
		contentManager = new ContentManager(getModelLayerFactory());

		contentImportFile = newTempFolder();
		contentImportFile.delete();
		configure(new ModelLayerConfigurationAlteration() {
			@Override
			public void alter(ModelLayerConfiguration configuration) {

				when(configuration.getContentImportThreadFolder()).thenReturn(contentImportFile);

			}
		});

		toImport = new File(contentImportFile, "toImport");
		errorsEmpty = new File(contentImportFile, "errors-empty");
		errorsUnparsable = new File(contentImportFile, "errors-unparsable");
		indexProperties = new File(contentImportFile, "filename-sha1-index.properties");

		assertThat(contentImportFile).doesNotExist();

		folder1 = new File(toImport, "folder1");
		folder1.mkdirs();
		folder2 = new File(toImport, "folder2");
		folder2.mkdirs();
	}

	@Test
	public void whenCalledTheFirstTimeThenCreateFolders()
			throws Exception {

		contentManager.uploadFilesInImportFolder();

		assertThat(contentImportFile).exists();
		assertThat(toImport).exists();
		assertThat(errorsEmpty).exists();
		assertThat(errorsUnparsable).exists();
		assertThat(indexProperties).doesNotExist();

	}

	@Test
	public void givenContentImportFolderConfiguredThenImportFileUnmodifiedFor10Seconds()
			throws Exception {

		String hash1 = addTextFileToImportAndReturnHash(new File(toImport, "file.html"), htmlWithBody("Chuck Norris"));
		String hash2 = addTextFileToImportAndReturnHash(new File(toImport, "file2.html"), htmlWithBody("Dakota l'Indien"));
		String hash3 = addTextFileToImportAndReturnHash(new File(folder1, "file.html"), htmlWithBody("Edouard Lechat"));
		String hash4 = addTextFileToImportAndReturnHash(new File(folder2, "file.html"), htmlWithBody("Darth Vador"));

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
		assertThat(contentImportFile.listFiles()).isEmpty();
		assertContentAndParsedContentWithHash(hash1, hash2, hash3, hash4);
		assertThat(contentManager.getParsedContent(hash1).getParsedContent()).isEqualTo("Chuck Norris");
		assertThat(contentManager.getParsedContent(hash2).getParsedContent()).isEqualTo("Dakota l'Indien");
		assertThat(contentManager.getParsedContent(hash3).getParsedContent()).isEqualTo("Edouard Lechat");
		assertThat(contentManager.getParsedContent(hash4).getParsedContent()).isEqualTo("Darth Vador");

		assertThat(indexProperties).exists();
		assertThat(contentManager.getImportedFilesMap()).containsOnly(
				entry("file.html", hash1),
				entry("file2.html", hash2),
				entry("folder1" + File.separator + "file.html", hash3),
				entry("folder2" + File.separator + "file.html", hash4)
		);
		assertThat(errorsEmpty.list()).isEmpty();
		assertThat(errorsUnparsable.list()).isEmpty();
	}

	@Test
	public void givenEmptyFileThenNotImportedAndMovedInErrors()
			throws Exception {

		String hash1 = addTextFileToImportAndReturnHash(new File(toImport, "file.html"), htmlWithBody("Chuck Norris"));
		String hash2 = addTextFileToImportAndReturnHash(new File(toImport, "file2.html"), "");
		String hash3 = addTextFileToImportAndReturnHash(new File(folder1, "file.html"), htmlWithBody("Edouard Lechat"));
		givenTimeIs(LocalDateTime.now().plusSeconds(11));

		contentManager.uploadFilesInImportFolder();

		assertThat(contentImportFile.listFiles()).isEmpty();
		assertContentAndParsedContentWithHash(hash1, hash3);
		assertNoContentAndParsedContentWithHash(hash2);

		assertThat(contentImportFile.listFiles()).isEmpty();
		assertThat(contentManager.getImportedFilesMap()).containsOnly(
				entry("file.html", hash1),
				entry("folder1" + File.separator + "file.html", hash3)
		);

		assertThat(errorsEmpty.list()).containsOnly("file2.html");
		assertThat(errorsUnparsable.list()).isEmpty();
	}

	@Test
	public void givenUnparsableFileThenImportedAndMovedInErrors()
			throws Exception {

		String hash1 = addTextFileToImportAndReturnHash(new File(toImport, "file.html"), htmlWithBody("Chuck Norris"));
		String hash2 = addFileToImportAndReturnHash(new File(toImport, "file2.pdf"), "passwordProtectedFile.pdf");
		String hash3 = addTextFileToImportAndReturnHash(new File(folder1, "file.html"), htmlWithBody("Edouard Lechat"));

		givenTimeIs(LocalDateTime.now().plusSeconds(11));
		contentManager.uploadFilesInImportFolder();

		assertThat(contentImportFile.listFiles()).isEmpty();
		assertContentAndParsedContentWithHash(hash1, hash3);
		assertNoContentAndParsedContentWithHash(hash2);

		assertThat(contentImportFile.listFiles()).isEmpty();
		assertThat(contentManager.getImportedFilesMap()).containsOnly(
				entry("file.html", hash1),
				entry("file2.pdf", hash2),
				entry("folder1" + File.separator + "file.html", hash3)
		);
		assertThat(contentManager.getContentInputStream(hash2, SDK_STREAM))
				.hasContentEqualTo(getTestResourceInputStream("passwordProtectedFile"));

		assertThat(errorsEmpty.list()).isEmpty();
		assertThat(errorsUnparsable.list()).containsOnly("file2.pdf");
	}

	@Test
	public void givenBigFilesAreCopiedInImportFolderThenAllEntriesImported()
			throws Exception {

		addFileToImportAndReturnHash(new File(toImport, "bigFile.html"), "file1.bigf");
		addFileToImportAndReturnHash(new File(folder1, "bigFile.html"), "file2.bigf");

		givenTimeIs(LocalDateTime.now().plusSeconds(11));

		contentManager.uploadFilesInImportFolder();

		assertThat(contentImportFile.listFiles()).isEmpty();
		assertContentAndParsedContentWithHash(pdf1Hash, pdf2Hash, pdf3Hash, docx1Hash, docx2Hash);

		assertThat(contentImportFile.listFiles()).isEmpty();
		assertThat(contentManager.getImportedFilesMap()).containsOnly(
				entry("ContentManagementAcceptTest-pdf1.pdf", pdf1Hash),
				entry("ContentManagementAcceptTest-pdf2.pdf", pdf2Hash),
				entry("ContentManagementAcceptTest-pdf3.pdf", pdf3Hash),
				entry("ContentManagementAcceptTest-docx1.docx", pdf1Hash),
				entry("ContentManagementAcceptTest-docx2.docx", pdf2Hash),
				entry("folder1" + File.separator + "ContentManagementAcceptTest-pdf1.pdf", pdf1Hash),
				entry("folder1" + File.separator + "ContentManagementAcceptTest-pdf2.pdf", pdf2Hash),
				entry("folder1" + File.separator + "ContentManagementAcceptTest-pdf3.pdf", pdf3Hash),
				entry("folder1" + File.separator + "ContentManagementAcceptTest-docx1.docx", pdf1Hash),
				entry("folder1" + File.separator + "ContentManagementAcceptTest-docx2.docx", pdf2Hash)
		);

		assertThat(errorsEmpty.list()).isEmpty();
		assertThat(errorsUnparsable.list()).isEmpty();
	}

	@Test
	public void givenBigFilesContainingBlankAndUnparsableEntriesThenHandledSameAsNormalFiles()
			throws Exception {

		addFileToImportAndReturnHash(new File(toImport, "fileWithErrors.bigf"), "file.bigf");

		givenTimeIs(LocalDateTime.now().plusSeconds(11));

		contentManager.uploadFilesInImportFolder();

		assertThat(contentImportFile.listFiles()).isEmpty();
		assertContentAndParsedContentWithHash(pdf1Hash, pdf2Hash, pdf3Hash, docx1Hash, docx2Hash);

		assertThat(contentImportFile.listFiles()).isEmpty();
		assertThat(contentManager.getImportedFilesMap()).containsOnly(
				entry("ContentManagementAcceptTest-pdf1.pdf", pdf1Hash),
				entry("ContentManagementAcceptTest-pdf2.pdf", pdf2Hash),
				entry("ContentManagementAcceptTest-pdf3.pdf", pdf3Hash),
				entry("ContentManagementAcceptTest-docx1.docx", pdf1Hash),
				entry("ContentManagementAcceptTest-docx2.docx", pdf2Hash),
				entry("ContentManagerAcceptanceTest-passwordProtected.pdf", passwordProtectedPDFHash)
		);

		assertThat(contentManager.getContentInputStream(passwordProtectedPDFHash, SDK_STREAM))
				.hasContentEqualTo(getTestResourceInputStream("passwordProtectedFile"));

		assertThat(toImport.list()).isEmpty();
		assertThat(errorsEmpty.list()).containsOnly("blankfile.txt");
		assertThat(errorsUnparsable.list()).containsOnly("ContentManagerAcceptanceTest-passwordProtected.pdf");
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

	private String addTextFileToImportAndReturnHash(File file, String content) {
		try {
			FileUtils.write(file, content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		try {
			return getIOLayerFactory().newHashingService().getHashFromFile(file);
		} catch (HashingServiceException e) {
			throw new RuntimeException(e);
		}
	}

	private String addFileToImportAndReturnHash(File file, String fileName) {
		try {
			FileUtils.copyFile(getTestResourceFile(fileName), file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		try {
			return getIOLayerFactory().newHashingService().getHashFromFile(file);
		} catch (HashingServiceException e) {
			throw new RuntimeException(e);
		}
	}

	private void assertContentAndParsedContentWithHash(String... hashes) {
		for (String hash : hashes) {
			contentManager.getContentInputStream(hash, SDK_STREAM);
			contentManager.getParsedContent(hash);
		}
	}

}
