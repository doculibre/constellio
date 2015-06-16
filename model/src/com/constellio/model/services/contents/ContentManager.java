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
package com.constellio.model.services.contents;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.RecordsFlushing;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.services.bigVault.RecordDaoException.OptimisticLocking;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.contents.ContentDaoException;
import com.constellio.data.dao.services.factories.DataLayerFactory;
import com.constellio.data.dao.services.idGenerator.UUIDV1Generator;
import com.constellio.data.dao.services.idGenerator.UniqueIdGenerator;
import com.constellio.data.dao.services.records.RecordDao;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.io.streamFactories.CloseableStreamFactory;
import com.constellio.data.io.streamFactories.StreamFactory;
import com.constellio.data.io.streamFactories.services.one.StreamOperationThrowingException;
import com.constellio.data.threads.BackgroundThreadConfiguration;
import com.constellio.data.threads.BackgroundThreadExceptionHandling;
import com.constellio.data.threads.BackgroundThreadsManager;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.TimeProvider;
import com.constellio.data.utils.hashing.HashingService;
import com.constellio.data.utils.hashing.HashingServiceException;
import com.constellio.model.conf.ModelLayerConfiguration;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_CannotReadInputStream;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_CannotReadParsedContent;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_CannotSaveContent;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.parser.FileParserException.FileParserException_CannotParse;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;

public class ContentManager implements StatefulService {

	static final String BACKGROUND_THREAD = "DeleteUnreferencedContent";

	static final String READ_PARSED_CONTENT = "ContentServices-ReadParsedContent";

	static final String PARSE_CONTENT_STORED_IN_TEMP_FILE = "ContentServices-ParseContentStoredInTempFile";

	private static final Logger LOGGER = LoggerFactory.getLogger(ContentManager.class);
	private final ContentDao contentDao;
	private final RecordDao recordDao;
	private final FileParser fileParser;
	private final HashingService hashingService;
	private final IOServices ioServices;
	private final UniqueIdGenerator uniqueIdGenerator;
	private final SearchServices searchServices;
	private final BackgroundThreadsManager backgroundThreadsManager;
	private final MetadataSchemasManager metadataSchemasManager;
	private final ModelLayerConfiguration configuration;

	public ContentManager(FileParser fileParser, SearchServices searchServices,
			MetadataSchemasManager metadataSchemasManager, DataLayerFactory dataLayerFactory,
			ModelLayerConfiguration configuration) {
		super();
		this.contentDao = dataLayerFactory.getContentsDao();
		this.recordDao = dataLayerFactory.newRecordDao();
		this.fileParser = fileParser;
		this.hashingService = dataLayerFactory.getIOServicesFactory().newHashingService();
		this.ioServices = dataLayerFactory.getIOServicesFactory().newIOServices();
		this.uniqueIdGenerator = dataLayerFactory.getUniqueIdGenerator();
		this.searchServices = searchServices;
		this.backgroundThreadsManager = dataLayerFactory.getBackgroundThreadsManager();
		this.metadataSchemasManager = metadataSchemasManager;
		this.configuration = configuration;

	}

	@Override
	public void initialize() {
		Runnable deleteUnreferencedContentRunnable = new Runnable() {

			@Override
			public void run() {
				deleteUnreferencedContents();
			}
		};

		backgroundThreadsManager.configure(
				BackgroundThreadConfiguration.repeatingAction(BACKGROUND_THREAD, deleteUnreferencedContentRunnable).executedEvery(
						configuration.getUnreferencedContentsThreadDelayBetweenChecks())
						.handlingExceptionWith(BackgroundThreadExceptionHandling.CONTINUE));
	}

	@Override
	public void close() {

	}

	public Content createMajor(User user, String filename, ContentVersionDataSummary newVersion) {
		String uniqueId = uniqueIdGenerator.next();
		return ContentImpl.create(uniqueId, user, filename, newVersion, true);
	}

	public Content createMinor(User user, String filename, ContentVersionDataSummary newVersion) {
		String uniqueId = uniqueIdGenerator.next();
		return ContentImpl.create(uniqueId, user, filename, newVersion, false);
	}

	public InputStream getContentInputStream(String id, String streamName)
			throws ContentManagerRuntimeException_NoSuchContent {
		try {
			return contentDao.getContentInputStream(id, streamName);
		} catch (ContentDaoException.ContentDaoException_NoSuchContent e) {
			throw new ContentManagerRuntimeException_NoSuchContent(id, e);
		}
	}

	void saveContent(String id, CloseableStreamFactory<InputStream> streamFactory)
			throws ContentManagerRuntimeException_CannotSaveContent {

		try {
			ioServices.execute(addInContentDaoOperation(id), streamFactory);
		} catch (ContentDaoException | IOException e) {
			throw new ContentManagerRuntimeException_CannotSaveContent(e);
		}

	}

	void saveParsedContent(String id, ParsedContent parsingResults)
			throws ContentManagerRuntimeException_CannotSaveContent {

		String parsingResultsString = writeParsingResults(parsingResults);

		synchronized (ContentManager.class) {

			StreamFactory<InputStream> parsingResultsStreamFactory = ioServices.newInputStreamFactory(parsingResultsString);
			try {
				ioServices.execute(addParsecContentInContentDaoOperation(id), parsingResultsStreamFactory);
			} catch (ContentDaoException | IOException e) {
				throw new ContentManagerRuntimeException_CannotSaveContent(e);
			}

		}
	}

	String writeParsingResults(ParsedContent parsingResults) {
		return new ParsedContentConverter().convertToString(parsingResults);
	}

	public ContentVersionDataSummary upload(InputStream inputStream) {
		CloseableStreamFactory<InputStream> closeableInputStreamFactory = ioServices.copyToReusableStreamFactory(
				inputStream);

		try {
			String hash = hashingService.getHashFromStream(closeableInputStreamFactory);
			markForDeletionIfNotReferenced(hash);
			ParsedContent parsedContent = getPreviouslyParsedContentOrParseFromStream(hash, closeableInputStreamFactory);
			String mimeType = parsedContent.getMimeType();
			saveContent(hash, closeableInputStreamFactory);
			return new ContentVersionDataSummary(hash, mimeType, closeableInputStreamFactory.length());

		} catch (HashingServiceException | IOException e) {
			throw new ContentManagerRuntimeException_CannotReadInputStream(e);

		} finally {
			ioServices.closeQuietly(closeableInputStreamFactory);
		}
	}

	public ContentVersionDataSummary getContentVersionSummary(String hash) {
		ParsedContent parsedContent = getParsedContent(hash);

		return new ContentVersionDataSummary(hash, parsedContent.getMimeType(), parsedContent.getLength());
	}

	ParsedContent getPreviouslyParsedContentOrParseFromStream(String hash,
			CloseableStreamFactory<InputStream> inputStreamFactory)
			throws IOException {

		ParsedContent parsedContent;
		synchronized (ContentManager.class) {
			try {
				parsedContent = getParsedContent(hash);
			} catch (ContentManagerRuntimeException_NoSuchContent e) {
				LOGGER.info("Indexing new content '" + hash + "'");
				parsedContent = tryToParse(inputStreamFactory);
				saveParsedContent(hash, parsedContent);

			}
		}
		return parsedContent;
	}

	ParsedContent tryToParse(CloseableStreamFactory<InputStream> inputStreamFactory)
			throws IOException {

		InputStream inputStream = null;

		try {
			inputStream = inputStreamFactory.create(PARSE_CONTENT_STORED_IN_TEMP_FILE);

			return fileParser.parse(inputStream, inputStreamFactory.length());

		} catch (FileParserException_CannotParse e) {
			return ParsedContent.unparsable(e.getDetectedMimetype(), inputStreamFactory.length());

		} finally {
			ioServices.closeQuietly(inputStream);
		}
	}

	public StreamOperationThrowingException<InputStream, ContentDaoException> addParsecContentInContentDaoOperation(
			final String newContentId) {
		return new StreamOperationThrowingException<InputStream, ContentDaoException>() {

			@Override
			public void execute(InputStream stream)
					throws ContentDaoException {
				contentDao.add(newContentId + "__parsed", stream);
			}
		};
	}

	public StreamOperationThrowingException<InputStream, ContentDaoException> addInContentDaoOperation(
			final String newContentId) {
		return new StreamOperationThrowingException<InputStream, ContentDaoException>() {

			@Override
			public void execute(InputStream stream)
					throws ContentDaoException {
				contentDao.add(newContentId, stream);
			}
		};
	}

	public void silentlyMarkForDeletionIfNotReferenced(String hash) {
		try {
			markForDeletionIfNotReferenced(hash);
		} catch (ContentManagerRuntimeException e) {
			LOGGER.warn("Cannot mark hash for potential deletion'" + hash + "'. ");
		}

	}

	public void markForDeletionIfNotReferenced(String hash) {

		String id = UUIDV1Generator.newRandomId();
		Map<String, Object> fields = new HashMap<>();
		fields.put("type_s", "marker");
		fields.put("contentMarkerHash_s", hash);
		fields.put("time_dt", TimeProvider.getLocalDateTime());

		RecordDTO recordDTO = new RecordDTO(id, fields);
		try {
			recordDao.execute(new TransactionDTO(RecordsFlushing.LATER()).withNewRecords(Arrays.asList(recordDTO)));
		} catch (OptimisticLocking e) {
			throw new ImpossibleRuntimeException(e);
		}
	}

	boolean isReferenced(String hash) {
		boolean referenced = false;
		for (MetadataSchemaTypes types : metadataSchemasManager.getAllCollectionsSchemaTypes()) {
			List<Metadata> contentMetadatas = types.getAllContentMetadatas();
			if (!contentMetadatas.isEmpty()) {
				referenced |= searchServices.hasResults(
						fromAllSchemasIn(types.getCollection()).whereAny(contentMetadatas).is(ContentFactory.isHash(hash)));
			}
		}

		return referenced;
	}

	public void deleteUnreferencedContents() {

		List<RecordDTO> potentiallyDeletableContentMarkers;

		while (!(potentiallyDeletableContentMarkers = getNextPotentiallyUnreferencedContentMarkers()).isEmpty()) {
			List<String> hashToDelete = new ArrayList<>();
			for (RecordDTO marker : potentiallyDeletableContentMarkers) {
				String hash = marker.getFields().get("contentMarkerHash_s").toString();
				if (!isReferenced(hash)) {
					hashToDelete.add(hash);
					hashToDelete.add(hash + "__parsed");
				}
				if (!hashToDelete.isEmpty()) {
					contentDao.delete(hashToDelete);
				}

			}
			try {
				recordDao.execute(
						new TransactionDTO(RecordsFlushing.NOW()).withDeletedRecords(potentiallyDeletableContentMarkers));
			} catch (OptimisticLocking e) {
				throw new ImpossibleRuntimeException(e);
			}
		}
	}

	ContentModificationsBuilder newContentsModificationBuilder(MetadataSchemaTypes metadataSchemaTypes) {
		return new ContentModificationsBuilder(metadataSchemaTypes);
	}

	public ParsedContent getParsedContent(String hash) {

		String parsedContent = null;

		synchronized (ContentManager.class) {

			InputStream inputStream = null;

			try {
				inputStream = contentDao.getContentInputStream(hash + "__parsed", READ_PARSED_CONTENT);
				parsedContent = ioServices.readStreamToString(inputStream);
			} catch (ContentDaoException.ContentDaoException_NoSuchContent e) {
				throw new ContentManagerRuntimeException_NoSuchContent(hash);

			} catch (IOException e) {
				throw new ContentManagerRuntimeException_CannotReadInputStream(e);

			} finally {
				ioServices.closeQuietly(inputStream);
			}
			try {
				return newParsedContentConverter().convertToParsedContent(parsedContent);
			} catch (Exception e) {
				throw new ContentManagerRuntimeException_CannotReadParsedContent(e, hash, parsedContent);
			}

		}

	}

	ParsedContentConverter newParsedContentConverter() {
		return new ParsedContentConverter();
	}

	public List<RecordDTO> getNextPotentiallyUnreferencedContentMarkers() {

		LocalDateTime now = TimeProvider.getLocalDateTime();
		LocalDateTime maxTimeForDelete = now.minus(configuration.getDelayBeforeDeletingUnreferencedContents());

		ModifiableSolrParams params = new ModifiableSolrParams();
		params.set("q", "time_dt:[* TO " + maxTimeForDelete + "Z]");
		params.set("fq", "contentMarkerHash_s:*");
		params.set("rows", 50);

		return recordDao.searchQuery(params);
	}
}
