package com.constellio.data.io.services.facades;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import com.constellio.sdk.tests.ConstellioTest;

public class FileServiceAcceptanceTest extends ConstellioTest {

	@Test
	public void whenReadingFileWithMultipleLinesThenInsertCarriageReturns()
			throws IOException {
		File file = getTestResourceFile("carriageReturns.txt");
		String data = new FileService(null).readFileToString(file);
		assertEquals(2, data.split("\n").length);
	}

	@Test
	public void whenWritingAndReadingFileWithCarriageReturnsThenContentIsEqual()
			throws IOException {

		String content = "Line 1\nLine 2";

		File tempFile = new File(newTempFolder(), "test.txt");

		FileService fileService = new FileService(null);

		fileService.appendFileContent(tempFile, content);
		String writtenContent = fileService.readFileToString(tempFile);

		assertEquals(content, writtenContent);
	}
}
