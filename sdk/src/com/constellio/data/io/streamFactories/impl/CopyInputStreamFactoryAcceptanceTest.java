package com.constellio.data.io.streamFactories.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.Octets;
import com.constellio.sdk.tests.ConstellioTest;

public class CopyInputStreamFactoryAcceptanceTest extends ConstellioTest {

	private final Octets memoryByteArraySize = Octets.kilooctets(15);
	private File ioServicesTempFolder;
	private IOServices ioServices;

	private CopyInputStreamFactory getCopyInputStreamFactoryFromFile(File file)
			throws Exception {
		CopyInputStreamFactory copyInputStreamFactory = new CopyInputStreamFactory(ioServices, memoryByteArraySize);
		copyInputStreamFactory.saveInputStreamContent(closeAfterTest(newFileInputStream(file)));

		return closeAfterTest(copyInputStreamFactory);
	}

	@Before
	public void setup() {
		ioServicesTempFolder = newTempFolder();
		ioServices = new IOServices(ioServicesTempFolder);
	}

	@Test
	public void whenCopyingContentLargerThanMemoryByteArrayThenFileCreatedWithContentAndCorrectSize()
			throws Exception {
		File file = modifyFileSystem().creatingBinaryFileWithSize(Octets.octets(memoryByteArraySize.getOctets() + 1));

		CopyInputStreamFactory inputStreamFactory = getCopyInputStreamFactoryFromFile(file);
		assertThat(ioServicesTempFolder.listFiles().length).isEqualTo(1);
		assertThat(ioServicesTempFolder.listFiles()[0]).hasContentEqualTo(file);
		assertThat(inputStreamFactory.length()).isEqualTo(memoryByteArraySize.getOctets() + 1);
	}

	@Test
	public void whenCopyingFileWithOneCharacterThenInMemoryAndEmptyInputStreamsAreCreated()
			throws Exception {
		File file = newTempFileWithContent("zeFilename.txt", "z");

		CopyInputStreamFactory inputStreamFactory = getCopyInputStreamFactoryFromFile(file);
		assertThat(inputStreamFactory.length()).isEqualTo(1);
		assertThat(IOUtils.toString(inputStreamFactory.create(SDK_STREAM))).isEqualTo("z");
	}

	@Test
	public void whenTwoInputStreamsAreCreatedThenTwoDifferentInstancesReturned()
			throws Exception {
		File file = newTempFileWithContentInFolder(ioServicesTempFolder, "test", "test content");
		byte[] fileContent = FileUtils.readFileToByteArray(file);

		CopyInputStreamFactory copyInputStreamFactory = getCopyInputStreamFactoryFromFile(file);

		InputStream firstCopiedInputStream = copyInputStreamFactory.create(SDK_STREAM);
		InputStream secondCopiedInputStream = copyInputStreamFactory.create(SDK_STREAM);

		byte[] bytesFirstCopy = IOUtils.toByteArray(firstCopiedInputStream);
		IOUtils.closeQuietly(firstCopiedInputStream);

		byte[] bytesSecondCopy = IOUtils.toByteArray(secondCopiedInputStream);
		IOUtils.closeQuietly(secondCopiedInputStream);

		assertThat(bytesFirstCopy).isEqualTo(fileContent);
		assertThat(bytesSecondCopy).isEqualTo(fileContent);

	}

	@Test
	public void whenCopyInputStreamClosedThenTempFileDeleted()
			throws Exception {
		File file = modifyFileSystem().creatingBinaryFileWithSize(Octets.octets(memoryByteArraySize.getOctets() + 1));

		CopyInputStreamFactory inputStreamFactory = new CopyInputStreamFactory(ioServices, memoryByteArraySize);
		inputStreamFactory.saveInputStreamContent(closeAfterTest(newFileInputStream(file)));
		assertThat(ioServicesTempFolder.listFiles().length).isEqualTo(1);

		inputStreamFactory.close();

		assertThat(ioServicesTempFolder.listFiles().length).isEqualTo(0);
	}

}