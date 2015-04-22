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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.tika.exception.TikaException;
import org.apache.tika.fork.ForkParser;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.constellio.model.services.parser.FileParserException.FileParserException_CannotParse;
import com.constellio.sdk.tests.ConstellioTest;

public class FileParserTest extends ConstellioTest {

	@Mock InputStream stream;
	@Mock ForkParser forkParser;
	@Mock AutoDetectParser autoDetectParser;
	@Mock ForkParsers forkParsers;
	@Mock LanguageDetectionManager languageDetectionManager;
	FileParser fileParserWithForkProcess;
	FileParser fileParserWithoutForkProcess;

	@Before
	public void setup() {
		fileParserWithForkProcess = spy(new FileParser(forkParsers, languageDetectionManager, true));
		fileParserWithoutForkProcess = spy(new FileParser(forkParsers, languageDetectionManager, false));
	}

	@Test(expected = FileParserException_CannotParse.class)
	public void whenIOExceptionThenThrowFileParserException()
			throws Exception {
		doReturn(forkParser).when(forkParsers).getForkParser();
		doThrow(IOException.class).when(forkParser).parse(any(InputStream.class), any(ContentHandler.class), any(Metadata.class),
				any(ParseContext.class));

		fileParserWithForkProcess.parse(stream, 42);

	}

	@Test
	public void whenParsingXLSThenXLSParsed()
			throws Exception {
		doReturn(forkParser).when(forkParsers).getForkParser();
		doReturn(new HashMap<String, Object>()).when(fileParserWithForkProcess)
				.getPropertiesHashMap(any(Metadata.class), anyString());

		fileParserWithForkProcess.parse(stream, 42);

		verify(forkParser, times(1)).parse(eq(stream), any(BodyContentHandler.class), any(Metadata.class),
				any(ParseContext.class));
	}

	@Test
	public void givenForkParserDisabledwhenParsingXLSThenXLSParsedWithAutoDetectParser()
			throws Exception {
		doReturn(autoDetectParser).when(fileParserWithoutForkProcess).newAutoDetectParser();
		doReturn(new HashMap<String, Object>()).when(fileParserWithoutForkProcess)
				.getPropertiesHashMap(any(Metadata.class), anyString());

		fileParserWithoutForkProcess.parse(stream, 42);
		verify(autoDetectParser, times(1)).parse(eq(stream), any(BodyContentHandler.class), any(Metadata.class));
	}

	@Test(expected = FileParserException_CannotParse.class)
	public void whenSaxExceptionThenThrowFileParserException()
			throws Exception {
		doReturn(forkParser).when(forkParsers).getForkParser();
		doThrow(SAXException.class).when(forkParser).parse(any(InputStream.class), any(ContentHandler.class),
				any(Metadata.class), any(ParseContext.class));

		fileParserWithForkProcess.parse(stream, 42);

	}

	@Test(expected = FileParserException_CannotParse.class)
	public void whenTikaExceptionThenThrowFileParserException()
			throws Exception {
		doReturn(forkParser).when(forkParsers).getForkParser();
		doThrow(TikaException.class).when(forkParser).parse(any(InputStream.class), any(ContentHandler.class),
				any(Metadata.class), any(ParseContext.class));

		fileParserWithForkProcess.parse(stream, 42);

	}

}
