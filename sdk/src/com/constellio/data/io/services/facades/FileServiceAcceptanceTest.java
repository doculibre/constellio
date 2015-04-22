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
