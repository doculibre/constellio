package com.constellio.model.services.contents;

import com.constellio.data.utils.Factory;
import com.constellio.data.utils.hashing.HashingServiceException;
import com.constellio.model.conf.PropertiesModelLayerConfiguration.InMemoryModelLayerConfiguration;
import com.constellio.model.services.contents.ContentManager.ImportedFilesMap;
import com.constellio.model.services.contents.ContentManagerException.ContentManagerException_ContentNotParsed;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.ModelLayerConfigurationAlteration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.assertj.core.api.MapAssert;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.constellio.data.conf.HashingEncoding.BASE64_URL_ENCODED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.Assert.fail;

public class ContentManagerImportThreadServicesAcceptanceTest extends ConstellioTest {

	private String htmlMimetype = "text/html; charset=ISO-8859-1";
	private String pdfMimetype = "application/pdf";
	private String tikaOOXMLMimetype = "application/x-tika-ooxml";
	private String docxMimetype = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	private String pptxMimetype = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
	private String pdf1Hash = "KN8RjbrnBgq1EDDV2U71a6_6gd4=";
	private String pdf2Hash = "T-4zq4cGP_tXkdJp_qz1WVWYhoQ=";
	private String pdf3Hash = "2O9RyZlxNUL3asxk2yGDT6VIlbs=";
	private String docx1Hash = "Fss7pKBafi8ok5KaOwEpmNdeGCE=";
	private String docx2Hash = "TIKwSvHOXHOOtRd1K9t2fm4TQ4I=";
	private String passwordProtectedPDFHash = "pcHnZL3eFeuGqdkcdxqQVfeyuXc=";
	private String fileTooBigHash = "zeHash";

	ContentVersionDataSummary pdf1Data = new ContentVersionDataSummary(pdf1Hash, pdfMimetype, 170039);
	ContentVersionDataSummary pdf2Data = new ContentVersionDataSummary(pdf2Hash, pdfMimetype, 167347);
	ContentVersionDataSummary pdf3Data = new ContentVersionDataSummary(pdf3Hash, pdfMimetype, 141667);
	ContentVersionDataSummary passwordProtectedPdfData = new ContentVersionDataSummary(passwordProtectedPDFHash, pdfMimetype,
			158836);
	ContentVersionDataSummary fileTooBigData = new ContentVersionDataSummary(tikaOOXMLMimetype, pdfMimetype, 1250742);
	ContentVersionDataSummary docx1Data = new ContentVersionDataSummary(docx1Hash, docxMimetype, 27055);
	ContentVersionDataSummary docx2Data = new ContentVersionDataSummary(docx2Hash, docxMimetype, 27325);

	File contentImportFile;
	File toImport;
	File errorsEmpty;
	File filesExceedingParsingFileSizeLimit;
	File errorsUnparsable;
	File indexProperties;

	ContentManager contentManager;

	File folder1, folder2;

	public void setUp(final boolean deleteUnusedContent)
			throws Exception {
		givenHashingEncodingIs(BASE64_URL_ENCODED);

		contentImportFile = newTempFolder();

		configure(new ModelLayerConfigurationAlteration() {
			@Override
			public void alter(InMemoryModelLayerConfiguration configuration) {
				configuration.setContentImportThreadFolder(contentImportFile);
				configuration.setDeleteUnusedContentEnabled(deleteUnusedContent);
			}
		});

		toImport = new File(contentImportFile, "toImport");
		errorsEmpty = new File(contentImportFile, "errors-empty");
		errorsUnparsable = new File(contentImportFile, "errors-unparsable");
		indexProperties = new File(contentImportFile, "filename-sha1-index.properties");
		filesExceedingParsingFileSizeLimit = new File(contentImportFile, "files-exceeding-parsing-size-limit.txt");
		contentImportFile.delete();

		assertThat(contentImportFile).doesNotExist();

		folder1 = new File(toImport, "folder1");
		folder1.mkdirs();
		folder2 = new File(toImport, "folder2");
		folder2.mkdirs();

		contentManager = new ContentManager(getModelLayerFactory());
	}

	@Test
	public void whenCalledTheFirstTimeThenCreateFolders()
			throws Exception {

		setUp(false);
		contentManager.uploadFilesInImportFolder();

		assertThat(contentImportFile).exists();
		assertThat(toImport).exists();
		assertThat(errorsEmpty).exists();
		assertThat(errorsUnparsable).exists();
		assertThat(filesExceedingParsingFileSizeLimit).exists().hasContent("");
		assertThat(indexProperties).doesNotExist();

	}

	@Test
	public void givenContentImportFolderConfiguredThenImportFileUnmodifiedFor10Seconds()
			throws Exception {

		setUp(false);
		ContentVersionDataSummary data1 = addTextFileToImportAndReturnHash(new File(toImport, "file.html"),
				htmlWithBody("Chuck Norris"));
		ContentVersionDataSummary data2 = addTextFileToImportAndReturnHash(new File(toImport, "file2.html"),
				htmlWithBody("Dakota l'Indien"));
		ContentVersionDataSummary data3 = addTextFileToImportAndReturnHash(new File(folder1, "file.html"),
				htmlWithBody("Edouard Lechat"));
		ContentVersionDataSummary data4 = addTextFileToImportAndReturnHash(new File(folder2, "file.html"),
				htmlWithBody("Darth Vador"));
		assertThat(allFilesRecursivelyIn(contentImportFile)).hasSize(4);

		LocalDateTime momentWhereFilesWereAdded = LocalDateTime.now();

		givenTimeIs(momentWhereFilesWereAdded);
		contentManager.uploadFilesInImportFolder();
		assertThat(allFilesRecursivelyIn(toImport)).hasSize(4);
		assertNoContentAndParsedContentWithHash(data1.getHash(), data2.getHash(), data3.getHash(), data4.getHash());

		givenTimeIs(momentWhereFilesWereAdded.plusSeconds(7));
		contentManager.uploadFilesInImportFolder();
		assertThat(allFilesRecursivelyIn(toImport)).hasSize(4);
		assertNoContentAndParsedContentWithHash(data1.getHash(), data2.getHash(), data3.getHash(), data4.getHash());
		assertThatMapContent(contentManager.getImportedFilesMap()).isEmpty();

		givenTimeIs(momentWhereFilesWereAdded.plusSeconds(11));
		contentManager.uploadFilesInImportFolder();
		assertThat(toImport.list()).isEmpty();
		assertContentAndParsedContentWithHash(data1.getHash(), data2.getHash(), data3.getHash(), data4.getHash());
		assertThat(contentManager.getParsedContent(data1.getHash()).getParsedContent()).isEqualTo("Chuck Norris");
		assertThat(contentManager.getParsedContent(data2.getHash()).getParsedContent()).isEqualTo("Dakota l'Indien");
		assertThat(contentManager.getParsedContent(data3.getHash()).getParsedContent()).isEqualTo("Edouard Lechat");
		assertThat(contentManager.getParsedContent(data4.getHash()).getParsedContent()).isEqualTo("Darth Vador");

		assertThat(indexProperties).exists();
		assertThatMapContent(contentManager.getImportedFilesMap()).containsOnly(
				entry("file.html", data1),
				entry("file2.html", data2),
				entry("folder1/file.html", data3),
				entry("folder2/file.html", data4)
		);
		assertThat(errorsEmpty.list()).isEmpty();
		assertThat(errorsUnparsable.list()).isEmpty();
	}

	private MapAssert<String, ContentVersionDataSummary> assertThatMapContent(ImportedFilesMap importedFilesMap) {
		Map<String, ContentVersionDataSummary> map = new HashMap<>();

		importedFilesMap.getAllPaths().forEach(path-> map.put(path, importedFilesMap.get(path)));

		return assertThat(map);
	}

	@Test
	public void givenContentDeleteThreadIsNotDisabledThenRefuseToImport()
			throws Exception {

		setUp(true);
		ContentVersionDataSummary data1 = addTextFileToImportAndReturnHash(new File(toImport, "file.html"),
				htmlWithBody("Chuck Norris"));
		ContentVersionDataSummary data2 = addTextFileToImportAndReturnHash(new File(toImport, "file2.html"),
				htmlWithBody("Dakota l'Indien"));
		ContentVersionDataSummary data3 = addTextFileToImportAndReturnHash(new File(folder1, "file.html"),
				htmlWithBody("Edouard Lechat"));
		ContentVersionDataSummary data4 = addTextFileToImportAndReturnHash(new File(folder2, "file.html"),
				htmlWithBody("Darth Vador"));
		assertThat(allFilesRecursivelyIn(contentImportFile)).hasSize(4);

		LocalDateTime momentWhereFilesWereAdded = LocalDateTime.now();

		givenTimeIs(momentWhereFilesWereAdded.plusSeconds(11));
		contentManager.uploadFilesInImportFolder();
		assertThat(allFilesRecursivelyIn(toImport)).hasSize(4);
		assertNoContentAndParsedContentWithHash(data1.getHash(), data2.getHash(), data3.getHash(), data4.getHash());
		assertThatMapContent(contentManager.getImportedFilesMap()).isEmpty();
	}

	@Test
	public void whenImportingContentsThenImportedByBatch()
			throws Exception {

		setUp(false);
		ContentVersionDataSummary data1 = addTextFileToImportAndReturnHash(new File(toImport, "file.html"),
				htmlWithBody("Chuck Norris"));
		ContentVersionDataSummary data2 = addTextFileToImportAndReturnHash(new File(toImport, "file2.html"),
				htmlWithBody("Dakota l'Indien"));
		ContentVersionDataSummary data3 = addTextFileToImportAndReturnHash(new File(folder1, "file.html"),
				htmlWithBody("Edouard Lechat"));
		ContentVersionDataSummary data4 = addTextFileToImportAndReturnHash(new File(folder2, "file.html"),
				htmlWithBody("Darth Vador"));
		assertThat(allFilesRecursivelyIn(contentImportFile)).hasSize(4);

		givenTimeIs(LocalDateTime.now().plusSeconds(11));
		ContentManagerImportThreadServices services = new ContentManagerImportThreadServices(getModelLayerFactory(), 1);

		//Call #1
		services.importFiles();
		assertContentAndParsedContentWithHash(data1.getHash());
		assertNoContentAndParsedContentWithHash(data2.getHash(), data3.getHash(), data4.getHash());
		assertThatMapContent(contentManager.getImportedFilesMap()).containsOnly(
				entry("file.html", data1)
		);

		//Call #2
		services.importFiles();
		assertContentAndParsedContentWithHash(data1.getHash(), data2.getHash());
		assertNoContentAndParsedContentWithHash(data3.getHash(), data4.getHash());
		assertThatMapContent(contentManager.getImportedFilesMap()).containsOnly(
				entry("file.html", data1),
				entry("file2.html", data2)
		);

		//Call #3
		services.importFiles();
		assertContentAndParsedContentWithHash(data1.getHash(), data2.getHash(), data3.getHash());
		assertNoContentAndParsedContentWithHash(data4.getHash());
		assertThatMapContent(contentManager.getImportedFilesMap()).containsOnly(
				entry("file.html", data1),
				entry("file2.html", data2),
				entry("folder1/file.html", data3)
		);

		//Call #4
		services.importFiles();
		assertContentAndParsedContentWithHash(data1.getHash(), data2.getHash(), data3.getHash(), data4.getHash());
		assertThat(contentManager.getParsedContent(data1.getHash()).getParsedContent()).isEqualTo("Chuck Norris");
		assertThat(contentManager.getParsedContent(data2.getHash()).getParsedContent()).isEqualTo("Dakota l'Indien");
		assertThat(contentManager.getParsedContent(data3.getHash()).getParsedContent()).isEqualTo("Edouard Lechat");
		assertThat(contentManager.getParsedContent(data4.getHash()).getParsedContent()).isEqualTo("Darth Vador");

		assertThat(indexProperties).exists();
		assertThatMapContent(contentManager.getImportedFilesMap()).containsOnly(
				entry("file.html", data1),
				entry("file2.html", data2),
				entry("folder1/file.html", data3),
				entry("folder2/file.html", data4)
		);

		assertThat(toImport.list()).isEmpty();
		assertThat(errorsEmpty.list()).isEmpty();
		assertThat(errorsUnparsable.list()).isEmpty();
	}

	@Test
	public void givenEmptyFileThenNotImportedAndMovedInErrors()
			throws Exception {

		setUp(false);
		ContentVersionDataSummary data1 = addTextFileToImportAndReturnHash(new File(toImport, "file.html"),
				htmlWithBody("Chuck Norris"));
		ContentVersionDataSummary data2 = addTextFileToImportAndReturnHash(new File(toImport, "file2.html"), "");
		ContentVersionDataSummary data3 = addTextFileToImportAndReturnHash(new File(folder1, "file.html"),
				htmlWithBody("Edouard Lechat"));
		ContentVersionDataSummary data4 = addTextFileToImportAndReturnHash(new File(folder1, "file3.html"), "");
		givenTimeIs(LocalDateTime.now().plusSeconds(11));

		contentManager.uploadFilesInImportFolder();

		assertThat(toImport.list()).isEmpty();
		assertContentAndParsedContentWithHash(data1.getHash(), data3.getHash());
		assertNoContentAndParsedContentWithHash(data2.getHash());

		assertThat(toImport.list()).isEmpty();
		assertThatMapContent(contentManager.getImportedFilesMap()).containsOnly(
				entry("file.html", data1),
				entry("folder1/file.html", data3)
		);

		assertThat(errorsEmpty.list()).containsOnly("file2.html", "folder1");
		assertThat(new File(errorsEmpty, "folder1").list()).containsOnly("file3.html");
		assertThat(errorsUnparsable.list()).isEmpty();
		assertThat(filesExceedingParsingFileSizeLimit).exists().hasContent("");
	}

	@Test
	public void givenUnparsableFileThenImportedAndMovedInErrors()
			throws Exception {

		setUp(false);
		ContentVersionDataSummary data1 = addTextFileToImportAndReturnHash(new File(toImport, "file.html"),
				htmlWithBody("Chuck Norris"));
		ContentVersionDataSummary data2 = addFileToImportAndReturnHash(new File(toImport, "file2.pdf"),
				"passwordProtectedFile.pdf", pdfMimetype);
		ContentVersionDataSummary data3 = addTextFileToImportAndReturnHash(new File(folder1, "file.html"),
				htmlWithBody("Edouard Lechat"));
		ContentVersionDataSummary data4 = addFileToImportAndReturnHash(new File(folder1, "file3.pdf"),
				"passwordProtectedFile.pdf", pdfMimetype);

		givenTimeIs(LocalDateTime.now().plusSeconds(11));
		contentManager.uploadFilesInImportFolder();

		assertThat(toImport.list()).isEmpty();
		assertContentAndParsedContentWithHash(data1.getHash(), data3.getHash());

		assertThat(toImport.list()).isEmpty();
		assertThatMapContent(contentManager.getImportedFilesMap()).containsOnly(
				entry("file.html", data1),
				entry("file2.pdf", data2),
				entry("folder1/file.html", data3),
				entry("folder1/file3.pdf", data4)
		);
		assertThat(contentManager.getContentInputStream(data2.getHash(), SDK_STREAM))
				.hasContentEqualTo(getTestResourceInputStream("passwordProtectedFile.pdf"));

		assertThat(errorsEmpty.list()).isEmpty();
		assertThat(errorsUnparsable.list()).containsOnly("file2.pdf", "folder1");
		assertThat(new File(errorsUnparsable, "folder1").list()).containsOnly("file3.pdf");
		assertThat(filesExceedingParsingFileSizeLimit).exists().hasContent("");
	}

	@Test
	public void givenUnparsableFileBecauseTooBigThenImportedAndMovedInErrors()
			throws Exception {

		setUp(false);
		getModelLayerFactory().getSystemConfigurationsManager()
				.setValue(ConstellioEIMConfigs.CONTENT_MAX_LENGTH_FOR_PARSING_IN_MEGAOCTETS, 1);

		ContentVersionDataSummary data1 = addTextFileToImportAndReturnHash(new File(toImport, "file.html"),
				htmlWithBody("Chuck Norris"));
		ContentVersionDataSummary data2 = addFileToImportAndReturnHash(new File(toImport, "file2.pptx"),
				"testFileWithLargePictureOfEdouard.pptx", pptxMimetype);
		ContentVersionDataSummary data3 = addTextFileToImportAndReturnHash(new File(folder1, "file.html"),
				htmlWithBody("Edouard Lechat"));
		ContentVersionDataSummary data4 = addFileToImportAndReturnHash(new File(folder1, "file3.pptx"),
				"testFileWithLargePictureOfEdouard.pptx", pptxMimetype);

		givenTimeIs(LocalDateTime.now().plusSeconds(11));
		contentManager.uploadFilesInImportFolder();

		assertThat(toImport.list()).isEmpty();
		assertContentAndParsedContentWithHash(data1.getHash(), data3.getHash());

		assertThat(toImport.list()).isEmpty();
		assertThatMapContent(contentManager.getImportedFilesMap()).containsOnly(
				entry("file.html", data1),
				entry("file2.pptx", data2),
				entry("folder1/file.html", data3),
				entry("folder1/file3.pptx", data4)
		);
		assertThat(contentManager.getContentInputStream(data2.getHash(), SDK_STREAM))
				.hasContentEqualTo(getTestResourceInputStream("testFileWithLargePictureOfEdouard.pptx"));

		assertThat(errorsEmpty.list()).isEmpty();
		assertThat(errorsEmpty.list()).isEmpty();
		assertThat(errorsUnparsable.list()).isEmpty();
		assertThat(FileUtils.readLines(filesExceedingParsingFileSizeLimit)).containsOnly(
				"folder1/file3.pptx",
				"file2.pptx"
		);
	}

	@Test
	public void givenBigFilesAreCopiedInImportFolderThenAllEntriesImported()
			throws Exception {

		setUp(false);
		addFileToImportAndReturnHash(new File(toImport, "bigFile.bigf"), "file1.bigf", null);
		addFileToImportAndReturnHash(new File(folder1, "bigFile.bigf"), "file1.bigf", null);
		addFileToImportAndReturnHash(new File(folder1, "bigFile2.bigf"), "file2.bigf", null);

		givenTimeIs(LocalDateTime.now().plusSeconds(11));

		contentManager.uploadFilesInImportFolder();

		assertThat(toImport.list()).isEmpty();
		assertContentAndParsedContentWithHash(pdf1Hash, pdf2Hash, pdf3Hash, docx1Hash, docx2Hash);

		assertThat(toImport.list()).isEmpty();
		assertThatMapContent(contentManager.getImportedFilesMap()).containsOnly(
				entry("bigFile.bigf" + File.separator + "ContentManagementAcceptTest-pdf1.pdf", pdf1Data),
				entry("bigFile.bigf" + File.separator + "ContentManagementAcceptTest-docx1.docx", docx1Data),
				entry("bigFile.bigf" + File.separator + "ContentManagementAcceptTest-docx2.docx", docx2Data),
				entry("folder1" + File.separator + "bigFile.bigf" + File.separator + "ContentManagementAcceptTest-pdf1.pdf",
						pdf1Data),
				entry("folder1" + File.separator + "bigFile.bigf" + File.separator + "ContentManagementAcceptTest-docx1.docx",
						docx1Data),
				entry("folder1" + File.separator + "bigFile.bigf" + File.separator + "ContentManagementAcceptTest-docx2.docx",
						docx2Data),
				entry("folder1" + File.separator + "bigFile2.bigf" + File.separator + "ContentManagementAcceptTest-pdf1.pdf",
						pdf1Data),
				entry("folder1" + File.separator + "bigFile2.bigf" + File.separator + "ContentManagementAcceptTest-pdf2.pdf",
						pdf2Data),
				entry("folder1" + File.separator + "bigFile2.bigf" + File.separator + "ContentManagementAcceptTest-pdf3.pdf",
						pdf3Data)
		);

		assertThat(errorsEmpty.list()).isEmpty();
		assertThat(errorsUnparsable.list()).isEmpty();
		assertThat(filesExceedingParsingFileSizeLimit).exists().hasContent("");
	}

	private Map<String, ContentVersionDataSummary> factorize(
			Map<String, Factory<ContentVersionDataSummary>> importedFilesMap) {
		Map<String, ContentVersionDataSummary> map = new HashMap<>();

		for (Map.Entry<String, Factory<ContentVersionDataSummary>> mapEntry : importedFilesMap.entrySet()) {
			map.put(mapEntry.getKey(), mapEntry.getValue().get());
		}

		return map;
	}

	@Test
	public void givenBigFilesAreCopiedInImportFolderThenAreImportedInBatch()
			throws Exception {

		setUp(false);
		addFileToImportAndReturnHash(new File(toImport, "bigFile.bigf"), "file1.bigf", null);
		addFileToImportAndReturnHash(new File(folder1, "bigFile.bigf"), "file1.bigf", null);
		addFileToImportAndReturnHash(new File(folder1, "bigFile2.bigf"), "file2.bigf", null);

		givenTimeIs(LocalDateTime.now().plusSeconds(11));
		ContentManagerImportThreadServices services = new ContentManagerImportThreadServices(getModelLayerFactory(), 4);

		//Call #1
		System.out.println("Step #1");
		services.importFiles();
		assertContentAndParsedContentWithHash(pdf1Hash, docx1Hash, docx2Hash);
		assertNoContentAndParsedContentWithHash(pdf3Hash, pdf2Hash);
		assertThatMapContent(contentManager.getImportedFilesMap()).containsOnly(
				entry("bigFile.bigf" + File.separator + "ContentManagementAcceptTest-pdf1.pdf", pdf1Data),
				entry("bigFile.bigf" + File.separator + "ContentManagementAcceptTest-docx1.docx", docx1Data),
				entry("bigFile.bigf" + File.separator + "ContentManagementAcceptTest-docx2.docx", docx2Data),
				entry("folder1" + File.separator + "bigFile.bigf" + File.separator + "ContentManagementAcceptTest-pdf1.pdf",
						pdf1Data),
				entry("folder1" + File.separator + "bigFile.bigf" + File.separator + "ContentManagementAcceptTest-docx1.docx",
						docx1Data),
				entry("folder1" + File.separator + "bigFile.bigf" + File.separator + "ContentManagementAcceptTest-docx2.docx",
						docx2Data)
		);

		//Call #2
		System.out.println("Step #2");
		services.importFiles();
		assertContentAndParsedContentWithHash(pdf1Hash, pdf2Hash, pdf3Hash, docx1Hash, docx2Hash);
		assertThatMapContent(contentManager.getImportedFilesMap()).containsOnly(
				entry("bigFile.bigf" + File.separator + "ContentManagementAcceptTest-pdf1.pdf", pdf1Data),
				entry("bigFile.bigf" + File.separator + "ContentManagementAcceptTest-docx1.docx", docx1Data),
				entry("bigFile.bigf" + File.separator + "ContentManagementAcceptTest-docx2.docx", docx2Data),
				entry("folder1" + File.separator + "bigFile.bigf" + File.separator + "ContentManagementAcceptTest-pdf1.pdf",
						pdf1Data),
				entry("folder1" + File.separator + "bigFile.bigf" + File.separator + "ContentManagementAcceptTest-docx1.docx",
						docx1Data),
				entry("folder1" + File.separator + "bigFile.bigf" + File.separator + "ContentManagementAcceptTest-docx2.docx",
						docx2Data),
				entry("folder1" + File.separator + "bigFile2.bigf" + File.separator + "ContentManagementAcceptTest-pdf1.pdf",
						pdf1Data),
				entry("folder1" + File.separator + "bigFile2.bigf" + File.separator + "ContentManagementAcceptTest-pdf2.pdf",
						pdf2Data),
				entry("folder1" + File.separator + "bigFile2.bigf" + File.separator + "ContentManagementAcceptTest-pdf3.pdf",
						pdf3Data)
		);

		assertThat(toImport.list()).isEmpty();
		assertThat(errorsEmpty.list()).isEmpty();
		assertThat(errorsUnparsable.list()).isEmpty();
		assertThat(filesExceedingParsingFileSizeLimit).exists().hasContent("");
	}

	@Test
	public void givenBigFilesContainingBlankAndUnparsableEntriesThenHandledSameAsNormalFiles()
			throws Exception {

		setUp(false);
		addFileToImportAndReturnHash(new File(toImport, "fileWithErrors.bigf"), "fileWithErrors.bigf", null);

		givenTimeIs(LocalDateTime.now().plusSeconds(11));

		contentManager.uploadFilesInImportFolder();

		assertThat(toImport.list()).isEmpty();
		assertContentAndParsedContentWithHash(pdf1Hash, pdf2Hash, pdf3Hash, docx1Hash, docx2Hash);

		assertThat(toImport.list()).isEmpty();
		assertThatMapContent(contentManager.getImportedFilesMap()).containsOnly(
				entry("fileWithErrors.bigf" + File.separator + "ContentManagementAcceptTest-pdf1.pdf", pdf1Data),
				entry("fileWithErrors.bigf" + File.separator + "ContentManagementAcceptTest-pdf2.pdf", pdf2Data),
				entry("fileWithErrors.bigf" + File.separator + "ContentManagementAcceptTest-pdf3.pdf", pdf3Data),
				entry("fileWithErrors.bigf" + File.separator + "ContentManagementAcceptTest-docx1.docx", docx1Data),
				entry("fileWithErrors.bigf" + File.separator + "ContentManagementAcceptTest-docx2.docx", docx2Data),
				entry("fileWithErrors.bigf" + File.separator + "ContentManagerAcceptanceTest-passwordProtected.pdf",
						passwordProtectedPdfData)
		);

		assertThat(contentManager.getContentInputStream(passwordProtectedPDFHash, SDK_STREAM))
				.hasContentEqualTo(getTestResourceInputStream("passwordProtectedFile.pdf"));

		assertThat(toImport.list()).isEmpty();
		assertThat(errorsEmpty.list()).containsOnly("fileWithErrors.bigf");
		assertThat(new File(errorsEmpty, "fileWithErrors.bigf").list()).containsOnly("blankfile.txt");
		assertThat(errorsUnparsable.list()).containsOnly("fileWithErrors.bigf");
		assertThat(new File(errorsUnparsable, "fileWithErrors.bigf").list())
				.containsOnly("ContentManagerAcceptanceTest-passwordProtected.pdf");
		assertThat(filesExceedingParsingFileSizeLimit).exists().hasContent("");
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
			} catch (ContentManagerException_ContentNotParsed e) {
				//OK
			}
		}
	}

	private ContentVersionDataSummary addTextFileToImportAndReturnHash(File file, String content) {
		try {
			FileUtils.write(file, content);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		try {
			String hash = getIOLayerFactory().newHashingService(BASE64_URL_ENCODED).getHashFromFile(file);
			return new ContentVersionDataSummary(hash, htmlMimetype, file.length());
		} catch (HashingServiceException e) {
			throw new RuntimeException(e);
		}
	}

	private ContentVersionDataSummary addFileToImportAndReturnHash(File file, String fileName, String mimetype) {
		try {
			FileUtils.copyFile(getTestResourceFile(fileName), file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		try {
			String hash = getIOLayerFactory().newHashingService(BASE64_URL_ENCODED).getHashFromFile(file);
			return new ContentVersionDataSummary(hash, mimetype, file.length());
		} catch (HashingServiceException e) {
			throw new RuntimeException(e);
		}
	}

	private void assertContentAndParsedContentWithHash(String... hashes) {
		for (String hash : hashes) {
			contentManager.getContentInputStream(hash, SDK_STREAM);
			try {
				contentManager.getParsedContent(hash);
			} catch (ContentManagerException_ContentNotParsed contentManagerException_contentNotParsed) {
				throw new RuntimeException(contentManagerException_contentNotParsed);
			}
		}
	}

	private Collection<File> allFilesRecursivelyIn(File folder) {
		return FileUtils.listFiles(folder, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
	}
}
