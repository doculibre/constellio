package com.constellio.app.services.sip.record;

import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.sip.ead.RecordEADBuilder;
import com.constellio.app.services.sip.mets.MetsContentFileReference;
import com.constellio.app.services.sip.mets.MetsEADMetadataReference;
import com.constellio.app.services.sip.zip.SIPZipWriter;
import com.constellio.app.services.sip.zip.SIPZipWriterTransaction;
import com.constellio.data.dao.services.contents.ContentDaoException.ContentDaoException_NoSuchContent;
import com.constellio.data.dao.services.contents.DaoFile;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.RecordAuthorization;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.security.SecurityModel;
import com.constellio.model.entities.security.SecurityModelAuthorization;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.parser.EmailParser;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.security.AuthorizationsServices;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import static com.constellio.data.utils.AccentApostropheCleaner.cleanPonctuationExceptDot;
import static com.constellio.data.utils.AccentApostropheCleaner.removeAccents;
import static com.constellio.model.entities.schemas.MetadataValueType.CONTENT;
import static org.apache.commons.io.FilenameUtils.getExtension;


public class RecordSIPWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecordSIPWriter.class);

	private static final String READ_VAULT_FILE_STREAM_NAME = RecordSIPWriter.class.getSimpleName() + "-ReadVaultFile";

	private static final String TEMP_EAD_FILE_STREAM_NAME = RecordSIPWriter.class.getSimpleName() + "-ReadVaultFile";

	private SIPZipWriter sipZipWriter;

	private Locale locale;

	private AppLayerFactory appLayerFactory;

	private RecordServices recordServices;

	private IOServices ioServices;

	private ContentManager contentManager;

	private MetadataSchemasManager metadataSchemasManager;

	private RecordPathProvider recordPathProvider;

	private AuthorizationsServices authorizationsServices;

	/**
	 * For test purposes only
	 */
	private boolean includeContentFiles = true;

	private boolean includeAuths = true;

	private boolean includeRelatedMaterials = true;

	private boolean includeArchiveDescriptionMetadatasFromODDs = false;

	private KeySetMap<String, String> savedRecords = new KeySetMap<>();

	private Predicate<Metadata> metadataIgnore;

	public RecordSIPWriter(AppLayerFactory appLayerFactory,
						   SIPZipWriter sipZipWriter,
						   RecordPathProvider recordPathProvider,
						   Locale locale, Predicate<Metadata> metadataIgnore) {

		this.appLayerFactory = appLayerFactory;
		this.recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		this.ioServices = appLayerFactory.getModelLayerFactory().getIOServicesFactory().newIOServices();
		this.contentManager = appLayerFactory.getModelLayerFactory().getContentManager();
		this.recordPathProvider = recordPathProvider;
		this.metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		this.sipZipWriter = sipZipWriter;
		this.authorizationsServices = appLayerFactory.getModelLayerFactory().newAuthorizationsServices();
		this.locale = locale;
		this.metadataIgnore = metadataIgnore;
	}

	public RecordSIPWriter(AppLayerFactory appLayerFactory,
						   SIPZipWriter sipZipWriter,
						   RecordPathProvider recordPathProvider,
						   Locale locale) {
		this(appLayerFactory, sipZipWriter, recordPathProvider, locale, null);
	}

	public SIPZipWriter getSipZipWriter() {
		return sipZipWriter;
	}

	public RecordSIPWriter setIncludeContentFiles(boolean includeContentFiles) {
		this.includeContentFiles = includeContentFiles;
		return this;
	}

	public RecordSIPWriter setIncludeRelatedMaterials(boolean includeRelatedMaterials) {
		this.includeRelatedMaterials = includeRelatedMaterials;
		return this;
	}

	public RecordSIPWriter setIncludeArchiveDescriptionMetadatasFromODDs(
			boolean includeArchiveDescriptionMetadatasFromODDs) {
		this.includeArchiveDescriptionMetadatasFromODDs = includeArchiveDescriptionMetadatasFromODDs;
		return this;
	}

	public RecordSIPWriter setIncludeAuths(boolean includeAuths) {
		this.includeAuths = includeAuths;
		return this;
	}

	public ValidationErrors add(Record record) throws IOException {
		return add(Collections.singletonList(record));
	}


	public ValidationErrors add(Iterable<Record> records) throws IOException {
		return add(records.iterator());
	}

	public ValidationErrors add(Iterator<Record> recordsIterator) throws IOException {
		ValidationErrors errors = new ValidationErrors();

		KeySetMap<String, String> recordIdsToAdd = new KeySetMap<>();

		SIPZipWriterTransaction transaction = sipZipWriter.newInsertTransaction();
		try {

			while (recordsIterator.hasNext()) {
				Record record = recordsIterator.next();
				recordIdsToAdd.add(record.getTypeCode(), record.getId());
				addToSIP(transaction, record, errors);

				if (includeAuths) {
					SecurityModel securityModel = recordServices.getSecurityModel(record.getCollection());
					for (SecurityModelAuthorization authorization : securityModel.getAuthorizationsOnTarget(record.getId())) {
						recordIdsToAdd.add(RecordAuthorization.SCHEMA_TYPE, authorization.getDetails().getId());
						addToSIP(transaction, ((RecordAuthorization) authorization.getDetails()).getWrappedRecord(), errors);
					}
				}
			}

		} catch (Throwable t) {
			sipZipWriter.discard(transaction);
			throw new RuntimeException(t);

		}

		sipZipWriter.insertAll(transaction);
		savedRecords.addAll(recordIdsToAdd);

		return errors;
	}

	public void close() {
		sipZipWriter.close();
	}

	private void addToSIP(SIPZipWriterTransaction transaction, Record record, ValidationErrors errors)
			throws IOException {

		RecordInsertionContext recordInsertionContext = new RecordInsertionContext(transaction, record, errors);
		buildRecordEADFile(transaction, recordInsertionContext);

		if (includeContentFiles) {
			for (Metadata contentMetadata : recordInsertionContext.schema.getMetadatas().onlyWithType(CONTENT)) {
				for (Content content : recordInsertionContext.record.<Content>getValues(contentMetadata)) {
					for (ContentVersion contentVersion : content.getVersions()) {
						try {
							insertContentVersion(recordInsertionContext, contentMetadata, contentVersion);
						} catch (ContentDaoException_NoSuchContent ignored) {
							LOGGER.warn("No such file in vault '" + contentVersion.getHash() + "'");
						}
					}
				}
			}
		}
	}

	private void buildRecordEADFile(SIPZipWriterTransaction transaction, RecordInsertionContext ctx)
			throws IOException {
		RecordEADBuilder recordEadBuilder = new RecordEADBuilder(appLayerFactory, locale, ctx.errors, metadataIgnore);
		recordEadBuilder.setIncludeRelatedMaterials(includeRelatedMaterials);
		recordEadBuilder.setIncludeArchiveDescriptionMetadatasFromODDs(includeArchiveDescriptionMetadatasFromODDs);
		File tempXMLFile = ioServices.newTemporaryFile(TEMP_EAD_FILE_STREAM_NAME);
		try {
			recordEadBuilder.build(ctx, tempXMLFile);

			ctx.transaction.add(ctx.newMetsEADMetadataReference());
			transaction.moveFileToSIPAsUnreferencedContentFile(ctx.sipXMLPath, tempXMLFile);


		} finally {
			ioServices.deleteQuietly(tempXMLFile);
		}
	}

	private void insertContentVersion(RecordInsertionContext ctx, Metadata metadata,
									  ContentVersion contentVersion)
			throws IOException, ContentDaoException_NoSuchContent {


		String fileId = ctx.fileId(metadata, contentVersion);
		String zipFilePath = ctx.sipXMLPath(metadata, contentVersion);

		DaoFile vaultFile = contentManager.getContentDao().getFile(contentVersion.getHash());
		MetsContentFileReference reference = ctx.transaction.addContentFileFromVaultFile(zipFilePath, vaultFile);
		reference.setId(fileId);
		reference.setDmdid(ctx.dmdId);
		reference.setTitle(contentVersion.getFilename());

		String extension = StringUtils.lowerCase(getExtension(contentVersion.getFilename()));
		if ("eml".equals(extension) || "msg".equals(extension)) {
			addEmailAttachements(ctx, contentVersion, fileId);
		}
	}

	private void addEmailAttachements(RecordInsertionContext ctx, ContentVersion content, String fileId)
			throws IOException {
		InputStream in = contentManager.getContentInputStream(content.getHash(), READ_VAULT_FILE_STREAM_NAME);

		try {
			Map<String, byte[]> attachements = new EmailParser().parseEmailAttachements(content.getFilename(), in);
			for (Entry<String, byte[]> entry : attachements.entrySet()) {

				String attachmentFilename = cleanPonctuationExceptDot(removeAccents(entry.getKey()));
				if (StringUtils.isNotBlank(getExtension(attachmentFilename))) {
					String attachmentFileId = fileId + "-" + attachmentFilename;
					String attachmentPath = ctx.parentPath + "/" + attachmentFileId;

					MetsContentFileReference ref = ctx.transaction.addContentFile(attachmentPath, entry.getValue());
					ref.setId(attachmentFileId);
					ref.setDmdid(ctx.dmdId);
					ref.setTitle(attachmentFilename);
					ref.setUse("Attachment");
				}
			}

		} finally {
			ioServices.closeQuietly(in);
		}
	}

	public KeySetMap<String, String> getSavedRecords() {
		return savedRecords;
	}

	public class RecordInsertionContext {

		String dmdId;
		String sipRecordPath;
		String sipXMLPath;
		String parent;
		String parentPath;

		SIPZipWriterTransaction transaction;
		Record record;
		ValidationErrors errors;
		MetadataSchema schema;

		RecordInsertionContext(SIPZipWriterTransaction transaction, Record record, ValidationErrors errors) {
			this.transaction = transaction;
			this.record = record;
			this.errors = errors;
			sipRecordPath = recordPathProvider.getPath(record);
			sipXMLPath = sipRecordPath + ".xml";
			dmdId = StringUtils.substringAfterLast(sipRecordPath, "/");
			parentPath = StringUtils.substringBeforeLast(sipXMLPath, "/");
			parent = StringUtils.substringAfterLast(parentPath, "/");
			schema = metadataSchemasManager.getSchemaOf(record);
			if (parent.equals("data")) {
				parent = null;
				parentPath = null;
			}
		}

		MetsEADMetadataReference newMetsEADMetadataReference() {
			String recordTitle = record.getTitle();

			if (recordTitle == null) {
				recordTitle = record.getId();
			}

			return new MetsEADMetadataReference(dmdId, parent, record.getTypeCode(), recordTitle, sipXMLPath);
		}

		public String fileId(Metadata metadata, ContentVersion contentVersion) {
			return dmdId + "-" + metadata.getLocalCode() + "-" + contentVersion.getVersion();
		}

		public String sipXMLPath(Metadata metadata, ContentVersion contentVersion) {
			String path = parentPath + "/" + fileId(metadata, contentVersion);
			String extension = getExtension(contentVersion.getFilename());
			if (!extension.isEmpty()) {
				path += "." + extension;
			}
			return path;
		}

		public String getDmdId() {
			return dmdId;
		}

		public String getSipRecordPath() {
			return sipRecordPath;
		}

		public String getSipXMLPath() {
			return sipXMLPath;
		}

		public String getParent() {
			return parent;
		}

		public String getParentPath() {
			return parentPath;
		}

		public SIPZipWriterTransaction getTransaction() {
			return transaction;
		}

		public Record getRecord() {
			return record;
		}

		public ValidationErrors getErrors() {
			return errors;
		}

		public MetadataSchema getSchema() {
			return schema;
		}
	}
}
