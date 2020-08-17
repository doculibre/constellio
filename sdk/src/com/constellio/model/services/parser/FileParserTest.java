package com.constellio.model.services.parser;

import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.parser.FileParserException.FileParserException_CannotParse;
import com.constellio.sdk.tests.ConstellioTest;
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

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FileParserTest extends ConstellioTest {

	@Mock SystemConfigurationsManager systemConfigurationsManager;
	@Mock IOServices ioServices;
	@Mock InputStream stream;
	@Mock ForkParser forkParser;
	@Mock AutoDetectParser autoDetectParser;
	@Mock ForkParsers forkParsers;
	@Mock LanguageDetectionManager languageDetectionManager;
	@Mock StreamFactory<InputStream> inputStreamFactory;
	FileParser fileParserWithForkProcess;
	FileParser fileParserWithoutForkProcess;

	@Before
	public void setup()
			throws IOException {
		fileParserWithForkProcess = spy(
				new FileParser(forkParsers, languageDetectionManager, ioServices, systemConfigurationsManager, true));
		fileParserWithoutForkProcess = spy(
				new FileParser(forkParsers, languageDetectionManager, ioServices, systemConfigurationsManager, false));
		when(inputStreamFactory.create(anyString())).thenReturn(stream);
		when(systemConfigurationsManager.getValue(ConstellioEIMConfigs.PARSED_CONTENT_MAX_LENGTH_IN_KILOOCTETS)).thenReturn(2);
		when(systemConfigurationsManager.getValue(ConstellioEIMConfigs.CONTENT_MAX_LENGTH_FOR_PARSING_IN_MEGAOCTETS))
				.thenReturn(10);
	}

	@Test(expected = FileParserException_CannotParse.class)
	public void whenIOExceptionThenThrowFileParserException()
			throws Exception {
		doReturn(forkParser).when(forkParsers).getForkParser();
		doThrow(IOException.class).when(forkParser).parse(any(InputStream.class), any(ContentHandler.class), any(Metadata.class),
				any(ParseContext.class));

		fileParserWithForkProcess.parse(inputStreamFactory, 42);

	}

	@Test
	public void whenParsingXLSThenXLSParsed()
			throws Exception {
		doReturn(forkParser).when(forkParsers).getForkParser();
		doReturn(new HashMap<String, Object>()).when(fileParserWithForkProcess)
				.getPropertiesHashMap(any(Metadata.class), anyString(), any(Dimension.class));

		fileParserWithForkProcess.parse(inputStreamFactory, 42);

		verify(forkParser, times(1)).parse(eq(stream), any(BodyContentHandler.class), any(Metadata.class),
				any(ParseContext.class));
	}

	@Test
	public void givenForkParserDisabledwhenParsingXLSThenXLSParsedWithAutoDetectParser()
			throws Exception {
		doReturn(autoDetectParser).when(fileParserWithoutForkProcess).newAutoDetectParser();
		doReturn(new HashMap<String, Object>()).when(fileParserWithoutForkProcess)
				.getPropertiesHashMap(any(Metadata.class), anyString(), any(Dimension.class));

		fileParserWithoutForkProcess.parse(inputStreamFactory, 42);
		verify(autoDetectParser, times(1))
				.parse(eq(stream), any(BodyContentHandler.class), any(Metadata.class), any(ParseContext.class));
	}

	@Test(expected = FileParserException_CannotParse.class)
	public void whenSaxExceptionThenThrowFileParserException()
			throws Exception {
		doReturn(forkParser).when(forkParsers).getForkParser();
		doThrow(SAXException.class).when(forkParser).parse(any(InputStream.class), any(ContentHandler.class),
				any(Metadata.class), any(ParseContext.class));

		fileParserWithForkProcess.parse(inputStreamFactory, 42);

	}

	@Test(expected = FileParserException_CannotParse.class)
	public void whenTikaExceptionThenThrowFileParserException()
			throws Exception {
		doReturn(forkParser).when(forkParsers).getForkParser();
		doThrow(TikaException.class).when(forkParser).parse(any(InputStream.class), any(ContentHandler.class),
				any(Metadata.class), any(ParseContext.class));

		fileParserWithForkProcess.parse(inputStreamFactory, 42);

	}

}
