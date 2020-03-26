package com.constellio.model.services.contents;

import com.constellio.data.conf.DataLayerConfiguration;
import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.contents.ContentDaoException;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.extensions.DataLayerExtensions;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.data.io.IOServicesFactory;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.CloseableStreamFactory;
import com.constellio.data.io.streamFactories.impl.CopyInputStreamFactory;
import com.constellio.data.io.streamFactories.services.one.StreamOperationReturningValueOrThrowingException;
import com.constellio.data.io.streamFactories.services.one.StreamOperationThrowingException;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.data.utils.hashing.HashingServiceException;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.enums.ParsingBehavior;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.contents.ContentManager.ParseOptions;
import com.constellio.model.services.contents.ContentManagerException.ContentManagerException_ContentNotParsed;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_CannotReadInputStream;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.parser.FileParserException;
import com.constellio.model.services.parser.FileParserException.FileParserException_CannotParse;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.sdk.tests.ConstellioTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.constellio.data.conf.HashingEncoding.BASE64_URL_ENCODED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class ContentManagerTest extends ConstellioTest {

	String zeStreamName = "zeStreamName";

	@Mock RecordServices recordServices;

	@Mock ContentDao contentDao;

	@Mock RecordDao recordDao;

	@Mock FileParser fileParser;

	@Mock HashingService hashingService;

	@Mock IOServices ioServices;

	@Mock SearchServices searchServices;

	@Mock InputStream contentInputStream, firstCreatedStream, secondCreatedStream;

	@Mock CopyInputStreamFactory streamFactory;

	@Mock UniqueIdGenerator uniqueIdGenerator;

	String newContentId = aString();

	String theParsedContent = aString();

	ContentManager contentManager;

	@Mock StreamOperationReturningValueOrThrowingException<InputStream, ParsedContent, FileParserException> parseOperation;

	@Mock StreamOperationThrowingException<InputStream, ContentDaoException> addOperation;
	@Mock StreamOperationThrowingException<InputStream, ContentDaoException> addParsedContentOperation;
	@Mock StreamOperationThrowingException<InputStream, ContentDaoException> addWithEmptyContentOperation;

	@Mock ContentModificationsBuilder contentModificationsBuilder;

	@Mock MetadataSchemaTypes metadataSchemaTypes;

	@Mock Record aRecord;
	@Mock Record anotherRecord;

	@Mock ContentImpl aContent;
	@Mock ContentImpl anotherContent;

	String aContentHash = "aContentHash";

	@Mock CopyInputStreamFactory closeableStreamFactory;

	@Mock InputStream aContentNewVersionInputStream;
	@Mock InputStream anotherContentNewVersionInputStream;

	String theMimetype = "mimetype";

	@Mock ParsedContent parsingResults;

	@Mock ParsedContentConverter parsedContentConverter;

	@Mock IOServicesFactory ioServicesFactory;
	@Mock DataLayerFactory dataLayerFactory;
	@Mock MetadataSchemasManager metadataSchemasManager;
	@Mock ModelLayerConfiguration modelLayerConfiguration;
	@Mock ModelLayerFactory modelLayerFactory;
	@Mock DataLayerConfiguration dataLayerConfiguration;
	@Mock ConstellioEIMConfigs constellioEIMConfigs;
	@Mock SystemConfigurationsManager systemConfigurationsManager;
	@Mock DataLayerExtensions dataLayerExtensions;
	@Mock DataLayerSystemExtensions dataLayerSystemExtensions;

	@Before
	public void setUp()
			throws Exception {

		when(modelLayerFactory.getDataLayerFactory()).thenReturn(dataLayerFactory);
		when(modelLayerFactory.newSearchServices()).thenReturn(searchServices);
		when(modelLayerFactory.newRecordServices()).thenReturn(recordServices);
		when(modelLayerFactory.getMetadataSchemasManager()).thenReturn(metadataSchemasManager);
		when(modelLayerFactory.getConfiguration()).thenReturn(modelLayerConfiguration);
		when(modelLayerFactory.newFileParser()).thenReturn(fileParser);
		when(modelLayerFactory.getSystemConfigs()).thenReturn(constellioEIMConfigs);

		when(dataLayerExtensions.getSystemWideExtensions()).thenReturn(dataLayerSystemExtensions);

		when(dataLayerFactory.newRecordDao()).thenReturn(recordDao);
		when(dataLayerFactory.getContentsDao()).thenReturn(contentDao);
		when(dataLayerFactory.getIOServicesFactory()).thenReturn(ioServicesFactory);
		when(dataLayerFactory.getUniqueIdGenerator()).thenReturn(uniqueIdGenerator);
		when(dataLayerFactory.getExtensions()).thenReturn(dataLayerExtensions);
		when(ioServicesFactory.newHashingService(BASE64_URL_ENCODED)).thenReturn(hashingService);
		when(ioServicesFactory.newIOServices()).thenReturn(ioServices);

		when(dataLayerFactory.getDataLayerConfiguration()).thenReturn(dataLayerConfiguration);
		when(dataLayerConfiguration.getHashingEncoding()).thenReturn(BASE64_URL_ENCODED);


		contentManager = spy(new ContentManager(modelLayerFactory));
		when(ioServices.copyToReusableStreamFactory(contentInputStream, null)).thenReturn(streamFactory);
		when(streamFactory.create(anyString())).thenReturn(firstCreatedStream).thenReturn(secondCreatedStream)
				.thenThrow(new Error());
		doReturn(addOperation).when(contentManager).addInContentDaoOperation(newContentId);
		doReturn(addParsedContentOperation).when(contentManager).addInContentDaoOperation(newContentId);
		doReturn(addWithEmptyContentOperation).when(contentManager).addInContentDaoOperation(newContentId);

		doReturn(contentModificationsBuilder).when(contentManager).newContentsModificationBuilder(metadataSchemaTypes);

		when(parsingResults.getParsedContent()).thenReturn(theParsedContent);
		when(parsingResults.getMimeType()).thenReturn(theMimetype);
		doReturn(parsedContentConverter).when(contentManager).newParsedContentConverter();

		when(constellioEIMConfigs.getDefaultParsingBehavior()).thenReturn(ParsingBehavior.SYNC_PARSING_FOR_ALL_CONTENTS);
		when(modelLayerFactory.getSystemConfigurationsManager()).thenReturn(systemConfigurationsManager);
	}

	@Test
	public void whenGetParsedContentThenAskContentDaoAndConvertResult()
			throws Exception {

		String newContentParsedContentId = newContentId + "__parsed";
		InputStream parsedContentInputStream = mock(InputStream.class);
		String parsedContentString = "parsedContentString";
		ParsedContent parsedContentObject = mock(ParsedContent.class);

		when(parsedContentConverter.convertToParsedContent(parsedContentString)).thenReturn(parsedContentObject);
		when(contentDao.getContentInputStream(newContentParsedContentId, ContentManager.READ_PARSED_CONTENT)).thenReturn(
				parsedContentInputStream);
		when(ioServices.readStreamToString(parsedContentInputStream)).thenReturn(parsedContentString);

		ParsedContent parsedContent = contentManager.getParsedContent(newContentId);

		assertThat(parsedContent).isSameAs(parsedContentObject);
		verify(contentDao).getContentInputStream(newContentParsedContentId, ContentManager.READ_PARSED_CONTENT);

	}

	@Test(expected = ContentManagerException_ContentNotParsed.class)
	public void givenNoParsedContentWhenGetParsedContentThenThrowException()
			throws Exception {

		String newContentParsedContentId = newContentId + "__parsed";
		String parsedContentString = "parsedContentString";
		ParsedContent parsedContentObject = mock(ParsedContent.class);

		when(parsedContentConverter.convertToParsedContent(parsedContentString)).thenReturn(parsedContentObject);
		when(contentDao.getContentInputStream(newContentParsedContentId, ContentManager.READ_PARSED_CONTENT))
				.thenThrow(ContentDaoException_NoSuchContent.class);

		contentManager.getParsedContent(newContentId);

	}

	@Test(expected = ContentManagerRuntimeException_CannotReadInputStream.class)
	public void givenIExceptionWhenGetParsedContentThenThrowException()
			throws Exception {

		String newContentParsedContentId = newContentId + "__parsed";
		String parsedContentString = "parsedContentString";
		ParsedContent parsedContentObject = mock(ParsedContent.class);

		when(parsedContentConverter.convertToParsedContent(parsedContentString)).thenReturn(parsedContentObject);
		when(contentDao.getContentInputStream(newContentParsedContentId, ContentManager.READ_PARSED_CONTENT))
				.thenThrow(IOException.class);

		contentManager.getParsedContent(newContentId);

	}

	@Test(expected = ContentManagerException_ContentNotParsed.class)
	public void givenNoSuchContentContentDaoExceptionWhenGetParsedContentThenThrowNoSuchContentContentServicesException()
			throws Exception {
		when(contentDao.getContentInputStream(newContentId + "__parsed", ContentManager.READ_PARSED_CONTENT))
				.thenThrow(ContentDaoException_NoSuchContent.class);

		contentManager.getParsedContent(newContentId);

	}

	@Test
	public void whenGetContentInputStreamThenAskContentDao()
			throws Exception {
		when(contentDao.getContentInputStream(newContentId, zeStreamName)).thenReturn(contentInputStream);

		InputStream returnedContentInputStream = contentManager.getContentInputStream(newContentId, zeStreamName);

		assertThat(returnedContentInputStream).isEqualTo(contentInputStream);
		verify(contentDao).getContentInputStream(newContentId, zeStreamName);

	}

	@Test(expected = ContentManagerRuntimeException_NoSuchContent.class)
	public void givenNoSuchContentContentDaoExceptionWhenGetContentInputStreamThenThrowNoSuchContentContentServicesException()
			throws Exception {
		when(contentDao.getContentInputStream(newContentId, zeStreamName)).thenThrow(ContentDaoException_NoSuchContent.class);

		contentManager.getContentInputStream(newContentId, zeStreamName);

	}

	@Test(expected = ContentManagerException.ContentManagerException_ContentNotParsed.class)
	public void givenContentDaoReturnNullWhenGetContentParsedContentThenThrowContentNotParsedException()
			throws Exception {
		when(contentDao.getContentInputStream(newContentId + "__parsed", zeStreamName)).thenReturn(null);

		contentManager.getParsedContent(newContentId);

	}

	@Test
	public void whenSaveContentThenReadInputStreamToReusableInputStreamFactorySaveAndClose()
			throws Exception {

		when(ioServices.copyToReusableStreamFactory(aContentNewVersionInputStream, null)).thenReturn(closeableStreamFactory);
		when(closeableStreamFactory.length()).thenReturn(42L);
		doReturn(aContentHash).when(hashingService).getHashFromStream(closeableStreamFactory);
		ContentManager.ParsedContentResponse parsedContentResponse = contentManager
				.buildParsedContentResponse(false, parsingResults);
		doReturn(parsedContentResponse).when(contentManager).getPreviouslyParsedContentOrParseFromStream(
				eq(aContentHash), eq(closeableStreamFactory), any(ContentManager.ParseOptions.class));
		doNothing().when(contentManager).saveContent(anyString(), any(CopyInputStreamFactory.class));

		ContentVersionDataSummary dataSummary = contentManager.upload(aContentNewVersionInputStream);

		InOrder inOrder = inOrder(ioServices, contentManager, aContent);
		inOrder.verify(ioServices).copyToReusableStreamFactory(aContentNewVersionInputStream, null);
		inOrder.verify(contentManager).getPreviouslyParsedContentOrParseFromStream(
				eq(aContentHash), eq(closeableStreamFactory), any(ContentManager.ParseOptions.class));
		inOrder.verify(contentManager).saveContent(anyString(), any(CopyInputStreamFactory.class));
		inOrder.verify(ioServices).closeQuietly(closeableStreamFactory);

		assertThat(dataSummary.getHash()).isEqualTo(aContentHash);
		assertThat(dataSummary.getMimetype()).isEqualTo(theMimetype);
		assertThat(dataSummary.getLength()).isEqualTo(42);
	}

	@Test
	public void givenHashingServiceExceptionWhenUploadContentThenCloseInputStreamFactoryAndThrowException()
			throws Exception {

		doThrow(HashingServiceException.class).when(hashingService).getHashFromStream(closeableStreamFactory);
		when(ioServices.copyToReusableStreamFactory(aContentNewVersionInputStream, null)).thenReturn(closeableStreamFactory);

		try {
			contentManager.upload(aContentNewVersionInputStream);
			fail("ContentServicesRuntimeException_CannotReadInputStream");
		} catch (ContentManagerRuntimeException_CannotReadInputStream e) {
			//OK
		}

		verify(contentManager, never()).saveContent(anyString(), any(CopyInputStreamFactory.class));
		verify(contentManager, never()).getPreviouslyParsedContentOrParseFromStream(anyString(),
				any(CloseableStreamFactory.class));
		verify(ioServices).closeQuietly(closeableStreamFactory);
	}

	@Test
	public void givenCannotParseExceptionWhenTryToParseThenReturnParsedContentWithEmptyTextAndUnknownLanguageAndCorrectMimetype()
			throws Exception {

		doReturn(aContentHash).when(hashingService).getHashFromStream(closeableStreamFactory);
		doThrow(new FileParserException_CannotParse(mock(Exception.class), "zeMimetype")).when(fileParser)
				.parse(eq(closeableStreamFactory), anyInt(), any(ContentManager.ParseOptions.class));

		ParsedContent parsedContent = contentManager.tryToParse(closeableStreamFactory, new ParseOptions());

		assertThat(parsedContent.getProperties()).isEmpty();
		assertThat(parsedContent.getParsedContent()).isEmpty();
		assertThat(parsedContent.getLanguage()).isEqualTo(Language.UNKNOWN.getCode());
		assertThat(parsedContent.getMimeType()).isEqualTo("zeMimetype");

	}

	@Test
	public void whenTryToParseThenParsedContentWithEmptyTextAndUnknownLanguageAndCorrectMimetype()
			throws Exception {
		doReturn(aContentHash).when(hashingService).getHashFromStream(closeableStreamFactory);
		doReturn(parsingResults).when(fileParser).parse(eq(closeableStreamFactory), anyInt(), any(ContentManager.ParseOptions.class));

		assertThat(contentManager.tryToParse(closeableStreamFactory, new ParseOptions())).isSameAs(parsingResults);

	}

	@Test
	public void givenContentServiceRuntimeExceptionWhileSavingContentWhenUploadContentThenCloseInputStreamFactoryAndThrowException()
			throws Exception {

		when(ioServices.copyToReusableStreamFactory(aContentNewVersionInputStream, null)).thenReturn(closeableStreamFactory);
		doReturn(aContentHash).when(hashingService).getHashFromStream(closeableStreamFactory);
		ContentManager.ParsedContentResponse parsedContentResponse = contentManager
				.buildParsedContentResponse(false, parsingResults);
		doReturn(parsedContentResponse).when(contentManager).getPreviouslyParsedContentOrParseFromStream(
				eq(aContentHash), eq(closeableStreamFactory), any(ContentManager.ParseOptions.class));
		doThrow(ContentManagerRuntimeException.class).when(contentManager)
				.saveContent(aContentHash, closeableStreamFactory);

		try {
			contentManager.upload(aContentNewVersionInputStream);
			fail("ContentServicesRuntimeException_CannotReadInputStream");
		} catch (ContentManagerRuntimeException e) {
			//OK
		}

		verify(ioServices).closeQuietly(closeableStreamFactory);
	}

	@Test
	public void givenContentServiceRuntimeExceptionWhileParsingThenCloseInputStreamFactoryAndThrowException()
			throws Exception {

		when(ioServices.copyToReusableStreamFactory(aContentNewVersionInputStream, null)).thenReturn(closeableStreamFactory);
		doReturn(aContentHash).when(hashingService).getHashFromStream(closeableStreamFactory);
		ContentManager.ParsedContentResponse parsedContentResponse = contentManager
				.buildParsedContentResponse(false, parsingResults);
		doReturn(parsedContentResponse).when(contentManager).getPreviouslyParsedContentOrParseFromStream(aContentHash,
				closeableStreamFactory);
		doThrow(IOException.class).when(contentManager)
				.getPreviouslyParsedContentOrParseFromStream(eq(aContentHash), eq(closeableStreamFactory), any(ContentManager.ParseOptions.class));

		try {
			contentManager.upload(aContentNewVersionInputStream);
			fail("ContentServicesRuntimeException_CannotReadInputStream");
		} catch (ContentManagerRuntimeException e) {
			//OK
		}

		verify(ioServices).closeQuietly(closeableStreamFactory);
	}

	@Test
	public void whenDeleteUnreferencedContentsThenDeleteUnreferencedContentAndAllMarkers()
			throws Exception {

		MetadataSchemaTypes types1 = mock(MetadataSchemaTypes.class, "types1");
		MetadataSchemaTypes types2 = mock(MetadataSchemaTypes.class, "types2");
		when(metadataSchemasManager.getAllCollectionsSchemaTypes()).thenReturn(Arrays.asList(types1, types2));

		RecordDTO recordDTO1 = mockMarkerWithHash("hash1");
		RecordDTO recordDTO2 = mockMarkerWithHash("hash2");
		RecordDTO recordDTO3 = mockMarkerWithHash("hash3");
		RecordDTO recordDTO4 = mockMarkerWithHash("hash4");
		RecordDTO recordDTO5 = mockMarkerWithHash("hash5");
		RecordDTO recordDTO6 = mockMarkerWithHash("hash6");
		RecordDTO recordDTO7 = mockMarkerWithHash("hash7");
		RecordDTO recordDTO8 = mockMarkerWithHash("hash8");
		RecordDTO recordDTO9 = mockMarkerWithHash("hash9");

		doReturn(true).when(contentManager).isReferenced("hash1");
		doReturn(false).when(contentManager).isReferenced("hash2");
		doReturn(false).when(contentManager).isReferenced("hash3");
		doReturn(true).when(contentManager).isReferenced("hash4");
		doReturn(true).when(contentManager).isReferenced("hash5");
		doReturn(false).when(contentManager).isReferenced("hash6");
		doReturn(true).when(contentManager).isReferenced("hash7");
		doReturn(false).when(contentManager).isReferenced("hash8");
		doReturn(true).when(contentManager).isReferenced("hash9");

		doReturn(Arrays.asList(recordDTO1))
				.doReturn(Arrays.asList(recordDTO2, recordDTO3))
				.doReturn(Arrays.asList(recordDTO7))
				.doReturn(Arrays.asList(recordDTO5, recordDTO6))
				.doReturn(Arrays.asList(recordDTO7))
				.doReturn(Arrays.asList(recordDTO8))
				.doReturn(new ArrayList<String>())
				.doReturn(Arrays.asList(recordDTO9))
				.when(contentManager).getNextPotentiallyUnreferencedContentMarkers();

		contentManager.deleteUnreferencedContents(RecordsFlushing.NOW());

		TransactionDTO flushNow = new TransactionDTO(RecordsFlushing.NOW());
		InOrder inOrder = inOrder(contentManager, recordDao, contentDao);
		inOrder.verify(contentManager).getNextPotentiallyUnreferencedContentMarkers();
		inOrder.verify(contentManager).isReferenced("hash1");
		inOrder.verify(recordDao).execute(flushNow.withDeletedRecords(Arrays.asList(recordDTO1)));
		inOrder.verify(contentManager).getNextPotentiallyUnreferencedContentMarkers();
		inOrder.verify(contentManager).isReferenced("hash2");
		inOrder.verify(contentDao).delete(Arrays.asList("hash2", "hash2__parsed", "hash2.preview", "hash2.thumbnail",
				"hash2.jpegConversion"));
		inOrder.verify(contentManager).isReferenced("hash3");
		inOrder.verify(contentDao).delete(Arrays.asList("hash3", "hash3__parsed", "hash3.preview", "hash3.thumbnail",
				"hash3.jpegConversion"));
		inOrder.verify(recordDao).execute(flushNow.withDeletedRecords(Arrays.asList(recordDTO2, recordDTO3)));
		inOrder.verify(contentManager).getNextPotentiallyUnreferencedContentMarkers();
		inOrder.verify(contentManager).isReferenced("hash5");
		inOrder.verify(contentManager).isReferenced("hash6");
		inOrder.verify(contentDao).delete(Arrays.asList("hash6", "hash6__parsed", "hash6.preview", "hash6.thumbnail", "hash6.jpegConversion"));
		inOrder.verify(recordDao).execute(flushNow.withDeletedRecords(Arrays.asList(recordDTO5, recordDTO6)));
		inOrder.verify(contentManager).getNextPotentiallyUnreferencedContentMarkers();
		inOrder.verify(contentManager).isReferenced("hash7");
		inOrder.verify(recordDao).execute(flushNow.withDeletedRecords(Arrays.asList(recordDTO7)));
		inOrder.verify(contentManager).getNextPotentiallyUnreferencedContentMarkers();
		inOrder.verify(contentManager).isReferenced("hash8");
		inOrder.verify(contentDao).delete(Arrays.asList("hash8", "hash8__parsed", "hash8.preview", "hash8.thumbnail", "hash8.jpegConversion"));
		inOrder.verify(recordDao).execute(flushNow.withDeletedRecords(Arrays.asList(recordDTO8)));
		inOrder.verify(contentManager).getNextPotentiallyUnreferencedContentMarkers();

	}

	@Test
	public void givenContentAlreadyParsedWhenGetPreviouslyParsedContentOrParseFromStreamThenReturnPreviouslyParsedContent()
			throws Exception {

		doReturn(parsingResults).when(contentManager).getParsedContent(aContentHash);

		ParsedContent parsedContent = contentManager.getPreviouslyParsedContentOrParseFromStream(aContentHash, streamFactory)
				.getParsedContent();

		assertThat(parsedContent).isSameAs(parsingResults);
		verify(contentManager, never()).saveParsedContent(aContentHash, parsingResults);
		verify(contentManager, never()).tryToParse(streamFactory, new ParseOptions());
		verify(ioServices, never()).closeQuietly(firstCreatedStream);

	}

	@Test
	public void givenNewContentParsedWhenGetPreviouslyParsedContentOrParseFromStreamThenReturnParseAndSaveParsedContent()
			throws Exception {

		doThrow(ContentManagerException_ContentNotParsed.class).when(contentManager).getParsedContent(aContentHash);
		doReturn(parsingResults).when(contentManager).tryToParse(eq(streamFactory), any(ContentManager.ParseOptions.class));

		ParsedContent parsedContent = contentManager.getPreviouslyParsedContentOrParseFromStream(aContentHash, streamFactory)
				.getParsedContent();

		assertThat(parsedContent).isSameAs(parsingResults);
		verify(contentManager).saveParsedContent(aContentHash, parsingResults);
		verify(contentManager).tryToParse(eq(streamFactory), any(ContentManager.ParseOptions.class));
	}

	private RecordDTO mockMarkerWithHash(String hash) {
		RecordDTO marker = mock(RecordDTO.class, hash + "Marker");
		Map<String, Object> fields = new HashMap<>();
		fields.put("contentMarkerHash_s", hash);
		fields.put("id", hash + "Marker");
		when(marker.getFields()).thenReturn(fields);
		return marker;
	}
}
