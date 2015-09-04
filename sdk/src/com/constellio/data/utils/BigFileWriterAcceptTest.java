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
package com.constellio.data.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.sdk.tests.ConstellioTest;

public class BigFileWriterAcceptTest extends ConstellioTest {

	IOServices ioServices;

	String file1 = "file1.doc";
	String file2 = "file2.ppt";
	String file3 = "file3.xlsx";
	String file4 = "file4.doc";

	@Before
	public void setUp()
			throws Exception {
		ioServices = getIOLayerFactory().newIOServices();

	}

	@Test
	public void whenWritingBigFileThenCanReadItAndObtainSameContentAndTitles()
			throws Exception {

		File bigFile = new File(newTempFolder(), "zeBigFile.big");
		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(bigFile));
		try {
			BigFileWriter w = new BigFileWriter(outputStream);
			w.write(file3());
			w.write(file1());
			w.write(file2());
			w.write(file4());
			w.write(file1());
			w.write(file2());

		} finally {
			outputStream.close();
		}

		InputStream inputStream = newFileInputStream(bigFile);
		BigFileIterator iterator = new BigFileIterator(inputStream);

		byte[] bytes = FileUtils.readFileToByteArray(bigFile);
		System.out.println(bytes);

		BigFileEntry entry1 = iterator.next();
		assertThat(entry1.fileName).isEqualTo("BigFileWriterAcceptTest-" + file3);
		assertThat(entry1.bytes).isEqualTo(file3Bytes());

		BigFileEntry entry2 = iterator.next();
		assertThat(entry2.fileName).isEqualTo("BigFileWriterAcceptTest-" + file1);
		assertThat(entry2.bytes).isEqualTo(file1Bytes());

		BigFileEntry entry3 = iterator.next();
		assertThat(entry3.fileName).isEqualTo("BigFileWriterAcceptTest-" + file2);
		assertThat(entry3.bytes).isEqualTo(file2Bytes());

		BigFileEntry entry4 = iterator.next();
		assertThat(entry4.fileName).isEqualTo("BigFileWriterAcceptTest-" + file4);
		assertThat(entry4.bytes).isEqualTo(file4Bytes());

		BigFileEntry entry5 = iterator.next();
		assertThat(entry5.fileName).isEqualTo("BigFileWriterAcceptTest-" + file1);
		assertThat(entry5.bytes).isEqualTo(file1Bytes());

		BigFileEntry entry6 = iterator.next();
		assertThat(entry6.fileName).isEqualTo("BigFileWriterAcceptTest-" + file2);
		assertThat(entry6.bytes).isEqualTo(file2Bytes());

		assertThat(iterator.hasNext()).isFalse();

	}

	@Test
	public void whenWritingMultipleBigFilesInAFolderThenCanReadThemAndObtainSameContentAndTitles()
			throws Exception {

		File zeFolder = newTempFolder();
		File bigFile1 = new File(zeFolder, "bigFile1.bigf");
		File bigFile2 = new File(zeFolder, "bigFile2.bigf");
		File bigFile3 = new File(zeFolder, "bigFile3.bigf");
		File textFile = new File(zeFolder, "file.txt");
		FileUtils.touch(textFile);

		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(bigFile1));
		try {
			BigFileWriter w = new BigFileWriter(outputStream);
			w.write(file3());
			w.write(file1());
			w.write(file2());
			w.write(file4());
			w.write(file1());
			w.write(file2());

		} finally {
			outputStream.close();
		}

		outputStream = new BufferedOutputStream(new FileOutputStream(bigFile2));
		try {
			BigFileWriter w = new BigFileWriter(outputStream);

		} finally {
			outputStream.close();
		}

		outputStream = new BufferedOutputStream(new FileOutputStream(bigFile3));
		try {
			BigFileWriter w = new BigFileWriter(outputStream);
			w.write(file3());
		} finally {
			outputStream.close();
		}

		BigFileFolderIterator iterator = new BigFileFolderIterator(zeFolder, ioServices, "zeIterator");

		BigFileEntry entry1 = iterator.next();
		assertThat(entry1.fileName).isEqualTo("BigFileWriterAcceptTest-" + file3);
		assertThat(entry1.bytes).isEqualTo(file3Bytes());

		BigFileEntry entry2 = iterator.next();
		assertThat(entry2.fileName).isEqualTo("BigFileWriterAcceptTest-" + file1);
		assertThat(entry2.bytes).isEqualTo(file1Bytes());

		BigFileEntry entry3 = iterator.next();
		assertThat(entry3.fileName).isEqualTo("BigFileWriterAcceptTest-" + file2);
		assertThat(entry3.bytes).isEqualTo(file2Bytes());

		BigFileEntry entry4 = iterator.next();
		assertThat(entry4.fileName).isEqualTo("BigFileWriterAcceptTest-" + file4);
		assertThat(entry4.bytes).isEqualTo(file4Bytes());

		BigFileEntry entry5 = iterator.next();
		assertThat(entry5.fileName).isEqualTo("BigFileWriterAcceptTest-" + file1);
		assertThat(entry5.bytes).isEqualTo(file1Bytes());

		BigFileEntry entry6 = iterator.next();
		assertThat(entry6.fileName).isEqualTo("BigFileWriterAcceptTest-" + file2);
		assertThat(entry6.bytes).isEqualTo(file2Bytes());

		BigFileEntry entry7 = iterator.next();
		assertThat(entry7.fileName).isEqualTo("BigFileWriterAcceptTest-" + file3);
		assertThat(entry7.bytes).isEqualTo(file3Bytes());

		assertThat(iterator.hasNext()).isFalse();

		iterator.close();

	}

	private File file1() {
		return getTestResourceFile(file1);
	}

	private File file2() {
		return getTestResourceFile(file2);
	}

	private File file3() {
		return getTestResourceFile(file3);
	}

	private File file4() {
		return getTestResourceFile(file4);
	}

	private byte[] file1Bytes()
			throws IOException {
		return FileUtils.readFileToByteArray(file1());
	}

	private byte[] file2Bytes()
			throws IOException {
		return FileUtils.readFileToByteArray(file2());
	}

	private byte[] file3Bytes()
			throws IOException {
		return FileUtils.readFileToByteArray(file3());
	}

	private byte[] file4Bytes()
			throws IOException {
		return FileUtils.readFileToByteArray(file4());
	}
}
