package com.constellio.model.services.contents;

import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.utils.BigFileIterator;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.model.entities.enums.ParsingBehavior;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.parser.FileParser;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.SlowTest;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.constellio.data.conf.HashingEncoding.BASE32;
import static org.assertj.core.api.Assertions.assertThat;

public class BulkUploaderAcceptTest extends ConstellioTest {

	HashingService hashingService;
	FileParser fileParser;

	File bigFile;

	@Before
	public void setUp()
			throws Exception {
		hashingService = getIOLayerFactory().newHashingService(BASE32);
		fileParser = getModelLayerFactory().newFileParser();
		givenConfig(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS);

		File zipFile = getTestResourceFile("10000files.bigf.zip");
		File tempFolder = newTempFolder();
		getIOLayerFactory().newZipService().unzip(zipFile, tempFolder);
		bigFile = new File(tempFolder, "10000files.bigf");
	}

	@Test
	public void whenBulkUploading100FilesThenAllHaveCorrectSizeHashingLanguageParsedContentAndBinaryContent()
			throws Exception {
		test(100);
	}

	@Test
	@SlowTest
	public void whenBulkUploading10000FilesThenAllHaveCorrectSizeHashingLanguageParsedContentAndBinaryContent()
			throws Exception {

		test(10000);
	}

	@Test
	public void whenBulkUploading100FilesWithDuplicatesThenNoExceptions()
			throws Exception {
		givenConfig(ConstellioEIMConfigs.DEFAULT_PARSING_BEHAVIOR, ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS);
		testWithDuplicates(100);
	}

	private void testWithDuplicates(int qty)
			throws Exception {
		BigFileIterator iterator = newIterator();

		BulkUploader bulkUploader = new BulkUploader(getModelLayerFactory());
		for (int i = 0; i < qty; i++) {
			final byte[] bytes = iterator.next().getBytes();
			for (int j = 0; j < 5; j++) {
				bulkUploader.uploadAsync("upload" + i, new StreamFactory<InputStream>() {
					@Override
					public InputStream create(String name)
							throws IOException {
						return new ByteArrayInputStream(bytes);
					}
				});
			}
		}

		bulkUploader.close();

		iterator = newIterator();
		for (int i = 0; i < qty; i++) {
			System.out.println("Validating content #" + i);
			String key = "upload" + i;
			final byte[] bytes = iterator.next().getBytes();

			ContentVersionDataSummary dataSummary = bulkUploader.get(key);
			String expectedMimetype = "application/xhtml+xml; charset=UTF-8";
			String expectedHash = hashingService.getHashFromBytes(bytes);
			StreamFactory<InputStream> inputStreamFactory = new StreamFactory<InputStream>() {
				@Override
				public InputStream create(String name)
						throws IOException {
					return new ByteArrayInputStream(bytes);
				}
			};
			ParsedContent expectedParsedContent = fileParser.parse(inputStreamFactory, bytes.length);
			assertThat(dataSummary.getHash()).describedAs("Hash of " + key).isEqualTo(expectedHash);
			assertThat(dataSummary.getMimetype()).describedAs("Mimetype of " + key).isEqualTo(expectedMimetype);
			assertThat(dataSummary.getLength()).describedAs("Length of " + key).isEqualTo((long) bytes.length);
			ParsedContent parsedContent = getModelLayerFactory().getContentManager().getParsedContent(expectedHash);
			assertThat(parsedContent.getParsedContent()).isEqualTo(expectedParsedContent.getParsedContent());
			assertThat(parsedContent).isEqualTo(expectedParsedContent);
			InputStream contentInputStream = getModelLayerFactory().getContentManager()
					.getContentInputStream(expectedHash, SDK_STREAM);
			assertThat(contentInputStream).describedAs("Content of " + key).hasContentEqualTo(new ByteArrayInputStream(bytes));
		}
	}

	private void test(int qty)
			throws Exception {
		BigFileIterator iterator = newIterator();

		BulkUploader bulkUploader = new BulkUploader(getModelLayerFactory());
		for (int i = 0; i < qty; i++) {
			final byte[] bytes = iterator.next().getBytes();
			bulkUploader.uploadAsync("upload" + i, new StreamFactory<InputStream>() {
				@Override
				public InputStream create(String name)
						throws IOException {
					return new ByteArrayInputStream(bytes);
				}
			});
		}

		bulkUploader.close();

		iterator = newIterator();
		for (int i = 0; i < qty; i++) {
			if (i % 250 == 0) {
				System.out.println("Validating content #" + i);
			}
			String key = "upload" + i;
			final byte[] bytes = iterator.next().getBytes();

			ContentVersionDataSummary dataSummary = bulkUploader.get(key);
			String expectedMimetype = "application/xhtml+xml; charset=UTF-8";
			String expectedHash = hashingService.getHashFromBytes(bytes);
			StreamFactory<InputStream> inputStreamFactory = new StreamFactory<InputStream>() {
				@Override
				public InputStream create(String name)
						throws IOException {
					return new ByteArrayInputStream(bytes);
				}
			};
			ParsedContent expectedParsedContent = fileParser.parse(inputStreamFactory, bytes.length);
			assertThat(dataSummary.getHash()).describedAs("Hash of " + key).isEqualTo(expectedHash);
			assertThat(dataSummary.getMimetype()).describedAs("Mimetype of " + key).isEqualTo(expectedMimetype);
			assertThat(dataSummary.getLength()).describedAs("Length of " + key).isEqualTo((long) bytes.length);
			ParsedContent parsedContent = getModelLayerFactory().getContentManager().getParsedContent(expectedHash);
			assertThat(parsedContent).isEqualTo(expectedParsedContent);
			InputStream contentInputStream = getModelLayerFactory().getContentManager()
					.getContentInputStream(expectedHash, SDK_STREAM);
			assertThat(contentInputStream).describedAs("Content of " + key).hasContentEqualTo(new ByteArrayInputStream(bytes));
		}
	}

	private BigFileIterator newIterator() {
		InputStream bigFilesInputStream = null;
		try {
			bigFilesInputStream = newFileInputStream(bigFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		return new BigFileIterator(bigFilesInputStream);
	}
}
