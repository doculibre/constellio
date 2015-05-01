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
package com.constellio.model.services.parser;

import static java.util.Arrays.asList;
import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.services.parser.FileParserException.FileParserException_CannotParse;
import com.constellio.sdk.tests.ConstellioTest;

//@SlowTest
public class FileParserAcceptanceTest extends ConstellioTest {

	InputStream inputStream;

	private FileParser fileParser;

	@Test
	public void givenStreamOfDOCMimetypeWhenParsingThenValidParsedContentReturned()
			throws Exception {
		inputStream = getTestResourceInputStream("testFile.doc");
		long length = getLengthOf("testFile.doc");

		ParsedContent parsedContent = fileParser.parse(inputStream, length);

		assertThat(parsedContent.getParsedContent()).contains("This is the content of").contains("a doc file");
		assertThat(parsedContent.getLanguage()).isEqualTo(Language.English.getCode());
		assertThat(parsedContent.getMimeType()).isEqualTo("application/msword");
		assertThat(parsedContent.getLength()).isEqualTo(22528L);
		assertThat(parsedContent.getProperties()).containsEntry("Company", "DocuLibre");
	}

	@Test
	public void givenStreamOfDOCXMimetypeWhenParsingThenValidParsedContentReturned()
			throws Exception {
		inputStream = getTestResourceInputStream("testFile.docx");
		long length = getLengthOf("testFile.docx");

		ParsedContent parsedContent = fileParser.parse(inputStream, length);

		assertThat(parsedContent.getParsedContent()).contains("This is the content of").contains("a docx file");
		assertThat(parsedContent.getLanguage()).isEqualTo(Language.English.getCode());
		assertThat(parsedContent.getMimeType())
				.isEqualTo("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
		assertThat(parsedContent.getLength()).isEqualTo(13771L);
		assertThat(parsedContent.getProperties()).containsEntry("Title", "Document sans titre.docx");
	}

	@Test
	public void givenStreamOfHTMLMimetypeWhenParsingThenCorrectContentReturned()
			throws Exception {
		inputStream = getTestResourceInputStream("testFile.html");
		long length = getLengthOf("testFile.html");

		ParsedContent parsedContent = fileParser.parse(inputStream, length);

		assertThat(parsedContent.getParsedContent()).contains("This is the content of").contains("a html file");
		assertThat(parsedContent.getLanguage()).isEqualTo(Language.English.getCode());
		assertThat(parsedContent.getMimeType()).startsWith("text/html;");
		assertThat(parsedContent.getProperties()).isEmpty();
	}

	@Test
	public void givenStreamOfPDFMimetypeWhenParsingThenCorrectContentReturned()
			throws Exception {
		inputStream = getTestResourceInputStream("testFile.pdf");
		long length = getLengthOf("testFile.pdf");

		ParsedContent parsedContent = fileParser.parse(inputStream, length);

		assertThat(parsedContent.getParsedContent()).contains("This is the content of").contains("a pdf file");
		assertThat(parsedContent.getLanguage()).isEqualTo(Language.English.getCode());
		assertThat(parsedContent.getMimeType()).isEqualTo("application/pdf");
		assertThat(parsedContent.getLength()).isEqualTo(27171L);
		assertThat(parsedContent.getProperties()).containsEntry("Title", "Untitled");
	}

	@Test
	public void givenPasswordProtectedPDFFileThenReturnEmptyParsedContentWithUnknownLanguageAndPDFMimetype()
			throws Exception {
		inputStream = getTestResourceInputStream("passwordProtected.pdf");
		long length = getLengthOf("passwordProtected.pdf");

		try {

			fileParser.parse(inputStream, length);
			fail("Exception expected");
		} catch (FileParserException_CannotParse e) {
			assertThat(e.getDetectedMimetype()).isEqualTo("application/pdf");
		}

	}

	@Test
	public void givenStreamOfXLSMimetypeWhenParsingThenCorrectContentReturned()
			throws Exception {
		inputStream = getTestResourceInputStream("testFile.xls");
		long length = getLengthOf("testFile.xls");

		ParsedContent parsedContent = fileParser.parse(inputStream, length);

		assertThat(parsedContent.getParsedContent()).contains("Feuille1").contains("This is the content of")
				.contains("the xsl file");
		assertThat(parsedContent.getLanguage()).isEqualTo(Language.English.getCode());
		assertThat(parsedContent.getMimeType()).isEqualTo("application/vnd.ms-excel");
		assertThat(parsedContent.getLength()).isEqualTo(23552L);
		assertThat(parsedContent.getProperties()).containsEntry("Title", "zeTitle");
		assertThat(parsedContent.getProperties()).containsEntry("List:Keywords", asList("zeKeywords"));
		assertThat(parsedContent.getProperties()).containsEntry("Comments", "zeComments");
		assertThat(parsedContent.getProperties()).containsEntry("Author", "zeAuthor");
	}

	@Test
	public void givenStreamOfXLSXMimetypeWhenParsingThenCorrectContentReturned()
			throws Exception {
		inputStream = getTestResourceInputStream("testFile.xlsx");
		long length = getLengthOf("testFile.xlsx");

		ParsedContent parsedContent = fileParser.parse(inputStream, length);

		assertThat(parsedContent.getParsedContent()).contains("Sheet1").contains("This is the content of")
				.contains("the xslx file");
		assertThat(parsedContent.getLanguage()).isEqualTo(Language.English.getCode());
		assertThat(parsedContent.getMimeType()).isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		assertThat(parsedContent.getLength()).isEqualTo(8022L);
		assertThat(parsedContent.getProperties()).isEmpty();
	}

	@Test
	public void givenStreamOfXMLMimetypeWhenParsingThenCorrectContentReturned()
			throws Exception {
		inputStream = getTestResourceInputStream("testFile.xml");
		long length = getLengthOf("testFile.xml");

		ParsedContent parsedContent = fileParser.parse(inputStream, length);

		assertThat(parsedContent.getParsedContent()).contains("This is the content of").contains("the xml file");
		assertThat(parsedContent.getLanguage()).isEqualTo(Language.English.getCode());
		assertThat(parsedContent.getMimeType()).startsWith("text/html;");
		assertThat(parsedContent.getProperties()).isEmpty();
	}

	@Test
	public void givenStreamOfDOCWhenParsingThenAllPropertiesAreCatch()
			throws Exception {
		inputStream = getTestResourceInputStream("testFileWithProperties.doc");
		long length = getLengthOf("testFileWithProperties.doc");

		ParsedContent parsedContent = fileParser.parse(inputStream, length);

		assertThat(parsedContent.getProperties()).isNotEmpty();
		assertThatAllCommunPropertiesAreCatchIn(parsedContent);
		assertThatAllLessCommunPropertiesAreCatchIn(parsedContent);
	}

	@Test
	public void givenStreamOfDOCXWhenParsingThenAllPropertiesAreCatch()
			throws Exception {
		inputStream = getTestResourceInputStream("testFileWithProperties.docx");
		long length = getLengthOf("testFileWithProperties.docx");

		ParsedContent parsedContent = fileParser.parse(inputStream, length);

		assertThat(parsedContent.getProperties()).isNotEmpty();
		assertThatAllCommunPropertiesAreCatchIn(parsedContent);
		assertThatAllLessCommunPropertiesAreCatchIn(parsedContent);
	}

	@Test
	public void givenStreamOfPDFWhenParsingThenAllPropertiesAreCatch()
			throws Exception {
		inputStream = getTestResourceInputStream("testFileWithProperties.pdf");
		long length = getLengthOf("testFileWithProperties.pdf");

		ParsedContent parsedContent = fileParser.parse(inputStream, length);

		assertThat(parsedContent.getProperties()).isNotEmpty();
		assertThatAllCommunPropertiesAreCatchIn(parsedContent);
	}

	@Test
	public void givenStreamOfPPTWhenParsingThenAllPropertiesAreCatch()
			throws Exception {
		inputStream = getTestResourceInputStream("testFileWithProperties.ppt");
		long length = getLengthOf("testFileWithProperties.ppt");

		ParsedContent parsedContent = fileParser.parse(inputStream, length);

		assertThat(parsedContent.getProperties()).isNotEmpty();
		assertThatAllCommunPropertiesAreCatchIn(parsedContent);
		assertThatAllLessCommunPropertiesAreCatchIn(parsedContent);
	}

	@Test
	public void givenStreamOfPPTXWhenParsingThenAllPropertiesAreCatch()
			throws Exception {
		inputStream = getTestResourceInputStream("testFileWithProperties.pptx");
		long length = getLengthOf("testFileWithProperties.pptx");

		ParsedContent parsedContent = fileParser.parse(inputStream, length);

		assertThat(parsedContent.getProperties()).isNotEmpty();
		assertThatAllCommunPropertiesAreCatchIn(parsedContent);
		assertThatAllLessCommunPropertiesAreCatchIn(parsedContent);
	}

	@Test
	public void givenStreamOfXLSWhenParsingThenAllPropertiesAreCatch()
			throws Exception {
		inputStream = getTestResourceInputStream("testFileWithProperties.xls");
		long length = getLengthOf("testFileWithProperties.xls");

		ParsedContent parsedContent = fileParser.parse(inputStream, length);

		assertThat(parsedContent.getProperties()).isNotEmpty();
		assertThatAllCommunPropertiesAreCatchIn(parsedContent);
		assertThatAllLessCommunPropertiesAreCatchIn(parsedContent);
	}

	@Test
	public void givenStreamOfXLSXWhenParsingThenAllPropertiesAreCatch()
			throws Exception {
		inputStream = getTestResourceInputStream("testFileWithProperties.xlsx");
		long length = getLengthOf("testFileWithProperties.xlsx");

		ParsedContent parsedContent = fileParser.parse(inputStream, length);
		assertThat(parsedContent.getProperties()).isNotEmpty();
		assertThatAllCommunPropertiesAreCatchIn(parsedContent);
		assertThatAllLessCommunPropertiesAreCatchIn(parsedContent);
	}

	private void assertThatAllCommunPropertiesAreCatchIn(ParsedContent parsedContent) {
		assertThat(parsedContent.getProperties()).containsEntry("Title", "Ze title");
		assertThat(parsedContent.getProperties())
				.containsEntry("List:Keywords", asList("Ze keyword1", "Ze keyword2", "Ze keyword 3"));
		assertThat(parsedContent.getProperties()).containsEntry("Author", "Ze author");
		assertThat(parsedContent.getProperties()).containsEntry("Subject", "Ze subject");
	}

	private void assertThatAllLessCommunPropertiesAreCatchIn(ParsedContent parsedContent) {
		assertThat(parsedContent.getProperties()).containsEntry("Company", "Ze company");
		assertThat(parsedContent.getProperties()).containsEntry("Category", "Ze category");
		assertThat(parsedContent.getProperties()).containsEntry("Manager", "Ze ultimate manager");
		assertThat(parsedContent.getProperties()).containsEntry("Comments", "Ze very useful comments Line2");
	}

	@Before
	public void setup() {
		fileParser = getModelLayerFactory().newFileParser();
	}

	private long getLengthOf(String resourceName) {
		return getTestResourceFile(resourceName).length();
	}
}
